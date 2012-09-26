package archimulator.web.pages.wicketinaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Person {
    public String name;
    public List<Phone> phones = new ArrayList<Phone>();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[Person name=").append(name);
        str.append(" phones=[");
        Iterator<Phone> it = phones.iterator();
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
