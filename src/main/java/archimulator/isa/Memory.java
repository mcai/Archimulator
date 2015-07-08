/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.isa;

import archimulator.common.BasicSimulationObject;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.os.Kernel;
import archimulator.uncore.cache.CacheGeometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Memory.
 *
 * @author Min Cai
 */
public class Memory extends BasicSimulationObject implements Reportable {
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
     * Create a "memory".
     *
     * @param kernel       the kernel
     * @param littleEndian a value indicating whether the memory is little endian or not
     * @param processId    the ID of the process
     */
    public Memory(Kernel kernel, boolean littleEndian, int processId) {
        super(kernel);

        this.kernel = kernel;
        this.littleEndian = littleEndian;
        this.processId = processId;

        this.id = this.kernel.currentMemoryId++;

        this.pages = new TreeMap<>();

        this.byteBuffers = new HashMap<>();

        this.speculative = false;
        this.speculativeMemoryBlocks = new TreeMap<>();
    }

    /**
     * Read a byte at the specified address.
     *
     * @param address the address
     * @return a byte at the specified address
     */
    public byte readByte(int address) {
        byte[] buffer = new byte[1];
        this.access(address, 1, buffer, false, true);
        return buffer[0];
    }

    /**
     * Read a half word at the specified address.
     *
     * @param address the address
     * @return a half word at the specified address
     */
    public short readHalfWord(int address) {
        byte[] buffer = new byte[2];
        this.access(address, 2, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getShort();
    }

    /**
     * Read a word at the specified address.
     *
     * @param address the address
     * @return a word at the specified address
     */
    public int readWord(int address) {
        byte[] buffer = new byte[4];
        this.access(address, 4, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
     * Read a double word at the specified address.
     *
     * @param address the address
     * @return a double word at the specified address
     */
    public long readDoubleWord(int address) {
        byte[] buffer = new byte[8];
        this.access(address, 8, buffer, false, true);
        return ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getLong();
    }

    /**
     * Read a block of bytes at the specified address.
     *
     * @param address the address
     * @param size    the size in bytes of the block
     * @return a block of bytes at the specified address
     */
    public byte[] readBlock(int address, int size) {
        byte[] buffer = new byte[size];
        this.access(address, size, buffer, false, true);
        return buffer;
    }

    /**
     * Read a string of the specified size at the specified address.
     *
     * @param address the address
     * @param size    the size of the string to be read
     * @return a string of the specified size at the specified address
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
     * Write a byte at the specified address.
     *
     * @param address the address
     * @param data    one byte of data to be written
     */
    public void writeByte(int address, byte data) {
        byte[] buffer = new byte[]{data};
        this.access(address, 1, buffer, true, true);
    }

    /**
     * Write a half word at the specified address.
     *
     * @param address the address
     * @param data    one half word of data to be written
     */
    public void writeHalfWord(int address, short data) {
        byte[] buffer = new byte[2];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putShort(data);
        this.access(address, 2, buffer, true, true);
    }

    /**
     * Write a word at the specified address.
     *
     * @param address the address
     * @param data    one word of data to be written
     */
    public void writeWord(int address, int data) {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putInt(data);
        this.access(address, 4, buffer, true, true);
    }

    /**
     * Write a double word at the specified address.
     *
     * @param address the address
     * @param data    one double word of data to be written
     */
    public void writeDoubleWord(int address, long data) {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).order(this.littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).putLong(data);
        this.access(address, 8, buffer, true, true);
    }

    /**
     * Write a string at the specified address.
     *
     * @param address the address
     * @param data    the string to be written
     * @return the length of the string that is written
     */
    public int writeString(int address, String data) {
        byte[] buffer = (data + "\0").getBytes();
        int bytesCount = buffer.length;
        this.access(address, bytesCount, buffer, true, true);
        return bytesCount;
    }

    /**
     * Write a block of data of the specified size at the specified address.
     *
     * @param address the address
     * @param size    the size of the block to be written
     * @param data    the block of data to be written
     */
    public void writeBlock(int address, int size, byte[] data) {
        this.access(address, size, data, true, true);
    }

    /**
     * Fill zeros of the specified size at the specified address.
     *
     * @param address the address
     * @param size    the size of zeros to be filled
     */
    public void zero(int address, int size) {
        this.writeBlock(address, size, new byte[size]);
    }

    /**
     * Perform a read or write operation on the specified range of addresses.
     *
     * @param address                  the starting address
     * @param size                     the size
     * @param buffer                   the buffer
     * @param write                    a value indicating whether the access is a read or write
     * @param createNewPageIfNecessary a value indicating whether creating a new page if necessary
     */
    public void access(int address, int size, byte[] buffer, boolean write, boolean createNewPageIfNecessary) {
        if (this.speculative) {
            this.doSpeculativeAccess(address, size, buffer, write);
        } else {
            this.doNonSpeculativeAccess(address, size, buffer, write, createNewPageIfNecessary);
        }
    }

    /**
     * Perform a speculative read or write operation on the specified range of addresses.
     *
     * @param address the starting address
     * @param size    the size
     * @param buffer  the buffer
     * @param write   a value indicating whether the access is a read or write
     */
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
                this.speculativeMemoryBlocks.put(index, new ArrayList<>());
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
     * Enter the speculative state.
     */
    public void enterSpeculativeState() {
        this.speculative = true;
    }

    /**
     * Exit the speculative state.
     */
    public void exitSpeculativeState() {
        this.speculativeMemoryBlocks.clear();
        this.speculative = false;
    }

    /**
     * Perform a non-speculative access operation on the specified range of addresses.
     *
     * @param address                  the starting address
     * @param size                     the size
     * @param buffer                   the buffer
     * @param write                    a value indicating whether the access is a read or write
     * @param createNewPageIfNecessary a value indicating whether creating a new page if necessary
     */
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
     * Create pages for the specified range of addresses if necessary.
     *
     * @param address the starting address
     * @param size    the size
     * @return the starting tag
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
            if (this.getPage(tag) != null) {
                throw new IllegalArgumentException();
            }
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
     * Remove pages for the specified range of addresses.
     *
     * @param address the starting address
     * @param size    the size
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
     * Remap the specified range of addresses.
     *
     * @param oldAddr the old starting address
     * @param oldSize the old size
     * @param newSize the new size
     * @return the new starting address
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
     * Get the physical address of the specified virtual address.
     *
     * @param virtualAddress the virtual address
     * @return the translated physical address of the specified virtual address
     */
    public int getPhysicalAddress(int virtualAddress) {
        return this.getPage(virtualAddress).physicalAddress + getDisplacement(virtualAddress);
    }

    /**
     * Get the page containing the specified address.
     *
     * @param address the address
     * @return the page containing the specified address if any exists; otherwise null
     */
    private Page getPage(int address) {
        int index = getIndex(address);

        return this.pages.containsKey(index) ? this.pages.get(index) : null;
    }

    /**
     * Add a page containing the specified address.
     *
     * @param address the address
     * @return a newly created page containing the specified address
     */
    private Page addPage(int address) {
        int index = getIndex(address);

        this.numPages++;
        Page page = new Page(getExperiment().currentMemoryPageId++);

        this.pages.put(index, page);

        return page;
    }

    /**
     * Remove a page containing the specified address.
     *
     * @param address the address
     */
    private void removePage(int address) {
        int index = getIndex(address);

        this.pages.remove(index);
    }

    /**
     * Perform a non-speculative access operation on the specified range of addresses at the page boundary.
     *
     * @param address                  the starting address
     * @param size                     the size
     * @param buffer                   the buffer
     * @param write                    a value indicating whether the access is a read or write
     * @param createNewPageIfNecessary a value indicating whether creating a new page if necessary
     */
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
     * Act on when a page is created.
     *
     * @param id the ID of the newly created page
     */
    protected void onPageCreated(int id) {
        this.byteBuffers.put(id, ByteBuffer.allocate(Memory.getPageSize()).order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
    }

    /**
     * Act on when a page is accessed.
     *
     * @param pageId       the ID of the page
     * @param displacement the displacement within the page
     * @param buffer       the buffer
     * @param offset       the offset within the buffer
     * @param size         the size of data to be accessed within the buffer
     * @param write        whether the access is a read or write
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

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "mem-" + getId()) {{
            getChildren().add(new ReportNode(this, "numPages", getNumPages() + ""));
        }});
    }

    /**
     * Get the ID of the memory.
     *
     * @return the ID of the memory
     */
    public int getId() {
        return id;
    }

    /**
     * Get a value indicating whether the memory is little endian or not.
     *
     * @return a value indicating whether the memory is little endian or not
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     * Get the kernel.
     *
     * @return the kernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Get the ID of the process.
     *
     * @return the ID of the process
     */
    public int getProcessId() {
        return processId;
    }

    /**
     * Get a value indicating whether the memory is currently in the speculative mode or not.
     *
     * @return a value indicating whether the memory is currently in the speculative mode or not
     */
    public boolean isSpeculative() {
        return speculative;
    }

    /**
     * Get the geometry of the memory.
     *
     * @return the geometry of the memory
     */
    public static CacheGeometry getGeometry() {
        return geometry;
    }

    /**
     * Get the number of pages contained in the memory.
     *
     * @return the number of pages contained in the memory
     */
    public int getNumPages() {
        return numPages;
    }

    /**
     * Get the byte buffer for the specified page ID.
     *
     * @param pageId the ID of the page
     * @return the byte buffer for the specified page ID.
     */
    private ByteBuffer getByteBuffer(int pageId) {
        return this.byteBuffers.containsKey(pageId) ? this.byteBuffers.get(pageId) : null;
    }

    /**
     * Get the name of the memory.
     *
     * @return the name of the memory
     */
    @Override
    public String getName() {
        return getKernel().getProcessFromId(getProcessId()).getName() + "/mem";
    }

    /**
     * Page.
     */
    private class Page {
        private int id;
        private int physicalAddress;

        /**
         * Create a page.
         *
         * @param id the ID of the page that is to be created
         */
        private Page(int id) {
            this.id = id;
            this.physicalAddress = this.id << Memory.getPageSizeInLog2();

            onPageCreated(id);
        }

        /**
         * Perform an access for the specified range of addresses.
         *
         * @param address the starting address
         * @param buffer  the buffer
         * @param offset  the offset within the buffer
         * @param size    the size of data to be accessed within the buffer
         * @param write   a value indicating whether the access is a read or write
         */
        private void doAccess(int address, byte[] buffer, int offset, int size, boolean write) {
            doPageAccess(id, getDisplacement(address), buffer, offset, size, write);
        }
    }

    /**
     * Speculative memory block. Used for representing a range of addresses involved in a speculative access.
     */
    private class SpeculativeMemoryBlock {
        private int tag;
        private byte[] data;

        /**
         * Create a speculative memory block.
         *
         * @param tag the tag
         */
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
     * Get the displacement for the specified address.
     *
     * @param address the address
     * @return the displacement for the specified address
     */
    public static int getDisplacement(int address) {
        return CacheGeometry.getDisplacement(address, geometry);
    }

    /**
     * Get the tag for the specified address.
     *
     * @param address the address
     * @return the tag for the specified address
     */
    private static int getTag(int address) {
        return CacheGeometry.getTag(address, geometry);
    }

    /**
     * Get the index for the specified address.
     *
     * @param address the address
     * @return the index for the specified address
     */
    private static int getIndex(int address) {
        return CacheGeometry.getLineId(address, geometry);
    }

    /**
     * Get the memory's page size in bytes in Log2.
     *
     * @return the memory's page size in in bytes in Log2
     */
    public static int getPageSizeInLog2() {
        return geometry.getLineSizeInLog2();
    }

    /**
     * Get the memory's page size in bytes.
     *
     * @return the memory's page size in bytes
     */
    public static int getPageSize() {
        return geometry.getLineSize();
    }
}
