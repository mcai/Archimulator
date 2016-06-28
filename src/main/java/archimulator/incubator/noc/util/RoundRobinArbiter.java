package archimulator.incubator.noc.util;

import java.util.List;

/**
 * Round robin arbiter.
 *
 * @author Min Cai
 */
public abstract class RoundRobinArbiter<ResourceT, RequesterT> {
    private ResourceT resource;
    private List<RequesterT> requesters;

    public RoundRobinArbiter(ResourceT resource, List<RequesterT> requesters) {
        this.resource = resource;
        this.requesters = requesters;
    }

    protected abstract boolean resourceAvailable(ResourceT resource);

    protected abstract boolean requesterHasRequests(RequesterT requester);

    private int lastServicedRequesterIndex = -1;

    public RequesterT next() {
        for (int i = lastServicedRequesterIndex + 1, count = 0;  count < requesters.size(); i++, count++) {
            if(!this.resourceAvailable(resource)) {
                return null;
            }

            RequesterT requester = requesters.get(i % requesters.size());

            if(this.requesterHasRequests(requester)) {
                lastServicedRequesterIndex = i;
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
