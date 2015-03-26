/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openepics.discs.conf.ejb.DataTypeEJB;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ejb.SlotPairEJB;
import org.openepics.discs.conf.ejb.SlotRelationEJB;
import org.openepics.discs.conf.ent.ComptypeArtifact;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.DataType;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.DeviceArtifact;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.PositionInformation;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotArtifact;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.SlotRelation;
import org.openepics.discs.conf.ent.Tag;
import org.openepics.discs.conf.ui.common.UIException;
import org.openepics.discs.conf.util.BuiltInDataType;
import org.openepics.discs.conf.util.Utility;
import org.openepics.discs.conf.views.BuiltInProperty;
import org.openepics.discs.conf.views.EntityAttributeView;
import org.openepics.discs.conf.views.EntityAttributeViewKind;
import org.openepics.discs.conf.views.SlotBuiltInPropertyName;
import org.openepics.discs.conf.views.SlotRelationshipView;
import org.openepics.discs.conf.views.SlotView;
import org.primefaces.model.TreeNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

/**
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
@Named
@ViewScoped
public class HierarchiesController implements Serializable {
    private static final long serialVersionUID = 2743408661782529373L;

    private static final Logger LOGGER = Logger.getLogger(HierarchiesController.class.getCanonicalName());

    @Inject private SlotsTreeBuilder slotsTreeBuilder;
    @Inject private transient SlotEJB slotEJB;
    @Inject private transient SlotPairEJB slotPairEJB;
    @Inject private transient InstallationEJB installationEJB;
    @Inject private transient DataTypeEJB dataTypeEJB;
    @Inject private transient SlotRelationEJB slotRelationEJB;

    private transient List<EntityAttributeView> attributes;
    private transient List<EntityAttributeView> filteredAttributes;
    private transient List<SelectItem> attributeKinds;
    private transient List<SlotRelationshipView> relationships;
    private transient List<SlotRelationshipView> filteredRelationships;
    private transient List<SelectItem> relationshipTypes;
    private List<Device> uninstalledDevices;
    private List<Device> filteredUninstalledDevices;
    private TreeNode rootNode;
    private TreeNode selectedNode;
    private InstallationRecord installationRecord;
    private Device deviceToInstall;
    private Slot selectedSlot;
    protected DataType strDataType;
    protected DataType dblDataType;

    /** Java EE post construct life-cycle method. */
    @PostConstruct
    public void init() {
        try {
            rootNode = slotsTreeBuilder.newSlotsTree(slotEJB.findAll(), null, true);
            strDataType = dataTypeEJB.findByName(BuiltInDataType.STR_NAME);
            dblDataType = dataTypeEJB.findByName(BuiltInDataType.DBL_NAME);
            attributeKinds = Utility.buildAttributeKinds();
            relationshipTypes = buildRelationshipTypeList();
        } catch(Exception e) {
            throw new UIException("Hierarchies display initialization fialed: " + e.getMessage(), e);
        }
    }

    private void initAttributeList() {
        final List<EntityAttributeView> attributesList = new ArrayList<>();

        if (selectedNode != null) {
            addBuiltInProperties(attributesList);
            addPropertyValues(attributesList);
            addArtifacts(attributesList);
            addTags(attributesList);
        }
        this.attributes = attributesList;
    }

    private void addBuiltInProperties(List<EntityAttributeView> attributesList) {
        final boolean isHostingSlot = selectedSlot.isHostingSlot();

        attributesList.add(new EntityAttributeView(new BuiltInProperty(SlotBuiltInPropertyName.BIP_DESCRIPTION,
                                        selectedSlot.getDescription(), strDataType)));
        if (isHostingSlot) {
            attributesList.add(new EntityAttributeView(
                                        new BuiltInProperty(SlotBuiltInPropertyName.BIP_BEAMLINE_POS,
                                                selectedSlot.getBeamlinePosition(), dblDataType)));
            final PositionInformation slotPosition = selectedSlot.getPositionInformation();
            attributesList.add(new EntityAttributeView(new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_X,
                                                                slotPosition.getGlobalX(), dblDataType)));
            attributesList.add(new EntityAttributeView(new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_Y,
                                                                slotPosition.getGlobalY(), dblDataType)));
            attributesList.add(new EntityAttributeView(new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_Z,
                                                                slotPosition.getGlobalZ(), dblDataType)));
            attributesList.add(new EntityAttributeView(
                                        new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_PITCH,
                                                slotPosition.getGlobalPitch(), dblDataType)));
            attributesList.add(new EntityAttributeView(
                                        new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_ROLL,
                                                slotPosition.getGlobalRoll(), dblDataType)));
            attributesList.add(new EntityAttributeView(new BuiltInProperty(SlotBuiltInPropertyName.BIP_GLOBAL_YAW,
                                                                slotPosition.getGlobalYaw(), dblDataType)));
        }

    }

    private void addPropertyValues(List<EntityAttributeView> attributesList) {
        final boolean isHostingSlot = selectedSlot.isHostingSlot();
        final Device installedDevice = getInstalledDevice();

        for (ComptypePropertyValue value : selectedSlot.getComponentType().getComptypePropertyList()) {
            if (!value.isPropertyDefinition()) {
                attributesList.add(new EntityAttributeView(value, EntityAttributeViewKind.DEVICE_TYPE_PROPERTY));
            }
        }

        for (SlotPropertyValue value : selectedSlot.getSlotPropertyList()) {
            attributesList.add(new EntityAttributeView(value, isHostingSlot
                                                            ? EntityAttributeViewKind.INSTALL_SLOT_PROPERTY
                                                            : EntityAttributeViewKind.CONTAINER_SLOT_PROPERTY));
        }

        if (installedDevice != null) {
            for (DevicePropertyValue devicePropertyValue : installedDevice.getDevicePropertyList()) {
                attributesList.add(new EntityAttributeView(devicePropertyValue,
                                                            EntityAttributeViewKind.DEVICE_PROPERTY));
            }
        }
    }

    private void addArtifacts(List<EntityAttributeView> attributesList) {
        final boolean isHostingSlot = selectedSlot.isHostingSlot();
        final Device installedDevice = getInstalledDevice();

        for (ComptypeArtifact artifact : selectedSlot.getComponentType().getComptypeArtifactList()) {
            attributesList.add(new EntityAttributeView(artifact, EntityAttributeViewKind.DEVICE_TYPE_ARTIFACT));
        }

        for (SlotArtifact artifact : selectedSlot.getSlotArtifactList()) {
            attributesList.add(new EntityAttributeView(artifact, isHostingSlot
                                                            ? EntityAttributeViewKind.INSTALL_SLOT_ARTIFACT
                                                            : EntityAttributeViewKind.CONTAINER_SLOT_ARTIFACT));
        }

        if (installedDevice != null) {
            for (DeviceArtifact deviceArtifact : installedDevice.getDeviceArtifactList()) {
                attributesList.add(new EntityAttributeView(deviceArtifact,
                                                            EntityAttributeViewKind.DEVICE_ARTIFACT));
            }
        }
    }

    private void addTags(List<EntityAttributeView> attributesList) {
        final boolean isHostingSlot = selectedSlot.isHostingSlot();
        final Device installedDevice = getInstalledDevice();

        for (Tag tag : selectedSlot.getComponentType().getTags()) {
            attributesList.add(new EntityAttributeView(tag, EntityAttributeViewKind.DEVICE_TYPE_TAG));
        }

        for (Tag tag : selectedSlot.getTags()) {
            attributesList.add(new EntityAttributeView(tag, isHostingSlot
                                                            ? EntityAttributeViewKind.INSTALL_SLOT_TAG
                                                            : EntityAttributeViewKind.CONTAINER_SLOT_TAG));
        }

        if (installedDevice != null) {
            for (Tag tag : installedDevice.getTags()) {
                attributesList.add(new EntityAttributeView(tag, EntityAttributeViewKind.DEVICE_TAG));
            }
        }
    }

    private void initRelationshipList() {
        relationships = Lists.newArrayList();

        if (selectedNode != null) {
            final Slot rootSlot = slotEJB.getRootNode();
            final List<SlotPair> slotPairs = slotPairEJB.getSlotRleations(selectedSlot);

            for (SlotPair slotPair : slotPairs) {
                if (!slotPair.getParentSlot().equals(rootSlot)) {
                    relationships.add(new SlotRelationshipView(slotPair, selectedSlot));
                }
            }
        }
    }

    /** Clears the attribute list for display when user deselects the slot in the hierarchy. */
    public void clearAttributeList() {
        attributes = null;
        relationships = null;
    }

    /**
     * @return The list of attributes (property values, artifacts and tags) for at all levels:
     * <ul>
     * <li>device type properties</li>
     * <li>container or installation slot properties</li>
     * </ul>
     */
    public List<EntityAttributeView> getAttributes() {
        return attributes;
    }

    public void setAttrbutes(List<EntityAttributeView> attributes) {
        this.attributes = attributes;
    }

    /** @return The slot (container or installation slot) that is currently selected in the tree. */
    public Slot getSelectedNodeSlot() {
        return selectedSlot;
    }

    /** @return The list of relationships for the currently selected slot. */
    public List<SlotRelationshipView> getRelationships() {
        return relationships;
    }
    public void setRelationships(List<SlotRelationshipView> relationships) {
        this.relationships = relationships;
    }


    /** @return The root node of (and consequently the entire) hierarchy tree. */
    public TreeNode getRootNode() {
        return rootNode;
    }

    /** @return Getter for the currently selected node in a tree (required by PrimeFaces). */
    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    /** With this method PrimeFaces sets the currently selected node. The method has two side effects:
     * <ul>
     * <li>it also sets the container or installation slot associated with the currently selected tree node</li>
     * <li>it also searches id there is an installation record associated with the currently selected tree node</li>
     * </ul>
     * @param selectedNode The PrimeFaces tree node to that is selected.
     */
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
        this.selectedSlot = selectedNode == null ? null : ((SlotView)selectedNode.getData()).getSlot();
        this.installationRecord = selectedNode == null ? null
                                    : installationEJB.getActiveInstallationRecordForSlot(selectedSlot);
    }

    /**
     * @return The device that is installed in the currently selected slot, <code>null</code> if no device is installed
     * or this is a <i>container</i> or no node is selected.
     */
    public Device getInstalledDevice() {
        return installationRecord == null ? null : installationRecord.getDevice();
    }

    /**
     * @return The latest installation record associated with the selected installation slot, <code>null</code> if a
     * container is selected.
     */
    public InstallationRecord getInstallationRecord() {
        return installationRecord;
    }

    public void prepareUninstalledDevices() {
        uninstalledDevices = (selectedSlot == null) || !selectedSlot.isHostingSlot() ? null
                : installationEJB.getUninstalledDevices(selectedSlot.getComponentType());
        filteredUninstalledDevices = null;
    }

    /**
     * @return Based on the device type of the currently selected slot, this returns a list of all appropriate device
     * instances that are not installed.
     */
    public List<Device> getUninstalledDevices() {
        return uninstalledDevices;
    }

    public void setUninstalledDevices(List<Device> uninstalledDevices) {
        this.uninstalledDevices = uninstalledDevices;
    }

    /** @return the filteredUninstalledDevices */
    public List<Device> getFilteredUninstalledDevices() {
        return filteredUninstalledDevices;
    }

    /** @param filteredUninstalledDevices the filteredUninstalledDevices to set */
    public void setFilteredUninstalledDevices(List<Device> filteredUninstalledDevices) {
        this.filteredUninstalledDevices = filteredUninstalledDevices;
    }

    /** @return the deviceToInstall */
    public Device getDeviceToInstall() {
        return deviceToInstall;
    }

    /** @param deviceToInstall the deviceToInstall to set */
    public void setDeviceToInstall(Device deviceToInstall) {
        this.deviceToInstall = deviceToInstall;
    }

    /**
     * This method creates a new installation record for the device selected in the installation dialog. Before that
     * it checks whether a device slot and device are both selected, and that both are also uninstalled at the moment.
     * The checks are performed in the EJB.
     * This method creates a new installation record containing:
     * <ul>
     * <li>record number</li>
     * <li>install date</li>
     * <li>device</li>
     * <li>installation slot</li>
     * </ul>
     * The uninstall date is left empty (NULL).
     */
    public void installDevice() {
        final Date today = new Date();
        final InstallationRecord newRecord = new InstallationRecord(Long.toString(today.getTime()), today);
        newRecord.setDevice(deviceToInstall);
        newRecord.setSlot(selectedSlot);
        installationEJB.add(newRecord);

        deviceToInstall = null;
        installationRecord = installationEJB.getActiveInstallationRecordForSlot(selectedSlot);
    }

    /** This method is called when a user presses the "Uninstall" button in the hierarchies view.
     * @param device
     */
    public void uninstallDevice(Device device) {
        Preconditions.checkNotNull(device);
        final InstallationRecord deviceInstallationRecord =
                                                    installationEJB.getActiveInstallationRecordForDevice(device);
        if (deviceInstallationRecord == null) {
            LOGGER.log(Level.WARNING, "The device appears installed, but no active installation record for "
                    + "it could be retrieved. Device db ID: " + device.getId()
                    + ", serial number: " + device.getSerialNumber());
            throw new RuntimeException("No active installation record for the device exists.");
        }
        deviceInstallationRecord.setUninstallDate(new Date());
        installationEJB.save(deviceInstallationRecord);
        // the device is not installed any more. Clear the installation state information.
        this.installationRecord = null;
    }

    /**
     * @return The path to the installation slot the user is currently installing a device into. Used in the
     * "Install device" dialog.
     */
    public String getInstallationSlotPath() {
        final String slotPath = Utility.buildSlotPath(selectedSlot).toString();
        return slotPath.substring(1, slotPath.length() - 1);
    }

    /** The function to select a different node in the TreeTable by clicking on the link in the relationship table.
     * @param id the ID of the slot we want to switch to.
     */
    public void selectNode(Long id) {
        Preconditions.checkNotNull(id);

        final TreeNode nodeToSelect = findNode(id, rootNode);
        if (nodeToSelect != null) {
            selectedNode.setSelected(false);
            nodeToSelect.setSelected(true);
            setSelectedNode(nodeToSelect);
            initSelectedItemLists();
        }
    }

    /** The recursive function to search for the node in the "depth first" order.
     *
     * @param id the database ID of the {@link Slot} we're searching for
     * @param parent the node we're searching at the moment
     * @return The TreeNode containing the {@link Slot} we're looking for or <code>null</code>, if it dies not exist.
     */
    private TreeNode findNode(Long id, TreeNode parent) {
        if (id.equals(((SlotView)parent.getData()).getId())) {
            return parent;
        }
        for (TreeNode child : parent.getChildren()) {
            final TreeNode foundNode = findNode(id, child);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    /** @return the filteredAttributes */
    public List<EntityAttributeView> getFilteredAttributes() {
        return filteredAttributes;
    }

    /** @param filteredAttributes the filteredAttributes to set */
    public void setFilteredAttributes(List<EntityAttributeView> filteredAttributes) {
        this.filteredAttributes = filteredAttributes;
    }

    public List<SelectItem> getAttributeKinds() {
        return attributeKinds;
    }

    /** Prepares the attribute and relationship lists for display when user selects the slot in the hierarchy. */
    public void initSelectedItemLists() {
        initAttributeList();
        initRelationshipList();
    }

    /** @return the {@link List} of relationship types to display in the filter drop down selection. */
    public List<SelectItem> getRelationshipTypes() {
        return relationshipTypes;
    }

    private List<SelectItem> buildRelationshipTypeList() {
        Builder<SelectItem> immutableListBuilder = ImmutableList.builder();
        immutableListBuilder.add(new SelectItem("", "Select one"));

        final List<SlotRelation> slotRelations = slotRelationEJB.findAll();
        for (SlotRelation slotRelation : slotRelations) {
            immutableListBuilder.add(new SelectItem(slotRelation.getNameAsString(), slotRelation.getNameAsString()));
            immutableListBuilder.add(new SelectItem(slotRelation.getIname(), slotRelation.getIname()));
        }

        return immutableListBuilder.build();
    }

    /** @return the filteredRelationships */
    public List<SlotRelationshipView> getFilteredRelationships() {
        return filteredRelationships;
    }

    /** @param filteredRelationships the filteredRelationships to set */
    public void setFilteredRelationships(List<SlotRelationshipView> filteredRelationships) {
        this.filteredRelationships = filteredRelationships;
    }
}
