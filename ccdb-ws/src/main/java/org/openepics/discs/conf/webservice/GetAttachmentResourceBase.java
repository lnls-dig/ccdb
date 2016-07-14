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
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.Slot;
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

    static Response getFileForDeviceType(ComponentType componentType, final String entityName, final String fileName,
            final BlobStore blobStore) {
        return getFile(findArtifactUri(componentType.getEntityArtifactList(), fileName), entityName, fileName,
                blobStore);
    }

    static Response getFileForDevice(final org.openepics.discs.conf.ent.Device device,
            final InstallationRecord installationRecord, final String entityName, final String fileName,
            final BlobStore blobStore) {
        String uri = findArtifactUri(device.getEntityArtifactList(), fileName);

        if ((uri == null) && (installationRecord != null)) {
            uri = findArtifactUri(installationRecord.getSlot().getEntityArtifactList(), fileName);
        }
        if (uri == null) {
            uri = findArtifactUri(device.getComponentType().getEntityArtifactList(), fileName);
        }
        return getFile(uri, entityName, fileName, blobStore);
    }

    static Response getFileForSlot(final Slot slot, final InstallationRecord installationRecord,
            final String entityName, final String fileName, final BlobStore blobStore) {
        Device device = null;
        String uri = findArtifactUri(slot.getEntityArtifactList(), fileName);

        if ((uri == null) && (installationRecord != null)) {
            device = installationRecord.getDevice();
            uri = findArtifactUri(device.getEntityArtifactList(), fileName);
        }
        if (uri == null) {
            uri = findArtifactUri(slot.getComponentType().getEntityArtifactList(), fileName);
        }
        return getFile(uri, entityName, fileName, blobStore);
    }

    private static Response getFile(final String uri, final String entityName, final String fileName,
            final BlobStore blobStore) {
        ResponseBuilder response;

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

    private static String findArtifactUri(final List<Artifact> artifacts, final String fileName) {
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (fileName.equals(artifact.getName())) {
                    return artifact.getUri();
                }
            }
        }
        return null;
    }
}
