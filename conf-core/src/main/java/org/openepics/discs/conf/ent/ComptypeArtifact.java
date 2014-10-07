package org.openepics.discs.conf.ent;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An {@link Artifact} used in {@link ComponentType} entities
 *
 * @author vuppala
 */
@Entity
@Table(name = "comptype_artifact")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ComptypeArtifact.findAll", query = "SELECT c FROM ComptypeArtifact c"),
    @NamedQuery(name = "ComptypeArtifact.findByArtifactId", query = "SELECT c FROM ComptypeArtifact c "
            + "WHERE c.id = :id"),
    @NamedQuery(name = "ComptypeArtifact.findByName", query = "SELECT c FROM ComptypeArtifact c WHERE c.name = :name"),
    @NamedQuery(name = "ComptypeArtifact.findByIsInternal", query = "SELECT c FROM ComptypeArtifact c "
            + "WHERE c.isInternal = :isInternal"),
    @NamedQuery(name = "ComptypeArtifact.findByModifiedBy", query = "SELECT c FROM ComptypeArtifact c "
            + "WHERE c.modifiedBy = :modifiedBy")
})
public class ComptypeArtifact extends Artifact {
    @JoinColumn(name = "component_type")
    @ManyToOne(optional = false)
    private ComponentType componentType;

    public ComptypeArtifact() { }

    public ComptypeArtifact(String name, boolean isInternal, String description, String uri) {
        super(name, isInternal, description, uri);
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
