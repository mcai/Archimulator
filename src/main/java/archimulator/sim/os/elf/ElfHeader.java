/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
 *
 * @author Min Cai
 */
public class ElfHeader {
    /**
     *
     */
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

    /**
     *
     * @param elfFile
     * @throws IOException
     */
    public ElfHeader(ElfFile elfFile) throws IOException {
        this.e_type = elfFile.readUnsignedHalfWord();

        this.e_machine = elfFile.readUnsignedHalfWord();
        this.e_version = elfFile.readUnsignedWord();
        this.e_entry = elfFile.readUnsignedWord();
        this.e_phoff = elfFile.readUnsignedWord();
        this.e_shoff = elfFile.readUnsignedWord();
        this.e_flags = elfFile.readUnsignedWord();

        this.e_ehsize = elfFile.readUnsignedHalfWord();
        this.e_phentsize = elfFile.readUnsignedHalfWord();
        this.e_phnum = elfFile.readUnsignedHalfWord();
        this.e_shentsize = elfFile.readUnsignedHalfWord();
        this.e_shnum = elfFile.readUnsignedHalfWord();
        this.e_shstrndx = elfFile.readUnsignedHalfWord();
    }

    /**
     *
     * @return
     */
    public int getE_type() {
        return e_type;
    }

    /**
     *
     * @return
     */
    public int getE_machine() {
        return e_machine;
    }

    /**
     *
     * @return
     */
    public long getE_version() {
        return e_version;
    }

    /**
     *
     * @return
     */
    public long getE_entry() {
        return e_entry;
    }

    /**
     *
     * @return
     */
    public long getE_phoff() {
        return e_phoff;
    }

    /**
     *
     * @return
     */
    public long getE_shoff() {
        return e_shoff;
    }

    /**
     *
     * @return
     */
    public long getE_flags() {
        return e_flags;
    }

    /**
     *
     * @return
     */
    public int getE_ehsize() {
        return e_ehsize;
    }

    /**
     *
     * @return
     */
    public int getE_phentsize() {
        return e_phentsize;
    }

    /**
     *
     * @return
     */
    public int getE_phnum() {
        return e_phnum;
    }

    /**
     *
     * @return
     */
    public int getE_shentsize() {
        return e_shentsize;
    }

    /**
     *
     * @return
     */
    public int getE_shnum() {
        return e_shnum;
    }

    /**
     *
     * @return
     */
    public int getE_shstrndx() {
        return e_shstrndx;
    }
}
