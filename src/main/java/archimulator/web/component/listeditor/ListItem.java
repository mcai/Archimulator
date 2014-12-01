/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.component.listeditor;

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * List item.
 *
 * @param <T> the item type
 * @author Min Cai
 */
public class ListItem<T> extends Item<T> {
    /**
     * Create a list item.
     *
     * @param id    the markup ID of the list item that is to be created
     * @param index the index
     */
    public ListItem(String id, int index) {
        super(id, index);
        setModel(new ListItemModel());
    }

    private class ListItemModel extends AbstractReadOnlyModel<T> {
        @SuppressWarnings("unchecked")
        @Override
        public T getObject() {
            return ((ListEditor<T>) ListItem.this.getParent()).items.get(getIndex());
        }
    }
}
