package org.openepics.discs.conf.ent;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author vuppala
 */
@Entity
@Table(name = "slot")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Slot.findAll", query = "SELECT s FROM Slot s"),
    @NamedQuery(name = "Slot.findBySlotId", query = "SELECT s FROM Slot s WHERE s.id = :id"),
    @NamedQuery(name = "Slot.findByName", query = "SELECT s FROM Slot s WHERE s.name = :name"),
    @NamedQuery(name = "Slot.findByNameContaining", query = "SELECT s FROM Slot s WHERE s.name LIKE :name"),
    @NamedQuery(name = "Slot.findByIsHostingSlot", query = "SELECT s FROM Slot s WHERE s.isHostingSlot = :isHostingSlot"),
    @NamedQuery(name = "Slot.findByBeamlinePosition", query = "SELECT s FROM Slot s WHERE s.beamlinePosition = :beamlinePosition"),
    @NamedQuery(name = "Slot.findByModifiedBy", query = "SELECT s FROM Slot s WHERE s.modifiedBy = :modifiedBy")})
public class Slot extends ConfigurationEntity {
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Basic(optional = false)
    @NotNull
    @Column(name = "is_hosting_slot")
    private boolean isHostingSlot;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "beamline_position")
    private Double beamlinePosition;

    @Column(name = "global_x")
    private Double globalX;

    @Column(name = "global_y")
    private Double globalY;

    @Column(name = "global_z")
    private Double globalZ;

    @Column(name = "global_roll")
    private Double globalRoll;

    @Column(name = "global_yaw")
    private Double globalYaw;

    @Column(name = "global_pitch")
    private Double globalPitch;

    @Size(max = 255)
    @Column(name = "asm_comment")
    private String asmComment;

    @Size(max = 16)
    @Column(name = "asm_position")
    private String asmPosition;

    @Size(max = 255)
    @Column(name = "comment")
    private String comment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "slot")
    private List<SlotArtifact> slotArtifactList;

    @JoinColumn(name = "component_type", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ComponentType componentType;

    @OneToMany(mappedBy = "asmSlot")
    private List<Slot> slotList;

    @JoinColumn(name = "asm_slot", referencedColumnName = "id")
    @ManyToOne
    private Slot asmSlot;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "slot")
    private List<AlignmentRecord> alignmentRecordList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "slot")
    private List<InstallationRecord> installationRecordList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "childSlot")
    private List<SlotPair> childrenSlots;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parentSlot")
    private List<SlotPair> parentSlots;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "slot")
    private List<SlotPropertyValue> slotPropertyList;

    @ManyToMany
    @JoinTable(name = "slot_tags",
        joinColumns = { @JoinColumn(name = "slot_id") }, inverseJoinColumns = { @JoinColumn(name = "tag_id") })
    private Set<Tag> tags;


    protected Slot() {
    }

    public Slot(String name, boolean isHostingSlot, String modifiedBy) {
        this.name = name;
        this.isHostingSlot = isHostingSlot;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description;    }
    public void setDescription(String description) { this.description = description; }

    public boolean getIsHostingSlot() { return isHostingSlot; }
    public void setIsHostingSlot(boolean isHostingSlot) { this.isHostingSlot = isHostingSlot; }

    public Double getBeamlinePosition() { return beamlinePosition; }
    public void setBeamlinePosition(Double beamlinePosition) { this.beamlinePosition = beamlinePosition; }

    public Double getGlobalX() { return globalX; }
    public void setGlobalX(Double globalX) { this.globalX = globalX; }

    public Double getGlobalY() { return globalY; }
    public void setGlobalY(Double globalY) { this.globalY = globalY; }

    public Double getGlobalZ() { return globalZ; }
    public void setGlobalZ(Double globalZ) { this.globalZ = globalZ; }

    public Double getGlobalRoll() { return globalRoll; }
    public void setGlobalRoll(Double globalRoll) { this.globalRoll = globalRoll; }

    public Double getGlobalYaw() { return globalYaw; }
    public void setGlobalYaw(Double globalYaw) { this.globalYaw = globalYaw; }

    public Double getGlobalPitch() { return globalPitch; }
    public void setGlobalPitch(Double globalPitch) { this.globalPitch = globalPitch; }

    public String getAssemblyComment() { return asmComment; }
    public void setAssemblyComment(String asmComment) { this.asmComment = asmComment; }

    public String getAssemblyPosition() { return asmPosition; }
    public void setAssemblyPosition(String asmPosition) { this.asmPosition = asmPosition; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @XmlTransient
    public List<SlotArtifact> getSlotArtifactList() { return slotArtifactList; }
    public void setSlotArtifactList(List<SlotArtifact> slotArtifactList) { this.slotArtifactList = slotArtifactList; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    @XmlTransient
    public List<Slot> getSlotList() { return slotList; }
    public void setSlotList(List<Slot> slotList) { this.slotList = slotList; }

    public Slot getAssemblySlot() { return asmSlot; }
    public void setAssemblySlot(Slot asmSlot) { this.asmSlot = asmSlot; }

    @XmlTransient
    public List<AlignmentRecord> getAlignmentRecordList() { return alignmentRecordList; }
    public void setAlignmentRecordList(List<AlignmentRecord> alignmentRecordList) { this.alignmentRecordList = alignmentRecordList; }

    @XmlTransient
    public List<InstallationRecord> getInstallationRecordList() { return installationRecordList; }
    public void setInstallationRecordList(List<InstallationRecord> installationRecordList) { this.installationRecordList = installationRecordList; }

    @XmlTransient
    public List<SlotPair> getChildrenSlotsPairList() { return childrenSlots; }
    public void setSlotPairList(List<SlotPair> slotPairList) { this.childrenSlots = slotPairList; }

    @XmlTransient
    public List<SlotPair> getParentSlotsPairList() { return parentSlots; }
    public void setSlotPairList1(List<SlotPair> slotPairList1) { this.parentSlots = slotPairList1; }

    @XmlTransient
    public List<SlotPropertyValue> getSlotPropertyList() { return slotPropertyList; }
    public void setSlotPropertyList(List<SlotPropertyValue> slotPropertyList) { this.slotPropertyList = slotPropertyList; }

    @XmlTransient
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    @Override
    public String toString() { return "Slot[ slotId=" + id + " ]"; }

}
