package org.openepics.discs.client;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.openepics.discs.client.impl.ClosableResponse;
import org.openepics.discs.client.impl.ResponseException;
import org.openepics.discs.conf.jaxb.Device;
import org.openepics.discs.conf.jaxb.lists.DeviceList;
import org.openepics.discs.conf.jaxrs.client.DeviceClient;

public class DeviceClientImpl implements DeviceClient {

    private static final Logger LOG = Logger.getLogger(DeviceClientImpl.class.getCanonicalName());

    private static final String PATH_DEVICES = "devices";

    @Nonnull private final CCDBClient client;

    public DeviceClientImpl(CCDBClient client) {
        this.client = client;
    }

    /**
     * Requests a {@link DeviceList} containing a {@link List} of all {@link Device}s from the REST service.
     *
     * @throws ResponseException if data couldn't be retrieved
     *
     * @return {@link DeviceList}
     */
    @Override
    public List<Device> getAllDevices() {
        LOG.fine("Invoking getAllDevices");

        final String url = client.buildUrl(PATH_DEVICES);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(DeviceList.class).getDeviceList();
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    /**
     * Requests particular {@link Device} from the REST service.
     *
     * @param inventoryId the inventoryId of the desired {@link Device}
     *
     * @throws ResponseException if data couldn't be retrieved
     *
     * @return {@link Device}
     */
    @Override
    public Device getDevice(String inventoryId) {
        LOG.fine("Invoking getDevice");

        final String url = client.buildUrl(PATH_DEVICES, inventoryId);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(Device.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }

    @Override
    public InputStream getAttachment(String name, String fileName) {
        LOG.fine("Invoking getAttachment");

        final String url = client.buildUrl(PATH_DEVICES, name, "download", fileName);
        try (final ClosableResponse response = client.getResponse(url)) {
            return response.readEntity(InputStream.class);
        } catch (Exception e) {
            throw new ResponseException("Couldn't retrieve data from service at " + url + ".", e);
        }
    }
}
