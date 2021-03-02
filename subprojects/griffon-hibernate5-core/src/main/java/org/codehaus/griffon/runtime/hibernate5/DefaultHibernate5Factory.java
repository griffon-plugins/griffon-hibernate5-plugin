/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
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
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.core.injection.Injector;
import griffon.plugins.datasource.DataSourceFactory;
import griffon.plugins.datasource.DataSourceStorage;
import griffon.plugins.hibernate5.Hibernate5Bootstrap;
import griffon.plugins.hibernate5.Hibernate5Factory;
import griffon.plugins.hibernate5.events.Hibernate5ConfigurationAvailableEvent;
import griffon.plugins.hibernate5.events.Hibernate5ConnectEndEvent;
import griffon.plugins.hibernate5.events.Hibernate5ConnectStartEvent;
import griffon.plugins.hibernate5.events.Hibernate5DisconnectEndEvent;
import griffon.plugins.hibernate5.events.Hibernate5DisconnectStartEvent;
import griffon.plugins.monitor.MBeanManager;
import griffon.util.CollectionUtils;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;
import org.codehaus.griffon.runtime.datasource.DefaultDataSourceFactory;
import org.codehaus.griffon.runtime.hibernate5.internal.HibernateConfigurationHelper;
import org.codehaus.griffon.runtime.hibernate5.monitor.SessionFactoryMonitor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static griffon.util.ConfigUtils.getConfigValue;
import static griffon.util.ConfigUtils.getConfigValueAsBoolean;
import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultHibernate5Factory extends AbstractObjectFactory<SessionFactory> implements Hibernate5Factory {
    private static final String ERROR_SESSION_FACTORY_NAME_BLANK = "Argument 'sessionFactoryName' must not be blank";
    private final Set<String> sessionFactoryNames = new LinkedHashSet<>();

    @Inject
    private DataSourceFactory dataSourceFactory;

    @Inject
    private DataSourceStorage dataSourceStorage;

    @Inject
    private Injector injector;

    @Inject
    private MBeanManager mBeanManager;

    @Inject
    private Metadata metadata;

    @Inject
    public DefaultHibernate5Factory(@Nonnull @Named("hibernate5") griffon.core.Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
        sessionFactoryNames.add(KEY_DEFAULT);

        if (configuration.containsKey(getPluralKey())) {
            Map<String, Object> sessionFactories = configuration.get(getPluralKey());
            sessionFactoryNames.addAll(sessionFactories.keySet());
        }
    }

    @Nonnull
    @Override
    public Set<String> getSessionFactoryNames() {
        return sessionFactoryNames;
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String sessionFactoryName) {
        requireNonBlank(sessionFactoryName, ERROR_SESSION_FACTORY_NAME_BLANK);
        return narrowConfig(sessionFactoryName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "sessionFactory";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "sessionFactories";
    }

    @Nonnull
    @Override
    public SessionFactory create(@Nonnull String name) {
        Map<String, Object> config = narrowConfig(name);
        event(Hibernate5ConnectStartEvent.of(name, config));

        Configuration configuration = createConfiguration(config, name);
        createSchema(name, config, configuration);

        SessionFactory sessionFactory = new RecordingSessionFactory(configuration.buildSessionFactory());

        if (getConfigValueAsBoolean(config, "jmx", true)) {
            sessionFactory = new JMXAwareSessionFactory(sessionFactory);
            registerMBeans(name, (JMXAwareSessionFactory) sessionFactory);
        }

        Session session = null;
        try {
            session = openSession(name, sessionFactory);
            for (Object o : injector.getInstances(Hibernate5Bootstrap.class)) {
                ((Hibernate5Bootstrap) o).init(name, session);
            }
        } finally {
            if (session != null) {

                session.close();
            }
        }

        event(Hibernate5ConnectEndEvent.of(name, config, sessionFactory));
        return sessionFactory;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull SessionFactory instance) {
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = narrowConfig(name);
        event(Hibernate5DisconnectStartEvent.of(name, config, instance));

        Session session = null;
        try {
            session = openSession(name, instance);
            for (Object o : injector.getInstances(Hibernate5Bootstrap.class)) {
                ((Hibernate5Bootstrap) o).destroy(name, session);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        closeDataSource(name);

        if (getConfigValueAsBoolean(config, "jmx", true)) {
            unregisterMBeans((JMXAwareSessionFactory) instance);
        }

        event(Hibernate5DisconnectEndEvent.of(name, config));
    }

    private void registerMBeans(@Nonnull String name, @Nonnull JMXAwareSessionFactory sessionFactory) {
        RecordingSessionFactory recordingSessionFactory = (RecordingSessionFactory) sessionFactory.getDelegate();
        SessionFactoryMonitor sessionFactoryMonitor = new SessionFactoryMonitor(metadata, recordingSessionFactory, name);
        sessionFactory.addObjectName(mBeanManager.registerMBean(sessionFactoryMonitor, false).getCanonicalName());
    }

    private void unregisterMBeans(@Nonnull JMXAwareSessionFactory sessionFactory) {
        for (String objectName : sessionFactory.getObjectNames()) {
            mBeanManager.unregisterMBean(objectName);
        }
        sessionFactory.clearObjectNames();
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    protected Configuration createConfiguration(@Nonnull Map<String, Object> config, @Nonnull String dataSourceName) {
        DataSource dataSource = getDataSource(dataSourceName);
        griffon.core.Configuration dataSourcesConfiguration = ((DefaultDataSourceFactory) dataSourceFactory).getConfiguration();
        Map<String, Object> configurationMap = null;
        if (dataSourcesConfiguration.containsKey("dataSources"))
            configurationMap = dataSourcesConfiguration.get("dataSources");
        if (configurationMap == null)
            configurationMap = new HashMap<>();
        configurationMap.put("default", dataSourcesConfiguration.get("dataSource"));

        HibernateConfigurationHelper configHelper = new HibernateConfigurationHelper(getApplication(), config, dataSourceName, dataSource, configurationMap);
        Configuration configuration = configHelper.buildConfiguration();
        getApplication().getEventRouter().publishEvent(Hibernate5ConfigurationAvailableEvent.of(CollectionUtils.<String, Object>map()
            .e("configuration", configuration)
            .e("dataSourceName", dataSourceName)
            .e("sessionConfiguration", config)));
        return configuration;
    }

    protected void createSchema(@Nonnull String dataSourceName, @Nonnull Map<String, Object> config, @Nonnull Configuration configuration) {
        configuration.setProperty("hibernate.hbm2ddl.auto", getConfigValue(config, "schema", "create-drop"));
    }

    protected void closeDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource != null) {
            dataSourceFactory.destroy(dataSourceName, dataSource);
            dataSourceStorage.remove(dataSourceName);
        }
    }

    @Nonnull
    protected DataSource getDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource == null) {
            dataSource = dataSourceFactory.create(dataSourceName);
            dataSourceStorage.set(dataSourceName, dataSource);
        }
        return dataSource;
    }

    @Nonnull
    protected Session openSession(@Nonnull String sessionFactoryName, @Nonnull SessionFactory sessionFactory) {
        return sessionFactory.openSession();
    }
}
