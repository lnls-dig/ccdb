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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MultivaluedHashMap;

import org.openepics.discs.client.impl.ClosableResponse;
import org.openepics.discs.client.impl.ResponseException;
import org.openepics.discs.conf.jaxb.InstallationSlot;
import org.openepics.discs.conf.jaxb.InstallationSlotName;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotList;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotNameList;
import org.openepics.discs.conf.jaxrs.client.InstallationSlotClient;

/**
 * This is CCDB service client installation slot parser that is used to get data from server.
 * <p>All class methods are static.</p>
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>\
 * @author <a href="mailto:miroslav.pavleski@cosylab.com">Miroslav Pavleski</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */

class InstallationSlotClientImpl implements InstallationSlotClient {
    private static final Logger LOG = Logger.getLogger(InstallationSlotClientImpl.class.getCanonicalName());

    private static final String PATH_SLOTS = "slots";
    private static final String PATH_SLOT_NAMES = "slotNames";

    private static final String QUERY_PARAM_DEVICE_TYPE = "deviceType";

    @Nonnull private final CCDBClient client;

    InstallationSlotClientImpl(CCDBClient client) { this.client = client; }

    /* (non-Javadoc)
     * @see org.openepics.discs.conf.jaxrs.client.InstallationSlotClient#getInstallationSlots()
     */
    @Override
    public List<InstallationSlot> getInstallationSlots() {
        return getInstallationSlots(null);
    }

    /* (non-Javadoc)
     * @see org.openepics.discs.conf.jaxrs.client.InstallationSlotClient#getInstallationSlots(java.lang.String)
     */
    @Override
    public List<InstallationSlot> getInstallationSlots(String deviceType) {
        LOG.fine("Invoking getInstallationSlots. deviceType=" + deviceType);

        final String url = client.buildUrl(PATH_SLOTS);

        final MultivaluedHashMap<String, Object> queryParams = new MultivaluedHashMap<>();
        if (deviceType != null) {
            queryParams.put(QUERY_PARAM_DEVICE_TYPE, Arrays.asList(deviceType));
        }
        try (final ClosableResponse response = client.getResponse(url, queryParams)) {
            return response.readEntity(InstallationSlotList.class).getInstallationSlots();
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    /* (non-Javadoc)
     * @see org.openepics.discs.conf.jaxrs.client.InstallationSlotClient#getInstallationSlot(java.lang.String)
     */
    @Override
    public InstallationSlot getInstallationSlot(String name) {
        LOG.fine("Invoking getInstallationSlot. name=" + name);

        final String url = client.buildUrl(PATH_SLOTS, name);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(InstallationSlot.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    /* (non-Javadoc)
     * @see org.openepics.discs.conf.jaxrs.client.InstallationSlotClient#getAttachment(java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getAttachment(String name, String fileName) {
        LOG.fine("Invoking getAttachment. name=" + name + ", fileName=" + fileName);

        final String url = client.buildUrl(PATH_SLOTS, name, "download", fileName);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(InputStream.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    @Override
    public List<InstallationSlotName> getAllInstallationSlotNames() {
        return getAllInstallationSlotNames(null);
    }

    /* (non-Javadoc)
     * @see org.openepics.discs.conf.jaxrs.client.InstallationSlotClient#getAllInstallationSlotNames(java.lang.String)
     */
    @Override
    public List<InstallationSlotName> getAllInstallationSlotNames(String deviceTypeName) {
        LOG.fine("Invoking getAllInstallationSlotNames. deviceTypeName=" + deviceTypeName);

        final String url = client.buildUrl(PATH_SLOT_NAMES);

        final MultivaluedHashMap<String, Object> queryParams = new MultivaluedHashMap<>();
        if (deviceTypeName != null) {
            queryParams.put(QUERY_PARAM_DEVICE_TYPE, Arrays.asList(deviceTypeName));
        }

        try (final ClosableResponse response = client.getResponse(url, queryParams)) {
            return response.readEntity(InstallationSlotNameList.class).getNames();
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }
}
