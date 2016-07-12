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
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openepics.discs.conf.ent.Artifact;
import org.openepics.discs.conf.util.BlobStore;

/**
 * This is a package private class the implements the download attachment common code.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
class GetAttachmentResourceBase {

    /* Only static methods */
    private GetAttachmentResourceBase() {
    }

    static Response getFile(final List<Artifact> artifacts, final String entityName, final String fileName,
                    final BlobStore blobStore) {
        ResponseBuilder response;
        String uri = null;

        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (fileName.equals(artifact.getName())) {
                    uri = artifact.getUri();
                    break;
                }
            }
        }

        try {
            if (uri == null) {
                response = Response.status(Status.NOT_FOUND);
                response.type(MediaType.TEXT_HTML);
                response.entity("Attachment with name " + fileName + " not found on " + entityName + ".");
            } else {
                InputStream stream = blobStore.retreiveFile(uri);
                response = Response.ok(stream);
                MimetypesFileTypeMap mimeTypesFileTypeMap = new MimetypesFileTypeMap();
                response.type(mimeTypesFileTypeMap.getContentType(fileName));
                response.header("Content-Disposition", "attachment; filename = \"" + fileName + "\"");
            }
        } catch (FileNotFoundException e) {
            response = Response.status(Status.NOT_FOUND);
            response.type(MediaType.TEXT_HTML);
            response.entity("Attachment with name " + fileName + " not found on " + entityName + ".");
        } catch (IOException e) {
            response = Response.status(Status.INTERNAL_SERVER_ERROR);
            response.type(MediaType.TEXT_HTML);
            response.entity("Error reading attachment with name " + fileName + ".");
        }
        return response.build();
    }
}
