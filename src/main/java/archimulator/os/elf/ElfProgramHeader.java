/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os.elf;

import java.io.IOException;

/**
 * ELF program header.
 *
 * @author Min Cai
 */
public class ElfProgramHeader {
    private long type;
    private long offset;
    private long virtualAddress;
    private long physicalAddress;
    private long sizeInFile;
    private long sizeInMemory;
    private long flags;
    private long alignment;

    /**
     * Create an ELF program header.
     *
     * @param elfFile the ELF file
     * @throws IOException
     */
    public ElfProgramHeader(ElfFile elfFile) throws IOException {
        this.type = elfFile.readUnsignedWord();
        this.offset = elfFile.readUnsignedWord();
        this.virtualAddress = elfFile.readUnsignedWord();
        this.physicalAddress = elfFile.readUnsignedWord();
        this.sizeInFile = elfFile.readUnsignedWord();
        this.sizeInMemory = elfFile.readUnsignedWord();
        this.flags = elfFile.readUnsignedWord();
        this.alignment = elfFile.readUnsignedWord();
    }

    /**
     * Read the ELF program header as a byte array from the ELF file.
     *
     * @param elfFile the ELF file
     * @return the ELF program header as a byte array
     * @throws IOException
     */
    public byte[] readContent(ElfFile elfFile) throws IOException {
        long position = elfFile.getPosition();

        elfFile.setPosition(this.offset);

        byte[] content = new byte[(int) this.sizeInFile];
        elfFile.read(content);

        elfFile.setPosition(position);

        return content;
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public long getType() {
        return type;
    }

    /**
     * Get the offset.
     *
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Get the virtual address.
     *
     * @return the virtual address
     */
    public long getVirtualAddress() {
        return virtualAddress;
    }

    /**
     * Get the physical address.
     *
     * @return physical address
     */
    public long getPhysicalAddress() {
        return physicalAddress;
    }

    /**
     * Get the size of the ELF program header in the ELF file.
     *
     * @return the size of the ELF program header in the ELF file
     */
    public long getSizeInFile() {
        return sizeInFile;
    }

    /**
     * Get the size of the ELF program header in memory.
     *
     * @return the size of the ELF program header in memory
     */
    public long getSizeInMemory() {
        return sizeInMemory;
    }

    /**
     * Get the flags.
     *
     * @return the flags
     */
    public long getFlags() {
        return flags;
    }

    /**
     * Get the alignment.
     *
     * @return the alignment
     */
    public long getAlignment() {
        return alignment;
    }
}
