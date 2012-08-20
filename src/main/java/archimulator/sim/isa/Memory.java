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
package archimulator.sim.isa;

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.cache.CacheGeometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class Memory extends BasicSimulationObject {
    private int id;
    private boolean littleEndian;

    private Map<Integer, Page> pages;

    private Kernel kernel;
    private int processId;

    private int numPages;

    private Map<Integer, ByteBuffer> byteBuffers;

    private boolean speculative;
    private Map<Integer, List<SpeculativeMemoryBlock>> speculativeMemoryBlocks;

    public Memory(Kernel kernel, boolean littleEndian, int processId) {
        super(kernel);

        this.kernel = kernel;
        this.littleEndian = littleEndian;
        this.processId = processId;

        this.id = this.kernel.currentMemoryId++;

        this.pages = new TreeMap<Integer, Page>();

        this.byteBuffers = new HashMap<Integer, ByteBuffer>();

        this.speculative = false;
        this.speculativeMemoryBlocks = new TreeMap<Integer, List<SpeculativeMemoryBlock>>();
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

        SpeculativeMemoryBlock blockFound = null;

        if (this.speculativeMemoryBlocks.containsKey(index)) {
            for (SpeculativeMemoryBlock blk : this.speculativeMemoryBlocks.get(index)) {
                if (blk.tag == tag) {
                    blockFound = blk;
                    break;
                }
            }
        }

        if (blockFound == null) {
            blockFound = new SpeculativeMemoryBlock(tag);

            if (!this.speculativeMemoryBlocks.containsKey(index)) {
                this.speculativeMemoryBlocks.put(index, new ArrayList<SpeculativeMemoryBlock>());
            }

            this.speculativeMemoryBlocks.get(index).add(0, blockFound);

            this.doNonSpeculativeAccess(addr & ~(SpeculativeMemoryBlock.BLOCK_SIZE - 1), SpeculativeMemoryBlock.BLOCK_SIZE, blockFound.data, write, false);
        }

        if ((size & (size - 1)) != 0 || size > SpeculativeMemoryBlock.BLOCK_SIZE || (addr & (size - 1)) != 0) {
            return;
        }

        int displacement = addr & (SpeculativeMemoryBlock.BLOCK_SIZE - 1);

        if (!write) {
            System.arraycopy(blockFound.data, displacement, buf, 0, size);
        } else {
            System.arraycopy(buf, 0, blockFound.data, displacement, size);
        }
    }

    public void enterSpeculativeState() {
        this.speculative = true;
    }

    public void exitSpeculativeState() {
        this.speculativeMemoryBlocks.clear();
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

    //TODO: fixme, and fix mmap and brk system call implementations!!!, see: http://www.makelinux.net/ldd3/chp-15-sect-1
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

        return this.pages.containsKey(index) ? this.pages.get(index) : null;
    }

    private Page addPage(int addr) {
        int index = getIndex(addr);

        this.numPages++;
        Page page = new Page(getExperiment().currentMemoryPageId++);

        this.pages.put(index, page);

        return page;
    }

    private void removePage(int addr) {
        int index = getIndex(addr);

        this.pages.remove(index);
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

    protected void onPageCreated(int id) {
        this.byteBuffers.put(id, ByteBuffer.allocate(Memory.getPageSize()).order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
    }

    protected void doPageAccess(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        ByteBuffer bb = getByteBuffer(pageId);
        bb.position(displacement);

        if (write) {
            bb.put(buf, offset, size);
        } else {
            bb.get(buf, offset, size);
        }
    }

    public int getId() {
        return id;
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public Kernel getKernel() {
        return kernel;
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

    public int getNumPages() {
        return numPages;
    }

    private ByteBuffer getByteBuffer(int pageId) {
        return this.byteBuffers.get(pageId);
    }

    private class Page {
        private int id;
        private int physicalAddress;

        private Page(int id) {
            this.id = id;
            this.physicalAddress = this.id << Memory.getPageSizeInLog2();

            onPageCreated(id);
        }

        private void doAccess(int addr, byte[] buf, int offset, int size, boolean write) {
            doPageAccess(id, getDisplacement(addr), buf, offset, size, write);
        }
    }

    private class SpeculativeMemoryBlock {
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

    private static final CacheGeometry geometry = new CacheGeometry(-1, 1, 1 << 12);

    public static int getDisplacement(int addr) {
        return CacheGeometry.getDisplacement(addr, geometry);
    }

    private static int getTag(int addr) {
        return CacheGeometry.getTag(addr, geometry);
    }

    private static int getIndex(int addr) {
        return CacheGeometry.getLineId(addr, geometry);
    }

    public static int getPageSizeInLog2() {
        return geometry.getLineSizeInLog2();
    }

    public static int getPageSize() {
        return geometry.getLineSize();
    }
}
