/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 * Copyright (c) 2041 FRIB
 *
 * This file is part of Controls Configuration Database.
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any newer version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.openepics.discs.conf.dl.PropertiesLoaderQualifier;
import org.openepics.discs.conf.dl.common.DataLoader;
import org.openepics.discs.conf.dl.common.DataLoaderResult;
import org.openepics.discs.conf.ejb.ConfigurationEJB;
import org.openepics.discs.conf.ent.AuditRecord;
import org.openepics.discs.conf.ent.DataType;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.PropertyAssociation;
import org.openepics.discs.conf.ent.Unit;
import org.openepics.discs.conf.ui.common.DataLoaderHandler;
import org.openepics.discs.conf.util.Utility;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;

import com.google.common.io.ByteStreams;

/**
 *
 * @author vuppala
 * @author Miroslav Pavleski <miroslav.pavleski@cosylab.com>
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 *
 */
@Named
@ViewScoped
public class PropertyManager implements Serializable {
    @Inject private ConfigurationEJB configurationEJB;

    @Inject private DataLoaderHandler dataLoaderHandler;
    @Inject @PropertiesLoaderQualifier private DataLoader propertiesDataLoader;

    private List<Property> properties;
    private List<Property> sortedProperties;
    private List<Property> filteredProperties;

    private byte[] importData;
    private String importFileName;
    private DataLoaderResult loaderResult;

    private String name;
    private String description;
    private DataType dataType;
    private Unit unit;
    private PropertyAssociation association;
    private Property selectedProperty;
    private boolean unitComboEnabled;
    private List<AuditRecord> auditRecordsForEntity;


    /**
     * Creates a new instance of PropertyManager
     */
    public PropertyManager() {
    }

    @PostConstruct
    public void init() {
        properties = configurationEJB.findProperties();
        selectedProperty = null;
        resetFields();
    }

    public void onAdd() {
        final Property propertyToAdd = new Property(name, description, association);
        propertyToAdd.setDataType(dataType);
        propertyToAdd.setUnit(unit);
        configurationEJB.addProperty(propertyToAdd);
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "New property has been created");
        init();
    }

    public void onModify() {
        selectedProperty.setName(name);
        selectedProperty.setDescription(description);
        selectedProperty.setDataType(dataType);
        selectedProperty.setAssociation(association);
        selectedProperty.setUnit(unit);
        configurationEJB.saveProperty(selectedProperty);
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Property was modified");
        init();
    }

    public void prepareAddPopup() {
        resetFields();
        RequestContext.getCurrentInstance().update("addPropertyForm:addProperty");
    }

    public void prepareModifyPopup() {
        name = selectedProperty.getName();
        description = selectedProperty.getDescription();
        dataType = selectedProperty.getDataType();
        unit = selectedProperty.getUnit();
        association = selectedProperty.getAssociation();
        setIsUnitComboEnabled();
        RequestContext.getCurrentInstance().update("modifyPropertyForm:modifyProperty");
    }

    public void onDelete() {
        configurationEJB.deleteProperty(selectedProperty);
        Utility.showMessage(FacesMessage.SEVERITY_INFO, "Success", "Property was deleted");
        init();
    }

    public List<Property> getSortedProperties() {
        return sortedProperties;
    }

    public void setSortedOProperties(List<Property> sortedObjects) {
        this.sortedProperties = sortedObjects;
    }

    public List<Property> getFilteredProperties() {
        return filteredProperties;
    }

    public void setFilteredProperties(List<Property> filteredObjects) {
        this.filteredProperties = filteredObjects;
    }

    public List<Property> getObjects() {
        return properties;
    }

    public String getImportFileName() { return importFileName; }

    public void doImport() {
        final InputStream inputStream = new ByteArrayInputStream(importData);
        loaderResult = dataLoaderHandler.loadData(inputStream, propertiesDataLoader);
    }

    public void prepareImportPopup() {
        importData = null;
        importFileName = null;
    }

    public List<PropertyAssociation> getPropertyAssociations() {
        return Arrays.asList(PropertyAssociation.values());
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public PropertyAssociation getPropertyAssociation() { return association; }
    public void setPropertyAssociation(PropertyAssociation association) { this.association = association; }

    public void setIsUnitComboEnabled() {
        if (dataType.getName().equalsIgnoreCase("integer") || dataType.getName().equalsIgnoreCase("double") || dataType.getName().equalsIgnoreCase("integers vector") || dataType.getName().equalsIgnoreCase("doubles vector" ) || dataType.getName().equalsIgnoreCase("doubles table")) {
            unitComboEnabled = true;
        } else {
            unitComboEnabled = false;
        }
    }

    public boolean isUnitComboEnabled() { return unitComboEnabled;}

    public Property getSelectedProperty() { return selectedProperty; }
    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    public Property getSelectedPropertyToModify() { return selectedProperty; }
    public void setSelectedPropertyToModify(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
        prepareModifyPopup();
    }

    public Property getSelectedPropertyForLog() { return selectedProperty; }
    public void setSelectedPropertyForLog(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
        auditRecordsForEntity = configurationEJB.findAuditRecordsByEntityId(selectedProperty.getId());
        //RequestContext.getCurrentInstance().update("propertyLogForm:propertyLog");
    }

    public DataLoaderResult getLoaderResult() { return loaderResult; }

    public List<AuditRecord> getAuditRecordsForEntity() {
        return auditRecordsForEntity;
    }

    public void handleImportFileUpload(FileUploadEvent event) {
        try (InputStream inputStream = event.getFile().getInputstream()) {
            this.importData = ByteStreams.toByteArray(inputStream);
            this.importFileName = FilenameUtils.getName(event.getFile().getFileName());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void resetFields() {
        name = null;
        description = null;
        dataType = null;
        unit = null;
        association = null;
        unitComboEnabled = true;
    }
}
