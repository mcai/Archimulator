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
package archimulator.os.elf;

import archimulator.util.buffer.BigEndianBufferAccessor;
import archimulator.util.buffer.BufferAccessor;
import archimulator.util.buffer.LittleEndianBufferAccessor;
import archimulator.util.buffer.RandomAccessFileBuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ELF file.
 *
 * @author Min Cai
 */
public class ElfFile {
    private RandomAccessFile file;
    private RandomAccessFileBuffer buffer;

    private ElfIdentification identification;
    private ElfHeader header;
    private List<ElfSectionHeader> sectionHeaders;
    private List<ElfProgramHeader> programHeaders;

    private ElfStringTable stringTable;

    private boolean littleEndian;

    private Map<Integer, Symbol> symbols;

    private Map<Integer, Symbol> localFunctionSymbols;
    private Map<Integer, Symbol> localObjectSymbols;
    private Map<Integer, Symbol> commonObjectSymbols;

    private long position;

    /**
     * Create an ELF file.
     *
     * @param filename the file name
     */
    public ElfFile(String filename) {
        try {
            this.file = new RandomAccessFile(filename, "r");
            this.buffer = new RandomAccessFileBuffer(this.file);

            this.sectionHeaders = new ArrayList<>();
            this.programHeaders = new ArrayList<>();

            this.identification = new ElfIdentification();
            this.identification.read(this);

            if (this.identification.getClz() != ElfIdentification.ElfClass32) {
                throw new IllegalArgumentException();
            }

            this.littleEndian = (this.identification.getData() == ElfIdentification.ElfData2Lsb);

            this.header = new ElfHeader(this);

            if (this.header.getMachine() != ElfHeader.EM_MIPS) {
                throw new IllegalArgumentException();
            }

            this.setPosition(this.header.getSectionHeaderTableOffset());

            for (int i = 0; i < this.header.getSectionHeaderTableEntryCount(); i++) {
                this.setPosition(this.header.getSectionHeaderTableOffset() + (i * this.header.getSectionHeaderTableEntrySize()));
                this.sectionHeaders.add(new ElfSectionHeader(this));
            }

            this.stringTable = new ElfStringTable(this, this.sectionHeaders.get(this.header.getSectionHeaderStringTableIndex()));

            this.setPosition(this.header.getProgramHeaderTableOffset());

            for (int i = 0; i < this.header.getProgramHeaderTableEntryCount(); i++) {
                this.programHeaders.add(new ElfProgramHeader(this));
            }

            this.symbols = new HashMap<>();
            this.localFunctionSymbols = new HashMap<>();
            this.localObjectSymbols = new HashMap<>();
            this.commonObjectSymbols = new HashMap<>();

            this.loadSymbols();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load symbols.
     */
    private void loadSymbols() {
        List<ElfSectionHeader> sections = this.getSectionHeaders(ElfSectionHeader.SHT_SYMTAB);
        if (!sections.isEmpty()) {
            this.loadSymbolsBySection(sections.get(0));
        }

        this.loadLocalFunctions();
        this.loadLocalObjects();
        this.loadCommonObjects();
    }

    /**
     * Load symbols by the specified section.
     *
     * @param sectionHeader the section header
     */
    private void loadSymbolsBySection(ElfSectionHeader sectionHeader) {
        try {
            int numSymbols = 1;

            if (sectionHeader.getEntrySize() != 0) {
                numSymbols = (int) sectionHeader.getSize() / (int) sectionHeader.getEntrySize();
            }

            long offset = sectionHeader.getOffset();
            for (int c = 0; c < numSymbols; offset += sectionHeader.getEntrySize(), c++) {
                this.setPosition(offset);

                Symbol symbol = new Symbol(this, sectionHeader);
                symbol.setNameIndex(this.readUnsignedWord());
                symbol.setValue(this.readUnsignedWord());
                symbol.setSize(this.readUnsignedWord());
                symbol.setInfo(this.read());
                symbol.setOther(this.read());
                symbol.setSectionHeaderTableIndex(this.readUnsignedHalfWord());

                this.symbols.put((int) symbol.getValue(), symbol);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the symbol at the specified address.
     *
     * @param address the address
     * @return the symbol at the specified address
     */
    public Symbol getSymbol(long address) {
        for (Symbol symbol : this.symbols.values()) {
            if (symbol.getValue() == address) {
                return symbol;
            }
        }

        return null;
    }

    /**
     * Load local functions.
     */
    public void loadLocalFunctions() {
        this.symbols.values().stream().filter(symbol -> symbol.getType() == Symbol.STT_FUNC).forEach(symbol -> {
            int idx = symbol.getSectionHeaderTableIndex();
            if (idx > Symbol.SHN_LOPROC && idx < Symbol.SHN_HIPROC) {
                if (symbol.getName().length() > 0) {
                    this.localFunctionSymbols.put((int) symbol.getValue(), symbol);
                }
            } else if (idx >= 0 && this.getSectionHeaders().get(idx).getType() != ElfSectionHeader.SHT_NULL) {
                this.localFunctionSymbols.put((int) symbol.getValue(), symbol);
            }
        });
    }

    /**
     * Load local objects.
     */
    public void loadLocalObjects() {
        this.symbols.values().stream().filter(symbol -> symbol.getType() == Symbol.STT_OBJECT).forEach(symbol -> {
            int idx = symbol.getSectionHeaderTableIndex();
            if (idx > Symbol.SHN_LOPROC && idx < Symbol.SHN_HIPROC) {
                if (symbol.getName().length() > 0) {
                    this.localObjectSymbols.put((int) symbol.getValue(), symbol);
                }
            } else if (idx >= 0 && this.getSectionHeaders().get(idx).getType() != ElfSectionHeader.SHT_NULL) {
                this.localObjectSymbols.put((int) symbol.getValue(), symbol);
            }
        });
    }

    /**
     * Load common objects.
     */
    public void loadCommonObjects() {
        this.symbols.values().stream()
                .filter(symbol -> symbol.getBind() == Symbol.STB_GLOBAL && symbol.getType() == Symbol.STT_OBJECT)
                .filter(symbol -> symbol.getSectionHeaderTableIndex() == Symbol.SHN_COMMON)
                .forEach(symbol -> this.commonObjectSymbols.put((int) symbol.getValue(), symbol));
    }

    /**
     * Get the list of section headers matching the specified type.
     *
     * @param type the section header type to be matched
     * @return the list of section headers matching the specified type
     */
    public List<ElfSectionHeader> getSectionHeaders(int type) {
        return this.getSectionHeaders().stream().filter(sectionHeader -> sectionHeader.getType() == type).collect(Collectors.toList());
    }

    /**
     * Close the ELF file.
     *
     * @throws IOException
     */
    public void close()
            throws IOException {
        this.file.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

    /**
     * Get the current cursor position.
     *
     * @return the current cursor position
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Set the current cursor position.
     *
     * @param position the current cursor position
     */
    public void setPosition(long position) {
        this.position = position;
    }

    /**
     * Get the buffer accessor.
     *
     * @return the buffer accessor
     */
    public BufferAccessor getBufferAccessor() {
        if (this.littleEndian) {
            return new LittleEndianBufferAccessor();
        } else {
            return new BigEndianBufferAccessor();
        }
    }

    /**
     * Read the content from the current position in the ELF file.
     *
     * @param content the content buffer
     * @throws IOException
     */
    public void read(byte[] content)
            throws IOException {
        this.file.seek(this.position);
        this.file.readFully(content);

        this.position += content.length;
    }

    /**
     * Read the content from the specified offset in the ELF file.
     *
     * @param offset  the offset
     * @param content the content buffer
     * @throws IOException
     */
    public void read(long offset, byte[] content)
            throws IOException {
        this.file.seek(offset);
        this.file.readFully(content);
    }

    /**
     * Read a byte of data from the current position in the ELF file.
     *
     * @return a byte of data read from the current position in the ELF file
     * @throws IOException
     */
    public int read()
            throws IOException {
        this.file.seek(this.position);
        int data = this.file.read();

        this.position++;

        return data;
    }

    /**
     * Read a byte of data from the specified offset in the ELF file.
     *
     * @param offset the offset
     * @return a byte of data read from the specified offset in the ELF file
     * @throws IOException
     */
    public int read(long offset)
            throws IOException {
        this.file.seek(offset);
        return this.file.read();
    }

    /**
     * Read an unsigned word of data from the current position in the ELF file.
     *
     * @return an unsigned word of data from the current position in the ELF file
     * @throws IOException
     */
    public long readUnsignedWord() throws IOException {
        long data = this.readUnsignedWord(this.position);
        this.position += 4;
        return data;
    }

    /**
     * Read an unsigned word of data from the specified offset in the ELF file.
     *
     * @param offset the offset
     * @return an unsigned word of data from the specified offset in the ELF file
     * @throws IOException
     */
    public long readUnsignedWord(long offset) throws IOException {
        return this.getBufferAccessor().getU4(this.buffer, offset);
    }

    /**
     * Read an unsigned half word of data from the current position in the ELF file.
     *
     * @return an unsigned half word of data from the current position in the ELF file
     * @throws IOException
     */
    public int readUnsignedHalfWord() throws IOException {
        int data = this.readUnsignedHalfWord(this.position);

        this.position += 2;

        return data;
    }

    /**
     * Read an unsigned half word of data from the specified offset in the ELF file.
     *
     * @param offset the offset
     * @return an unsigned half word of data from the specified offset in the ELF file
     * @throws IOException
     */
    public int readUnsignedHalfWord(long offset) throws IOException {
        return this.getBufferAccessor().getU2(this.buffer, offset);
    }

    /**
     * Get the identification.
     *
     * @return the identification
     */
    public ElfIdentification getIdentification() {
        return this.identification;
    }

    /**
     * Get the header.
     *
     * @return the header
     */
    public ElfHeader getHeader() {
        return this.header;
    }

    /**
     * Get the list of section headers.
     *
     * @return the list of section headers
     */
    public List<ElfSectionHeader> getSectionHeaders() {
        return this.sectionHeaders;
    }

    /**
     * Get the list of program headers.
     *
     * @return the list of program headers
     */
    public List<ElfProgramHeader> getProgramHeaders() {
        return this.programHeaders;
    }

    /**
     * Get the string table.
     *
     * @return the string table
     */
    public ElfStringTable getStringTable() {
        return this.stringTable;
    }

    /**
     * Get a value indicating whether the ELF file is little endian or not.
     *
     * @return a value indicating whether the ELF file is little endian or not
     */
    public boolean isLittleEndian() {
        return this.littleEndian;
    }

    /**
     * Get the map of symbols.
     *
     * @return the map of symbols
     */
    public Map<Integer, Symbol> getSymbols() {
        return symbols;
    }

    /**
     * Get the map of local function symbols.
     *
     * @return the map of local function symbols
     */
    public Map<Integer, Symbol> getLocalFunctionSymbols() {
        return localFunctionSymbols;
    }

    /**
     * Get the map of local object symbols.
     *
     * @return the map of local object symbols
     */
    public Map<Integer, Symbol> getLocalObjectSymbols() {
        return localObjectSymbols;
    }

    /**
     * Get the map of common object symbols.
     *
     * @return the map of common object symbols
     */
    public Map<Integer, Symbol> getCommonObjectSymbols() {
        return commonObjectSymbols;
    }
}
