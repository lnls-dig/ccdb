/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.client;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.openepics.discs.client.impl.ClosableResponse;
import org.openepics.discs.client.impl.ResponseException;
import org.openepics.discs.conf.jaxb.InstallationSlot;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotList;
import org.openepics.discs.conf.jaxrs.InstallationSlotResource;

/**
 * This is CCDB service client installation slot parser that is used to get data from server.
 * <p>All class methods are static.</p>
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>\
 * @author <a href="mailto:miroslav.pavleski@cosylab.com">Miroslav Pavleski</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */

class InstallationSlotClient implements InstallationSlotResource {
    private static final Logger LOG = Logger.getLogger(InstallationSlot.class.getName());

    private static final String PATH_SLOTS = "slots";

    @Nonnull private final CCDBClient client;

    InstallationSlotClient(CCDBClient client) { this.client = client; }

    /**
     * Gets either all installation slots, or filtered by the deviceType parameter
     *
     * @param deviceType optional (can be null) device type to filter the slots
     * @return list of InstallationSlot
     */
    @Override
    public InstallationSlotList getInstallationSlots(String deviceType) {
        LOG.fine("Invoking getInstallationSlots");

        final String url = client.buildUrl(PATH_SLOTS);

        final MultivaluedHashMap<String, Object> queryParams = new MultivaluedHashMap<>();
        if (deviceType!=null) {
            queryParams.put("deviceType", Arrays.asList(deviceType));
        }
        try (final ClosableResponse response = client.getResponse(url, queryParams)) {
            return response.readEntity(InstallationSlotList.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    @Override
    public InstallationSlot getInstallationSlot(String name) {
        LOG.fine("Invoking getInstallationSlot");

        final String url = client.buildUrl(PATH_SLOTS, name);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(InstallationSlot.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    @Override
    public Response getAttachment(String name, String fileName) {
        LOG.fine("Invoking getAttachment");

        final String url = client.buildUrl(PATH_SLOTS, name, "download", fileName);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response;
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }
}
