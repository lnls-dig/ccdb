package org.openepics.discs.conf.jaxrs.client;

import java.io.InputStream;
import java.util.List;

import org.openepics.discs.conf.jaxb.DeviceType;
import org.openepics.discs.conf.jaxb.InstallationSlot;
import org.openepics.discs.conf.jaxb.InstallationSlotName;

/**
 * This interface provides methods for clients to access {@link InstallationSlot} specific data.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public interface InstallationSlotClient {
    /** @return all {@link InstallationSlot}s */
    public List<InstallationSlot> getInstallationSlots();

    /** @return a subset of {@link InstallationSlot}s based on the {@link DeviceType} name */
    public List<InstallationSlot> getInstallationSlots(String deviceType);

    /**
     * Returns a specific installation slot
     *
     * @param name
     *            the name of the installation slot to retrieve
     * @return the installation slot instance data
     */
    public InstallationSlot getInstallationSlot(String name);

    /**
     * Returns a specific installation slot artifact file.
     *
     * @param name
     *            the name of the installation slot from which to retrieve
     *            artifact file.
     * @param fileName
     *            the name of the artifact file to retrieve.
     * @return the installation slot artifact file
     */
    public InputStream getAttachment(String name, String fileName);

    /**
     * @return a {@link List} of all {@link InstallationSlotName}s.
     */
    public List<InstallationSlotName> getAllInstallationSlotNames();

    /**
     * @param deviceTypeName the name of the {@link DeviceType} to return information for.
     *
     * @return a {@link List} of {@link InstallationSlotName}s that correspond to a requested {@link DeviceType}.
     */
    public List<InstallationSlotName> getAllInstallationSlotNames(String deviceTypeName);
}
