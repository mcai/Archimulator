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
package archimulator.util;

/**
 * Value provider factory.
 *
 * @author Min Cai
 * @param <T> the type of the value
 * @param <ValueProviderT> the type of the value provider
 */
public interface ValueProviderFactory<T, ValueProviderT extends ValueProvider<T>> {
    /**
     * Create a value provider.
     *
     * @param args the array of arguments
     * @return the newly created value provider
     */
    ValueProviderT createValueProvider(Object... args);
}
