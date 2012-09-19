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
package archimulator.web.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.markup.html.panel.Panel;

public class PagingNavigator extends Panel {
    private PagingNavigation pagingNavigation;
    private final IPageable pageable;
    private final IPagingLabelProvider labelProvider;

    public PagingNavigator(final String id, final IPageable pageable) {
        this(id, pageable, null);
    }

    public PagingNavigator(final String id, final IPageable pageable,
                           final IPagingLabelProvider labelProvider) {
        super(id);
        this.pageable = pageable;
        this.labelProvider = labelProvider;
    }

    public final IPageable getPageable() {
        return pageable;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        pagingNavigation = newNavigation("navigation", pageable, labelProvider);
        add(pagingNavigation);

        add(newPagingNavigationLink("first", pageable, 0).add(
                new TitleAppender("PagingNavigator.first")));
        add(newPagingNavigationIncrementLink("prev", pageable, -1).add(
                new TitleAppender("PagingNavigator.previous")));
        add(newPagingNavigationIncrementLink("next", pageable, 1).add(
                new TitleAppender("PagingNavigator.next")));
        add(newPagingNavigationLink("last", pageable, -1).add(
                new TitleAppender("PagingNavigator.last")));
    }

    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable,
                                                            int increment) {
        return new PagingNavigationIncrementLink<Void>(id, pageable, increment);
    }

    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        return new PagingNavigationLink<Void>(id, pageable, pageNumber);
    }

    protected PagingNavigation newNavigation(final String id, final IPageable pageable,
                                             final IPagingLabelProvider labelProvider) {
        return new PagingNavigation(id, pageable, labelProvider);
    }

    public final PagingNavigation getPagingNavigation() {
        return pagingNavigation;
    }

    private final class TitleAppender extends Behavior {
        private static final long serialVersionUID = 1L;

        private final String resourceKey;

        public TitleAppender(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            tag.put("title", PagingNavigator.this.getString(resourceKey));
        }
    }
}