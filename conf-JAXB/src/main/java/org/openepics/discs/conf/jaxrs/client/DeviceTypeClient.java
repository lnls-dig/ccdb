package org.openepics.discs.conf.jaxrs.client;

import java.io.InputStream;
import java.util.List;

import org.openepics.discs.conf.jaxb.DeviceType;

/**
 * This interface provides methods for clients to access {@link DeviceType} specific data.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public interface DeviceTypeClient {
    /** @return returns all device types in the database. */
    public List<DeviceType> getAllDeviceTypes();

    /**
     * Returns a specific device type.
     *
     * @param name
     *            the name of the device type to retrieve
     * @return the device type instance data
     */
    public DeviceType getDeviceType(String name);

    /**
     * Returns a specific device type artifact file.
     *
     * @param name
     *            the name of the device type from which to retrieve artifact
     *            file.
     * @param fileName
     *            the name of the artifact file to retrieve.
     * @return the device type artifact file
     */
    public InputStream getAttachment(String name, String fileName);
}

