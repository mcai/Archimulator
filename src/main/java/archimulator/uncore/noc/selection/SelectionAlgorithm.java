package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;

import java.util.List;

/**
 * Selection algorithm.
 *
 * @author MIn Cai
 */
public interface SelectionAlgorithm {
    /**
     * Handle the event when a packet has arrived at its destination node.
     *
     * @param packet the packet
     * @param inputVirtualChannel the input virtual channel
     */
    void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel);

    /**
     * Do route calculation for a packet.
     *
     * @param packet the packet
     * @param inputVirtualChannel input virtual channel
     * @return the newly calculated output direction for routing the packet
     */
    Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel);

    /**
     * Select the best output direction from a list of candidate output directions.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param ivc the input virtual channel ID
     * @param directions the list of candidate output directions
     * @return the best output direction selected from a list of candidate output directions
     */
    Direction select(int src, int dest, int ivc, List<Direction> directions);
}
