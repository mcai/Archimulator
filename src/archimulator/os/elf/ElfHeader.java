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
package archimulator.os.elf;

import java.io.IOException;

public class ElfHeader {
    public static final short EM_MIPS = 8;

    private int e_type;
    private int e_machine;
    private long e_version;
    private long e_entry;
    private long e_phoff;
    private long e_shoff;
    private long e_flags;
    private int e_ehsize;
    private int e_phentsize;
    private int e_phnum;
    private int e_shentsize;
    private int e_shnum;
    private int e_shstrndx;

    public ElfHeader(ElfFile elfFile) throws IOException {
        this.e_type = elfFile.readUHalf();

        this.e_machine = elfFile.readUHalf();
        this.e_version = elfFile.readUWord();
        this.e_entry = elfFile.readUWord();
        this.e_phoff = elfFile.readUWord();
        this.e_shoff = elfFile.readUWord();
        this.e_flags = elfFile.readUWord();

        this.e_ehsize = elfFile.readUHalf();
        this.e_phentsize = elfFile.readUHalf();
        this.e_phnum = elfFile.readUHalf();
        this.e_shentsize = elfFile.readUHalf();
        this.e_shnum = elfFile.readUHalf();
        this.e_shstrndx = elfFile.readUHalf();
    }

    public int getE_type() {
        return e_type;
    }

    public int getE_machine() {
        return e_machine;
    }

    public long getE_version() {
        return e_version;
    }

    public long getE_entry() {
        return e_entry;
    }

    public long getE_phoff() {
        return e_phoff;
    }

    public long getE_shoff() {
        return e_shoff;
    }

    public long getE_flags() {
        return e_flags;
    }

    public int getE_ehsize() {
        return e_ehsize;
    }

    public int getE_phentsize() {
        return e_phentsize;
    }

    public int getE_phnum() {
        return e_phnum;
    }

    public int getE_shentsize() {
        return e_shentsize;
    }

    public int getE_shnum() {
        return e_shnum;
    }

    public int getE_shstrndx() {
        return e_shstrndx;
    }
}
