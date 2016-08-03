package org.openepics.discs.conf.jaxrs.client;

import java.io.InputStream;
import java.util.List;

import org.openepics.discs.conf.jaxb.Device;

/**
 * This interface provides methods for clients to access {@link Device} specific data.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public interface DeviceClient {

    /** @return returns all devices in the database. */
    public List<Device> getAllDevices();

    /**
     * Returns a specific device.
     *
     * @param inventoryId
     *            the name of the device to retrieve
     * @return the device instance data
     */
    public Device getDevice(String inventoryId);

    /**
     * Returns a specific device artifact file.
     *
     * @param name
     *            the name of the device from which to retrieve artifact file.
     * @param fileName
     *            the name of the artifact file to retrieve.
     * @return the device artifact file
     */
    public InputStream getAttachment(String name, String fileName);
}
