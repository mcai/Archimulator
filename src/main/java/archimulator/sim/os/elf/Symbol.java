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

/**
 * ELF symbol.
 *
 * @author Min Cai
 */
public class Symbol {
    private long nameIndex;
    private long value;
    private long size;
    private int info;
    private int other;
    private int sectionHeaderTableIndex;

    private ElfFile elfFile;
    private ElfSectionHeader symbolSectionHeader;

    private String name;

    private boolean inline;

    /**
     * Create an ELF symbol.
     *
     * @param elfFile             the ELF file
     * @param symbolSectionHeader the symbol section header
     */
    public Symbol(ElfFile elfFile, ElfSectionHeader symbolSectionHeader) {
        this.elfFile = elfFile;
        this.symbolSectionHeader = symbolSectionHeader;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        if (this.name == null) {
            ElfSectionHeader sectionHeader = this.elfFile.getSectionHeaders().get((int) this.symbolSectionHeader.getLink());
            this.name = new ElfStringTable(this.elfFile, sectionHeader).getString((int) this.getNameIndex());
        }

        return this.name;
    }

    /**
     * Get the symbol's type.
     *
     * @return the symbol's type
     */
    public int getType() {
        return this.getInfo() & 0xf;
    }

    /**
     * Get the symbol's binding.
     *
     * @return the symbol's binding
     */
    public int getBind() {
        return (this.getInfo() >> 4) & 0xf;
    }

    /**
     * Get an index into the object file's symbol string table.
     *
     * @return an index into the object file's symbol string table
     */
    public long getNameIndex() {
        return nameIndex;
    }

    /**
     * Set an index into the object file's symbol string table.
     *
     * @param nameIndex an index into the object file's symbol string table
     */
    public void setNameIndex(long nameIndex) {
        this.nameIndex = nameIndex;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * Set the value.
     *
     * @param value the value
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Set the size.
     *
     * @param size the size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Get the symbol's type and binding attributes.
     *
     * @return the symbol's type and binding attributes
     */
    public int getInfo() {
        return info;
    }

    /**
     * Set the symbol's type and binding attributes.
     *
     * @param info the symbol's type and binding attributes
     */
    public void setInfo(int info) {
        this.info = info;
    }

    /**
     * This member currently holds 0 and has no defined meaning.
     *
     * @return 0
     */
    public int getOther() {
        return other;
    }

    /**
     * This member currently holds 0 and has no defined meaning.
     *
     * @param other 0
     */
    public void setOther(int other) {
        this.other = other;
    }

    /**
     * Get the section header table index.
     *
     * @return the section header table index
     */
    public int getSectionHeaderTableIndex() {
        return sectionHeaderTableIndex;
    }

    /**
     * Set the section header table index.
     *
     * @param sectionHeaderTableIndex the section header table index
     */
    public void setSectionHeaderTableIndex(int sectionHeaderTableIndex) {
        this.sectionHeaderTableIndex = sectionHeaderTableIndex;
    }

    /**
     * Get a value indicating whether the symbol is inline or not.
     *
     * @return a value indicating whether the symbol is inline or not
     */
    public boolean isInline() {
        return inline;
    }

    /**
     * Set a value indicating whether the symbol is inline or not.
     *
     * @param inline a value indicating whether the symbol is inline or not
     */
    public void setInline(boolean inline) {
        this.inline = inline;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s\', st_type=0x%08x, st_bind=0x%08x, nameIndex=0x%08x, value=0x%08x, size=%d, info=%d, other=%d, sectionHeaderTableIndex=%d}", this.getName(), this.getType(), this.getBind(), this.getNameIndex(), this.getValue(), this.getSize(), this.getInfo(), this.getOther(), this.getSectionHeaderTableIndex());
    }

    /**
     * Local symbols are not visible outside the object file containing their definition.
     * Local symbols of the same name may exist in multiple files without interfering with each other.
     */
    public final static int STB_LOCAL = 0;

    /**
     * Global symbols are visible to all object files being combined.
     * One file's definition of a global symbol will satisfy another file's undefined reference
     * to the same global symbol.
     */
    public final static int STB_GLOBAL = 1;

    /**
     * Weak symbols resemble global symbols, but their definitions have lower precedence.
     */
    public final static int STB_WEAK = 2;

    /**
     * The symbol's type is not specified.
     */
    public final static int STT_NOTYPE = 0;

    /**
     * The symbol is associated with a data object, such as a variable, an array, and so on.
     */
    public final static int STT_OBJECT = 1;

    /**
     * The symbol is associated with a function or other executable code.
     */
    public final static int STT_FUNC = 2;

    /**
     * The symbol is associated with a section.
     */
    public final static int STT_SECTION = 3;

    /**
     * Conventionally, the symbol's name gives the name of the source file associated with the object file.
     * A file symbol has STB_LOCAL binding, its section index is SHN_ABS,
     * and it precedes the other STB_LOCAL symbols for the file, if it is present.
     */
    public final static int STT_FILE = 4;

    /**
     * This value marks an undefined, missing, irrelevant, or otherwise meaningless section reference.
     */
    public final static int SHN_UNDEF = 0;

    /**
     * This value specifies the lower bound of the range of reserved indexes.
     */
    public final static int SHN_LORESERVE = 0xffffff00;

    /**
     * Values in this inclusive range (SHN_LOPROC through SHN_HIPROC) are reserved for processor-specific semantics.
     */
    public final static int SHN_LOPROC = 0xffffff00;

    /**
     * Values in this inclusive range (SHN_LOPROC through SHN_HIPROC) are reserved for processor-specific semantics.
     */
    public final static int SHN_HIPROC = 0xffffff1f;

    /**
     * Values in this inclusive range (SHN_LOOS through SHN_HIOS) are reserved for operating system-specific semantics.
     */
    public final static int SHN_LOOS = 0xffffff20;

    /**
     * Values in this inclusive range (SHN_LOOS through SHN_HIOS) are reserved for operating system-specific semantics.
     */
    public final static int SHN_HIOS = 0xffffff3f;

    /**
     * This value specifies absolute values for the corresponding reference.
     */
    public final static int SHN_ABS = 0xfffffff1;

    /**
     * Symbols defined relative to this section are common symbols, such as FORTRAN COMMON or unallocated C external variables.
     */
    public final static int SHN_COMMON = 0xfffffff2;

    /**
     * This value is an escape value.
     */
    public final static int SHN_XINDEX = 0xffffffff;

    /**
     * This value specifies the upper bound of the range of reserved indexes.
     * The system reserves indexes between SHN_LORESERVE and SHN_HIRESERVE, inclusive; the values do not reference the section header table.
     * The section header table does not contain entries for the reserved indexes.
     */
    public final static int SHN_HIRESERVE = 0xffffffff;
}
