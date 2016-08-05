/*
 * Copyright (c) 2016 European Spallation Source
 * Copyright (c) 2016 Cosylab d.d.
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
package org.openepics.discs.conf.jaxb.lists;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.openepics.discs.conf.jaxb.InstallationSlotName;

@XmlRootElement(name = "names")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstallationSlotNameList {

    @XmlElement(name = "name")
    private final List<InstallationSlotName> names;

    public InstallationSlotNameList() { this.names = new ArrayList<>(); }
    public InstallationSlotNameList(List<InstallationSlotName> names) { this.names = names; }

    public List<InstallationSlotName> getNames() { return names; }
}
