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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openepics.discs.conf.ejb.ArtifactEJB;
import org.openepics.discs.conf.ent.Artifact;
import org.openepics.discs.conf.jaxrs.DownloadAttachmentResource;
import org.openepics.discs.conf.util.BlobStore;

/**
 * An implementation of the DownloadAttachmentResource interface.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class DownloadAttachmentResourceImpl implements DownloadAttachmentResource {

    @Inject private BlobStore blobStore;
    @Inject private ArtifactEJB artifactEJB;

    @Override
    public Response getFile(String id) {
        long Id = 0;
        ResponseBuilder response;

        try {
            Id = Long.parseLong(id);
            final Artifact artifact = artifactEJB.findArtifactById(Id);
            if (artifact == null) {
                response = Response.status(Status.NOT_FOUND);
                response.type(MediaType.TEXT_HTML);
                response.entity("Attachment with Id " + Id + " not found.");
            } else {
                InputStream stream = blobStore.retreiveFile(artifact.getUri());
                response = Response.ok(stream);
                MimetypesFileTypeMap mimeTypesFileTypeMap = new MimetypesFileTypeMap();
                response.type(mimeTypesFileTypeMap.getContentType(artifact
                        .getName()));
                response.header("Content-Disposition",
                        "attachment; filename = \"" + artifact.getName() + "\"");
            }
        } catch (NumberFormatException e) {
            response = Response.status(Status.BAD_REQUEST);
            response.type(MediaType.TEXT_HTML);
            response.entity("Attachment Id must be a number.");
        } catch (FileNotFoundException e) {
            response = Response.status(Status.NOT_FOUND);
            response.type(MediaType.TEXT_HTML);
            response.entity("Attachment with Id " + Id + " not found.");
        } catch (IOException e) {
            response = Response.status(Status.INTERNAL_SERVER_ERROR);
            response.type(MediaType.TEXT_HTML);
            response.entity("Error reading attachment with Id " + Id + ".");
        }
        return response.build();
    }
}
