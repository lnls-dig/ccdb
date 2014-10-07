/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ejb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openepics.discs.conf.auditlog.Audit;
import org.openepics.discs.conf.ent.EntityTypeOperation;
import org.openepics.discs.conf.security.Authorized;
import org.openepics.discs.conf.util.CRUDOperation;

import com.google.common.base.Preconditions;

/**
 * Abstract generic DAO used for all entities.
 *
 * It uses the concept of Parent and optional Child entities in {@link List} collections.
 * This one extends the read-only {@link ReadOnlyDAO}.
 *
 * @author Miroslav Pavleski <miroslav.pavleski@cosylab.com>
 *
 * @param <T> The entity type for which this DAO is defined.
 */
public abstract class DAO<T> extends ReadOnlyDAO<T> {
    @SuppressWarnings("rawtypes")
    private Map<Class, ParentChildInterface> interfaces;

    /**
     * Adds a new entity to the database
     *
     * @param entity the entity to be added
     */
    @CRUDOperation(operation=EntityTypeOperation.CREATE)
    @Audit
    @Authorized
    public void add(T entity) {
        Preconditions.checkNotNull(entity);
        entityUtility.setModified(entity);
        em.persist(entity);
    }

    /**
     * Updates an entity in the database
     *
     * @param entity the entity to be updated
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void save(T entity) {
        Preconditions.checkNotNull(entity);
        entityUtility.setModified(entity);
        em.merge(entity);
    }

    /**
     * Deletes entity from the database
     *
     * @param entity the entity to be deleted
     */
    @CRUDOperation(operation=EntityTypeOperation.DELETE)
    @Audit
    @Authorized
    public void delete(T entity) {
        Preconditions.checkNotNull(entity);
        em.remove( em.merge(entity) );
    }

    /**
     * Adds a child entity to a parent and the database
     *
     * @param child the child entity to be added
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public <S> void addChild(S child) {
        Preconditions.checkNotNull(child);
        final T parent = getParent(child);

        entityUtility.setModified(parent, child);

        getChildrenFromParent(child).add(child);
        em.merge(parent);
    }

    /**
     * Updates a child entity of a parent and in the database
     *
     * @param child the child entity to be updated
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public <S> void saveChild(S child) {
        Preconditions.checkNotNull(child);
        final S mergedChild = em.merge( child );
        entityUtility.setModified(getParent(mergedChild), mergedChild);
    }

    /**
     * Deletes a child entity from parent collection and removes from the database
     *
     * @param child the child entity to be removed
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public <S> void deleteChild(S child) {
        Preconditions.checkNotNull(child);
        getChildrenFromParent(child).remove(child);

        final S mergedChild = em.merge(child) ;
        em.remove(mergedChild);
    }

    /**
     * Lazy initialization getter for the interfaces member, solves the problem with constructor order
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected  Map<Class, ParentChildInterface> getResolverInterfaces() {
        if (interfaces == null)
        {
            interfaces = new HashMap<Class, ParentChildInterface>(3);
        }
        return interfaces;
    }

    /**
     * Defines a calling interface between the parent and a child collection
     *
     * @param childClass the child collection
     * @param iface the interface
     */
    protected <S> void defineParentChildInterface(Class<S> childClass, ParentChildInterface<T, S> iface) {
        Preconditions.checkNotNull(childClass);

        getResolverInterfaces().put(childClass, iface);
    }

    /**
     * Retrieves a {@link ParentChildInterface} for a child entity
     *
     * @param child the child entity
     * @return the {@link ParentChildInterface}
     */
    private <S> ParentChildInterface<T, S> getResolverInterface(S child) {
        @SuppressWarnings("unchecked")
        final ParentChildInterface<T,S> resolver = getResolverInterfaces().get(child.getClass());
        if (resolver == null) {
            throw new UnsupportedOperationException("No child interface defined for the class "+
                                                    child.getClass().getCanonicalName() + " in " +
                                                        this.getClass().getSimpleName()+" DAO.");
        }
        return resolver;
    }

    private <S> T getParent(S child) {
        Preconditions.checkNotNull(child);
        final T parent = getResolverInterface(child).getParentFromChild(child);
        Preconditions.checkNotNull(parent);
        return parent;
    }

    private <S> List<S> getChildrenFromParent(S child) {
        Preconditions.checkNotNull(child);
        return getResolverInterface(child).getChildCollection(getParent(child));
    }
}
