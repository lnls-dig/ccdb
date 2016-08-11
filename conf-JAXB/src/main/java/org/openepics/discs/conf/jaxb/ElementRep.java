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
import org.openepics.discs.ccdb.model.order.Element;

/**
 * Representation of an element
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD)
public class ElementRep {
    private String name;
    private String description;
    private String lattice;
    private String slot;

    public ElementRep() {
    }

    /**
     * From an element
     * 
     * @param e
     * @return 
     */
    public static ElementRep newInstance(Element e) {
        ElementRep elem = new ElementRep();
        
        elem.name = e.getName();
        elem.description = e.getDescription();
        elem.lattice = e.getLattice().getName();
        elem.slot = e.getSlot().getName();
        
        return elem;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLattice() {
        return lattice;
    }

    public void setLattice(String lattice) {
        this.lattice = lattice;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

}
