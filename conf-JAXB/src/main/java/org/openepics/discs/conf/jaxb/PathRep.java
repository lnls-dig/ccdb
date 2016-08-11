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

/**
 * Representation of a sequence of elements (path)
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 */
@XmlRootElement(name = "path")
@XmlAccessorType(XmlAccessType.FIELD)
public class PathRep {
    private String name;  
    
    @XmlElement(name = "element")
    @XmlElementWrapper(name = "elements")
    private List<ElementRep> elements;

    public PathRep() {
    }

    /**
     * From a list of elements
     * 
     * @param name
     * @param elements
     * 
     */
    public PathRep(String name, List<ElementRep> elements) {       
        this.name = name;
        this.elements = elements;     
    }
    
    public String getName() {
        return name;
    }

    public List<ElementRep> getElements() {
        return elements;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setElements(List<ElementRep> elements) {
        this.elements = elements;
    }
}
