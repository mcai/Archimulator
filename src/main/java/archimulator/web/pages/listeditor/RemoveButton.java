/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.pages.listeditor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;

public class RemoveButton extends EditorButton {
    private WebMarkupContainer container;

    public RemoveButton(String id, Form<?> form, WebMarkupContainer container) {
        super(id, form);
        this.container = container;
    }

    @Override
    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        int idx = getItem().getIndex();

        for (int i = idx + 1; i < getItem().getParent().size(); i++) {
            ListItem<?> item = (ListItem<?>) getItem().getParent().get(i);
            item.setIndex(item.getIndex() - 1);
        }

        getList().remove(idx);
        getEditor().remove(getItem());

        if(target != null) {
            target.add(container);
        }
    }

    @Override
    public boolean isEnabled() {
        return getEditor().checkRemove(getItem());
    }

    public WebMarkupContainer getContainer() {
        return container;
    }
}
