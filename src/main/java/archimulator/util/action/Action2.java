/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the PickaPack library.
 *
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util.action;

import java.io.Serializable;

/**
 * Action.
 *
 * @author Min Cai
 * @param <T1> the type of the first parameter
 * @param <T2> the type of the second parameter
 */
public interface Action2<T1, T2> extends Serializable {
    /**
     * Apply.
     *
     * @param param1 the first parameter
     * @param param2 the second parameter
     */
    void apply(T1 param1, T2 param2);
}
