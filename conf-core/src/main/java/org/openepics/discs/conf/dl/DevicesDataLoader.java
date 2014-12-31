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
package org.openepics.discs.conf.dl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.openepics.discs.conf.dl.common.AbstractDataLoader;
import org.openepics.discs.conf.dl.common.DataLoader;
import org.openepics.discs.conf.dl.common.DataLoaderResult;
import org.openepics.discs.conf.dl.common.ErrorMessage;
import org.openepics.discs.conf.dl.common.ValidationMessage;
import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ejb.DeviceEJB;
import org.openepics.discs.conf.ejb.PropertyEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.DeviceStatus;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.util.Conversion;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Implementation of data loader for device instances
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 */
@Stateless
@DevicesLoaderQualifier
public class DevicesDataLoader extends AbstractDataLoader implements DataLoader  {
    private static final Logger LOGGER = Logger.getLogger(DevicesDataLoader.class.getCanonicalName());

    @Inject private PropertyEJB propertyEJB;
    @Inject private ComptypeEJB comptypeEJB;
    @Inject private DeviceEJB deviceEJB;
    private int serialIndex, compTypeIndex, descriptionIndex, statusIndex, manufSerialIndex, locationIndex, purchaseOrderIndex, asmPositionIndex, asmDescriptionIndex, manufacturerIndex, manufModelIndex;

    @Override
    public DataLoaderResult loadDataToDatabase(List<List<String>> inputRows) {
        loaderResult = new DataLoaderResult();
        final List<String> fields = ImmutableList.of("SERIAL", "CTYPE", "DESCRIPTION", "STATUS", "MANUF-SERIAL", "LOCATION", "PURCHASE-ORDER", "ASM-POSITION", "ASM-DESCRIPTION", "MANUFACTURER", "MANUF-MODEL" );
        /*
         * List does not contain any rows that do not have a value (command)
         * in the first column. There should be no commands before "HEADER".
         */
        List<String> headerRow = inputRows.get(0);
        checkForDuplicateHeaderEntries(headerRow);
        if (rowResult.isError()) {
            loaderResult.addResult(rowResult);
            return loaderResult;
        }

        setUpIndexesForFields(headerRow);
        Map<String, Integer> indexByPropertyName = indexByPropertyName(fields, headerRow);
        checkPropertyAssociation(indexByPropertyName, headerRow.get(0));

        if (!rowResult.isError()) {
fileProcessing:
            for (List<String> row : inputRows.subList(1, inputRows.size())) {
                final String rowNumber = row.get(0);
                loaderResult.addResult(rowResult);
                rowResult = new DataLoaderResult();
                if (Objects.equal(row.get(commandIndex), CMD_HEADER)) {
                    headerRow = row;
                    checkForDuplicateHeaderEntries(headerRow);
                    if (rowResult.isError()) {
                        loaderResult.addResult(rowResult);
                    } else {
                        setUpIndexesForFields(headerRow);
                        indexByPropertyName = indexByPropertyName(fields, headerRow);
                        checkPropertyAssociation(indexByPropertyName, rowNumber);
                    }
                    if (rowResult.isError()) {
                        return loaderResult;
                    } else {
                        continue; // skip the rest of the processing for HEADER row
                    }
                } else if (row.get(1).equals(CMD_END)) {
                    break;
                }

                final String command = Preconditions.checkNotNull(row.get(commandIndex).toUpperCase());
                final @Nullable String serial = row.get(serialIndex);
                final @Nullable String componentType = row.get(compTypeIndex);
                final @Nullable String description = descriptionIndex == -1 ? null : row.get(descriptionIndex);
                final @Nullable String statusString = statusIndex == -1 ? null : row.get(statusIndex);
                final @Nullable String manufSerial = manufSerialIndex == -1 ? null : row.get(manufSerialIndex);
                final @Nullable String location = locationIndex == -1 ? null : row.get(locationIndex);
                final @Nullable String purchaseOrder = purchaseOrderIndex == -1 ? null : row.get(purchaseOrderIndex);
                final @Nullable String asmPosition = asmPositionIndex == -1 ? null : row.get(asmPositionIndex);
                final @Nullable String asmDescription = asmDescriptionIndex == -1 ? null : row.get(asmDescriptionIndex);
                final @Nullable String manufacturer = manufacturerIndex == -1 ? null : row.get(manufacturerIndex);
                final @Nullable String manufModel = manufModelIndex == -1 ? null : row.get(manufModelIndex);

                if (serial == null) {
                    rowResult.addMessage(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, rowNumber, headerRow.get(serialIndex)));
                } else if (componentType == null && !command.equals(CMD_DELETE)) {
                    rowResult.addMessage(new ValidationMessage(ErrorMessage.REQUIRED_FIELD_MISSING, rowNumber, headerRow.get(compTypeIndex)));
                }

                final @Nullable DeviceStatus status = setDeviceStatus(statusString, rowNumber, headerRow.get(statusIndex));


                if (!rowResult.isError()) {
                    switch (command) {
                        case CMD_UPDATE:
                            if (deviceEJB.findDeviceBySerialNumber(serial) != null) {
                                final @Nullable ComponentType compType = comptypeEJB.findByName(componentType);
                                if (compType == null) {
                                    rowResult.addMessage(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, rowNumber, headerRow.get(compTypeIndex)));
                                    continue;
                                } else {
                                    try {
                                        final Device deviceToUpdate = deviceEJB.findDeviceBySerialNumber(serial);
                                        addOrUpdateDevice(deviceToUpdate, compType, description, status, manufSerial, location, purchaseOrder, asmPosition, asmDescription, manufacturer, manufModel);
                                        addOrUpdateProperties(deviceToUpdate, indexByPropertyName, row, rowNumber);
                                    } catch (EJBTransactionRolledbackException e) {
                                        handleLoadingError(LOGGER, e, rowNumber, headerRow);
                                        // cannot continue when the transaction is already rolled back
                                        break fileProcessing;
                                    }
                                }
                            } else {
                                final @Nullable ComponentType compType = comptypeEJB.findByName(componentType);
                                if (compType == null) {
                                    rowResult.addMessage(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, rowNumber, headerRow.get(compTypeIndex)));
                                    continue;
                                } else {
                                    try {
                                        final Device newDevice = new Device(serial);
                                        addOrUpdateDevice(newDevice, compType, description, status, manufSerial, location, purchaseOrder, asmPosition, asmDescription, manufacturer, manufModel);
                                        deviceEJB.addDeviceAndPropertyDefs(newDevice);
                                        addOrUpdateProperties(newDevice, indexByPropertyName, row, rowNumber);
                                    } catch (EJBTransactionRolledbackException e) {
                                        handleLoadingError(LOGGER, e, rowNumber, headerRow);
                                        // cannot continue when the transaction is already rolled back
                                        break fileProcessing;
                                    }
                                }
                            }
                            break;
                        case CMD_DELETE:
                            final @Nullable Device deviceToDelete = deviceEJB.findDeviceBySerialNumber(serial);
                            if (deviceToDelete == null) {
                                rowResult.addMessage(new ValidationMessage(ErrorMessage.ENTITY_NOT_FOUND, rowNumber, headerRow.get(serialIndex)));
                            } else {
                                try {
                                    deviceEJB.delete(deviceToDelete);
                                } catch (EJBTransactionRolledbackException e) {
                                    handleLoadingError(LOGGER, e, rowNumber, headerRow);
                                    // cannot continue when the transaction is already rolled back
                                    break fileProcessing;
                                }
                            }
                            break;
                        default:
                            rowResult.addMessage(new ValidationMessage(ErrorMessage.COMMAND_NOT_VALID, rowNumber, headerRow.get(commandIndex)));
                    }
                }
            }
        }
        loaderResult.addResult(rowResult);
        return loaderResult;
    }

    private void addOrUpdateDevice(Device device, ComponentType compType, String description, DeviceStatus status, String manufSerial, String location, String purchaseOrder, String asmPosition, String asmDescription, String manufacturer, String manufModel) {
        device.setModifiedAt(new Date());
        device.setComponentType(compType);
        device.setDescription(description);
        device.setAssemblyPosition(asmPosition);
        device.setStatus(status);
        device.setManufacturer(manufacturer);
        device.setManufacturerModel(manufModel);
        device.setManufacturerSerialNumber(manufSerial);
        device.setAssemblyDescription(asmDescription);
        device.setLocation(location);
        device.setPurchaseOrder(purchaseOrder);
    }

    @Override protected void setUpIndexesForFields(List<String> header) {
        final String rowNumber = header.get(0);
        descriptionIndex = header.indexOf("DESCRIPTION");
        compTypeIndex = header.indexOf("CTYPE");
        serialIndex = header.indexOf("SERIAL");
        manufSerialIndex = header.indexOf("MANUF-SERIAL");
        statusIndex = header.indexOf("STATUS");
        locationIndex = header.indexOf("LOCATION");
        purchaseOrderIndex = header.indexOf("PURCHASE-ORDER");
        manufacturerIndex = header.indexOf("MANUFACTURER");
        asmPositionIndex = header.indexOf("ASM-POSITION");
        manufModelIndex = header.indexOf("MANUF-MODEL");
        asmDescriptionIndex = header.indexOf("ASM-DESCRIPTION");

        rowResult = new DataLoaderResult();
        if (serialIndex == -1) {
            rowResult.addMessage(new ValidationMessage(ErrorMessage.HEADER_FIELD_MISSING, rowNumber, "SERIAL"));
        } else if (compTypeIndex == -1) {
            rowResult.addMessage(new ValidationMessage(ErrorMessage.HEADER_FIELD_MISSING, rowNumber, "CTYPE"));
        }
    }

    private void checkPropertyAssociation(Map<String, Integer> properties, String rowNumber) {
        final Iterator<String> propertiesIterator = properties.keySet().iterator();
        while (propertiesIterator.hasNext()) {
            final String propertyName = propertiesIterator.next();
            final @Nullable Property property = propertyEJB.findByName(propertyName);
            if (property == null) {
                rowResult.addMessage(new ValidationMessage(ErrorMessage.PROPERTY_NOT_FOUND, rowNumber, propertyName));
            } else {
                if (!property.isDeviceAssociation()) {
                    rowResult.addMessage(new ValidationMessage(ErrorMessage.PROPERTY_ASSOCIATION_FAILURE, rowNumber, propertyName));
                }
            }
        }
    }

    private DeviceStatus setDeviceStatus(@Nullable String deviceStatusString, String rowNumber, String columnName) {
        final DeviceStatus deviceStatus;
        if (deviceStatusString == null) {
            deviceStatus = null;
        } else if (deviceStatusString.equalsIgnoreCase(DeviceStatus.IN_FABRICATION.name())) {
            deviceStatus = DeviceStatus.IN_FABRICATION;
        } else if (deviceStatusString.equalsIgnoreCase(DeviceStatus.READY.name())) {
            deviceStatus = DeviceStatus.READY;
        } else if (deviceStatusString.equalsIgnoreCase(DeviceStatus.SPARE.name())) {
            deviceStatus = DeviceStatus.SPARE;
        } else if (deviceStatusString.equalsIgnoreCase(DeviceStatus.UNDER_REPAIR.name())) {
            deviceStatus = DeviceStatus.UNDER_REPAIR;
        } else if (deviceStatusString.equalsIgnoreCase(DeviceStatus.UNDER_TESTING.name())) {
            deviceStatus = DeviceStatus.UNDER_TESTING;
        } else {
            rowResult.addMessage(new ValidationMessage(ErrorMessage.DEVICE_STATUS_NOT_FOUND, rowNumber, columnName));
            deviceStatus = null;
        }
        return deviceStatus;
    }

    private void addOrUpdateProperties(Device device, Map<String, Integer> properties, List<String> row, String rowNumber) {
        final List<DevicePropertyValue> deviceProperties = new ArrayList<>();
        if (device.getDevicePropertyList() != null) {
            deviceProperties.addAll(device.getDevicePropertyList());
        }
        final Map<Property, DevicePropertyValue> devicePropertyByProperty = new HashMap<>();

        for (DevicePropertyValue deviceProperty : deviceProperties) {
            devicePropertyByProperty.put(deviceProperty.getProperty(), deviceProperty);
        }

        for (Entry<String, Integer> entry : properties.entrySet()) {
            final String propertyName = entry.getKey();
            final int propertyIndex = entry.getValue();
            final @Nullable Property property = propertyEJB.findByName(propertyName);
            final @Nullable String propertyValue = row.get(propertyIndex);
            if (devicePropertyByProperty.containsKey(property)) {
                final DevicePropertyValue devicePropertyToUpdate = devicePropertyByProperty.get(property);

                if (propertyValue == null) {
                    deviceEJB.deleteChild(devicePropertyToUpdate);
                } else {
                    devicePropertyToUpdate.setPropValue(Conversion.stringToValue(propertyValue, property.getDataType()));
                    deviceEJB.saveChild(devicePropertyToUpdate);
                }

            } else if (propertyValue != null) {
                final DevicePropertyValue devicePropertyToAdd = new DevicePropertyValue(false);
                devicePropertyToAdd.setProperty(property);
                devicePropertyToAdd.setPropValue(Conversion.stringToValue(propertyValue, property.getDataType()));
                devicePropertyToAdd.setDevice(device);
                deviceEJB.addChild(devicePropertyToAdd);
            }
        }
    }
}
