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
package archimulator.os.event;

import archimulator.os.Context;

/**
 * Resume event.
 *
 * @author Min Cai
 */
public class ResumeEvent extends SystemEvent {
    private TimeCriterion timeCriterion;

    /**
     * Create a resume event for the specified context.
     *
     * @param context the context
     */
    public ResumeEvent(Context context) {
        super(context, SystemEventType.RESUME);

        this.timeCriterion = new TimeCriterion();
    }

    @Override
    public boolean needProcess() {
        return this.timeCriterion.needProcess(this.getContext());
    }

    @Override
    public void process() {
        this.getContext().resume();
    }

    /**
     * Get the time criterion.
     *
     * @return the time criterion
     */
    public TimeCriterion getTimeCriterion() {
        return timeCriterion;
    }
}
