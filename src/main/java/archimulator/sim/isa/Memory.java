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

/**
 *
 * @author Min Cai
 */
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

    /**
     *
     * @param kernel
     * @param littleEndian
     * @param processId
     */
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

    /**
     *
     * @param address
     * @return
     */
    public byte readByte(int address) {
        byte[] buffer = new byte[1];
        this.access(address, 1, buffer, false, true);
        return buffer[0];
    }

    /**
     *
     * @param address
     * @return
     */
    public short readHalfWord(int address) {
        byte[] buffer = new byte[2];
        this.access(address, 2, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getShort();
    }

    /**
     *
     * @param address
     * @return
     */
    public int readWord(int address) {
        byte[] buffer = new byte[4];
        this.access(address, 4, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
     *
     * @param address
     * @return
     */
    public long readDoubleWord(int address) {
        byte[] buffer = new byte[8];
        this.access(address, 8, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getLong();
    }

    /**
     *
     * @param address
     * @param size
     * @return
     */
    public byte[] readBlock(int address, int size) {
        byte[] buffer = new byte[size];
        this.access(address, size, buffer, false, true);
        return buffer;
    }

    /**
     *
     * @param address
     * @param size
     * @return
     */
    public String readString(int address, int size) {
        byte[] data = this.readBlock(address, size);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; data[i] != '\0'; i++) {
            sb.append((char) data[i]);
        }

        return sb.toString();
    }

    /**
     *
     * @param address
     * @param data
     */
    public void writeByte(int address, byte data) {
        byte[] buffer = new byte[]{data};
        this.access(address, 1, buffer, true, true);
    }

    /**
     *
     * @param address
     * @param data
     */
    public void writeHalfWord(int address, short data) {
        byte[] buffer = new byte[2];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putShort(data);
        this.access(address, 2, buffer, true, true);
    }

    /**
     *
     * @param address
     * @param data
     */
    public void writeWord(int address, int data) {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putInt(data);
        this.access(address, 4, buffer, true, true);
    }

    /**
     *
     * @param address
     * @param data
     */
    public void writeDoubleWord(int address, long data) {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putLong(data);
        this.access(address, 8, buffer, true, true);
    }

    /**
     *
     * @param address
     * @param data
     * @return
     */
    public int writeString(int address, String data) {
        byte[] buffer = (data + "\0").getBytes();
        int bytesCount = buffer.length;
        this.access(address, bytesCount, buffer, true, true);
        return bytesCount;
    }

    /**
     *
     * @param address
     * @param size
     * @param data
     */
    public void writeBlock(int address, int size, byte[] data) {
        this.access(address, size, data, true, true);
    }

    /**
     *
     * @param address
     * @param size
     */
    public void zero(int address, int size) {
        this.writeBlock(address, size, new byte[size]);
    }

    /**
     *
     * @param address
     * @param size
     * @param buffer
     * @param write
     * @param createNewPageIfNecessary
     */
    public void access(int address, int size, byte[] buffer, boolean write, boolean createNewPageIfNecessary) {
        if (this.speculative) {
            this.doSpeculativeAccess(address, size, buffer, write);
        } else {
            this.doNonSpeculativeAccess(address, size, buffer, write, createNewPageIfNecessary);
        }
    }

    private void doSpeculativeAccess(int address, int size, byte[] buffer, boolean write) {
        int tag = address >> SpeculativeMemoryBlock.BLOCK_LOGSIZE;
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

            this.doNonSpeculativeAccess(address & ~(SpeculativeMemoryBlock.BLOCK_SIZE - 1), SpeculativeMemoryBlock.BLOCK_SIZE, blockFound.data, write, false);
        }

        if ((size & (size - 1)) != 0 || size > SpeculativeMemoryBlock.BLOCK_SIZE || (address & (size - 1)) != 0) {
            return;
        }

        int displacement = address & (SpeculativeMemoryBlock.BLOCK_SIZE - 1);

        if (!write) {
            System.arraycopy(blockFound.data, displacement, buffer, 0, size);
        } else {
            System.arraycopy(buffer, 0, blockFound.data, displacement, size);
        }
    }

    /**
     *
     */
    public void enterSpeculativeState() {
        this.speculative = true;
    }

    /**
     *
     */
    public void exitSpeculativeState() {
        this.speculativeMemoryBlocks.clear();
        this.speculative = false;
    }

    private void doNonSpeculativeAccess(int address, int size, byte[] buffer, boolean write, boolean createNewPageIfNecessary) {
        int offset = 0;

        int pageSize = getPageSize();

        while (size > 0) {
            int chunkSize = Math.min(size, pageSize - getDisplacement(address));
            this.accessPageBoundary(address, chunkSize, buffer, offset, write, createNewPageIfNecessary);

            size -= chunkSize;
            offset += chunkSize;
            address += chunkSize;
        }
    }

    /**
     *
     * @param address
     * @param size
     * @return
     */
    public int map(int address, int size) {
        int tagStart, tagEnd;

        tagStart = tagEnd = getTag(address);

        int pageSize = getPageSize();

        for (int pageCount = (getTag(address + size - 1) - tagStart) / pageSize + 1; ; ) {
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
//    public void map2(int address, int size) {
//        int tagStart = getTag(address);
//        int tagEnd = getTag(address + size - 1);
//
//        int pageSize = getPageSize();
//
//        for (int tag = tagStart; tag <= tagEnd; tag += pageSize) {
//            if(this.getPage(tag) == null) {
//                this.addPage(tag);
//            }
//        }
//    }

    /**
     *
     * @param address
     * @param size
     */
    public void unmap(int address, int size) {
        int tagStart = getTag(address);
        int tagEnd = getTag(address + size - 1);

        int pageSize = getPageSize();

        for (int tag = tagStart; tag <= tagEnd; tag += pageSize) {
            this.removePage(tag);
        }
    }

    /**
     *
     * @param oldAddr
     * @param oldSize
     * @param newSize
     * @return
     */
    public int remap(int oldAddr, int oldSize, int newSize) {
        int start = this.map(0, newSize);

        if (start != -1) {
            this.copyPages(start, oldAddr, Math.min(oldSize, newSize));
            this.unmap(oldAddr, oldSize);
        }

        return start;
    }

    private void copyPages(int tagDestination, int tagSource, int numPages) {
        throw new UnsupportedOperationException(); //TODO: support it using BigMemory
//        for (int i = 0; i < numPages; i++) {
//            this.getPage(tagDestination + i * getPageSize()).bb = this.getPage(tagSource + i * getPageSize()).bb;
//        }
    }

    /**
     *
     * @param virtualAddress
     * @return
     */
    public int getPhysicalAddress(int virtualAddress) {
        return this.getPage(virtualAddress).physicalAddress + getDisplacement(virtualAddress);
    }

    private Page getPage(int address) {
        int index = getIndex(address);

        return this.pages.containsKey(index) ? this.pages.get(index) : null;
    }

    private Page addPage(int address) {
        int index = getIndex(address);

        this.numPages++;
        Page page = new Page(getExperiment().currentMemoryPageId++);

        this.pages.put(index, page);

        return page;
    }

    private void removePage(int address) {
        int index = getIndex(address);

        this.pages.remove(index);
    }

    private void accessPageBoundary(int address, int size, byte[] buffer, int offset, boolean write, boolean createNewPageIfNecessary) {
        Page page = this.getPage(address);

        if (page == null && createNewPageIfNecessary) {
            page = this.addPage(getTag(address));
        }

        if (page != null) {
            page.doAccess(address, buffer, offset, size, write);
        }
    }

    /**
     *
     * @param id
     */
    protected void onPageCreated(int id) {
        this.byteBuffers.put(id, ByteBuffer.allocate(Memory.getPageSize()).order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
    }

    /**
     *
     * @param pageId
     * @param displacement
     * @param buffer
     * @param offset
     * @param size
     * @param write
     */
    protected void doPageAccess(int pageId, int displacement, byte[] buffer, int offset, int size, boolean write) {
        ByteBuffer bb = getByteBuffer(pageId);
        bb.position(displacement);

        if (write) {
            bb.put(buffer, offset, size);
        } else {
            bb.get(buffer, offset, size);
        }
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     *
     * @return
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     *
     * @return
     */
    public int getProcessId() {
        return processId;
    }

    /**
     *
     * @return
     */
    public boolean isSpeculative() {
        return speculative;
    }

    /**
     *
     * @return
     */
    public static CacheGeometry getGeometry() {
        return geometry;
    }

    /**
     *
     * @return
     */
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

        private void doAccess(int address, byte[] buffer, int offset, int size, boolean write) {
            doPageAccess(id, getDisplacement(address), buffer, offset, size, write);
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

    /**
     *
     * @param address
     * @return
     */
    public static int getDisplacement(int address) {
        return CacheGeometry.getDisplacement(address, geometry);
    }

    private static int getTag(int address) {
        return CacheGeometry.getTag(address, geometry);
    }

    private static int getIndex(int address) {
        return CacheGeometry.getLineId(address, geometry);
    }

    /**
     *
     * @return
     */
    public static int getPageSizeInLog2() {
        return geometry.getLineSizeInLog2();
    }

    /**
     *
     * @return
     */
    public static int getPageSize() {
        return geometry.getLineSize();
    }
}
