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

public class ElfProgramHeader {
    private long p_type;
    private long p_offset;
    private long p_vaddr;
    private long p_paddr;
    private long p_filesz;
    private long p_memsz;
    private long p_flags;
    private long p_align;

    public ElfProgramHeader(ElfFile elfFile) throws IOException {
        this.p_type = elfFile.readUnsignedWord();
        this.p_offset = elfFile.readUnsignedWord();
        this.p_vaddr = elfFile.readUnsignedWord();
        this.p_paddr = elfFile.readUnsignedWord();
        this.p_filesz = elfFile.readUnsignedWord();
        this.p_memsz = elfFile.readUnsignedWord();
        this.p_flags = elfFile.readUnsignedWord();
        this.p_align = elfFile.readUnsignedWord();
    }

    public byte[] readContent(ElfFile elfFile) throws IOException {
        long position = elfFile.getPosition();

        elfFile.setPosition(this.p_offset);

        byte[] content = new byte[(int) this.p_filesz];
        elfFile.read(content);

        elfFile.setPosition(position);

        return content;
    }

    public long getP_type() {
        return p_type;
    }

    public long getP_offset() {
        return p_offset;
    }

    public long getP_vaddr() {
        return p_vaddr;
    }

    public long getP_paddr() {
        return p_paddr;
    }

    public long getP_filesz() {
        return p_filesz;
    }

    public long getP_memsz() {
        return p_memsz;
    }

    public long getP_flags() {
        return p_flags;
    }

    public long getP_align() {
        return p_align;
    }
}
