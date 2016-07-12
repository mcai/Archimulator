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

    private Iterator<RequesterT> requestersIterator;

    public RoundRobinArbiter(ResourceT resource) {
        this.resource = resource;
    }

    protected abstract List<RequesterT> getRequesters();

    protected abstract boolean resourceAvailable(ResourceT resource);

    protected abstract boolean requesterHasRequests(RequesterT requester);

    public RequesterT next() {
        if(!this.resourceAvailable(resource)) {
            return null;
        }

        for(int i = 0; i < this.getRequesters().size(); i++) {
            RequesterT requester = this.getRequestersIterator().next();
            if(this.requesterHasRequests(requester)) {
                return requester;
            }
        }

        return null;
    }

    public ResourceT getResource() {
        return resource;
    }

    private Iterator<RequesterT> getRequestersIterator() {
        if(requestersIterator == null) {
            this.requestersIterator = Iterators.cycle(this.getRequesters());
        }
        return requestersIterator;
    }
}
