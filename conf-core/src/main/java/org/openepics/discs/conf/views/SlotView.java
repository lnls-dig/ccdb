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
package org.openepics.discs.conf.views;

import java.io.Serializable;
import java.util.Objects;

import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotRelationName;
import org.openepics.discs.conf.util.CCDBRuntimeException;

/**
 * View of container used to compose and manipulate with container presentation in tree view
 *
 * @author <a href="mailto:andraz.pozar@cosylab.com">Andraž Požar</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class SlotView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private String name;
    private String description;
    private final SlotView parentNode;
    private final boolean isHostingSlot;
    private String deviceTypeName;
    private Device installedDevice;
    private final int order;
    private boolean isFirst;
    private boolean isLast;
    private final SlotEJB slotEJB;
    private String cableNumber;

    /** Simpler constructor, used in the new Hierarchy builder.
     * @param slot the {@link Slot} to create the UI view object for
     * @param parentNode a reference to the SlotView object of a parent in the hierarchy tree
     * @param order the ordinal number of the SlotView object - defines the order in the hierarchy tree
     * @param slotEJB the {@link Slot} Enterprise bean
     */
    public SlotView(Slot slot, SlotView parentNode, int order, SlotEJB slotEJB) {
        this.name = slot.getName();
        this.description = slot.getDescription();
        this.id = slot.getId();
        this.parentNode = parentNode;
        this.isHostingSlot = slot.isHostingSlot();
        this.deviceTypeName = slot.getComponentType().getName();
        this.order = order;
        this.slotEJB = slotEJB;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SlotView getParentNode() {
        return parentNode;
    }

    public Slot getSlot() {
        return slotEJB != null ? slotEJB.findById(id) : null;
    }

    /** This method sets the name and description at the same time as the slot.
     * @param slot the slot
     */
    public void setSlot(Slot slot) {
        name = slot.getName();
        description = slot.getDescription();
        deviceTypeName = slot.getComponentType().getName();
    }

    public boolean isHostingSlot() {
        return isHostingSlot;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public Device getInstalledDevice() {
        return installedDevice;
    }

    public void setInstalledDevice(Device device) {
        this.installedDevice = device;
    }

    public int getOrder() {
        return order;
    }

    public boolean isFirst() {
        return isFirst;
    }
    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isLast() {
        return isLast;
    }
    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    /**
     * @return the cableNumber
     */
    public String getCableNumber() {
        return cableNumber;
    }

    /**
     * @param cableNumber the cableNumber to set
     */
    public void setCableNumber(String cableNumber) {
        this.cableNumber = cableNumber;
    }

    /** @return a {@link SlotPair} of the {@link SlotRelationName#CONTAINS} type to the parent node */
    public SlotPair getParentRelationship() {
        final Slot parentSlot = parentNode.getSlot();
        for (SlotPair relationship : getSlot().getPairsInWhichThisSlotIsAChildList()) {
            if ((relationship.getSlotRelation().getName() == SlotRelationName.CONTAINS)
                    && relationship.getParentSlot().equals(parentSlot)) {
                return relationship;
            }
        }
        throw new CCDBRuntimeException("CONTAINS relationship with parent Slot not found (C, P): (" + name +", " +
                                        parentSlot.getName() + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlotView) {
            SlotView slotView = (SlotView)obj;
            return id.equals(slotView.id) && Objects.equals(parentNode, slotView.parentNode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getName() + "[id=" + getId() + (parentNode != null ? ", parent_id=" + parentNode.getId() : "") + "]";
    }
}
