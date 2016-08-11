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
import org.openepics.discs.ccdb.jaxb.PropertyRep;


/**
 * Property Resource.
 *
 * @author <a href="mailto:vuppala@frib.msu.edu">Vasu Vuppala</a>
 */
@Path("property")
public interface PropertyResource {
    /**
     * Properties
     * 
     * @param query query string
     * @return all properties matching the query
     */
    @GET    
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PropertyRep> searchProperties(
        @DefaultValue("") @QueryParam("name") String query);   
    
    /**
     * A Property
     * 
     * @param name property name
     * @return property
     */
    @GET    
    @Path("{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PropertyRep getProperty(@PathParam("name") String name);
}
