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

import archimulator.service.ServiceManager;
import net.pickapack.io.buffer.BigEndianBufferAccessor;
import net.pickapack.io.buffer.BufferAccessor;
import net.pickapack.io.buffer.LittleEndianBufferAccessor;
import net.pickapack.io.buffer.RandomAccessFileBuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
     *
     * @param filename
     */
    public ElfFile(String filename) {
        try {
            this.file = new RandomAccessFile(filename.replaceAll(ServiceManager.USER_HOME_TEMPLATE_ARG, System.getProperty("user.home")), "r");
            this.buffer = new RandomAccessFileBuffer(this.file);

            this.sectionHeaders = new ArrayList<ElfSectionHeader>();
            this.programHeaders = new ArrayList<ElfProgramHeader>();

            this.identification = new ElfIdentification();
            this.identification.read(this);

            if (this.identification.getEi_class() != ElfIdentification.ElfClass32) {
                throw new IllegalArgumentException();
            }

            this.littleEndian = (this.identification.getEi_data() == ElfIdentification.ElfData2Lsb);

            this.header = new ElfHeader(this);

            if (this.header.getE_machine() != ElfHeader.EM_MIPS) {
                throw new IllegalArgumentException();
            }

            this.setPosition(this.header.getE_shoff());

            for (int i = 0; i < this.header.getE_shnum(); i++) {
                this.setPosition(this.header.getE_shoff() + (i * this.header.getE_shentsize()));
                this.sectionHeaders.add(new ElfSectionHeader(this));
            }

            this.stringTable = new ElfStringTable(this, this.sectionHeaders.get(this.header.getE_shstrndx()));

            this.setPosition(this.header.getE_phoff());

            for (int i = 0; i < this.header.getE_phnum(); i++) {
                this.programHeaders.add(new ElfProgramHeader(this));
            }

            this.symbols = new HashMap<Integer, Symbol>();
            this.localFunctionSymbols = new HashMap<Integer, Symbol>();
            this.localObjectSymbols = new HashMap<Integer, Symbol>();
            this.commonObjectSymbols = new HashMap<Integer, Symbol>();

            this.loadSymbols();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSymbols() {
        List<ElfSectionHeader> sections = this.getSectionHeaders(ElfSectionHeader.SHT_SYMTAB);
        if (!sections.isEmpty()) {
            this.loadSymbolsBySection(sections.get(0));
        }

        this.loadLocalFunctions();
        this.loadLocalObjects();
        this.loadCommonObjects();
    }

    private void loadSymbolsBySection(ElfSectionHeader sectionHeader) {
        try {
            int numSymbols = 1;

            if (sectionHeader.getSh_entsize() != 0) {
                numSymbols = (int) sectionHeader.getSh_size() / (int) sectionHeader.getSh_entsize();
            }

            long offset = sectionHeader.getSh_offset();
            for (int c = 0; c < numSymbols; offset += sectionHeader.getSh_entsize(), c++) {
                this.setPosition(offset);

                Symbol symbol = new Symbol(this, sectionHeader);
                symbol.st_name = this.readUnsignedWord();
                symbol.st_value = this.readUnsignedWord();
                symbol.st_size = this.readUnsignedWord();
                symbol.st_info = this.read();
                symbol.st_other = this.read();
                symbol.st_shndx = this.readUnsignedHalfWord();

                this.symbols.put((int) symbol.st_value, symbol);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param address
     * @return
     */
    public Symbol getSymbol(long address) {
        for (Symbol symbol : this.symbols.values()) {
            if (symbol.st_value == address) {
                return symbol;
            }
        }

        return null;
    }

    /**
     *
     */
    public void loadLocalFunctions() {
        for (Symbol symbol : this.symbols.values()) {
            if (symbol.getSt_type() == Symbol.STT_FUNC) {
                int idx = symbol.st_shndx;
                if (idx > Symbol.SHN_LOPROC && idx < Symbol.SHN_HIPROC) {
                    if (symbol.getName().length() > 0) {
                        this.localFunctionSymbols.put((int) symbol.st_value, symbol);
                    }
                } else if (idx >= 0 && this.getSectionHeaders().get(idx).getSh_type() != ElfSectionHeader.SHT_NULL) {
                    this.localFunctionSymbols.put((int) symbol.st_value, symbol);
                }
            }
        }
    }

    /**
     *
     */
    public void loadLocalObjects() {
        for (Symbol symbol : this.symbols.values()) {
            if (symbol.getSt_type() == Symbol.STT_OBJECT) {
                int idx = symbol.st_shndx;
                if (idx > Symbol.SHN_LOPROC && idx < Symbol.SHN_HIPROC) {
                    if (symbol.getName().length() > 0) {
                        this.localObjectSymbols.put((int) symbol.st_value, symbol);
                    }
                } else if (idx >= 0 && this.getSectionHeaders().get(idx).getSh_type() != ElfSectionHeader.SHT_NULL) {
                    this.localObjectSymbols.put((int) symbol.st_value, symbol);
                }
            }
        }
    }

    /**
     *
     */
    public void loadCommonObjects() {
        for (Symbol symbol : this.symbols.values()) {
            if (symbol.getSt_bind() == Symbol.STB_GLOBAL && symbol.getSt_type() == Symbol.STT_OBJECT) {
                if (symbol.st_shndx == Symbol.SHN_COMMON) {
                    this.commonObjectSymbols.put((int) symbol.st_value, symbol);
                }
            }
        }
    }

    /**
     *
     * @param type
     * @return
     */
    public List<ElfSectionHeader> getSectionHeaders(int type) {
        List<ElfSectionHeader> sectionHeaders = new ArrayList<ElfSectionHeader>();

        for (ElfSectionHeader sectionHeader : this.getSectionHeaders()) {
            if (sectionHeader.getSh_type() == type) {
                sectionHeaders.add(sectionHeader);

            }
        }

        return sectionHeaders;
    }

    /**
     *
     * @throws IOException
     */
    public void close()
            throws IOException {
        this.file.close();
    }

    protected void finalize()
            throws Throwable {
        super.finalize();
        this.close();
    }

    /**
     *
     * @return
     */
    public long getPosition() {
        return this.position;
    }

    /**
     *
     * @param position
     */
    public void setPosition(long position) {
        this.position = position;
    }

    /**
     *
     * @return
     */
    public BufferAccessor getBufferAccessor() {
        if (this.littleEndian) {
            return new LittleEndianBufferAccessor();
        } else {
            return new BigEndianBufferAccessor();
        }
    }

    /**
     *
     * @param content
     * @throws IOException
     */
    public void read(byte[] content)
            throws IOException {
        this.file.seek(this.position);
        this.file.readFully(content);

        this.position += content.length;
    }

    /**
     *
     * @param offset
     * @param content
     * @throws IOException
     */
    public void read(long offset, byte[] content)
            throws IOException {
        this.file.seek(offset);
        this.file.readFully(content);
    }

    /**
     *
     * @return
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
     *
     * @param offset
     * @return
     * @throws IOException
     */
    public int read(long offset)
            throws IOException {
        this.file.seek(offset);
        return this.file.read();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public long readUnsignedWord() throws IOException {
        long data = this.readUnsignedWord(this.position);
        this.position += 4;
        return data;
    }

    /**
     *
     * @param offset
     * @return
     * @throws IOException
     */
    public long readUnsignedWord(long offset) throws IOException {
        return this.getBufferAccessor().getU4(this.buffer, offset);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public int readUnsignedHalfWord() throws IOException {
        int data = this.readUnsignedHalfWorld(this.position);

        this.position += 2;

        return data;
    }

    /**
     *
     * @param offset
     * @return
     * @throws IOException
     */
    public int readUnsignedHalfWorld(long offset) throws IOException {
        return this.getBufferAccessor().getU2(this.buffer, offset);
    }

    /**
     *
     * @return
     */
    public ElfIdentification getIdentification() {
        return this.identification;
    }

    /**
     *
     * @return
     */
    public ElfHeader getHeader() {
        return this.header;
    }

    /**
     *
     * @return
     */
    public List<ElfSectionHeader> getSectionHeaders() {
        return this.sectionHeaders;
    }

    /**
     *
     * @return
     */
    public List<ElfProgramHeader> getProgramHeaders() {
        return this.programHeaders;
    }

    /**
     *
     * @return
     */
    public ElfStringTable getStringTable() {
        return this.stringTable;
    }

    /**
     *
     * @return
     */
    public boolean isLittleEndian() {
        return this.littleEndian;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Symbol> getSymbols() {
        return symbols;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Symbol> getLocalFunctionSymbols() {
        return localFunctionSymbols;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Symbol> getLocalObjectSymbols() {
        return localObjectSymbols;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Symbol> getCommonObjectSymbols() {
        return commonObjectSymbols;
    }
}
