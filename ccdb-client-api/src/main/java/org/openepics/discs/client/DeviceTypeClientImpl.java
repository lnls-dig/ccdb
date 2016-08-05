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
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

import org.openepics.discs.client.impl.ClosableResponse;
import org.openepics.discs.client.impl.ResponseException;
import org.openepics.discs.conf.jaxb.DeviceType;
import org.openepics.discs.conf.jaxb.lists.DeviceTypeList;
import org.openepics.discs.conf.jaxrs.client.DeviceTypeClient;

/**
 * This is CCDB service clientdataType parser that is used to get data from server.
 * <p>
 * All class methods are static.
 * </p>
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 * @author <a href="mailto:miroslav.pavleski@cosylab.com">Miroslav Pavleski</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */

class DeviceTypeClientImpl implements DeviceTypeClient {

    private static final Logger LOG = Logger.getLogger(DeviceTypeClientImpl.class.getCanonicalName());

    private static final String PATH_DEVICE_TYPES = "deviceTypes";

    @Nonnull private final CCDBClient client;

    DeviceTypeClientImpl(CCDBClient client) { this.client = client; }

    /**
     * Requests a {@link DeviceTypeList} containing a {@link List} of all {@link DeviceType}s from the REST service.
     *
     * @throws ResponseException if data couldn't be retrieved
     *
     * @return {@link List} of all {@link DeviceType}s
     */
    @Override
    public List<DeviceType> getAllDeviceTypes() {
        LOG.fine("Invoking getAllDeviceTypes.");

        final String url = client.buildUrl(PATH_DEVICE_TYPES);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(DeviceTypeList.class).getDeviceTypes();
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    /**
     * Requests particular {@link DeviceType} from the REST service.
     *
     * @param name the name of the desired DeviceType
     *
     * @throws ResponseException  if data couldn't be retrieved
     *
     * @return {@link DeviceType}
     */
    @Override
    public DeviceType getDeviceType(String name) {
        LOG.fine("Invoking getDeviceType. name=" + name);

        final String url = client.buildUrl(PATH_DEVICE_TYPES, name);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(DeviceType.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    /**
     * Requests particular file attachment from the REST service. To read the attachment file use
     * {@link Response#readEntity(Class)}: <code>Reponse.readEntity(InputStream.class)</code>.
     *
     * @param name the {@link DeviceType}
     * @param fileName the name of the attachment
     * @return {@link Response}
     */
    @Override
    public InputStream getAttachment(String name, String fileName) {
        LOG.fine("Invoking getAttachment. name=" + name + ", fileName" + fileName);

        final String url = client.buildUrl(PATH_DEVICE_TYPES, name, "download", fileName);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(InputStream.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }
}
