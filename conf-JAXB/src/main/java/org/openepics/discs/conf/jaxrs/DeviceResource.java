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
package org.openepics.discs.conf.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openepics.discs.conf.jaxb.Device;
import org.openepics.discs.conf.jaxb.lists.DeviceList;

/**
 * This resource provides bulk and specific device data.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@Path("devices")
public interface DeviceResource {

    /** @return returns all devices in the database. */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public DeviceList getAllDevices();

    /**
     * Returns a specific device.
     *
     * @param name
     *            the name of the device to retrieve
     * @return the device instance data
     */
    @GET
    @Path("{name}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Device getDevice(@PathParam("name") String name);

    /**
     * Returns a specific device artifact file.
     *
     * @param name
     *            the name of the device from which to retrieve artifact file.
     * @param fileName
     *            the name of the artifact file to retrieve.
     * @return the device artifact file
     */
    @GET
    @Path("{name}/download/{fileName}")
    @Produces({ MediaType.MEDIA_TYPE_WILDCARD })
    public Response getAttachment(@PathParam("name") String name, @PathParam("fileName") String fileName);
}
