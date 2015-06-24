package archimulator.uncore.net.basic;

import java.util.ArrayList;
import java.util.List;

public class Port {
    private List<VirtualChannel> virtualChannels;

    public Port() {
        this.virtualChannels = new ArrayList<>();
    }
}
