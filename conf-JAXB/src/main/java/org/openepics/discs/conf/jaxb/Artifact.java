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
package org.openepics.discs.conf.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is data transfer object representing a CCDB artifact for JSON and XML serialization.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@XmlRootElement(name = "artifact")
@XmlAccessorType(XmlAccessType.FIELD)
public class Artifact {
    private String name;
    private boolean isURI;
    private String description;
    private String uri;

    /** Constructs empty Artifact. */
    public Artifact() {
    }

    /**
     * Constructs a new Artifact from conf.ent.Artifact.
     *
     * @param entityArtifact
     */
    public Artifact(org.openepics.discs.conf.ent.Artifact entityArtifact) {
        this.name = entityArtifact.getName();
        this.description = entityArtifact.getDescription();
        this.isURI = !entityArtifact.isInternal();
        if (!entityArtifact.isInternal()) {
            this.uri = entityArtifact.getUri();
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isURI() { return isURI; }
    public void setURI(boolean isURI) { this.isURI = isURI; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
}
