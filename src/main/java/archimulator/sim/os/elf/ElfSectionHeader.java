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
package archimulator.sim.os.elf;

import java.io.IOException;

public class ElfSectionHeader {
    public static final int SHT_NULL = 0;
    public static final int SHT_PROGBITS = 1;
    public static final int SHT_SYMTAB = 2;
    public static final int SHT_STRTAB = 3;
    public static final int SHT_RELA = 4;
    public static final int SHT_HASH = 5;
    public static final int SHT_DYNAMIC = 6;
    public static final int SHT_NOTE = 7;
    public static final int SHT_NOBITS = 8;
    public static final int SHT_REL = 9;
    public static final int SHT_SHLIB = 10;
    public static final int SHT_DYNSYM = 11;

    public static final int SHF_WRITE = 0x1;
    public static final int SHF_ALLOC = 0x2;
    public static final int SHF_EXECINSTR = 0x4;

//    public static final int SEC_ALLOC = 0x001;
//    public static final int SEC_LOAD = 0x002;
//    public static final int SEC_RELOC = 0x004;

    private long sh_name;
    private long sh_type;
    private long sh_flags;
    private long sh_addr;
    private long sh_offset;
    private long sh_size;
    private long sh_link;
    private long sh_info;
    private long sh_addralign;
    private long sh_entsize;

    private ElfFile elfFile;
    private String name;

    public ElfSectionHeader(ElfFile elfFile) throws IOException {
        this.elfFile = elfFile;

        this.sh_name = elfFile.readUnsignedWord();
        this.sh_type = elfFile.readUnsignedWord();
        this.sh_flags = elfFile.readUnsignedWord();
        this.sh_addr = elfFile.readUnsignedWord();
        this.sh_offset = elfFile.readUnsignedWord();
        this.sh_size = elfFile.readUnsignedWord();
        this.sh_link = elfFile.readUnsignedWord();
        this.sh_info = elfFile.readUnsignedWord();
        this.sh_addralign = elfFile.readUnsignedWord();
        this.sh_entsize = elfFile.readUnsignedWord();
    }

    public byte[] readContent(ElfFile elfFile) {
        long position = elfFile.getPosition();
        elfFile.setPosition(this.sh_offset);

        byte[] content = new byte[(int) this.sh_size];
        try {
            elfFile.read(this.sh_offset, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        elfFile.setPosition(position);

        return content;
    }

    public String getName() {
        if (this.name == null) {
            this.name = this.elfFile.getStringTable().getString((int) this.sh_name);
        }
        return this.name;
    }

    public long getSh_name() {
        return this.sh_name;
    }

    public long getSh_type() {
        return this.sh_type;
    }

    public long getSh_flags() {
        return this.sh_flags;
    }

    public long getSh_addr() {
        return this.sh_addr;
    }

    public long getSh_offset() {
        return this.sh_offset;
    }

    public long getSh_size() {
        return this.sh_size;
    }

    public long getSh_link() {
        return this.sh_link;
    }

    public long getSh_info() {
        return this.sh_info;
    }

    public long getSh_addralign() {
        return this.sh_addralign;
    }

    public long getSh_entsize() {
        return this.sh_entsize;
    }

    @Override
    public String toString() {
        return String.format("ElfSectionHeader{name=%s, sh_name=0x%08x, sh_type=0x%08x, sh_flags=0x%08x, sh_addr=0x%08x, sh_offset=%d, sh_size=%d, sh_link=0x%08x, sh_info=0x%08x, sh_addralign=0x%08x, sh_entsize=%d}", this.getName(), this.sh_name, this.sh_type, this.sh_flags, this.sh_addr, this.sh_offset, this.sh_size, this.sh_link, this.sh_info, this.sh_addralign, this.sh_entsize);
    }
}
