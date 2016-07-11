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
import java.util.stream.Stream;

import javax.inject.Inject;

import org.openepics.discs.conf.ejb.DeviceEJB;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.jaxb.Artifact;
import org.openepics.discs.conf.jaxb.Device;
import org.openepics.discs.conf.jaxb.PropertyKind;
import org.openepics.discs.conf.jaxb.PropertyValue;
import org.openepics.discs.conf.jaxrs.DeviceResource;
import org.openepics.discs.conf.util.UnhandledCaseException;

/**
 * An implementation of the DeviceResource interface.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class DeviceResourceImpl implements DeviceResource {

    @Inject private DeviceEJB deviceEJB;
    @Inject private InstallationEJB installationEJB;

    @Override
    public List<Device> getAllDevices() {
        return deviceEJB.findAll().stream().map(device -> getDevice(device)).collect(Collectors.toList());
    }

    @Override
    public Device getDevice(String name) {
        return getDevice(deviceEJB.findByName(name));
    }

    /**
     * Transforms a CCDB database entity into a REST DTO object. Called from
     * other web service classes as well.
     *
     * @param device
     *            the CCDB database entity to wrap
     * @return REST DTO object
     */
    protected Device getDevice(org.openepics.discs.conf.ent.Device device) {
        if (device == null) {
            return null;
        } else {
            final Device deviceJAXB = new Device();
            deviceJAXB.setSerialNumber(device.getSerialNumber());
            deviceJAXB.setDeviceType(DeviceTypeResourceImpl.getDeviceType(device.getComponentType()));
            deviceJAXB.setProperties(getPropertyValues(device));
            deviceJAXB .setArtifacts(getArtifacts(device.getEntityArtifactList()));
            return deviceJAXB;
        }
    }

    private List<PropertyValue> getPropertyValues(final org.openepics.discs.conf.ent.Device device) {
        final InstallationRecord record = installationEJB .getLastInstallationRecordForDevice(device);

        final Stream<? extends PropertyValue> externalProps = Stream.concat(
                device.getComponentType().getComptypePropertyList().stream().
                        filter(propValue -> !propValue.isPropertyDefinition()).
                        map(propValue -> createPropertyValue(propValue)),
                record == null ? Stream.empty() : record.getSlot().getSlotPropertyList().stream().
                                                                    map(propValue -> createPropertyValue(propValue)));

        return Stream.concat(device.getDevicePropertyList().stream().map(propValue -> createPropertyValue(propValue)),
                                                                            externalProps).collect(Collectors.toList());
    }

    private static PropertyValue createPropertyValue(
            final org.openepics.discs.conf.ent.PropertyValue slotPropertyValue) {
        final PropertyValue propertyValue = new PropertyValue();
        final Property parentProperty = slotPropertyValue.getProperty();
        propertyValue.setName(parentProperty.getName());
        propertyValue.setDataType(parentProperty.getDataType() != null ? parentProperty.getDataType().getName() : null);
        propertyValue.setUnit(parentProperty.getUnit() != null ? parentProperty.getUnit().getName() : null);
        propertyValue.setValue(Objects.toString(slotPropertyValue.getPropValue()));

        if (slotPropertyValue instanceof ComptypePropertyValue) {
            propertyValue.setPropertyKind(PropertyKind.TYPE);
        } else if (slotPropertyValue instanceof SlotPropertyValue) {
            propertyValue.setPropertyKind(PropertyKind.SLOT);
        } else if (slotPropertyValue instanceof DevicePropertyValue) {
            propertyValue.setPropertyKind(PropertyKind.DEVICE);
        } else {
            throw new UnhandledCaseException();
        }

        return propertyValue;
    }

    private static List<Artifact> getArtifacts(List<org.openepics.discs.conf.ent.Artifact> entityArtifactList) {
        return entityArtifactList.stream().map(Artifact::new).collect(Collectors.toList());
    }
}