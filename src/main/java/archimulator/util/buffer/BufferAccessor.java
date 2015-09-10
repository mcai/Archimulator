/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.buffer;

import java.io.IOException;

/**
 *
 * @author Min Cai
 */
public abstract class BufferAccessor {
    /**
     *
     * @param buffer
     * @param position
     * @return
     * @throws IOException
     */
    public abstract int getU2(Buffer buffer, long position) throws IOException;

    /**
     *
     * @param buffer
     * @param position
     * @return
     * @throws IOException
     */
    public abstract long getU4(Buffer buffer, long position) throws IOException;
}
