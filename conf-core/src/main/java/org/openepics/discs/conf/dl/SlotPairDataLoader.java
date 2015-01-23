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
package org.openepics.discs.conf.dl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.openepics.discs.conf.dl.common.AbstractDataLoader;
import org.openepics.discs.conf.dl.common.DataLoader;
import org.openepics.discs.conf.dl.common.DataLoaderResult;
import org.openepics.discs.conf.dl.common.ErrorMessage;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ejb.SlotPairEJB;
import org.openepics.discs.conf.ejb.SlotRelationEJB;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotRelation;
import org.openepics.discs.conf.ent.SlotRelationName;

/**
 * Implementation of data loader for slot pairs.
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 * @author Miroslav Pavleski <miroslav.pavleski@cosylab.com>
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
@Stateless
@SlotPairDataLoaderQualifier
public class SlotPairDataLoader extends AbstractDataLoader implements DataLoader {

    private static final Logger LOGGER = Logger.getLogger(SlotPairDataLoader.class.getCanonicalName());

    private static final String HDR_RELATION = "RELATION";
    private static final String HDR_PARENT = "PARENT";
    private static final String HDR_CHILD= "CHILD";

    private static final List<String> KNOWN_COLUMNS = Arrays.asList(HDR_RELATION, HDR_PARENT, HDR_CHILD);
    private static final Set<String> REQUIRED_COLUMNS = new HashSet<>(KNOWN_COLUMNS);

    private String relationString, parentString, childString;
    private List<Slot> childrenSlots;
    private Slot parentSlot;
    private List<Slot> newSlots;
    private Set<Slot> newSlotPairChildren;
    private SlotRelationName slotRelationName;
    private SlotRelation slotRelation;

    @Inject private SlotEJB slotEJB;
    @Inject private SlotPairEJB slotPairEJB;
    @Inject private SlotRelationEJB slotRelationEJB;

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {
        super.init();

        newSlots = (List<Slot>) getFromContext(DataLoaderResult.CTX_NEW_SLOTS);
        newSlotPairChildren = new HashSet<>();
        result.getContextualData().put(DataLoaderResult.CTX_NEW_SLOT_PAIR_CHILDREN, newSlotPairChildren);
    }

    @Override
    protected List<String> getKnownColumnNames() {
        return KNOWN_COLUMNS;
    }

    @Override
    protected Set<String> getRequiredColumnNames() {
        return REQUIRED_COLUMNS;
    }

    @Override
    protected String getUniqueColumnName() {
        // No unique column name
        return null;
    }

    @Override
    protected void assignMembersForCurrentRow() {
        relationString = readCurrentRowCellForHeader(HDR_RELATION);
        parentString = readCurrentRowCellForHeader(HDR_PARENT);
        childString = readCurrentRowCellForHeader(HDR_CHILD);

        childrenSlots = slotEJB.findSlotByNameContainingString(childString);
        parentSlot = slotEJB.findByName(parentString);

        if (childrenSlots == null || childrenSlots.isEmpty()) {
            result.addRowMessage(ErrorMessage.ENTITY_NOT_FOUND, HDR_CHILD);
        }

        if (parentSlot == null) {
            result.addRowMessage(ErrorMessage.ENTITY_NOT_FOUND, HDR_PARENT);
        }

        if (SlotRelationName.CONTAINS.name().equalsIgnoreCase(relationString)) {
            slotRelationName = SlotRelationName.CONTAINS;
        } else if (SlotRelationName.POWERS.name().equalsIgnoreCase(relationString)) {
            slotRelationName = SlotRelationName.POWERS;
        } else if (SlotRelationName.CONTROLS.name().equalsIgnoreCase(relationString)) {
            slotRelationName = SlotRelationName.CONTROLS;
        } else {
            slotRelationName = null;
            result.addRowMessage(ErrorMessage.UNKNOWN_SLOT_RELATION_TYPE, HDR_RELATION);
        }

        if (result.isRowError()) {
            return;
        }

        slotRelation = slotRelationEJB.findBySlotRelationName(slotRelationName);
    }

    @Override
    protected void handleUpdate() {
        if (newSlots == null) {
            return;
        }
        for (Slot childSlot : childrenSlots) {
            if (newSlots.contains(childSlot)) {
                try {
                    if (!childSlot.getName().equals(parentSlot.getName())) {
                        if (slotRelation.getName() == SlotRelationName.CONTAINS) {
                            if (childSlot.getComponentType().getName().equalsIgnoreCase(SlotEJB.ROOT_COMPONENT_TYPE)) {
                                result.addRowMessage(ErrorMessage.CANT_ADD_PARENT_TO_ROOT);
                                continue;
                            } else if (parentSlot.isHostingSlot() && !childSlot.isHostingSlot()) {
                                result.addRowMessage(ErrorMessage.INSTALL_CANT_CONTAIN_CONTAINER);
                                continue;
                            } else {
                                final SlotPair newSlotPair = new SlotPair(childSlot, parentSlot, slotRelation);
                                slotPairEJB.add(newSlotPair);
                                newSlotPairChildren.add(newSlotPair.getChildSlot());
                            }
                        } else if (slotRelation.getName() == SlotRelationName.POWERS) {
                            if (childSlot.isHostingSlot() && parentSlot.isHostingSlot()) {
                                slotPairEJB.add(new SlotPair(childSlot, parentSlot, slotRelation));
                            } else {
                                result.addRowMessage(ErrorMessage.POWER_RELATIONSHIP_RESTRICTIONS);
                                continue;
                            }
                        } else if (slotRelation.getName() == SlotRelationName.CONTROLS) {
                            if (childSlot.isHostingSlot() && parentSlot.isHostingSlot()) {
                                slotPairEJB.add(new SlotPair(childSlot, parentSlot, slotRelation));
                            } else {
                                result.addRowMessage(ErrorMessage.CONTROL_RELATIONSHIP_RESTRICTIONS);
                                continue;
                            }
                        }
                    } else {
                        result.addRowMessage(ErrorMessage.SAME_CHILD_AND_PARENT);
                    }
                } catch (EJBTransactionRolledbackException e) {
                    handleLoadingError(LOGGER, e);
                }
            }
        }
    }

    @Override
    protected void handleDelete() {
        final List<SlotPair> slotPairs = slotPairEJB.findSlotPairsByParentChildRelation(childString, parentString,
                slotRelationName);
        if (!slotPairs.isEmpty()) {
            try {
                for (SlotPair slotPair : slotPairs) {
                    slotPairEJB.delete(slotPair);
                }
            } catch (EJBTransactionRolledbackException e) {
                handleLoadingError(LOGGER, e);
            }
        } else {
            result.addRowMessage(ErrorMessage.ENTITY_NOT_FOUND);
        }
    }

    @Override
    protected void handleRename() {
        result.addRowMessage(ErrorMessage.COMMAND_NOT_VALID, CMD_HEADER);
    }
}