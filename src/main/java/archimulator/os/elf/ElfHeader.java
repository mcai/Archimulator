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
package archimulator.os.elf;

import java.io.IOException;

/**
 * ELF header.
 *
 * @author Min Cai
 */
public class ElfHeader {
    /**
     * Indicating this is a MIPS ELF header.
     */
    public static final short EM_MIPS = 8;

    private int type;

    private int machine;

    private long version;

    private long entry;

    private long programHeaderTableOffset;

    private long sectionHeaderTableOffset;

    private long flags;

    private int elfHeaderSize;

    private int programHeaderTableEntrySize;

    private int programHeaderTableEntryCount;

    private int sectionHeaderTableEntrySize;

    private int sectionHeaderTableEntryCount;

    private int sectionHeaderStringTableIndex;

    /**
     * Create an ELF header.
     *
     * @param elfFile the ELF file
     * @throws IOException
     */
    public ElfHeader(ElfFile elfFile) throws IOException {
        this.type = elfFile.readUnsignedHalfWord();

        this.machine = elfFile.readUnsignedHalfWord();
        this.version = elfFile.readUnsignedWord();
        this.entry = elfFile.readUnsignedWord();
        this.programHeaderTableOffset = elfFile.readUnsignedWord();
        this.sectionHeaderTableOffset = elfFile.readUnsignedWord();
        this.flags = elfFile.readUnsignedWord();

        this.elfHeaderSize = elfFile.readUnsignedHalfWord();
        this.programHeaderTableEntrySize = elfFile.readUnsignedHalfWord();
        this.programHeaderTableEntryCount = elfFile.readUnsignedHalfWord();
        this.sectionHeaderTableEntrySize = elfFile.readUnsignedHalfWord();
        this.sectionHeaderTableEntryCount = elfFile.readUnsignedHalfWord();
        this.sectionHeaderStringTableIndex = elfFile.readUnsignedHalfWord();
    }

    /**
     * Get the object file type.
     *
     * @return the object file type
     */
    public int getType() {
        return type;
    }

    /**
     * Get the machine architecture.
     *
     * @return the machine architecture
     */
    public int getMachine() {
        return machine;
    }

    /**
     * Get the object file version.
     *
     * @return the object file version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Get the entry point virtual address.
     *
     * @return the entry point virtual address
     */
    public long getEntry() {
        return entry;
    }

    /**
     * Get the program header table offset.
     *
     * @return the program header table offset
     */
    public long getProgramHeaderTableOffset() {
        return programHeaderTableOffset;
    }

    /**
     * Get the section header table offset.
     *
     * @return the section header table offset
     */
    public long getSectionHeaderTableOffset() {
        return sectionHeaderTableOffset;
    }

    /**
     * Get the processor-specific flags.
     *
     * @return the processor-specific flags
     */
    public long getFlags() {
        return flags;
    }

    /**
     * Get the ELF header size in bytes.
     *
     * @return the ELF header size in bytes
     */
    public int getElfHeaderSize() {
        return elfHeaderSize;
    }

    /**
     * Get the program header table entry size.
     *
     * @return the program header table entry size
     */
    public int getProgramHeaderTableEntrySize() {
        return programHeaderTableEntrySize;
    }

    /**
     * Get the program header table entry count.
     *
     * @return the program header table entry count
     */
    public int getProgramHeaderTableEntryCount() {
        return programHeaderTableEntryCount;
    }

    /**
     * Get the section header table entry size.
     *
     * @return the section header table entry size
     */
    public int getSectionHeaderTableEntrySize() {
        return sectionHeaderTableEntrySize;
    }

    /**
     * Get the section header table entry count.
     *
     * @return the section header table entry count
     */
    public int getSectionHeaderTableEntryCount() {
        return sectionHeaderTableEntryCount;
    }

    /**
     * Get the section header string table index.
     *
     * @return the section header string table index
     */
    public int getSectionHeaderStringTableIndex() {
        return sectionHeaderStringTableIndex;
    }
}
