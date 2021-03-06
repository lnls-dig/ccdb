/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
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
package org.openepics.discs.ccdb.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.Device;

/**
 * This is data transfer object representing a CCDB device type for JSON and XML serialization.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@XmlRootElement(name = "device")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResDevice {
    private String inventoryId;

    @XmlElement private DeviceType deviceType;
    
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyValue> properties;
    
    public ResDevice() { }

    /**
     * New instance from a device
     * 
     * @param device
     * @return 
     */
    public static ResDevice newInstance(Device device) {
        if (device == null) return null;
        
        ResDevice dev = new ResDevice();
        
        dev.inventoryId = device.getSerialNumber();
        // dev.deviceType = device.getComponentType();
        
        return dev;
    }
    // -- getters and setters
    
    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public List<PropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyValue> properties) {
        this.properties = properties;
    }
}
