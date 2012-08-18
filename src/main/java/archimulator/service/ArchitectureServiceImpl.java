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
package archimulator.service;

import archimulator.model.Architecture;
import net.pickapack.model.ModelElement;
import com.j256.ormlite.dao.Dao;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

public class ArchitectureServiceImpl extends AbstractService implements ArchitectureService {
    private Dao<Architecture, Long> architectures;

    @SuppressWarnings("unchecked")
    public ArchitectureServiceImpl() {
        super(ServiceManager.DATABASE_DIRECTORY, ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Architecture.class));

        this.architectures = createDao(Architecture.class);
    }
    @Override
    public List<Architecture> getAllArchitectures() {
        return this.getAllItems(this.architectures);
    }

    @Override
    public Architecture getArchitectureById(long id) {
        return this.getItemById(this.architectures, id);
    }

    @Override
    public Architecture getArchitectureByTitle(String title) {
        return this.getFirstItemByTitle(this.architectures, title);
    }

    @Override
    public Architecture getFirstArchitecture() {
        return this.getFirstItem(this.architectures);
    }

    @Override
    public long addArchitecture(Architecture architecture) {
        return this.addItem(this.architectures, Architecture.class, architecture);
    }

    @Override
    public void removeArchitectureById(long id) {
        this.removeItemById(this.architectures, Architecture.class, id);
    }

    @Override
    public void clearArchitectures() {
        this.clearItems(this.architectures, Architecture.class);
    }

    @Override
    public void updateArchitecture(Architecture architecture) {
        this.updateItem(this.architectures, Architecture.class, architecture);
    }
}
