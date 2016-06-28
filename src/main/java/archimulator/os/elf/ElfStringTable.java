/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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

/**
 * ELF string table.
 *
 * @author Min Cai
 */
public class ElfStringTable {
    private byte[] data;

    /**
     * Create an ELF string table.
     *
     * @param elfFile       the ELF file
     * @param sectionHeader the section header that containing the string table
     */
    public ElfStringTable(ElfFile elfFile, ElfSectionHeader sectionHeader) {
        if (sectionHeader.getType() != ElfSectionHeader.SHT_STRTAB) {
            throw new IllegalArgumentException("Section is not a string table");
        }

        this.data = sectionHeader.readContent(elfFile);
    }

    /**
     * Get the string by the specified index.
     *
     * @param index the index
     * @return the string by the specified index
     */
    public String getString(int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; data[i] != '\0'; i++) {
            sb.append((char) data[i]);
        }

        return sb.toString();
    }
}
