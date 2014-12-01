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

import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List editor.
 *
 * @param <T> the item type
 * @author Min Cai
 */
public abstract class ListEditor<T> extends RepeatingView implements IFormModelUpdateListener {
    List<T> items;

    /**
     * Create a list editor.
     *
     * @param id the markup ID of the list editor
     */
    public ListEditor(String id) {
        super(id);
    }

    /**
     * Create a list editor.
     *
     * @param id    the markup ID of the list editor that is to be created
     * @param model the model
     */
    public ListEditor(String id, IModel<List<T>> model) {
        super(id, model);
    }

    /**
     * Populate the list editor with the specified list item.
     *
     * @param item the list item
     */
    protected abstract void populateItem(ListItem<T> item);

    /**
     * Add the specified item.
     *
     * @param value the value of the item
     */
    public void addItem(T value) {
        items.add(value);
        ListItem<T> item = new ListItem<>(newChildId(), items.size() - 1);
        add(item);
        populateItem(item);
    }

    @Override
    protected void onBeforeRender() {
        if (!hasBeenRendered()) {
            items = new ArrayList<>(getModelObject());
            for (int i = 0; i < items.size(); i++) {
                ListItem<T> li = new ListItem<>(newChildId(), i);
                add(li);
                populateItem(li);
            }
        }
        super.onBeforeRender();
    }

    public void updateModel() {
        setModelObject(items);
    }

    /**
     * Get a value indicating whether the specified item can be removed or not.
     *
     * @param items the list of items
     * @param item  the item that is to be removed
     * @return a value indicating whether the specified item can be removed or not
     */
    public boolean canRemove(List<T> items, T item) {
        return true;
    }

    @SuppressWarnings("unchecked")
    final boolean checkRemove(ListItem<?> item) {
        List<T> list = Collections.unmodifiableList(items);
        ListItem<T> li = (ListItem<T>) item;
        return canRemove(list, li.getModelObject());
    }

    /**
     * Get the model.
     *
     * @return the model
     */
    @SuppressWarnings("unchecked")
    public final IModel<List<T>> getModel() {
        return (IModel<List<T>>) getDefaultModel();
    }

    /**
     * Set the model.
     *
     * @param model the model
     */
    public final void setModel(IModel<List<T>> model) {
        setDefaultModel(model);
    }

    /**
     * Get the model object.
     *
     * @return the model object
     */
    @SuppressWarnings("unchecked")
    public final List<T> getModelObject() {
        return (List<T>) getDefaultModelObject();
    }

    /**
     * Set the model object.
     *
     * @param object the model object
     */
    public final void setModelObject(List<T> object) {
        setDefaultModelObject(object);
    }
}
