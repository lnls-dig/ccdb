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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.openepics.discs.conf.ejb.DeviceEJB;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.jaxb.Artifact;
import org.openepics.discs.conf.jaxb.Device;
import org.openepics.discs.conf.jaxb.PropertyValue;
import org.openepics.discs.conf.jaxb.lists.DeviceList;
import org.openepics.discs.conf.jaxrs.DeviceResource;
import org.openepics.discs.conf.util.BlobStore;

/**
 * An implementation of the DeviceResource interface.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class DeviceResourceImpl implements DeviceResource, Serializable {
    private static final long serialVersionUID = 3108793668900422356L;

    @Inject private DeviceEJB deviceEJB;
    @Inject private InstallationEJB installationEJB;
    @Inject private BlobStore blobStore;

    @Override
    public DeviceList getAllDevices() {
        return new DeviceList(deviceEJB.findAll().stream().map(device -> getDevice(device)).
                                                                                        collect(Collectors.toList()));
    }

    @Override
    public Device getDevice(String name) {
        return getDevice(deviceEJB.findByName(name));
    }

    @Override
    public Response getAttachment(String name, String fileName) {
        final org.openepics.discs.conf.ent.Device device = deviceEJB.findByName(name);
        return GetAttachmentResourceBase.getFileForDevice(device,
                        installationEJB.getActiveInstallationRecordForDevice(device) , name, fileName, blobStore);
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
            deviceJAXB.setInventoryId(device.getSerialNumber());
            deviceJAXB.setDeviceType(device.getComponentType().getName());
            deviceJAXB.setProperties(Utils.emptyToNull(getPropertyValues(device)));
            deviceJAXB.setArtifacts(Utils.emptyToNull(getArtifacts(device)));
            return deviceJAXB;
        }
    }

    private List<PropertyValue> getPropertyValues(final org.openepics.discs.conf.ent.Device device) {
        final InstallationRecord record = installationEJB .getLastInstallationRecordForDevice(device);

        final Stream<PropertyValue> externalProps = Stream.concat(
                device.getComponentType().getComptypePropertyList().stream().
                        filter(propValue -> !propValue.isPropertyDefinition()).
                        map(propValue -> Utils.createPropertyValue(propValue)),
                record == null ? Stream.empty() : record.getSlot().getSlotPropertyList().stream().
                                                                map(propValue -> Utils.createPropertyValue(propValue)));

        return Stream.concat(device.getDevicePropertyList().stream().
                                            map(propValue -> Utils.createPropertyValue(propValue)), externalProps).
                                            collect(Collectors.toList());
    }

    private List<Artifact> getArtifacts(final org.openepics.discs.conf.ent.Device device) {
        final InstallationRecord record = installationEJB .getLastInstallationRecordForDevice(device);

        final List<Artifact> externalArtifacts = Utils.getArtifacts(device.getComponentType());
        if (record != null) {
            externalArtifacts.addAll(Utils.getArtifacts(record.getSlot()));
        }

        final List<Artifact> deviceArtifacts = Utils.getArtifacts(device);
        deviceArtifacts.addAll(externalArtifacts);

        return deviceArtifacts;
    }
}