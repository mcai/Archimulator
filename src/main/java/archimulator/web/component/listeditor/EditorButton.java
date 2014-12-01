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

import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;

import java.util.List;

/**
 * Editor button.
 *
 * @author Min Cai
 */
public abstract class EditorButton extends AjaxFallbackButton {
    /**
     * The form.
     */
    protected Form<?> form;

    private transient ListItem<?> parent;

    /**
     * Create an editor button.
     *
     * @param id   the markup ID of the button that is to be created
     * @param form the form
     */
    public EditorButton(String id, Form<?> form) {
        super(id, form);
        this.form = form;
    }

    /**
     * Get the list item.
     *
     * @return the list item
     */
    protected final ListItem<?> getItem() {
        if (parent == null) {
            parent = findParent(ListItem.class);
        }
        return parent;
    }

    /**
     * Get the list.
     *
     * @return the list
     */
    protected final List<?> getList() {
        return getEditor().items;
    }

    /**
     * Get the list editor.
     *
     * @return the list editor
     */
    protected final ListEditor<?> getEditor() {
        return (ListEditor<?>) getItem().getParent();
    }

    @Override
    protected void onDetach() {
        parent = null;
        super.onDetach();
    }
}