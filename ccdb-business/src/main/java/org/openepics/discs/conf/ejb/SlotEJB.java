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
package org.openepics.discs.conf.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.openepics.discs.conf.auditlog.Audit;
import org.openepics.discs.conf.dl.SlotsDataLoader;
import org.openepics.discs.conf.ent.AlignmentArtifact;
import org.openepics.discs.conf.ent.AlignmentPropertyValue;
import org.openepics.discs.conf.ent.Artifact;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.ComptypeArtifact;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.DeviceArtifact;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.EntityTypeOperation;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.PropertyValue;
import org.openepics.discs.conf.ent.PropertyValueUniqueness;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotArtifact;
import org.openepics.discs.conf.ent.SlotPair;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.SlotRelation;
import org.openepics.discs.conf.ent.SlotRelationName;
import org.openepics.discs.conf.ent.values.Value;
import org.openepics.discs.conf.security.Authorized;
import org.openepics.discs.conf.util.BlobStore;
import org.openepics.discs.conf.util.CRUDOperation;
import org.openepics.discs.conf.util.DuplicateNameException;
import org.openepics.discs.conf.util.UnhandledCaseException;

import com.google.common.base.Preconditions;

/**
 * DAO Service for accessing Installation {@link Slot} entities.
 *
 * @author vuppala
 * @author <a href="mailto:miroslav.pavleski@cosylab.com">Miroslav Pavleski</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@Stateless
public class SlotEJB extends DAO<Slot> {
    /** Special {@link ComponentType} name for root components */
    public static final String ROOT_COMPONENT_TYPE = "_ROOT";

    /** Special {@link ComponentType} name for group components */
    public static final String GRP_COMPONENT_TYPE = "_GRP";

    @Inject private SlotPairEJB slotPairEJB;
    @Inject private SlotRelationEJB slotRelationEJB;
    @Inject private ComptypeEJB comptypeEJB;
    @Inject private transient BlobStore blobStore;

    /**
     * Queries database for slots by partial name
     *
     * @param namePart partial name
     * @return {@link List} of slots
     */
    public List<Slot> findSlotByNameContainingString(String namePart) {
        return em.createNamedQuery("Slot.findByNameContaining", Slot.class)
                .setParameter("name", namePart).getResultList();
    }

    /**
     * Retrieves the special implicit root container from the database.
     *
     * @return the implicit root {@link Slot} of the contains hierarchy
     */
    public Slot getRootNode() {
        ComponentType rootComponentType = comptypeEJB.findByName(ROOT_COMPONENT_TYPE);
        return em.createNamedQuery("Slot.findByComponentType", Slot.class)
                .setParameter("componentType", rootComponentType).getSingleResult();
    }

    /**
     * Queries for all parent slots with a {@link SlotRelationName#CONTAINS} of a
     * given component type ( {@link ComponentType} ) name
     *
     * @param compName The {@link ComponentType} name
     * @return {@link List} of slots matching the query
     */
    public List<Slot> relatedChildren(String compName) {
        return em.createQuery("SELECT cp.childSlot FROM SlotPair cp "
                              + "WHERE cp.parentSlot.name = :compname AND cp.slotRelation.name = :relname", Slot.class)
               .setParameter("compname", compName)
               .setParameter("relname", SlotRelationName.CONTAINS).getResultList();
    }

    /**
     * All {@link Slot}s for given {@link ComponentType}
     *
     * @param componentType the {@link ComponentType}
     * @return list of slots with specific {@link ComponentType}
     */
    public List<Slot> findByComponentType(ComponentType componentType) {
        return em.createNamedQuery("Slot.findByComponentType", Slot.class)
                .setParameter("componentType", componentType).getResultList();
    }

    /**
     * All hosting or non-hosting {@link Slot}s.
     *
     * @param isHostingSlot is slot hosting or not
     * @return List of all hosting or non-hosting {@link Slot}s
     */
    public List<Slot> findByIsHostingSlot(boolean isHostingSlot) {
        return em.createNamedQuery("Slot.findByIsHostingSlot", Slot.class)
                .setParameter("isHostingSlot", isHostingSlot).getResultList();
    }

    /**
     * The method takes care of adding a new Slot and all its dependencies in one transaction. Called from Container
     * and Installation slot managed beans.
     *
     * @param newSlot the Container or Installation slot to be added
     * @param parentSlot its parent. <code>null</code> if the container is a new root.
     * @param fromDataLoader is data being imported via {@link SlotsDataLoader}. If it is slot pair should
     *          never be created since it is separately created from data loader.
     */
    @CRUDOperation(operation=EntityTypeOperation.CREATE)
    @Audit
    @Authorized
    public void addSlotToParentWithPropertyDefs(Slot newSlot, @Nullable Slot parentSlot, boolean fromDataLoader) {
        final Slot actualParentSlot = (parentSlot != null) ? parentSlot : getRootNode();
        if (!isNewSlotNameAllowed(newSlot, actualParentSlot)) {
            final String entity = newSlot.isHostingSlot() ? "Installation slot" : "Container";
            throw new DuplicateNameException(entity + " cannot be created, because equally named "
                                                + entity.toLowerCase() + " already exists.");
        }

        super.add(newSlot);
        if (!fromDataLoader) {
            slotPairEJB.addWithoutInterceptors(new SlotPair(newSlot, actualParentSlot,
                        slotRelationEJB.findBySlotRelationName(SlotRelationName.CONTAINS)));
        }

        final List<ComptypePropertyValue> propertyDefinitions =
                                                comptypeEJB.findPropertyDefinitions(newSlot.getComponentType());
        for (ComptypePropertyValue propertyDefinition : propertyDefinitions) {
            if (propertyDefinition.isDefinitionTargetSlot()) {
                final SlotPropertyValue slotPropertyValue = new SlotPropertyValue(false);
                slotPropertyValue.setProperty(propertyDefinition.getProperty());
                slotPropertyValue.setSlot(newSlot);
                addChild(slotPropertyValue);
            }
        }
    }

    private boolean isNewSlotNameAllowed(final Slot newSlot, final Slot parentSlot) {
        if (newSlot.isHostingSlot()) {
            return isInstallationSlotNameUnique(newSlot.getName());
        } else {
            return isContainerNameUnique(newSlot.getName(), parentSlot);
        }
    }

    /**
     * This method removed all needless property values in a single transaction. All the removals are only logged once.
     *
     * @param slot the {@link Slot} to work on
     * @param deleteList property values to delete
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void removePropertyDefinitionsForTypeChange(final Slot slot, final List<SlotPropertyValue> deleteList) {
        // delete all properties marked for removal
        for (SlotPropertyValue propertyValueToDelete : deleteList) {
            deleteChild(propertyValueToDelete);
        }
    }


    /**
     * This adds all the new properties in a single transaction.
     *
     * @param slot the {@link Slot} to work on
     * @param newComponentType the new {@link ComponentType} to add the properties on
     */
    @CRUDOperation(operation=EntityTypeOperation.UPDATE)
    @Audit
    @Authorized
    public void addPropertyDefinitionsForTypeChange(final Slot slot, final ComponentType newComponentType) {
        // add all property values to the new type
        final Slot slotFromDatabase = findById(slot.getId());
        slotFromDatabase.setComponentType(newComponentType);
        final List<SlotPropertyValue> existingPropertyValues = slotFromDatabase.getSlotPropertyList();
        for (ComptypePropertyValue newPropDefinition : newComponentType.getComptypePropertyList()) {
            if (newPropDefinition.isDefinitionTargetSlot()
                    && !isPropertyInParentList(newPropDefinition.getProperty(), existingPropertyValues)) {
                final SlotPropertyValue newPropertyValue = new SlotPropertyValue(false);
                newPropertyValue.setProperty(newPropDefinition.getProperty());
                newPropertyValue.setPropValue(null);
                newPropertyValue.setSlot(slotFromDatabase);
                addChild(newPropertyValue);
                // this is for logging only, since the application is logging the slot, instead the new one
                slot.getSlotPropertyList().add(newPropertyValue);
            }
        }
    }

    private boolean isPropertyInParentList(Property prop, List<SlotPropertyValue> parentPropertyValues) {
        for (SlotPropertyValue propertyValueChild : parentPropertyValues) {
            if (propertyValueChild.getProperty().equals(prop)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param newSlotName the installation slot name to check
     * @return <code>true</code> if the installation slot name is unique, <code>false</code> otherwise
     */
    public boolean isInstallationSlotNameUnique(String newSlotName) {
        return em.createNamedQuery("Slot.findByNameAndHosting", Slot.class).setParameter("name", newSlotName).
                setParameter("isHostingSlot", true).getResultList().isEmpty();
    }

    /**
     * Checks whether a new container can have the requested name.
     *
     * @param newContainerName the name of the container we're about to add to the parent
     * @param parentSlot the parent for the new container
     * @return <code>true</code> if the container's parent does not contain a equally named child,
     * <code>false</code> otherwise.
     */
    public boolean isContainerNameUnique(final String newContainerName, final @Nullable Slot parentSlot) {
        Preconditions.checkNotNull(newContainerName);
        final Slot actualParentSlot = (parentSlot != null) ? parentSlot : getRootNode();
        final String candidateName = newContainerName.toLowerCase();
        final long equalyNamedSiblings = actualParentSlot.getPairsInWhichThisSlotIsAParentList().stream().
                                            filter(e -> e.getChildSlot().getName().toLowerCase().equals(candidateName)).
                                            count();
        return equalyNamedSiblings == 0;
    }

    @Override
    protected Class<Slot> getEntityClass() {
        return Slot.class;
    }

    @Override
    protected boolean isPropertyValueTypeUnique(PropertyValue child, Slot parent) {
        Preconditions.checkNotNull(child);
        Preconditions.checkNotNull(parent);
        final Value value = child.getPropValue();
        if (value == null) {
            return true;
        }
        final List<PropertyValue> results = em.createNamedQuery("SlotPropertyValue.findSamePropertyValueByType",
                                                        PropertyValue.class)
                    .setParameter("componentType", parent.getComponentType())
                    .setParameter("property", child.getProperty())
                    .setParameter("propValue", value).setMaxResults(2).getResultList();
        // value is unique if there is no property value with the same value, or the only one found us the entity itself
        return (results.size() < 2) && (results.isEmpty() || results.get(0).equals(child));
    }

    /**
     * The method takes an instance of the property value ({@link SlotPropertyValue}, {@link ComptypePropertyValue},
     * {@link DevicePropertyValue}, {@link AlignmentPropertyValue}) that has already been persisted (it has a valid ID),
     * and returns a fresh instance of the entity from the database.
     *
     * @param propertyValue the {@link PropertyValue} to refresh. Must be already persisted.
     * @param <T> the actual instance type of the {@link PropertyValue}
     * @return a fresh instance of the property value from the database
     */
    @SuppressWarnings("unchecked")
    public <T extends PropertyValue> T refreshPropertyValue(T propertyValue) {
        Preconditions.checkNotNull(propertyValue);
        Preconditions.checkNotNull(propertyValue.getId());
        final T returnPropertyValue;
        if (propertyValue instanceof SlotPropertyValue) {
            returnPropertyValue = (T) em.find(SlotPropertyValue.class, propertyValue.getId());
        } else if (propertyValue instanceof ComptypePropertyValue) {
            returnPropertyValue = (T) em.find(ComptypePropertyValue.class, propertyValue.getId());
        } else if (propertyValue instanceof DevicePropertyValue) {
            returnPropertyValue = (T) em.find(DevicePropertyValue.class, propertyValue.getId());
        } else if (propertyValue instanceof AlignmentPropertyValue) {
            returnPropertyValue = (T) em.find(AlignmentPropertyValue.class, propertyValue.getId());
        } else {
            throw new UnhandledCaseException();
        }
        return returnPropertyValue;
    }

    /**
     * The method takes an instance of the artifact ({@link SlotArtifact}, {@link ComptypeArtifact},
     * {@link DeviceArtifact}, {@link AlignmentArtifact}) that has already been persisted (it has a valid ID),
     * and returns a fresh instance of the entity from the database.
     *
     * @param artifact the {@link Artifact} to refresh. Must be already persisted.
     * @param <T> the actual instance type of the {@link Artifact}
     * @return a fresh instance of the artifact from the database
     */
    @SuppressWarnings("unchecked")
    public <T extends Artifact> T refreshArtifact(T artifact) {
        Preconditions.checkNotNull(artifact);
        Preconditions.checkNotNull(artifact.getId());
        final T returnPropertyValue;
        if (artifact instanceof SlotArtifact) {
            returnPropertyValue = (T) em.find(SlotArtifact.class, artifact.getId());
        } else if (artifact instanceof ComptypeArtifact) {
            returnPropertyValue = (T) em.find(ComptypeArtifact.class, artifact.getId());
        } else if (artifact instanceof DeviceArtifact) {
            returnPropertyValue = (T) em.find(DeviceArtifact.class, artifact.getId());
        } else if (artifact instanceof AlignmentArtifact) {
            returnPropertyValue = (T) em.find(AlignmentArtifact.class, artifact.getId());
        } else {
            throw new UnhandledCaseException();
        }
        return returnPropertyValue;
    }

    /**
     * This method returns all {@link Slot}s, that are the root of some relationship (do not have any parents).
     *
     * @param relation the relationship to test for
     * @return a {@link List} of {@link Slot}s that are roots of a certain relationship type.
     */
    public List<Slot> findRootSlotsForRelation(final SlotRelation relation) {
        return em.createNamedQuery("SlotPair.findRootSlotsForRelation", Slot.class)
                .setParameter("relation", relation).getResultList();
    }

    public List<Slot> findAllByName(final String name) {
        return em.createNamedQuery("Slot.findByName", Slot.class).setParameter("name", name).getResultList();
    }

    /**
     * Creates a copy of the slots in the <code>sourceSlots</code> under a new <code>parentSlot</code>.
     *
     * @param sourceSlots a {@link List} of {@link Slot}s to copy
     * @param parentSlot a parent to contain the copies
     */
    public void copySlotsToParent(final List<Slot> sourceSlots, final Slot parentSlot) {
        // originalToCopy is a mapping between original slots and their copies.
        // It is used for creating relationship copies.
        final Map<Slot, Slot> originalToCopy = new HashMap<>();
        copySlotsToParent(sourceSlots, parentSlot, originalToCopy);
        createRelationshipCopies(originalToCopy);
    }

    private void copySlotsToParent(final List<Slot> sourceSlots, final Slot parentSlot,
                                                                            final Map<Slot, Slot> originalToCopy) {
        // creates copies recursively
        for (final Slot sourceSlot : sourceSlots) {
            // a slot of the same type but of different name
            final Slot newCopy = createSlotCopy(sourceSlot, parentSlot);
            originalToCopy.put(sourceSlot, newCopy);

            addAttributesToNewCopy(findById(newCopy.getId()), sourceSlot);

            List<Slot> children = sourceSlot.getPairsInWhichThisSlotIsAParentList().stream()
                .filter(sp -> sp.getSlotRelation().getName().equals(SlotRelationName.CONTAINS))
                .map(SlotPair::getChildSlot).collect(Collectors.toList());
            final Slot freshNewCopy = em.merge(newCopy);
            explicitAuditLog(freshNewCopy, EntityTypeOperation.CREATE);
            copySlotsToParent(children, freshNewCopy, originalToCopy);
        }
    }

    /**
     * Creates the relationships between all copies that exist between the original slots. This method only creates
     * relationships where both sides of the relationship are in the copy set.
     *
     * @param originalToCopy a mapping between original slots and their copies.
     */
    private void createRelationshipCopies(final Map<Slot, Slot> originalToCopy) {
        for (final Slot source : originalToCopy.keySet()) {
            // Since both slots need to be in the copy set, it is enough to only search the children list
            // and make sure the parent is in the copy set as well
            final List<SlotPair> relationshipCandidates = source.getPairsInWhichThisSlotIsAChildList().stream().
                                                        filter(e -> originalToCopy.containsKey(e.getParentSlot())).
                                                        collect(Collectors.toList());

            // go through all the candidates, remember the ones for which there is no copied relationship yet
            final List<SlotPair> relationshipsToCopy = new ArrayList<>();
            final Slot relChildCopySlot = findById(originalToCopy.get(source).getId());
            for (final SlotPair relationship : relationshipCandidates) {
                final Slot relParentCopySlot = originalToCopy.get(relationship.getParentSlot());
                final SlotRelationName relName = relationship.getSlotRelation().getName();
                if (!relationshipExists(relChildCopySlot, relParentCopySlot, relName)) {
                    relationshipsToCopy.add(relationship);
                }
            }

            // create copies of the remaining list
            for (final SlotPair relationshipToCopy : relationshipsToCopy) {
                final Slot newRelParent = findById(originalToCopy.get(relationshipToCopy.getParentSlot()).getId());
                slotPairEJB.add(new SlotPair(relChildCopySlot, newRelParent, relationshipToCopy.getSlotRelation()));
            }
        }
    }

    private boolean relationshipExists(final Slot child, final Slot parent, SlotRelationName name) {
        for (SlotPair pair : child.getPairsInWhichThisSlotIsAChildList()) {
            if (pair.getParentSlot().equals(parent) && pair.getSlotRelation().getName() == name) {
                return true;
            }
        }
        return false;
    }

    private Slot createSlotCopy(final Slot source, final Slot parentSlot) {
        final String newName = findNewSlotCopyName(source.getName());
        final Slot newSlot = new Slot(newName, source.isHostingSlot());
        newSlot.setDescription(source.getDescription());
        newSlot.setComponentType(source.getComponentType());
        addSlotToParentWithPropertyDefs(newSlot, parentSlot, false);
        return newSlot;
    }

    private String findNewSlotCopyName(String name) {
        int slotIndex = 1;
        String returnName = "";
        while (returnName.isEmpty()) {
            String candidateName = name + "_" + slotIndex;
            if (findByName(candidateName) == null) {
                return candidateName;
            }
            ++slotIndex;
        }
        return returnName;
    }

    /**
     * This method transfers from the copy source all the attributes that can be copied:
     * <ul>
     * <li>non-unique <b>DEFINED</b> property values</li>
     * <li>tags</li>
     * <li>URL artifacts</li>
     * </ul>
     * It does not copy the attachments, since this may be too heavy.
     *
     * @param newCopy the slot that was just created
     * @param copySource the slot that is the source of the data
     */
    private void addAttributesToNewCopy(final Slot newCopy, final Slot copySource) {
        if (newCopy.isHostingSlot()) {
            // installation slots already have the property value instances, we just need to copy the actual values
            transferValuesFromSource(newCopy, copySource);
        } else {
            // containers can have "free floating" property values. We need to copy them to the newly created containers
            copyValuesFromSource(newCopy, copySource);
        }

        copyArtifactsFromSource(findById(newCopy.getId()), copySource);

        final Slot tagCopy = findById(newCopy.getId());
        tagCopy.getTags().addAll(copySource.getTags());

        save(tagCopy);
    }

    private void transferValuesFromSource(final Slot newCopy, final Slot copySource) {
        for (final SlotPropertyValue pv : newCopy.getSlotPropertyList()) {
            if (pv.getProperty().getValueUniqueness() == PropertyValueUniqueness.NONE) {
                final SlotPropertyValue parentPv = getPropertyValue(copySource, pv.getProperty().getName());
                if (parentPv != null) {
                    pv.setPropValue(parentPv.getPropValue());
                }
            }
        }
    }

    private SlotPropertyValue getPropertyValue(final Slot slot, final String pvName) {
        for (final SlotPropertyValue pv : slot.getSlotPropertyList()) {
            if (pv.getProperty().getName().equals(pvName)) {
                return pv;
            }
        }
        return null;
    }

    private void copyValuesFromSource(final Slot newCopy, final Slot copySource) {
        for (final SlotPropertyValue pv : copySource.getSlotPropertyList()) {
            final SlotPropertyValue targetPv = new SlotPropertyValue(false);
            targetPv.setProperty(pv.getProperty());
            targetPv.setUnit(pv.getUnit());
            if (pv.getProperty().getValueUniqueness() == PropertyValueUniqueness.NONE) {
                targetPv.setPropValue(pv.getPropValue());
            }
            targetPv.setSlot(newCopy);
            addChild(targetPv);
            newCopy.getSlotPropertyList().add(targetPv);
        }
    }

    private void copyArtifactsFromSource(final Slot newCopy, final Slot copySource) {
        for (final SlotArtifact artifact : copySource.getSlotArtifactList()) {
            String uri = artifact.getUri();
            if (artifact.isInternal()) {
                try {
                    uri = blobStore.storeFile(blobStore.retreiveFile(uri));
                } catch (IOException e) {
                    throw new PersistenceException(e);
                }
            }

            final SlotArtifact newArtifact = new SlotArtifact(artifact.getName(), artifact.isInternal(),
                                                                artifact.getDescription(), artifact.getUri());
            newArtifact.setSlot(newCopy);
            addChild(newArtifact);
        }
    }
}
