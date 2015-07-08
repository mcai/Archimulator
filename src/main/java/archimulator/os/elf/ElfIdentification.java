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
 * ELF identification.
 *
 * @author Min Cai
 */
public class ElfIdentification {
    /**
     * Invalid class.
     */
    public static final int ElfClassNone = 0;

    /**
     * 32-bit objects.
     */
    public static final int ElfClass32 = 1;

    /**
     * 64-bit objects.
     */
    public static final int ElfClass64 = 2;

    /**
     * Invalid data encoding.
     */
    public static final int ElfDataNone = 0;

    /**
     * Specifies 2's complement values, with the least significant byte occupying the lowest address.
     */
    public static final int ElfData2Lsb = 1;
    /**
     * Specifies 2's complement values, with the most significant byte occupying the lowest address.
     */
    public static final int ElfData2Msb = 2;

    private int clz;
    private int data;
    private int version;

    /**
     * Read the ELF identification structure from the ELF file.
     *
     * @param elfFile the ELF file
     * @throws IOException
     */
    public void read(ElfFile elfFile) throws IOException {
        byte[] e_ident = new byte[16];
        elfFile.read(e_ident);

        if (!(e_ident[0] == 0x7f && e_ident[1] == (byte) 'E' && e_ident[2] == (byte) 'L' && e_ident[3] == (byte) 'F')) {
            throw new RuntimeException("Not elf file");
        }

        this.clz = e_ident[4] == 1 ? ElfClass32 : e_ident[4] == 2 ? ElfClass64 : ElfClassNone;
        this.data = e_ident[5] == 1 ? ElfData2Lsb : e_ident[5] == 2 ? ElfData2Msb : ElfDataNone;

        this.version = e_ident[6];
    }

    /**
     * Get the ELF file's class.
     *
     * @return the ELF file's class
     */
    public int getClz() {
        return clz;
    }

    /**
     * Get the data.
     *
     * @return the data
     */
    public int getData() {
        return data;
    }

    /**
     * Get the version.
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }
}
