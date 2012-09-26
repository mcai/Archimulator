package archimulator.web.pages.wicketinaction;

public class Phone {
    public int threadId;
    public String areacode;
    public String phone;
    public String ext;

    @Override
    public String toString() {
        return String.format("[Phone threadId=%d areacode=%s phone=%s ext=%s]", threadId, areacode, phone, ext);
    }
}
