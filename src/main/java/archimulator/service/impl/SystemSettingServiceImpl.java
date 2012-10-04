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
package archimulator.service.impl;

import archimulator.model.SystemSetting;
import archimulator.service.ServiceManager;
import archimulator.service.SystemSettingService;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.Arrays;

public class SystemSettingServiceImpl extends AbstractService implements SystemSettingService {
    private Dao<SystemSetting, Long> systemSettings;

    @SuppressWarnings("unchecked")
    public SystemSettingServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends ModelElement>>asList(SystemSetting.class));

        this.systemSettings = createDao(SystemSetting.class);
    }

    @Override
    public SystemSetting getSystemSettingSingleton() {
        if(getFirstItem(this.systemSettings) == null) {
            SystemSetting systemSetting = new SystemSetting("");
            systemSetting.setRunningExperimentsEnabled(true);

            addItem(this.systemSettings, SystemSetting.class, systemSetting);
        }

        return getFirstItem(this.systemSettings);
    }

    @Override
    public void updateSystemSettingSingleton(SystemSetting systemSetting) {
        updateItem(this.systemSettings, SystemSetting.class, systemSetting);
    }
}
