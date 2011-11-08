/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.isa.memory;

import archimulator.mem.cache.CacheGeometry;
import archimulator.os.Kernel;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.event.PollStatsEvent;
import archimulator.util.action.Action1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class Memory extends BasicSimulationObject {
    private int id;
    private boolean littleEndian;

    private Map<Integer, Map<Integer, Page>> pages;

    private int currentMemoryPageId = 0;

    private Kernel kernel;
    private String simulationDirectory;
    private int processId;

    private transient boolean speculative;
    private transient Map<Integer, List<SpeculativeMemoryBlock>> specBlks;

    public Memory(Kernel kernel, String simulationDirectory, boolean littleEndian, int processId) {
        super(kernel);

        this.kernel = kernel;
        this.simulationDirectory = simulationDirectory;
        this.processId = processId;

        this.id = this.kernel.currentMemoryId++;

        this.littleEndian = littleEndian;

        this.pages = new TreeMap<Integer, Map<Integer, Page>>();

        this.init();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        this.init();
    }

    protected void init() {
        this.speculative = false;
        this.specBlks = new TreeMap<Integer, List<SpeculativeMemoryBlock>>();

        this.kernel.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                event.getStats().put("mem-" + id + ".currentMemoryPageId", Memory.this.currentMemoryPageId);
            }
        });
    }

    public byte readByte(int addr) {
        byte[] buf = new byte[1];
        this.access(addr, 1, buf, false, true);
        return buf[0];
    }

    public short readHalfWord(int addr) {
        byte[] buf = new byte[2];
        this.access(addr, 2, buf, false, true);
        return ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getShort();
    }

    public int readWord(int addr) {
        byte[] buf = new byte[4];
        this.access(addr, 4, buf, false, true);
        return ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getInt();
    }

    public long readDoubleWord(int addr) {
        byte[] buf = new byte[8];
        this.access(addr, 8, buf, false, true);
        return ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getLong();
    }

    public byte[] readBlock(int addr, int size) {
        byte[] buf = new byte[size];
        this.access(addr, size, buf, false, true);
        return buf;
    }

    public String readString(int addr, int size) {
        byte[] data = this.readBlock(addr, size);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; data[i] != '\0'; i++) {
            sb.append((char) data[i]);
        }

        return sb.toString();
    }

    public void writeByte(int addr, byte data) {
        byte[] buf = new byte[]{data};
        this.access(addr, 1, buf, true, true);
    }

    public void writeHalfWord(int addr, short data) {
        byte[] buf = new byte[2];
        ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putShort(data);
        this.access(addr, 2, buf, true, true);
    }

    public void writeWord(int addr, int data) {
        byte[] buf = new byte[4];
        ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putInt(data);
        this.access(addr, 4, buf, true, true);
    }

    public void writeDoubleWord(int addr, long data) {
        byte[] buf = new byte[8];
        ByteBuffer.wrap(buf).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putLong(data);
        this.access(addr, 8, buf, true, true);
    }

    public int writeString(int addr, String data) {
        byte[] buf = (data + "\0").getBytes();
        int bytesCount = buf.length;
        this.access(addr, bytesCount, buf, true, true);
        return bytesCount;
    }

    public void writeBlock(int addr, int size, byte[] data) {
        this.access(addr, size, data, true, true);
    }

    public void zero(int addr, int size) {
        this.writeBlock(addr, size, new byte[size]);
    }

    public void access(int addr, int size, byte[] buf, boolean write, boolean createNewPageIfNecessary) {
        if (this.speculative) {
            this.doSpeculativeAccess(addr, size, buf, write);
        } else {
            this.doNonSpeculativeAccess(addr, size, buf, write, createNewPageIfNecessary);
        }
    }

    private void doSpeculativeAccess(int addr, int size, byte[] buf, boolean write) {
        int tag = addr >> SpeculativeMemoryBlock.BLOCK_LOGSIZE;
        int index = tag % SpeculativeMemoryBlock.COUNT;

        SpeculativeMemoryBlock blkFound = null;

        if (this.specBlks.containsKey(index)) {
            for (SpeculativeMemoryBlock blk : this.specBlks.get(index)) {
                if (blk.tag == tag) {
                    blkFound = blk;
                    break;
                }
            }
        }

        if (blkFound == null) {
            blkFound = new SpeculativeMemoryBlock(tag);

            if (!this.specBlks.containsKey(index)) {
                this.specBlks.put(index, new ArrayList<SpeculativeMemoryBlock>());
            }

            this.specBlks.get(index).add(0, blkFound);

            this.doNonSpeculativeAccess(addr & ~(SpeculativeMemoryBlock.BLOCK_SIZE - 1), SpeculativeMemoryBlock.BLOCK_SIZE, blkFound.data, write, false);
        }

        if ((size & (size - 1)) != 0 || size > SpeculativeMemoryBlock.BLOCK_SIZE || (addr & (size - 1)) != 0) {
            return;
        }

        int displacement = addr & (SpeculativeMemoryBlock.BLOCK_SIZE - 1);

        if (!write) {
            System.arraycopy(blkFound.data, displacement, buf, 0, size);
        } else {
            System.arraycopy(buf, 0, blkFound.data, displacement, size);
        }
    }

    public void enterSpeculativeState() {
        this.speculative = true;
    }

    public void exitSpeculativeState() {
        this.specBlks.clear();
        this.speculative = false;
    }

    private void doNonSpeculativeAccess(int addr, int size, byte[] buf, boolean write, boolean createNewPageIfNecessary) {
        int offset = 0;

        int pageSize = getPageSize();

        while (size > 0) {
            int chunkSize = Math.min(size, pageSize - getDisplacement(addr));
            this.accessPageBoundary(addr, chunkSize, buf, offset, write, createNewPageIfNecessary);

            size -= chunkSize;
            offset += chunkSize;
            addr += chunkSize;
        }
    }

    public int map(int addr, int size) {
        int tagStart, tagEnd;

        tagStart = tagEnd = getTag(addr);

        int pageSize = getPageSize();

        for (int pageCount = (getTag(addr + size - 1) - tagStart) / pageSize + 1; ; ) {
            if (tagEnd == 0) {
                return -1;
            }

            if (this.getPage(tagEnd) != null) {
                tagEnd += pageSize;
                tagStart = tagEnd;
                continue;
            }

            if ((tagEnd - tagStart) / pageSize + 1 == pageCount) {
                break;
            }

            tagEnd += pageSize;
        }

        for (int tag = tagStart; tag <= tagEnd; tag += pageSize) {
            assert (this.getPage(tag) == null);
            this.addPage(tag);
        }

        return tagStart;
    }

    //TODO: fixme, and fix mmap and brk syscall implementations!!!, see: http://www.makelinux.net/ldd3/chp-15-sect-1
//    public void map2(int addr, int size) {
//        int tagStart = getTag(addr);
//        int tagEnd = getTag(addr + size - 1);
//
//        int pageSize = getPageSize();
//
//        for (int tag = tagStart; tag <= tagEnd; tag += pageSize) {
//            if(this.getPage(tag) == null) {
//                this.addPage(tag);
//            }
//        }
//    }

    public void unmap(int addr, int size) {
        int tagStart = getTag(addr);
        int tagEnd = getTag(addr + size - 1);

        int pageSize = getPageSize();

        for (int tag = tagStart; tag <= tagEnd; tag += pageSize) {
            this.removePage(tag);
        }
    }

    public int remap(int oldAddr, int oldSize, int newSize) {
        int start = this.map(0, newSize);

        if (start != -1) {
            this.copyPages(start, oldAddr, Math.min(oldSize, newSize));
            this.unmap(oldAddr, oldSize);
        }

        return start;
    }

    private void copyPages(int tagDest, int tagSrc, int numPages) {
        throw new UnsupportedOperationException(); //TODO: support it using BigMemory
//        for (int i = 0; i < numPages; i++) {
//            this.getPage(tagDest + i * getPageSize()).bb = this.getPage(tagSrc + i * getPageSize()).bb;
//        }
    }

    public int getPhysicalAddress(int virtualAddress) {
        return this.getPage(virtualAddress).physicalAddress + getDisplacement(virtualAddress);
    }

    private Page getPage(int addr) {
        int index = getIndex(addr);
        int tag = getTag(addr);

        return this.pages.containsKey(index) && this.pages.get(index).containsKey(tag) ? this.pages.get(index).get(tag) : null;
    }

    private Page addPage(int addr) {
        int index = getIndex(addr);
        int tag = getTag(addr);

        if (!this.pages.containsKey(index)) {
            this.pages.put(index, new TreeMap<Integer, Page>());
        }

        Page page = new Page(this.currentMemoryPageId++);

        this.pages.get(index).put(tag, page);

        return page;
    }

    private void removePage(int addr) {
        int index = getIndex(addr);
        int tag = getTag(addr);

        if (this.pages.containsKey(index) && this.pages.get(index).containsKey(tag)) {
            this.pages.get(index).remove(tag);
        }
    }

    private void accessPageBoundary(int addr, int size, byte[] buf, int offset, boolean write, boolean createNewPageIfNecessary) {
        Page page = this.getPage(addr);

        if (page == null && createNewPageIfNecessary) {
            page = this.addPage(getTag(addr));
        }

        if (page != null) {
            page.doAccess(addr, buf, offset, size, write);
        }
    }

    protected abstract void onPageCreated(int id);

    protected abstract void doPageAccess(int pageId, int displacement, byte[] buf, int offset, int size, boolean write);

    public int getId() {
        return id;
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public String getSimulationDirectory() {
        return simulationDirectory;
    }

    public int getProcessId() {
        return processId;
    }

    public boolean isSpeculative() {
        return speculative;
    }

    public static CacheGeometry getGeometry() {
        return geometry;
    }

    private class Page implements Serializable {
        private int id;
        private int physicalAddress;

        private Page(int id) {
            this.id = id;
            this.physicalAddress = this.id << Memory.getPageSizeInLog2();

            onPageCreated(id);
        }

//        private void writeObject(ObjectOutputStream oos) throws IOException {
//            oos.defaultWriteObject();
//
//            StandardJavaSerializationHelper.writeDirectByteBuffer(oos, this.bb);
//        }
//
//        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
//            ois.defaultReadObject();
//
//            this.bb = StandardJavaSerializationHelper.readDirectByteBuffer(ois);
//        }

        private void doAccess(int addr, byte[] buf, int offset, int size, boolean write) {
            doPageAccess(id, getDisplacement(addr), buf, offset, size, write);
        }
    }

    private class SpeculativeMemoryBlock implements Serializable {
        private int tag;
        private byte[] data;

        private SpeculativeMemoryBlock(int tag) {
            this.tag = tag;
            this.data = new byte[BLOCK_SIZE];
        }

        private static final int BLOCK_LOGSIZE = 8;
        private static final int BLOCK_SIZE = 1 << BLOCK_LOGSIZE;
        private static final int COUNT = 1024;
    }

    private static CacheGeometry geometry = new CacheGeometry(1 << 22, 1, 1 << 12);

    public static int getDisplacement(int addr) {
        return CacheGeometry.getDisplacement(addr, geometry);
    }

    private static int getTag(int addr) {
        return CacheGeometry.getTag(addr, geometry);
    }

    private static int getIndex(int addr) {
        return CacheGeometry.getSet(addr, geometry);
    }

    public static int getPageSizeInLog2() {
        return geometry.getLineSizeInLog2();
    }

    public static int getPageSize() {
        return geometry.getLineSize();
    }
}
