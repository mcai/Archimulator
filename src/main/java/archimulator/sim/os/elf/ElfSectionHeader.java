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
package archimulator.sim.os.elf;

import java.io.IOException;

/**
 * ELF section header.
 *
 * @author Min Cai
 */
public class ElfSectionHeader {
    /**
     * This value marks the section header as inactive; it does not have an associated section.
     */
    public static final int SHT_NULL = 0;
    /**
     * The section holds information defined by the program, whose format and meaning are determined solely by the program.
     */
    public static final int SHT_PROGBITS = 1;
    /**
     * These sections hold a symbol table.
     */
    public static final int SHT_SYMTAB = 2;
    /**
     * The section holds a string table.
     */
    public static final int SHT_STRTAB = 3;
    /**
     * The section holds relocation entries with explicit addends,
     * such as type Elf32_Rela for the 32-bit class of object files
     * or type Elf64_Rela for the 64-bit class of object files.
     */
    public static final int SHT_RELA = 4;
    /**
     * The section holds a symbol hash table.
     */
    public static final int SHT_HASH = 5;
    /**
     * The section holds information for dynamic linking.
     */
    public static final int SHT_DYNAMIC = 6;
    /**
     * The section holds information that marks the file in some way.
     */
    public static final int SHT_NOTE = 7;
    /**
     * A section of this type occupies no space in the file but otherwise resembles SHT_PROGBITS.
     */
    public static final int SHT_NOBITS = 8;
    /**
     * The section holds relocation entries without explicit addends, such as type Elf32_Rel for the 32-bit class of object files or type Elf64_Rel for the 64-bit class of object files.
     */
    public static final int SHT_REL = 9;
    /**
     * This section type is reserved but has unspecified semantics.
     */
    public static final int SHT_SHLIB = 10;

    /**
     * This section holds a minimal set of symbols adequate for dynamic linking.
     */
    public static final int SHT_DYNSYM = 11;

    /**
     * The section contains data that should be writable during process execution.
     */
    public static final int SHF_WRITE = 0x1;

    /**
     * The section occupies memory during process execution.
     */
    public static final int SHF_ALLOC = 0x2;

    /**
     * The section contains executable machine instructions.
     */
    public static final int SHF_EXECINSTR = 0x4;

    private long nameIndex;
    private long type;
    private long flags;
    private long address;
    private long offset;
    private long size;
    private long link;
    private long info;
    private long addressAlignment;
    private long entrySize;

    private ElfFile elfFile;
    private String name;

    /**
     * Create an ELF section header.
     *
     * @param elfFile the ELF file
     * @throws IOException
     */
    public ElfSectionHeader(ElfFile elfFile) throws IOException {
        this.elfFile = elfFile;

        this.nameIndex = elfFile.readUnsignedWord();
        this.type = elfFile.readUnsignedWord();
        this.flags = elfFile.readUnsignedWord();
        this.address = elfFile.readUnsignedWord();
        this.offset = elfFile.readUnsignedWord();
        this.size = elfFile.readUnsignedWord();
        this.link = elfFile.readUnsignedWord();
        this.info = elfFile.readUnsignedWord();
        this.addressAlignment = elfFile.readUnsignedWord();
        this.entrySize = elfFile.readUnsignedWord();
    }

    /**
     * Read the content of the ELF section header as a byte array from the ELF file.
     *
     * @param elfFile the ELF file
     * @return the content of the ELF section header as a byte array read from the ELF file
     */
    public byte[] readContent(ElfFile elfFile) {
        long position = elfFile.getPosition();
        elfFile.setPosition(this.offset);

        byte[] content = new byte[(int) this.size];
        try {
            elfFile.read(this.offset, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        elfFile.setPosition(position);

        return content;
    }

    /**
     * Get the name of the ELF section header.
     *
     * @return the name of the ELF section header
     */
    public String getName() {
        if (this.name == null) {
            this.name = this.elfFile.getStringTable().getString((int) this.nameIndex);
        }
        return this.name;
    }

    /**
     * Get the name of the section. Its value is an index into the section header string table section, giving the location of a null-terminated string.
     *
     * @return the name of the section
     */
    public long getNameIndex() {
        return this.nameIndex;
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public long getType() {
        return this.type;
    }

    /**
     * Get the flags.
     *
     * @return the flags
     */
    public long getFlags() {
        return this.flags;
    }

    /**
     * Get the address at which the section's first byte should reside.
     *
     * @return the address at which the section's first byte should reside.
     */
    public long getAddress() {
        return this.address;
    }

    /**
     * Get the byte offset from the beginning of the file to the first byte in the section.
     *
     * @return the byte offset from the beginning of the file to the first byte in the section.
     */
    public long getOffset() {
        return this.offset;
    }

    /**
     * Get the section's size in bytes.
     *
     * @return the section's size in bytes
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Get a section header table index link, whose interpretation depends on the section type.
     *
     * @return a section header table index link, whose interpretation depends on the section type
     */
    public long getLink() {
        return this.link;
    }

    /**
     * Get the extra information, whose interpretation depends on the section type.
     *
     * @return the extra information, whose interpretation depends on the section type
     */
    public long getInfo() {
        return this.info;
    }

    /**
     * Get the address alignment.
     *
     * @return address alignment
     */
    public long getAddressAlignment() {
        return this.addressAlignment;
    }

    /**
     * Get the size in bytes of each entry.
     *
     * @return the size in bytes of each entry
     */
    public long getEntrySize() {
        return this.entrySize;
    }

    @Override
    public String toString() {
        return String.format("ElfSectionHeader{name=%s, nameIndex=0x%08x, type=0x%08x, flags=0x%08x, address=0x%08x, offset=%d, size=%d, link=0x%08x, info=0x%08x, addressAlignment=0x%08x, entrySize=%d}", this.getName(), this.nameIndex, this.type, this.flags, this.address, this.offset, this.size, this.link, this.info, this.addressAlignment, this.entrySize);
    }
}
