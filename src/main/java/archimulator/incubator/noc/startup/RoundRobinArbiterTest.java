package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.util.RoundRobinArbiter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Round robin arbiter test.
 */
public class RoundRobinArbiterTest {
    private class Resource {
    }

    private class Requester {
        private String name;

        Requester(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() {
        Resource resource = new Resource();
        List<Requester> requesters = new ArrayList<Requester>() {{
            add(new Requester("A"));
            add(new Requester("B"));
            add(new Requester("C"));
            add(new Requester("D"));
        }};

        RoundRobinArbiter<Resource, Requester> arbiter = new RoundRobinArbiter<Resource, Requester>(resource, requesters) {
            @Override
            protected boolean resourceAvailable(Resource resource) {
                return true;
            }

            @Override
            protected boolean requesterHasRequests(Requester requester) {
                return !requester.name.equals("B");
            }
        };

        for(int i = 0; i < 20; i++) {
            Requester next = arbiter.next();
            System.out.println(next != null ? next.name : null);
        }
    }
}
