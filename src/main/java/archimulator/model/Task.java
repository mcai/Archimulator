/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Experiment pack.
 *
 * @author Min Cai
 */
@Root
public class Task {
    @Attribute
    private String title;

    @Attribute
    private boolean active;

    @Attribute
    private boolean reset;

    @ElementList
    private ArrayList<String> tags;

    @ElementList
    private ArrayList<String> experimentPackTitles;

    /**
     * Get the title
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get a value indicating whether it is active or not.
     *
     * @return a value indicating whether it is active or not
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set a value indicating whether it is active or not.
     *
     * @param active a value indicating whether it is active or not
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get a value indicating whether a reset should be performed before run.
     *
     * @return a value indicating whether a reset should be performed before run
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * Set a value indicating whether a reset should be performed before run.
     *
     * @param reset a value indicating whether a reset should be performed before run
     */
    public void setReset(boolean reset) {
        this.reset = reset;
    }

    /**
     * Get the list of tags to run.
     *
     * @return the list of tags to run
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    /**
     * Set the list of tags to run.
     *
     * @param tags the list of tags to run
     */
    public void setTags(List<String> tags) {
        this.tags = new ArrayList<String>(tags);
    }

    /**
     * Get the list of experiment pack titles to run.
     *
     * @return the list of experiment pack titles to run
     */
    public List<String> getExperimentPackTitles() {
        return experimentPackTitles;
    }

    /**
     * Set the list of experiment pack titles to run.
     *
     * @param experimentPackTitles the list of experiment pack titles to run
     */
    public void setExperimentPackTitles(List<String> experimentPackTitles) {
        this.experimentPackTitles = new ArrayList<String>(experimentPackTitles);
    }
}
