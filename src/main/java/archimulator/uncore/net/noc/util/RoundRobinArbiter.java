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

    /**
     * Create a round robin arbiter.
     *
     * @param resource the resource
     */
    public RoundRobinArbiter(ResourceT resource) {
        this.resource = resource;
    }

    /**
     * Get the list of requesters.
     *
     * @return the list of requesters
     */
    protected abstract List<RequesterT> getRequesters();

    /**
     * Get a boolean value indicating whether the resource is available or not.
     *
     * @param resource the resource
     * @return a boolean value indicating whether the resource is available or not
     */
    protected abstract boolean resourceAvailable(ResourceT resource);

    /**
     * Get a boolean value indicating whether the requester has requests or not.
     *
     * @param requester the requester
     * @return a boolean value indicating whether the requester has requests or not
     */
    protected abstract boolean requesterHasRequests(RequesterT requester);

    /**
     * Get the next requester.
     *
     * @return the next requester
     */
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

    /**
     * Get the resource.
     *
     * @return the resource
     */
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
