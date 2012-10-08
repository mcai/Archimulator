package archimulator.sim.os.elf;

/**
 *
 * @author Min Cai
 */
public class Symbol {
    /**
     *
     */
    public long st_name;
    /**
     *
     */
    public long st_value;
    /**
     *
     */
    public long st_size;
    /**
     *
     */
    public int st_info;
    /**
     *
     */
    public int st_other;
    /**
     *
     */
    public int st_shndx;

    private ElfFile elfFile;
    private ElfSectionHeader symbolSectionHeader;

    private String name;

    private boolean inline;

    /**
     *
     * @param elfFile
     * @param symbolSectionHeader
     */
    public Symbol(ElfFile elfFile, ElfSectionHeader symbolSectionHeader) {
        this.elfFile = elfFile;
        this.symbolSectionHeader = symbolSectionHeader;
    }

    /**
     *
     * @return
     */
    public String getName() {
        if (this.name == null) {
            ElfSectionHeader sectionHeader = this.elfFile.getSectionHeaders().get((int) this.symbolSectionHeader.getSh_link());
            this.name = new ElfStringTable(this.elfFile, sectionHeader).getString((int) this.st_name);
        }

        return this.name;
    }

    /**
     *
     * @return
     */
    public int getSt_type() {
        return this.st_info & 0xf;
    }

    /**
     *
     * @return
     */
    public int getSt_bind() {
        return (this.st_info >> 4) & 0xf;
    }

    /**
     *
     * @return
     */
    public long getSt_name() {
        return this.st_name;
    }

    /**
     *
     * @return
     */
    public long getSt_value() {
        return this.st_value;
    }

    /**
     *
     * @return
     */
    public long getSt_size() {
        return this.st_size;
    }

    /**
     *
     * @return
     */
    public int getSt_info() {
        return this.st_info;
    }

    /**
     *
     * @return
     */
    public int getSt_other() {
        return this.st_other;
    }

    /**
     *
     * @return
     */
    public int getSt_shndx() {
        return this.st_shndx;
    }

    /**
     *
     * @return
     */
    public boolean isInline() {
        return inline;
    }

    /**
     *
     * @param inline
     */
    public void setInline(boolean inline) {
        this.inline = inline;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s\', st_type=0x%08x, st_bind=0x%08x, st_name=0x%08x, st_value=0x%08x, st_size=%d, st_info=%d, st_other=%d, st_shndx=%d}", this.getName(), this.getSt_type(), this.getSt_bind(), this.st_name, this.st_value, this.st_size, this.st_info, this.st_other, this.st_shndx);
    }

    /**
     *
     */
    public final static int STB_LOCAL = 0;
    /**
     *
     */
    public final static int STB_GLOBAL = 1;
    /**
     *
     */
    public final static int STB_WEAK = 2;

    /**
     *
     */
    public final static int STT_NOTYPE = 0;
    /**
     *
     */
    public final static int STT_OBJECT = 1;
    /**
     *
     */
    public final static int STT_FUNC = 2;
    /**
     *
     */
    public final static int STT_SECTION = 3;
    /**
     *
     */
    public final static int STT_FILE = 4;

    /**
     *
     */
    public final static int SHN_UNDEF = 0;
    /**
     *
     */
    public final static int SHN_LORESERVE = 0xffffff00;
    /**
     *
     */
    public final static int SHN_LOPROC = 0xffffff00;
    /**
     *
     */
    public final static int SHN_HIPROC = 0xffffff1f;
    /**
     *
     */
    public final static int SHN_LOOS = 0xffffff20;
    /**
     *
     */
    public final static int SHN_HIOS = 0xffffff3f;
    /**
     *
     */
    public final static int SHN_ABS = 0xfffffff1;
    /**
     *
     */
    public final static int SHN_COMMON = 0xfffffff2;
    /**
     *
     */
    public final static int SHN_XINDEX = 0xffffffff;
    /**
     *
     */
    public final static int SHN_HIRESERVE = 0xffffffff;
}
