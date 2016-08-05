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
package org.openepics.discs.conf.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import joptsimple.internal.Strings;

import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ejb.InstallationEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.InstallationRecord;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotRelationName;
import org.openepics.discs.conf.jaxb.Artifact;
import org.openepics.discs.conf.jaxb.InstallationSlot;
import org.openepics.discs.conf.jaxb.PropertyValue;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotList;
import org.openepics.discs.conf.jaxrs.InstallationSlotResource;
import org.openepics.discs.conf.util.BlobStore;

/**
 * An implementation of the InstallationSlotResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class InstallationSlotResourceImpl implements InstallationSlotResource, Serializable {
    private static final long serialVersionUID = 3125813190423087085L;

    @Inject private SlotEJB slotEJB;
    @Inject private ComptypeEJB compTypeEJB;
    @Inject private InstallationEJB installationEJB;
    @Inject private BlobStore blobStore;

    @FunctionalInterface
    private interface RelatedSlotExtractor {
        public Slot getRelatedSlot(final SlotPair pair);
    }

    @Override
    public InstallationSlotList getInstallationSlots(String deviceType) {
        // Get all slots
        if ("undefined".equals(deviceType)) {
            return new InstallationSlotList(slotEJB.findAll().stream().
                        filter(slot -> slot!=null && slot.isHostingSlot()).map(slot -> createInstallationSlot(slot)).
                        collect(Collectors.toList()));
        } else {
            // Get them filtered by deviceType
            return new InstallationSlotList(getInstallationSlotsForType(deviceType));
        }
    }

    @Override
    public InstallationSlot getInstallationSlot(String name) {
        final Slot installationSlot = slotEJB.findByName(name);
        if ((installationSlot == null) || !installationSlot.isHostingSlot()) {
            return null;
        }
        return createInstallationSlot(installationSlot);
    }


    @Override
    public Response getAttachment(String name, String fileName) {
        final Slot slot = slotEJB.findByName(name);
        return GetAttachmentResourceBase.getFileForSlot(slot, installationEJB.getActiveInstallationRecordForSlot(slot),
                                                                                            name, fileName, blobStore);
    }

    private List<InstallationSlot> getInstallationSlotsForType(String deviceType) {
        if (Strings.isNullOrEmpty(deviceType)) {
            return new ArrayList<>();
        }

        final ComponentType ct = compTypeEJB.findByName(deviceType);
        if (ct == null) {
            return new ArrayList<>();
        }

        return slotEJB.findByComponentType(ct).stream().
                map(slot -> createInstallationSlot(slot)).
                collect(Collectors.toList());
    }

    private InstallationSlot createInstallationSlot(final Slot slot) {
        if (slot == null) {
            return null;
        }

        final InstallationSlot installationSlot = new InstallationSlot();
        installationSlot.setName(slot.getName());
        installationSlot.setDescription(slot.getDescription());
        installationSlot.setDeviceType(slot.getComponentType().getName());
        installationSlot.setArtifacts(Utils.emptyToNull(getArtifacts(slot)));

        installationSlot.setParents(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAChildList().stream(),
                        SlotRelationName.CONTAINS,
                        pair -> pair.getParentSlot())));
        installationSlot.setChildren(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAParentList().stream(),
                        SlotRelationName.CONTAINS,
                        pair -> pair.getChildSlot())));

        installationSlot.setPoweredBy(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAChildList().stream(),
                        SlotRelationName.POWERS,
                        pair -> pair.getParentSlot())));
        installationSlot.setPowers(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAParentList().stream(),
                        SlotRelationName.POWERS,
                        pair -> pair.getChildSlot())));

        installationSlot.setControlledBy(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAChildList().stream(),
                        SlotRelationName.CONTROLS,
                        pair -> pair.getParentSlot())));
        installationSlot.setControls(Utils.emptyToNull(
                getRelatedSlots(slot.getPairsInWhichThisSlotIsAParentList().stream(),
                        SlotRelationName.CONTROLS,
                        pair -> pair.getChildSlot())));

        installationSlot.setProperties(getPropertyValues(slot));
        return installationSlot;
    }

    private List<String> getRelatedSlots(final Stream<SlotPair> relatedSlotPairs,
            final SlotRelationName relationName,
            final RelatedSlotExtractor extractor) {
        return relatedSlotPairs.
                filter(slotPair -> relationName.equals(slotPair.getSlotRelation().getName())).
                map(relatedSlotPair -> extractor.getRelatedSlot(relatedSlotPair)).
                filter(slot -> slot.isHostingSlot()).
                map(slot -> slot.getName()).
                collect(Collectors.toList());
    }

    private List<PropertyValue> getPropertyValues(final Slot slot) {
        final InstallationRecord record = installationEJB.getActiveInstallationRecordForSlot(slot);

        final Stream<? extends PropertyValue> externalProps = Stream.concat(
                            slot.getComponentType().getComptypePropertyList().stream().
                                filter(propValue -> !propValue.isPropertyDefinition()).
                                map(propValue -> Utils.createPropertyValue(propValue)),
                            record == null ? Stream.empty() :
                                record.getDevice().getDevicePropertyList().stream().
                                    map(propValue -> Utils.createPropertyValue(propValue)));

        return Stream.concat(slot.getSlotPropertyList().stream().map(propValue -> Utils.createPropertyValue(propValue)),
                                externalProps).
                        collect(Collectors.toList());
    }

    private List<Artifact> getArtifacts(final Slot slot) {
        final InstallationRecord record = installationEJB .getLastInstallationRecordForSlot(slot);

        final List<Artifact> externalArtifacts = Utils.getArtifacts(slot.getComponentType());
        if (record != null) {
            externalArtifacts.addAll(Utils.getArtifacts(record.getSlot()));
        }

        final List<Artifact> deviceArtifacts = Utils.getArtifacts(slot);
        deviceArtifacts.addAll(externalArtifacts);

        return deviceArtifacts;
    }
}
