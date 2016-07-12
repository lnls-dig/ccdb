/*
 * Copyright (c) 2016 European Spallation Source
 * Copyright (c) 2016 Cosylab d.d.
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

import org.openepics.discs.conf.ent.Artifact;
import org.openepics.discs.conf.ent.ComptypeArtifact;
import org.openepics.discs.conf.ent.DeviceArtifact;
import org.openepics.discs.conf.ent.SlotArtifact;

/**
 * DAO service for accessing artifact instances.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 *
 */
public class ArtifactEJB extends DAO<Artifact> {

    /**
     * Searches for a artifact instance in the database, by its id.
     *
     * @param id
     *            the artifact id
     * @return a artifact entity matching the criteria or <code>null</code>
     */
    @Override
     public Artifact findById(Object id) {
         Artifact art = em.find(ComptypeArtifact.class, id);
         if (art == null) {
             art = em.find(SlotArtifact.class, id);
             if (art == null) {
                 art = em.find(DeviceArtifact.class, id);
             }
         }

         return art;
     }

     @Override
     protected Class<Artifact> getEntityClass() {
         return Artifact.class;
     }
}
