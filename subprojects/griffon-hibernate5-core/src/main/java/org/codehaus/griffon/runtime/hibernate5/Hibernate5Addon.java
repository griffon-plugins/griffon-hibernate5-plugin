/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.hibernate5;

import griffon.annotations.core.Nonnull;
import griffon.annotations.inject.DependsOn;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.core.events.StartupStartEvent;
import griffon.plugins.hibernate5.Hibernate5Callback;
import griffon.plugins.hibernate5.Hibernate5Factory;
import griffon.plugins.hibernate5.Hibernate5Handler;
import griffon.plugins.hibernate5.Hibernate5Storage;
import griffon.plugins.monitor.MBeanManager;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.hibernate5.monitor.Hibernate5StorageMonitor;
import org.hibernate.Session;

import javax.application.event.EventHandler;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@DependsOn("datasource")
@Named("hibernate5")
public class Hibernate5Addon extends AbstractGriffonAddon {
    @Inject
    private Hibernate5Handler hibernate5Handler;

    @Inject
    private Hibernate5Factory hibernate5Factory;

    @Inject
    private Hibernate5Storage hibernate5Storage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new Hibernate5StorageMonitor(metadata, hibernate5Storage));
    }

    @EventHandler
    public void handleStartupStartEvent(@Nonnull StartupStartEvent event) {
        for (String sessionFactoryName : hibernate5Factory.getSessionFactoryNames()) {
            Map<String, Object> config = hibernate5Factory.getConfigurationFor(sessionFactoryName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                hibernate5Handler.withHbm5Session(sessionFactoryName, new Hibernate5Callback<Void>() {
                    @Override
                    public Void handle(@Nonnull String sessionFactoryName, @Nonnull Session session) {
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void onShutdown(@Nonnull GriffonApplication application) {
        for (String sessionFactoryName : hibernate5Factory.getSessionFactoryNames()) {
            hibernate5Handler.closeHbm5Session(sessionFactoryName);
        }
    }
}
