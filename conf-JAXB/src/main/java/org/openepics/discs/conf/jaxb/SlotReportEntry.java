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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a sequence of elements (path)
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.FIELD)
public class SlotReportEntry {

    private String slot;
    private String device = null;
    private Long subSlots = 0L;
    private Long installedSlots = 0L;
    private Long approvedSlots = 0L;
    // private Long percentComplete = 0L;

    public SlotReportEntry() {
    }
    
    public SlotReportEntry(String slotName) {
        slot = slotName;
    }

    // getters/setters

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Long getSubSlots() {
        return subSlots;
    }

    public void setSubSlots(Long subSlots) {
        this.subSlots = subSlots;
    }

    public Long getInstalledSlots() {
        return installedSlots;
    }

    public void setInstalledSlots(Long installedSlots) {
        this.installedSlots = installedSlots;
    }

    public Long getApprovedSlots() {
        return approvedSlots;
    }

    public void setApprovedSlots(Long approvedSlots) {
        this.approvedSlots = approvedSlots;
    }
}
