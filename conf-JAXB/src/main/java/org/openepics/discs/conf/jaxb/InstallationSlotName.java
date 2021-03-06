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
package org.openepics.discs.conf.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.openepics.discs.conf.ent.Slot;

/**
 * This is data transfer object representing a CCDB installation slot for JSON and XML serialization. This object only carries
 * basic information, like slot name.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@XmlRootElement(name = "name")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstallationSlotName {
    @XmlValue
    private String name;

    @XmlAttribute(name = "type")
    private SlotType slotType;

    public InstallationSlotName() {}

    public InstallationSlotName(Slot slot) {
        name = slot.getName();
        slotType = slot.isHostingSlot() ? SlotType.SLOT : SlotType.CONTAINER;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SlotType getSlotType() { return slotType; }
    public void setSlotType(SlotType slotType) { this.slotType = slotType; }
}


