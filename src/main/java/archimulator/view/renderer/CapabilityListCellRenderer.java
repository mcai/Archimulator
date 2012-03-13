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
package archimulator.view.renderer;

import archimulator.util.Pair;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

public class CapabilityListCellRenderer implements ListitemRenderer<Pair<Class<?>, Boolean>> {
    @Override
    public void render(Listitem item, final Pair<Class<?>, Boolean> data, int index) throws Exception {
        Listcell listCell = new Listcell();
        final Checkbox checkBox = new Checkbox(data.getFirst().getName());
        checkBox.setChecked(data.getSecond());
        checkBox.addEventListener("onCheck", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                data.setSecond(checkBox.isChecked());
            }
        });
        listCell.appendChild(checkBox);
        item.appendChild(listCell);
    }
}
