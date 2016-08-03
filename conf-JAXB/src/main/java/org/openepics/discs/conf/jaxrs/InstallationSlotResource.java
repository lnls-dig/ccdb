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
package org.openepics.discs.conf.jaxrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openepics.discs.conf.jaxb.DeviceType;
import org.openepics.discs.conf.jaxb.InstallationSlot;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotList;

/**
 * This resource provides bulk and specific installation slot data.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@Path("slots")
public interface InstallationSlotResource {
    /**
     * This method returns all the {@link InstallationSlot}s in the database or their subset based on
     * the {@link DeviceType}.
     *
     * @param deviceType optional {@link DeviceType} name
     * @return {@link InstallationSlotList}
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public InstallationSlotList getInstallationSlots(
            @DefaultValue("undefined") @QueryParam("deviceType") String deviceType);

    /**
     * Returns a specific installation slot
     *
     * @param name
     *            the name of the installation slot to retrieve
     * @return the installation slot instance data
     */
    @GET
    @Path("{name}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public InstallationSlot getInstallationSlot(@PathParam("name") String name);

    /**
     * Returns a specific installation slot artifact file.
     *
     * @param name
     *            the name of the installation slot from which to retrieve
     *            artifact file.
     * @param fileName
     *            the name of the artifact file to retrieve.
     * @return the installation slot artifact file
     */
    @GET
    @Path("{name}/download/{fileName}")
    @Produces({ MediaType.MEDIA_TYPE_WILDCARD })
    public Response getAttachment(@PathParam("name") String name, @PathParam("fileName") String fileName);
}
