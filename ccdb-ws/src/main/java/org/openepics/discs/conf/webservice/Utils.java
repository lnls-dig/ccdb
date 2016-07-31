/*
 * Copyright (c) 2016 European Spallation Source
 * Copyright (c) 2016 Cosylab d.d.
 *
 * This file is part of Cable Database.
 * Cable Database is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 2 of the License, or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.webservice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.EntityWithArtifacts;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.jaxb.Artifact;
import org.openepics.discs.conf.jaxb.PropertyKind;
import org.openepics.discs.conf.jaxb.PropertyValue;
import org.openepics.discs.conf.util.UnhandledCaseException;

public class Utils {
    public static List<Artifact> getArtifacts(final EntityWithArtifacts entity) {
        final PropertyKind kind = getKind(entity);
        return entity.getEntityArtifactList().stream().
                map(ea -> {Artifact a = new Artifact(ea); a.setKind(kind); return a; }).collect(Collectors.toList());
    }

    private static PropertyKind getKind(final EntityWithArtifacts entity) {
        if (entity instanceof ComponentType) {
            return PropertyKind.TYPE;
        }
        if (entity instanceof Slot) {
            final Slot slot = (Slot) entity;
            return slot.isHostingSlot() ? PropertyKind.SLOT : PropertyKind.CONTAINER;
        }

        return PropertyKind.DEVICE;
    }

    public static <T> List<T> emptyToNull(List<T> list) {
        return list == null ? null :(list.isEmpty() ? null : list);
    }

    static PropertyValue createPropertyValue(
            final org.openepics.discs.conf.ent.PropertyValue entityPropertyValue) {
        final PropertyValue propertyValue = new PropertyValue();
        final Property parentProperty = entityPropertyValue.getProperty();
        propertyValue.setName(parentProperty.getName());
        propertyValue.setDataType(parentProperty.getDataType() != null ? parentProperty.getDataType().getName() : null);
        propertyValue.setUnit(parentProperty.getUnit() != null ? parentProperty.getUnit().getName() : null);
        propertyValue.setValue(Objects.toString(entityPropertyValue.getPropValue()));

        if (entityPropertyValue instanceof ComptypePropertyValue) {
            propertyValue.setKind(PropertyKind.TYPE);
        } else if (entityPropertyValue instanceof SlotPropertyValue) {
            propertyValue.setKind(PropertyKind.SLOT);
        } else if (entityPropertyValue instanceof DevicePropertyValue) {
            propertyValue.setKind(PropertyKind.DEVICE);
        } else {
            throw new UnhandledCaseException();
        }

        return propertyValue;
    }
}
