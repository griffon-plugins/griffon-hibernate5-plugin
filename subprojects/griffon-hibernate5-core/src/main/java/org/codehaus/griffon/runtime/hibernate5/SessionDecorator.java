/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.hibernate5;

import org.hibernate.*;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.stat.SessionStatistics;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.sql.Connection;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class SessionDecorator implements Session {
    private final Session delegate;

    public SessionDecorator(@Nonnull Session delegate) {
        this.delegate = requireNonNull(delegate, "Argument 'delegate' must not be null");
    }

    @Nonnull
    protected Session getDelegate() {
        return delegate;
    }

    @Override
    public SharedSessionBuilder sessionWithOptions() {
        return delegate.sessionWithOptions();
    }

    @Override
    public void flush() throws HibernateException {
        delegate.flush();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushMode getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        delegate.setCacheMode(cacheMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return delegate.getCacheMode();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return delegate.getSessionFactory();
    }

    @Override
    public void close() throws HibernateException {
        delegate.close();
    }

    @Override
    public void cancelQuery() throws HibernateException {
        delegate.cancelQuery();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return delegate.isDirty();
    }

    @Override
    public boolean isDefaultReadOnly() {
        return delegate.isDefaultReadOnly();
    }

    @Override
    public void setDefaultReadOnly(boolean readOnly) {
        delegate.setDefaultReadOnly(readOnly);
    }

    @Override
    public Serializable getIdentifier(Object object) {
        return delegate.getIdentifier(object);
    }

    @Override
    public boolean contains(Object object) {
        return delegate.contains(object);
    }

    @Override
    public void evict(Object object) {
        delegate.evict(object);
    }

    @Override
    @Deprecated
    public Object load(Class theClass, Serializable id, LockMode lockMode) {
        return delegate.load(theClass, id, lockMode);
    }

    @Override
    public Object load(Class theClass, Serializable id, LockOptions lockOptions) {
        return delegate.load(theClass, id, lockOptions);
    }

    @Override
    @Deprecated
    public Object load(String entityName, Serializable id, LockMode lockMode) {
        return delegate.load(entityName, id, lockMode);
    }

    @Override
    public Object load(String entityName, Serializable id, LockOptions lockOptions) {
        return delegate.load(entityName, id, lockOptions);
    }

    @Override
    public Object load(Class theClass, Serializable id) {
        return delegate.load(theClass, id);
    }

    @Override
    public Object load(String entityName, Serializable id) {
        return delegate.load(entityName, id);
    }

    @Override
    public void load(Object object, Serializable id) {
        delegate.load(object, id);
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode) {
        delegate.replicate(object, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
        delegate.replicate(entityName, object, replicationMode);
    }

    @Override
    public Serializable save(Object object) {
        return delegate.save(object);
    }

    @Override
    public Serializable save(String entityName, Object object) {
        return delegate.save(entityName, object);
    }

    @Override
    public void saveOrUpdate(Object object) {
        delegate.saveOrUpdate(object);
    }

    @Override
    public void saveOrUpdate(String entityName, Object object) {
        delegate.saveOrUpdate(entityName, object);
    }

    @Override
    public void update(Object object) {
        delegate.update(object);
    }

    @Override
    public void update(String entityName, Object object) {
        delegate.update(entityName, object);
    }

    @Override
    public Object merge(Object object) {
        return delegate.merge(object);
    }

    @Override
    public Object merge(String entityName, Object object) {
        return delegate.merge(entityName, object);
    }

    @Override
    public void persist(Object object) {
        delegate.persist(object);
    }

    @Override
    public void persist(String entityName, Object object) {
        delegate.persist(entityName, object);
    }

    @Override
    public void delete(Object object) {
        delegate.delete(object);
    }

    @Override
    public void delete(String entityName, Object object) {
        delegate.delete(entityName, object);
    }

    @Override
    @Deprecated
    public void lock(Object object, LockMode lockMode) {
        delegate.lock(object, lockMode);
    }

    @Override
    @Deprecated
    public void lock(String entityName, Object object, LockMode lockMode) {
        delegate.lock(entityName, object, lockMode);
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return delegate.buildLockRequest(lockOptions);
    }

    @Override
    public void refresh(Object object) {
        delegate.refresh(object);
    }

    @Override
    public void refresh(String entityName, Object object) {
        delegate.refresh(entityName, object);
    }

    @Override
    @Deprecated
    public void refresh(Object object, LockMode lockMode) {
        delegate.refresh(object, lockMode);
    }

    @Override
    public void refresh(Object object, LockOptions lockOptions) {
        delegate.refresh(object, lockOptions);
    }

    @Override
    public void refresh(String entityName, Object object, LockOptions lockOptions) {
        delegate.refresh(entityName, object, lockOptions);
    }

    @Override
    public LockMode getCurrentLockMode(Object object) {
        return delegate.getCurrentLockMode(object);
    }

    @Override
    public Query createFilter(Object collection, String queryString) {
        return delegate.createFilter(collection, queryString);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Object get(Class clazz, Serializable id) {
        return delegate.get(clazz, id);
    }

    @Override
    @Deprecated
    public Object get(Class clazz, Serializable id, LockMode lockMode) {
        return delegate.get(clazz, id, lockMode);
    }

    @Override
    public Object get(Class clazz, Serializable id, LockOptions lockOptions) {
        return delegate.get(clazz, id, lockOptions);
    }

    @Override
    public Object get(String entityName, Serializable id) {
        return delegate.get(entityName, id);
    }

    @Override
    @Deprecated
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        return delegate.get(entityName, id, lockMode);
    }

    @Override
    public Object get(String entityName, Serializable id, LockOptions lockOptions) {
        return delegate.get(entityName, id, lockOptions);
    }

    @Override
    public String getEntityName(Object object) {
        return delegate.getEntityName(object);
    }

    @Override
    public IdentifierLoadAccess byId(String entityName) {
        return delegate.byId(entityName);
    }

    @Override
    public IdentifierLoadAccess byId(Class entityClass) {
        return delegate.byId(entityClass);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String entityName) {
        return delegate.byNaturalId(entityName);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(Class entityClass) {
        return delegate.byNaturalId(entityClass);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
        return delegate.bySimpleNaturalId(entityName);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
        return delegate.bySimpleNaturalId(entityClass);
    }

    @Override
    public Filter enableFilter(String filterName) {
        return delegate.enableFilter(filterName);
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        return delegate.getEnabledFilter(filterName);
    }

    @Override
    public void disableFilter(String filterName) {
        delegate.disableFilter(filterName);
    }

    @Override
    public SessionStatistics getStatistics() {
        return delegate.getStatistics();
    }

    @Override
    public boolean isReadOnly(Object entityOrProxy) {
        return delegate.isReadOnly(entityOrProxy);
    }

    @Override
    public void setReadOnly(Object entityOrProxy, boolean readOnly) {
        delegate.setReadOnly(entityOrProxy, readOnly);
    }

    @Override
    public void doWork(Work work) throws HibernateException {
        delegate.doWork(work);
    }

    @Override
    public <T> T doReturningWork(ReturningWork<T> work) throws HibernateException {
        return delegate.doReturningWork(work);
    }

    @Override
    public Connection disconnect() {
        return delegate.disconnect();
    }

    @Override
    public void reconnect(Connection connection) {
        delegate.reconnect(connection);
    }

    @Override
    public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
        return delegate.isFetchProfileEnabled(name);
    }

    @Override
    public void enableFetchProfile(String name) throws UnknownProfileException {
        delegate.enableFetchProfile(name);
    }

    @Override
    public void disableFetchProfile(String name) throws UnknownProfileException {
        delegate.disableFetchProfile(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return delegate.getTypeHelper();
    }

    @Override
    public LobHelper getLobHelper() {
        return delegate.getLobHelper();
    }

    @Override
    public void addEventListeners(SessionEventListener... listeners) {
        delegate.addEventListeners(listeners);
    }

    @Override
    public String getTenantIdentifier() {
        return delegate.getTenantIdentifier();
    }

    @Override
    public Transaction beginTransaction() {
        return delegate.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public Query getNamedQuery(String queryName) {
        return delegate.getNamedQuery(queryName);
    }

    @Override
    public Query createQuery(String queryString) {
        return delegate.createQuery(queryString);
    }

    @Override
    public SQLQuery createSQLQuery(String queryString) {
        return delegate.createSQLQuery(queryString);
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        return delegate.getNamedProcedureCall(name);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return delegate.createStoredProcedureCall(procedureName);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return delegate.createStoredProcedureCall(procedureName, resultClasses);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return delegate.createStoredProcedureCall(procedureName, resultSetMappings);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        return delegate.createCriteria(persistentClass);
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        return delegate.createCriteria(persistentClass, alias);
    }

    @Override
    public Criteria createCriteria(String entityName) {
        return delegate.createCriteria(entityName);
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        return delegate.createCriteria(entityName, alias);
    }
}
