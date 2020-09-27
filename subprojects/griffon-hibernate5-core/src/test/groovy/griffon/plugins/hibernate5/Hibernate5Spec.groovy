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
import griffon.plugins.hibernate5.exceptions.RuntimeHibernate5Exception
import griffon.test.core.GriffonUnitRule
import org.hibernate.Session
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.application.event.EventHandler
import javax.inject.Inject

@Unroll
class Hibernate5Spec extends Specification {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace")
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private Hibernate5Handler hibernate5Handler

    @Inject
    private GriffonApplication application

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

    void 'Connect to default SessionFactory'() {
        expect:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            sessionFactoryName == 'default' && session
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session -> }
        hibernate5Handler.closeHbm5Session()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name SessionFactory'() {
        expect:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            sessionFactoryName == name && session
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus SessionFactory name (#name) results in error'() {
        when:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people table'() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new Person(data))
            }
        }

        List peopleOut = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.createQuery('from Person').list()*.asMap()
        }

        then:
        peopleIn == peopleOut
    }

    void 'A runtime exception is thrown within session handling'() {
        when:
        hibernate5Handler.withHbm5Session { String sessionFactoryName, Session session ->
            session.save(new Person())
        }

        then:
        thrown(RuntimeHibernate5Exception)
    }

    void "Exception is thrown when using Person class on 'people' datasource"() {
        when:
        hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            session.createQuery("from Person").list()
        }

        then:
        thrown(RuntimeHibernate5Exception)

        where:
        name     | _
        'people' | _
    }

    void "AnotherPerson class can be used on 'people' datasource"() {
        when:
        List peopleIn = hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
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

        List peopleOut = hibernate5Handler.withHbm5Session(name) { String sessionFactoryName, Session session ->
            session.createQuery('from AnotherPerson').list()*.asMap()
        }

        then:
        peopleIn == peopleOut

        where:
        name     | _
        'people' | _
    }

    void 'Execute statements on annotated class'() {
        when:
        List userIn = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].each { data ->
                session.save(new User(data))
            }
        }

        List userOut = hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.createCriteria(User).list()*.asMap()
        }

        then:
        userIn == userOut
    }

    void 'Mapped class without @Entity annotation should throw exception'() {
        when:
        hibernate5Handler.withHbm5Session() { String sessionFactoryName, Session session ->
            session.saveOrUpdate(new NotAnnotatedClass([id: 2, name: 'Andres', lastname: 'Almiray']))
        }

        then:
        thrown(RuntimeHibernate5Exception)
    }

    @BindTo(Hibernate5Bootstrap)
    private TestHibernate5Bootstrap bootstrap = new TestHibernate5Bootstrap()

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
