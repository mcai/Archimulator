package archimulator.web.pages.wicketinaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Person {
    private String name;
    private List<Phone> phones = new ArrayList<Phone>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[Person name=").append(getName());
        str.append(" phones=[");
        Iterator<Phone> it = getPhones().iterator();
        while (it.hasNext()) {
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        str.append("]]");
        return str.toString();
    }
}
