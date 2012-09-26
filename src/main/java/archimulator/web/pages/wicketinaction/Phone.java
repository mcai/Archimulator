package archimulator.web.pages.wicketinaction;

public class Phone {
    private int threadId;
    private String areaCode;
    private String phone;
    private String ext;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return String.format("[Phone threadId=%d areaCode=%s phone=%s ext=%s]", getThreadId(), getAreaCode(), getPhone(), getExt());
    }
}
