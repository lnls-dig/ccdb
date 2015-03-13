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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ejb.DAO;
import org.openepics.discs.conf.ejb.DeviceEJB;
import org.openepics.discs.conf.ejb.PropertyEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.ComptypeArtifact;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.DataType;
import org.openepics.discs.conf.ent.Device;
import org.openepics.discs.conf.ent.DevicePropertyValue;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.PropertyValue;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.Tag;
import org.openepics.discs.conf.ent.values.Value;
import org.openepics.discs.conf.ui.common.AbstractAttributesController;
import org.openepics.discs.conf.ui.common.UIException;
import org.openepics.discs.conf.util.Conversion;
import org.openepics.discs.conf.util.PropertyValueUIElement;
import org.openepics.discs.conf.util.UnhandledCaseException;
import org.openepics.discs.conf.util.Utility;
import org.openepics.discs.conf.views.EntityAttributeView;
import org.openepics.discs.conf.views.EntityAttributeViewKind;
import org.openepics.discs.conf.views.MultiPropertyValueView;
import org.primefaces.event.CellEditEvent;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Controller bean for manipulation of {@link ComponentType} attributes
 *
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
@Named
@ViewScoped
public class ComptypeAttributesController extends AbstractAttributesController<ComptypePropertyValue, ComptypeArtifact> {
    private static final long serialVersionUID = 1156974438243970794L;

    private static final Logger LOGGER = Logger.getLogger(ComptypeAttributesController.class.getCanonicalName());

    @Inject private transient ComptypeEJB comptypeEJB;
    @Inject private transient PropertyEJB propertyEJB;
    @Inject private transient SlotEJB slotEJB;
    @Inject private transient DeviceEJB deviceEJB;

    private ComponentType compType;
    private List<MultiPropertyValueView> filteredPropertyValues;
    private List<MultiPropertyValueView> selectedPropertyValues;
    private List<MultiPropertyValueView> selectionPropertyValuesFiltered;
    private boolean selectAllRows;

    @Override
    @PostConstruct
    public void init() {
        try {
            super.init();
            setArtifactClass(ComptypeArtifact.class);
            setPropertyValueClass(ComptypePropertyValue.class);
            setDao(comptypeEJB);
        } catch(Exception e) {
            throw new UIException("Device type details display initialization fialed: " + e.getMessage(), e);
        }
    }

    @Override
    public void addNewPropertyValue() {
        super.addNewPropertyValue();

        if (propertyValueInstance.isPropertyDefinition()) {
            if (propertyValueInstance.isDefinitionTargetSlot()) {
                for (Slot slot : slotEJB.findByComponentType(compType)) {
                    if (canAddProperty(slot.getSlotPropertyList(), propertyValueInstance.getProperty())) {
                        final SlotPropertyValue newSlotProperty = new SlotPropertyValue();
                        newSlotProperty.setProperty(propertyValueInstance.getProperty());
                        newSlotProperty.setSlot(slot);
                        slotEJB.addChild(newSlotProperty);
                    } else {
                        LOGGER.log(Level.FINE, "Type: " + compType.getName() + "; Slot: " + slot.getName()
                                + ";  Trying to add the same property value again: "
                                + propertyValueInstance.getProperty().getName());
                    }
                }
            }

            if (propertyValueInstance.isDefinitionTargetDevice()) {
                for (Device device : deviceEJB.findDevicesByComponentType(compType)) {
                    if (canAddProperty(device.getDevicePropertyList(), propertyValueInstance.getProperty())) {
                        final DevicePropertyValue newDeviceProperty = new DevicePropertyValue();
                        newDeviceProperty.setProperty(propertyValueInstance.getProperty());
                        newDeviceProperty.setDevice(device);
                        deviceEJB.addChild(newDeviceProperty);
                    } else {
                        LOGGER.log(Level.FINE, "Type: " + compType.getName() + "; Device: " + device.getSerialNumber()
                                + ";  Trying to add the same property value again: "
                                + propertyValueInstance.getProperty().getName());
                    }
                }
            }
        }
    }

    /** Checks whether it is safe to add a new property (definition) to the entity.
     * @param entityProperties the list of properties the entity already has
     * @param propertyToAdd the property we want to add
     * @return <code>true</code> if the property is safe to add, <code>false</code> otherwise (it already exists).
     */
    private <T extends PropertyValue> boolean canAddProperty(final List<T> entityProperties,
                                                                        final Property propertyToAdd) {
        for (T entityProperty : entityProperties) {
            if (entityProperty.getProperty().equals(propertyToAdd)) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void deletePropertyValue() {
        final ComptypePropertyValue propValue = (ComptypePropertyValue) selectedAttribute.getEntity();
        if (propValue.isPropertyDefinition()) {
            if (propValue.isDefinitionTargetSlot()) {
                for (Slot slot : slotEJB.findByComponentType(compType)) {
                    removeUndefinedProperty(slot.getSlotPropertyList(), propValue.getProperty(), slotEJB);
                }
            }

            if (propValue.isDefinitionTargetDevice()) {
                for (Device device : deviceEJB.findDevicesByComponentType(compType)) {
                    removeUndefinedProperty(device.getDevicePropertyList(), propValue.getProperty(), deviceEJB);
                }
            }
        }
        super.deletePropertyValue();
    }

    private <T extends PropertyValue> void removeUndefinedProperty(final List<T> entityProperties,
                                                final Property propertyToDelete, final DAO<?> daoEJB) {
        T propValueToDelete = null;
        for (T entityPropValue : entityProperties) {
            if (entityPropValue.getProperty().equals(propertyToDelete)) {
                if (entityPropValue.getPropValue() == null) {
                    // value not defined, safe to delete
                    propValueToDelete = entityPropValue;
                }
                // attribute found
                break;
            }
        }
        if (propValueToDelete != null) {
            daoEJB.deleteChild(propValueToDelete);
        }
    }

    @Override
    protected void populateAttributesList() {
        attributes = new ArrayList<>();
        // refresh the component type from database. This refreshes all related collections as well.
        compType = comptypeEJB.findById(this.compType.getId());

        for (ComptypePropertyValue prop : compType.getComptypePropertyList()) {
            attributes.add(new EntityAttributeView(prop));
        }

        for (ComptypeArtifact art : compType.getComptypeArtifactList()) {
            attributes.add(new EntityAttributeView(art, EntityAttributeViewKind.DEVICE_TYPE_ARTIFACT));
        }

        for (Tag tagAttr : compType.getTags()) {
            attributes.add(new EntityAttributeView(tagAttr, EntityAttributeViewKind.DEVICE_TYPE_TAG));
        }
    }

    @Override
    protected void filterProperties() {
        List<Property> propertyCandidates = propertyEJB.findAllOrderedByName();

        for (ComptypePropertyValue comptypePropertyValue : compType.getComptypePropertyList()) {
            final Property currentProperty = comptypePropertyValue.getProperty();
            // in modify dialog the 'property' is set to the property of the current value
            if (!currentProperty.equals(property)) {
                propertyCandidates.remove(currentProperty);
            }
        }

        filteredProperties = propertyCandidates;
    }

    /**
     * Returns {@link ComponentType} for which attributes are being manipulated
     * @return the {@link ComponentType}
     */
    public ComponentType getDeviceType() {
        return compType;
    }

    public void prepareDeviceType(ComponentType deviceType) {
        compType = deviceType;
        selectedAttribute = null;
        populateAttributesList();
        filterProperties();
    }

    public void clearDeviceType() {
        compType = null;
        attributes = null;
        filteredProperties = null;
        selectedAttribute = null;
    }

    @Override
    public void saveNewName() {
        // TODO ready for removal
    }

    @Override
    protected void setPropertyValueParent(ComptypePropertyValue child) {
        child.setComponentType(compType);
    }

    @Override
    protected void setArtifactParent(ComptypeArtifact child) {
        child.setComponentType(compType);
    }

    @Override
    protected void setTagParent(Tag tag) {
        final Set<Tag> existingTags = compType.getTags();
        if (!existingTags.contains(tag)) {
            existingTags.add(tag);
            comptypeEJB.save(compType);
        }
    }

    @Override
    protected void deleteTagFromParent(Tag tag) {
        compType.getTags().remove(tag);
        comptypeEJB.save(compType);
    }

    @Override
    public void prepareForPropertyValueAdd() {
        filterProperties();
        filteredPropertyValues = transformIntoViewList(filteredProperties);
        selectedPropertyValues = Lists.newArrayList();
        selectionPropertyValuesFiltered = null;
        selectAllRows = false;
    }

    private List<MultiPropertyValueView> transformIntoViewList(List<Property> fromList) {
        List<MultiPropertyValueView> destination = Lists.newArrayList();
        for (Property prop : fromList) {
            destination.add(new MultiPropertyValueView(prop));
        }
        return destination;
    }

    @Override
    protected void populateParentTags() {
        // Nothing to do since component types don't inherit anything
    }

    @Override
    public void modifyBuiltInProperty() {
        // no built in property to modify
    }

    /** Prepares the data for slot property (definition) creation */
    public void prepareForSlotPropertyAdd() {
        definitionTarget = AbstractAttributesController.DefinitionTarget.SLOT;
        isPropertyDefinition = true;
        super.prepareForPropertyValueAdd();
    }

    /** Prepares the data for device property (definition) creation */
    public void prepareForDevicePropertyAdd() {
        definitionTarget = AbstractAttributesController.DefinitionTarget.DEVICE;
        isPropertyDefinition = true;
        super.prepareForPropertyValueAdd();
    }

    @Override
    public boolean canEdit(EntityAttributeView attribute) {
        final EntityAttributeViewKind attributeKind = attribute.getKind();
        return attributeKind != EntityAttributeViewKind.INSTALL_SLOT_PROPERTY
                && attributeKind != EntityAttributeViewKind.DEVICE_PROPERTY
                && attributeKind != EntityAttributeViewKind.DEVICE_TYPE_TAG
                && attributeKind != EntityAttributeViewKind.TAG;
    }

    @Override
    public boolean canDelete(EntityAttributeView attribute) {
        return attribute.getKind() == EntityAttributeViewKind.DEVICE_TYPE_ARTIFACT
                || attribute.getKind() == EntityAttributeViewKind.DEVICE_TYPE_TAG
                || super.canDelete(attribute);
    }

    /** The event handler for when user clicks on the check-box in the "Add property values" dialog.
     * @param prop the property value to handle the event for
     */
    public void rowSelectListener(MultiPropertyValueView prop) {
        if (prop.isSelected()) {
            selectedPropertyValues.add(prop);
        } else {
            selectedPropertyValues.remove(prop);
        }
    }

    /** The function to handle the state of the "Select all" checkbox after the filter change */
    public void updateToggle() {
        final List<MultiPropertyValueView> pvList = selectionPropertyValuesFiltered == null
                                                        ? filteredPropertyValues : selectionPropertyValuesFiltered;
        for (MultiPropertyValueView pv : pvList) {
            if (!pv.isSelected()) {
                selectAllRows = false;
                return;
            }
        }
        selectAllRows = true;
    }

    /** The event handler for toggling selection of all property values */
    public void handleToggleAll() {
        final List<MultiPropertyValueView> pvList = selectionPropertyValuesFiltered == null
                ? filteredPropertyValues : selectionPropertyValuesFiltered;
        if (selectAllRows) {
            selectAllFiltered(pvList);
        } else {
            unselectAllFiltered(pvList);
        }
    }

    private void selectAllFiltered(List<MultiPropertyValueView> pvList) {
        for (MultiPropertyValueView pv : pvList) {
            if (!pv.isSelected()) {
                pv.setSelected(true);
                selectedPropertyValues.add(pv);
            }
        }
    }

    private void unselectAllFiltered(List<MultiPropertyValueView> pvList) {
        for (MultiPropertyValueView pv : pvList) {
            if (pv.isSelected()) {
                pv.setSelected(false);
                selectedPropertyValues.remove(pv);
            }
        }
    }

    /** This method handled the value once the users is done putting it in. This method actually performs the
     * input validation.
     * @param event
     */
    public void onEditCell(CellEditEvent event) {
        final Object newValue = event.getNewValue();
        final Object oldValue = event.getOldValue();

        // TODO check for uniqueness as well
        if (newValue != null && !newValue.equals(oldValue)) {
            final MultiPropertyValueView editedPropVal = selectionPropertyValuesFiltered == null
                                                            ? filteredPropertyValues.get(event.getRowIndex())
                                                            : selectionPropertyValuesFiltered.get(event.getRowIndex());
            final DataType dataType = editedPropVal.getDataType();
            String newValueStr = getEditEventValue(newValue, editedPropVal.getPropertyValueUIElement());
            try {
                switch (editedPropVal.getPropertyValueUIElement()) {
                    case INPUT:
                        validateSingleLine(newValueStr, dataType);
                        break;
                    case TEXT_AREA:
                        validateMultiLine(newValueStr, dataType);
                        break;
                    case SELECT_ONE_MENU:
                        if (Strings.isNullOrEmpty(newValueStr)) {
                            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    Utility.MESSAGE_SUMMARY_ERROR, "A value must be selected."));
                        }
                        break;
                    case NONE:
                    default:
                        throw new UnhandledCaseException();
                }
                editedPropVal.setValue(Conversion.stringToValue(newValueStr, dataType));
            } catch (ValidatorException e) {
                editedPropVal.setUiValue(oldValue == null ? null : getEditEventValue(oldValue, null));
                FacesContext.getCurrentInstance().addMessage("inputValidationFail", e.getFacesMessage());
                FacesContext.getCurrentInstance().validationFailed();
            }
        }
    }

    private String getEditEventValue(Object val, PropertyValueUIElement propValueUIElement) {
        if (val == null) return null;
        if (val instanceof String) return val.toString();
        if (val instanceof List<?>) {
            List<?> valList = (List<?>)val;
            if (propValueUIElement == null) {
                for (Object v : valList) {
                    if (v != null) {
                        return v.toString();
                    }
                }
                return null;
            } else {
                switch (propValueUIElement) {
                    case INPUT:
                        return valList.get(0).toString();
                    case TEXT_AREA:
                        return valList.get(1).toString();
                    case SELECT_ONE_MENU:
                        return valList.get(2).toString();
                    case NONE:
                    default:
                        throw new UnhandledCaseException();
                }
            }
        }
        throw new RuntimeException("MultiPropertyValue: UI string value cannot be extracted.");
    }

    /** This method returns a String representation of the property value.
     * @param prop the value of the property to show value for
     * @return the string representation
     */
    public String displayPropertyValue(MultiPropertyValueView prop) {
        final Value val = prop.getValue();
        return val == null ? "<Please define>" : Conversion.valueToString(val);
    }

    /** @return the filteredPropertyValues */
    public List<MultiPropertyValueView> getFilteredPropertyValues() {
        return filteredPropertyValues;
    }

    /** @param the filteredPropertyValues the filteredPropertyValues to set */
    public void setFilteredPropertyValues(List<MultiPropertyValueView> filteredPropertyValues) {
        this.filteredPropertyValues = filteredPropertyValues;
    }

    /** @return the selectedPropertyValues */
    public List<MultiPropertyValueView> getSelectedPropertyValues() {
        return selectedPropertyValues;
    }

    /** @param selectedPropertyValues the selectedPropertyValues to set */
    public void setSelectedPropertyValues(List<MultiPropertyValueView> selectedPropertyValues) {
        this.selectedPropertyValues = selectedPropertyValues;
    }

    /** @return the selectionPropertyValuesFiltered */
    public List<MultiPropertyValueView> getSelectionPropertyValuesFiltered() {
        return selectionPropertyValuesFiltered;
    }

    /** @param selectionPropertyValuesFiltered the selectionPropertyValuesFiltered to set */
    public void setSelectionPropertyValuesFiltered(List<MultiPropertyValueView> selectionPropertyValuesFiltered) {
        this.selectionPropertyValuesFiltered = selectionPropertyValuesFiltered;
    }

    /**
     * @return the selectAllRows
     */
    public boolean isSelectAllRows() {
        return selectAllRows;
    }

    /**
     * @param selectAllRows the selectAllRows to set
     */
    public void setSelectAllRows(boolean selectAllRows) {
        this.selectAllRows = selectAllRows;
    }
}
