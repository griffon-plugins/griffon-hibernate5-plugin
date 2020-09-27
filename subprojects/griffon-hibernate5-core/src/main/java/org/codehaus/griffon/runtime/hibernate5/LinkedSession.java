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
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * @author Andres Almiray
 */
public class LinkedSession extends SessionDelegatorBaseImpl {
    private RecordingSessionFactory sessionFactory;

    public LinkedSession(@Nonnull SessionImplementor delegate, @Nonnull RecordingSessionFactory sessionFactory) {
        super(delegate);
        this.sessionFactory = sessionFactory;
    }

    // @Nonnull
    // public SessionFactory getSessionFactory() {
    //     return sessionFactory;
    // }

    @Override
    public void close() throws HibernateException {
        super.close();
        sessionFactory.decreaseSessionCount();
    }
}
