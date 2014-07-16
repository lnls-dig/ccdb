package org.openepics.discs.conf.ent;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author vuppala
 */
@Entity
@Table(name = "comptype_artifact")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ComptypeArtifact.findAll", query = "SELECT c FROM ComptypeArtifact c"),
    @NamedQuery(name = "ComptypeArtifact.findByArtifactId", query = "SELECT c FROM ComptypeArtifact c WHERE c.id = :id"),
    @NamedQuery(name = "ComptypeArtifact.findByName", query = "SELECT c FROM ComptypeArtifact c WHERE c.name = :name"),
    @NamedQuery(name = "ComptypeArtifact.findByIsInternal", query = "SELECT c FROM ComptypeArtifact c WHERE c.isInternal = :isInternal"),
    @NamedQuery(name = "ComptypeArtifact.findByDescription", query = "SELECT c FROM ComptypeArtifact c WHERE c.description = :description"),
    @NamedQuery(name = "ComptypeArtifact.findByModifiedBy", query = "SELECT c FROM ComptypeArtifact c WHERE c.modifiedBy = :modifiedBy"),
    @NamedQuery(name = "ComptypeArtifact.findByModifiedAt", query = "SELECT c FROM ComptypeArtifact c WHERE c.modifiedAt = :modifiedAt")})
public class ComptypeArtifact extends ConfigurationEntity {
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "name")
    private String name;

    @Basic(optional = false)
    @NotNull
    @Column(name = "is_internal")
    private boolean isInternal;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "description")
    private String description;

    @Basic(optional = false)
    @NotNull
    @Column(name = "uri", columnDefinition="TEXT")
    private String uri;

    @JoinColumn(name = "component_type", referencedColumnName = "component_type_id")
    @ManyToOne(optional = false)
    private ComponentType componentType;

    public ComptypeArtifact() {
    }

    public ComptypeArtifact(String name, boolean isInternal, String description, String uri, String modifiedBy) {
        this.name = name;
        this.isInternal = isInternal;
        this.description = description;
        this.uri = uri;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    @Override
    public String toString() {
        return "ComptypeArtifact[ artifactId=" + id + " ]";
    }

}
