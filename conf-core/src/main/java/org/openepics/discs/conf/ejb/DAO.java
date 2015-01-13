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

import java.util.List;

import org.openepics.discs.conf.auditlog.Audit;
import org.openepics.discs.conf.ent.Artifact;
import org.openepics.discs.conf.ent.EntityTypeOperation;
import org.openepics.discs.conf.ent.EntityWithArtifacts;
import org.openepics.discs.conf.ent.EntityWithProperties;
import org.openepics.discs.conf.ent.PropertyValue;
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

    @SuppressWarnings("unchecked")
    private <S> T getParent(S child) {
        Preconditions.checkNotNull(child);
        if (child instanceof PropertyValue) {
            return (T) ((PropertyValue)child).getPropertiesParent();
        } else if (child instanceof Artifact) {
            return (T) ((Artifact)child).getArtifactsParent();
        } else {
            throw new IllegalStateException("getParent called on entity that has neither properties nor artifacts.");
        }
    }

    @SuppressWarnings("unchecked")
    private <S> List<S> getChildrenFromParent(S child) {
        Preconditions.checkNotNull(child);
        if (child instanceof PropertyValue) {
            final EntityWithProperties parent = ((PropertyValue)child).getPropertiesParent();
            return (List<S>) parent.getEntityPropertyList();
        } else if (child instanceof Artifact) {
            final EntityWithArtifacts parent = ((Artifact)child).getArtifactsParent();
            return (List<S>) parent.getEntityArtifactList(); 
        } else {
            throw new IllegalStateException("getParent called on entity that has neither properties nor artifacts.");
        }
    }
}
