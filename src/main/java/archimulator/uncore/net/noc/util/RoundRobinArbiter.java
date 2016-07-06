package archimulator.uncore.net.noc.util;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.List;

/**
 * Round robin arbiter.
 *
 * @author Min Cai
 */
public abstract class RoundRobinArbiter<ResourceT, RequesterT> {
    private ResourceT resource;
    private List<RequesterT> requesters;
    private Iterator<RequesterT> requestersIter;

    public RoundRobinArbiter(ResourceT resource, List<RequesterT> requesters) {
        this.resource = resource;
        this.requesters = requesters;
        this.requestersIter = Iterators.cycle(this.requesters);
    }

    protected abstract boolean resourceAvailable(ResourceT resource);

    protected abstract boolean requesterHasRequests(RequesterT requester);

    public RequesterT next() {
        if(!this.resourceAvailable(resource)) {
            return null;
        }

        for(int count = 0; count < requesters.size(); count++) {
            RequesterT requester = this.requestersIter.next();
            if(this.requesterHasRequests(requester)) {
                return requester;
            }
        }

        return null;
    }

    public ResourceT getResource() {
        return resource;
    }

    public List<RequesterT> getRequesters() {
        return requesters;
    }
}
