package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;

import java.util.List;

public interface SelectionAlgorithm {
    void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel);

    Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel);

    Direction select(int src, int dest, int ivc, List<Direction> directions);
}
