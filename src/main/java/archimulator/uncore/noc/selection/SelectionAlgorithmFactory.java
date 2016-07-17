package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Node;

/**
 * Node factory.
 *
 * @author Min Cai
 */
public interface SelectionAlgorithmFactory {
    SelectionAlgorithm create(Node node);
}
