/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.common.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Report node.
 *
 * @author Min Cai
 */
public class ReportNode implements Serializable {
    private ReportNode parent;
    private String key;
    private String value;
    private List<ReportNode> children;

    /**
     * Create a report node.
     *
     * @param parent the parent node
     * @param key the key
     */
    public ReportNode(ReportNode parent, String key) {
        this(parent, key, null);
    }

    /**
     * Create a report node.
     *
     * @param parent the parent node
     * @param key the key
     * @param value the value
     */
    public ReportNode(ReportNode parent, String key, String value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.children = new ArrayList<>();
    }

    /**
     * Get the parent node.
     *
     * @return the parent node
     */
    public ReportNode getParent() {
        return parent;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the child nodes.
     *
     * @return the child nodes
     */
    public List<ReportNode> getChildren() {
        return children;
    }

    /**
     * Get a value indicating whether the node is leaf or not.
     *
     * @return a value indicating whether the node is leaf or not
     */
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    /**
     * Get the path of the node.
     *
     * @return the path of the node
     */
    public String getPath() {
        return (getParent() != null && !getParent().getKey().isEmpty() ? getParent().getPath() + "/" : "") + getKey();
    }

    /**
     * Print recursively the node and its child nodes.
     */
    public void print() {
        traverse(System.out::println);
    }

    /**
     * Traverse the node and its child nodes using the specified callback action.
     */
    public void traverse(Consumer<ReportNode> callback) {
        if (this.value != null) {
            callback.accept(this);
        }

        for (ReportNode child : getChildren()) {
            child.traverse(callback);
        }
    }

    @Override
    public String toString() {
        return String.format("%s=%s", getPath(), value);
    }
}
