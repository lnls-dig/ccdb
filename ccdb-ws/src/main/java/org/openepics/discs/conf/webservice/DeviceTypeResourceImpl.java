/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.jaxb.DeviceType;
import org.openepics.discs.conf.jaxb.lists.DeviceTypeList;
import org.openepics.discs.conf.jaxrs.DeviceTypeResource;
import org.openepics.discs.conf.util.BlobStore;

/**
 * An implementation of the DeviceTypeResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class DeviceTypeResourceImpl implements DeviceTypeResource, Serializable {
    private static final long serialVersionUID = 2029600617687884478L;

    @Inject private ComptypeEJB comptypeEJB;
    @Inject private BlobStore blobStore;

    @Override
    public DeviceTypeList getAllDeviceTypes() {
        return new DeviceTypeList(comptypeEJB.findAll().stream().map(compType -> getDeviceType(compType)).
                                                                                        collect(Collectors.toList()));
    }

    @Override
    public DeviceType getDeviceType(String name) {
        return getDeviceType(comptypeEJB.findByName(name));
    }

    @Override
    public Response getAttachment(String name, String fileName) {
        return GetAttachmentResourceBase.getFileForDeviceType(comptypeEJB.findByName(name), name, fileName, blobStore);
    }

    /** Transforms a CCDB database entity into a REST DTO object. Called from other web service classes as well.
     * @param componentType the CCDB database entity to wrap
     * @return REST DTO object
     */
    private DeviceType getDeviceType(ComponentType componentType) {
        if (componentType == null) {
            return null;
        } else {
            final DeviceType deviceType = new DeviceType();
            deviceType.setName(componentType.getName());
            deviceType.setDescription(componentType.getDescription());
            deviceType.setArtifacts(Utils.emptyToNull(Utils.getArtifacts(componentType)));
            return deviceType;
        }
    }
}
