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
package org.openepics.discs.ccdb.jaxrs;

import java.util.List;
import javax.ws.rs.DefaultValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.openepics.discs.ccdb.jaxb.ResDevice;

/**
 * This resource provides bulk and specific device type data.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@Path("device")
public interface DeviceResource {
 
    /**
     * search devices
     * 
     * @param name
     * @param type
     * @return 
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ResDevice> searchDevices( 
        @DefaultValue("") @QueryParam("name") String name, 
        @DefaultValue("") @QueryParam("type") String type);
    
    /**
     * Returns a specific device type.
     *
     * @param name the name of the device type to retrieve
     * @return the device type instance data
     */
    @GET
    @Path("{iid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ResDevice getDevice(@PathParam("iid") String name);
}
