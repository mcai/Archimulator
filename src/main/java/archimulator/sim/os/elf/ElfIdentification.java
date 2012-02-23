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

public class ElfIdentification {
    public static final int ElfClassNone = 0;
    public static final int ElfClass32 = 1;
    public static final int ElfClass64 = 2;

    public static final int ElfDataNone = 0;
    public static final int ElfData2Lsb = 1;
    public static final int ElfData2Msb = 2;

    private int ei_class;
    private int ei_data;
    private int ei_version;

    public void read(ElfFile elfFile) throws IOException {
        byte[] e_ident = new byte[16];
        elfFile.read(e_ident);

        if (!(e_ident[0] == 0x7f && e_ident[1] == (byte) 'E' && e_ident[2] == (byte) 'L' && e_ident[3] == (byte) 'F')) {
            throw new RuntimeException("Not elf file");
        }

        this.ei_class = e_ident[4] == 1 ? ElfClass32 : e_ident[4] == 2 ? ElfClass64 : ElfClassNone;
        this.ei_data = e_ident[5] == 1 ? ElfData2Lsb : e_ident[5] == 2 ? ElfData2Msb : ElfDataNone;

        this.ei_version = e_ident[6];
    }

    public int getEi_class() {
        return ei_class;
    }

    public int getEi_data() {
        return ei_data;
    }

    public int getEi_version() {
        return ei_version;
    }
}
