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
package griffon.plugins.hibernate5

import griffon.annotations.inject.BindTo
import griffon.core.GriffonApplication
import griffon.plugins.datasource.events.DataSourceConnectEndEvent
import griffon.plugins.datasource.events.DataSourceConnectStartEvent
import griffon.plugins.datasource.events.DataSourceDisconnectEndEvent
import griffon.plugins.datasource.events.DataSourceDisconnectStartEvent
import griffon.plugins.hibernate5.events.Hibernate5ConfigurationAvailableEvent
import griffon.plugins.hibernate5.events.Hibernate5ConnectEndEvent
import griffon.plugins.hibernate5.events.Hibernate5ConnectStartEvent
import griffon.plugins.hibernate5.events.Hibernate5DisconnectEndEvent
import griffon.plugins.hibernate5.events.Hibernate5DisconnectStartEvent
import griffon.test.core.GriffonUnitRule
import org.codehaus.griffon.runtime.util.ResourceBundleProvider
import org.hibernate.Session
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.application.event.EventHandler
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Unroll
class Hibernate5SingleDataSource extends Specification {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace")
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private Hibernate5Handler hibernate5Handler

    @Inject
    private GriffonApplication application

    @BindTo(ResourceBundle)
    @Named("datasource")
    @Singleton
    private Provider<ResourceBundle> dataSourceResourceBundleProvider = new ResourceBundleProvider("SingleDataSource")

    @BindTo(ResourceBundle)
    @Named("hibernate5")
    @Singleton
    private Provider<ResourceBundle> hibernateResourceBundleProvider = new ResourceBundleProvider("SingleDataSource")

    void 'Open and close default hibernate5'() {
        given:
        List eventNames = [
            'Hibernate5ConnectStartEvent', 'DataSourceConnectStartEvent',
            'DataSourceConnectEndEvent', 'Hibernate5ConfigurationAvailableEvent', 'Hibernate5ConnectEndEvent',
            'Hibernate5DisconnectStartEvent', 'DataSourceDisconnectStartEvent',
            'DataSourceDisconnectEndEvent', 'Hibernate5DisconnectEndEvent'
        ]
        TestEventHandler testEventHandler = new TestEventHandler()
        application.eventRouter.subscribe(testEventHandler)

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            true
        }
        hibernate5Handler.closeHbm5Session()
        // second call should be a NOOP
        hibernate5Handler.closeHbm5Session()

        then:
        testEventHandler.events.size() == 9
        testEventHandler.events == eventNames
    }

    void "AnotherPerson class can be used on 'default' datasource"() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new AnotherPerson(data))
            }
        }

        List peopleOut = hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            session.createQuery('from AnotherPerson').list()*.asMap()
        }

        then:
        peopleIn == peopleOut
    }

    private class TestEventHandler {
        List<String> events = []

        @EventHandler
        void handleDataSourceConnectStartEvent(DataSourceConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceConnectEndEvent(DataSourceConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectStartEvent(DataSourceDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectEndEvent(DataSourceDisconnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleHibernate5ConnectStartEvent(Hibernate5ConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleHibernate5ConfigurationAvailableEvent(Hibernate5ConfigurationAvailableEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleHibernate5ConnectEndEvent(Hibernate5ConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleHibernate5DisconnectStartEvent(Hibernate5DisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleHibernate5DisconnectEndEvent(Hibernate5DisconnectEndEvent event) {
            events << event.class.simpleName
        }
    }
}