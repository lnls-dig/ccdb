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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import org.openepics.discs.conf.dl.annotations.DataTypeLoader;
import org.openepics.discs.conf.dl.common.DataLoader;
import org.openepics.discs.conf.ejb.DataTypeEJB;
import org.openepics.discs.conf.ent.DataType;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.export.ExportTable;
import org.openepics.discs.conf.ui.common.AbstractExcelSingleFileImportUI;
import org.openepics.discs.conf.ui.common.DataLoaderHandler;
import org.openepics.discs.conf.ui.common.UIException;
import org.openepics.discs.conf.ui.export.ExportSimpleTableDialog;
import org.openepics.discs.conf.ui.export.SimpleTableExporter;
import org.openepics.discs.conf.ui.lazymodels.CCDBLazyModel;
import org.openepics.discs.conf.ui.lazymodels.DataTypeLazyModel;
import org.openepics.discs.conf.ui.util.UiUtility;
import org.openepics.discs.conf.util.BuiltInDataType;
import org.openepics.discs.conf.util.Conversion;
import org.openepics.discs.conf.util.Utility;
import org.openepics.discs.conf.views.UserEnumerationView;
import org.openepics.seds.api.datatypes.SedsEnum;
import org.openepics.seds.core.Seds;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

/**
 * The Java EE managed bean for supporting UI actions for data types an user defined enumeration manipulation.
 * @author vuppala
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
@Named
@ViewScoped
public class DataTypeManager extends AbstractExcelSingleFileImportUI implements Serializable, SimpleTableExporter {
    private static final long serialVersionUID = -7538356350403365152L;
    private static final Logger LOGGER = Logger.getLogger(DataTypeManager.class.getCanonicalName());

    @Inject private DataTypeEJB dataTypeEJB;
    @Inject private DataLoaderHandler dataLoaderHandler;
    @Inject @DataTypeLoader private DataLoader enumsDataLoader;

    private CCDBLazyModel<UserEnumerationView> lazyModel;

    private List<UserEnumerationView> selectedEnums;
    private List<UserEnumerationView> usedEnums;
    private List<UserEnumerationView> filteredDialogEnums;
    private List<String> builtInDataTypeNames;

    private UserEnumerationView dialogEnum;

    // * * * * * * * Add/modify dialog fields * * * * * * *

    private transient ExportSimpleTableDialog simpleTableExporterDialog;

    private class ExportSimpleEnumTableDialog extends ExportSimpleTableDialog {
        @Override
        protected String getTableName() {
            return "Enumerations";
        }

        @Override
        protected String getFileName() {
            return "ccdb_enumerations";
        }

        @Override
        protected void addHeaderRow(ExportTable exportTable) {
            exportTable.addHeaderRow("Operation", "Name", "Description", "Definition");
        }

        @Override
        protected void addData(ExportTable exportTable) {
            final List<UserEnumerationView> exportData = lazyModel.load(0, Integer.MAX_VALUE,
                                        lazyModel.getSortField(), lazyModel.getSortOrder(), lazyModel.getFilters());
            for (final UserEnumerationView enumeration : exportData) {
                exportTable.addDataRow(DataLoader.CMD_UPDATE, enumeration.getName(), enumeration.getDescription(),
                        String.join(", ",  enumeration.getDefinitionList()));
            }
        }

        @Override
        protected String getExcelTemplatePath() {
            return "/resources/templates/ccdb_enumerations.xlsx";
        }

        @Override
        protected int getExcelDataStartRow() {
            return 9;
        }
    }

    /** Creates a new instance of DataTypeManager */
    public DataTypeManager() {
    }

    /** Java EE post construct life-cycle method */
    @Override
    @PostConstruct
    public void init() {
        super.init();
        final String dataTypeIdStr = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().
                getRequest()).getParameter("id");
        try {
            simpleTableExporterDialog = new ExportSimpleEnumTableDialog();
            prepareBuiltInDataTypes();
            refreshUserDataTypes();
            if (!Strings.isNullOrEmpty(dataTypeIdStr)) {
                final long dataTypeId = Long.parseLong(dataTypeIdStr);
                final DataType dataType = dataTypeEJB.findById(dataTypeId);
                if (dataType != null) {
                    RequestContext.getCurrentInstance().execute("CCDB.config.jumpToElementOnLoad = true;"
                            + "selectEntityInTable(" + dataTypeId + ", 'enumsTableVar');");
                }
            }
        } catch (NumberFormatException e) {
            // just log
            LOGGER.log(Level.WARNING, "URL contained strange dataType ID: " + dataTypeIdStr );
        } catch(Exception e) {
            throw new UIException("Device type display initialization fialed: " + e.getMessage(), e);
        }
    }

    @Override
    public void setDataLoader() {
        dataLoader = enumsDataLoader;
    }

    /**
     * The validator for the UI input field for user defined enumeration name. Called when saving enumeration.
     *
     * @param ctx {@link javax.faces.context.FacesContext}
     * @param component {@link javax.faces.component.UIComponent}
     * @param value The value
     * @throws ValidatorException {@link javax.faces.validator.ValidatorException}
     */
    public void nameValidator(FacesContext ctx, UIComponent component, Object value) {
        if (value == null) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, UiUtility.MESSAGE_SUMMARY_ERROR,
                                                                    "Enumeration name required."));
        }

        final String enumName = value.toString();

        if (dialogEnum.isEnumerationBeingAdded() || !dialogEnum.getName().equals(enumName)) {
            final DataType existingDataType = dataTypeEJB.findByName(enumName);
            if (existingDataType != null) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                                    UiUtility.MESSAGE_SUMMARY_ERROR,
                                                                    "Enumeration with the same name already exists."));
            }
        }
    }

    private void refreshUserDataTypes() {
        lazyModel = new DataTypeLazyModel(dataTypeEJB, builtInDataTypeNames);
        selectedEnums = null;
        filteredDialogEnums = null;
    }

    private void prepareBuiltInDataTypes() {
        Builder<String> builtInDataTypeBuilder = ImmutableList.builder();
        for (BuiltInDataType type : BuiltInDataType.values()) {
            builtInDataTypeBuilder.add(type.toString());
        }
        builtInDataTypeNames = builtInDataTypeBuilder.build();
    }

    /** This method clears all input fields used in the "Add enumeration" dialog. */
    public void prepareAddPopup() {
        dialogEnum = new UserEnumerationView();
    }

    /** This method prepares the input fields used in the "Edit enumeration" dialog. */
    public void prepareModifyPopup() {
        Preconditions.checkState(isSingleEnumSelected());
        dialogEnum = new UserEnumerationView(dataTypeEJB.refreshEntity(selectedEnums.get(0).getEnumeration()));
        dialogEnum.setUsed(dataTypeEJB.isDataTypeUsed(dialogEnum.getEnumeration()));
    }

    /** Method that saves a new enumeration definition, when user presses the "Save" button in the "Add new" dialog */
    public void onAdd() {
        try {
            final DataType newEnum = dialogEnum.getEnumeration();
            newEnum.setDefinition(jsonDefinitionFromList(dialogEnum.getDefinitionList()));
            dataTypeEJB.add(newEnum);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                                                                "Enumeration has been successfully created.");
        } finally {
            dialogEnum = null;
            refreshUserDataTypes();
        }
    }

    /**
     * Method that saves the modified enumeration definition, when user presses the "Save" button in the
     * "Modify enumeration" dialog.
     */
    public void onModify() {
        try {
            Preconditions.checkNotNull(dialogEnum);
            final DataType modifiedEnum = dialogEnum.getEnumeration();
            modifiedEnum.setDefinition(jsonDefinitionFromList(dialogEnum.getDefinitionList()));
            dataTypeEJB.save(modifiedEnum);
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                                                                "Enumeration has been successfully modified.");
        } finally {
            dialogEnum = null;
            refreshUserDataTypes();
        }
    }

    /**
     * The method builds a list of user enumerations that are already used. If the list is not empty, it is displayed
     * to the user and the user is prevented from deleting them.
     */
    public void checkEnumsForDeletion() {
        Preconditions.checkNotNull(selectedEnums);
        Preconditions.checkState(!selectedEnums.isEmpty());

        usedEnums = Lists.newArrayList();
        for (final UserEnumerationView enumToDelete : selectedEnums) {
            List<Property> properties = dataTypeEJB.findProperties(enumToDelete.getEnumeration(), 2);
            if (!properties.isEmpty()) {
                enumToDelete.setUsedBy(properties.get(0).getName()+(properties.size()>1 ? ", ..." : ""));
                usedEnums.add(enumToDelete);
            }
        }
    }

    /**
     * Method that deletes the user enumeration if that is allowed. Enumeration deletion is prevented if the enumeration
     * is used in some property value.
     */
    public void onDelete() {
        try {
            Preconditions.checkNotNull(selectedEnums);
            Preconditions.checkState(!selectedEnums.isEmpty());
            Preconditions.checkNotNull(usedEnums);
            Preconditions.checkState(usedEnums.isEmpty());

            int deletedEnums = 0;
            for (final UserEnumerationView enumToDelete : selectedEnums) {
                dataTypeEJB.delete(enumToDelete.getEnumeration());
                ++deletedEnums;
            }

            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                                                                "Deleted " + deletedEnums + " enumerations.");
        } finally {
            selectedEnums = null;
            usedEnums = null;
            refreshUserDataTypes();
        }
    }

    private String jsonDefinitionFromList(List<String> definitionValues) {
        final SedsEnum testEnum = Seds.newFactory().newEnum(definitionValues.get(0),
                                                                definitionValues.toArray(new String[] {}));
        final JsonObject jsonEnum = Seds.newDBConverter().serialize(testEnum);
        return  jsonEnum.toString();
    }

    /** @return <code>true</code> if a single enumeration is selected , <code>false</code> otherwise */
    public boolean isSingleEnumSelected() {
        return (selectedEnums != null) && (selectedEnums.size() == 1);
    }

    /**
     * The method creates a new copy of the currently selected user enumeration(s)
     */
    public void duplicate() {
        try {
            Preconditions.checkState(!Utility.isNullOrEmpty(selectedEnums));
            final int duplicated =  dataTypeEJB.duplicate(selectedEnums.stream().
                                            map(UserEnumerationView::getEnumeration).collect(Collectors.toList()));
            UiUtility.showMessage(FacesMessage.SEVERITY_INFO, UiUtility.MESSAGE_SUMMARY_SUCCESS,
                                                                "Duplicated " + duplicated + " enumerations.");
        } finally {
            refreshUserDataTypes();
        }
    }

    @Override
    public void doImport() {
        try (InputStream inputStream = new ByteArrayInputStream(importData)) {
            setLoaderResult(dataLoaderHandler.loadData(inputStream, enumsDataLoader));
            refreshUserDataTypes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    //* * * * * * * * * * * * * * * * * getters and setters * * * * * * * * * * * * * * * * *
    //

    /** @return a list of all {@link DataType}s */
    public List<DataType> getDataTypes() {
        final List<DataType> sortedDataTypes = dataTypeEJB.findAll().stream().
                sorted((dt1, dt2) ->
                    {
                        BuiltInDataType bidt = Conversion.getBuiltInDataType(dt1);
                        final long dt1Id = (bidt == BuiltInDataType.USER_DEFINED_ENUM)
                                            ? dt1.getId() + BuiltInDataType.values().length : bidt.ordinal();
                        bidt = Conversion.getBuiltInDataType(dt2);
                        final long dt2Id = (bidt == BuiltInDataType.USER_DEFINED_ENUM)
                                ? dt2.getId() + BuiltInDataType.values().length : bidt.ordinal();

                        final long diff = dt1Id - dt2Id;
                        return (diff < Integer.MIN_VALUE) ? Integer.MIN_VALUE :
                            ((diff > Integer.MAX_VALUE) ? Integer.MAX_VALUE : Long.valueOf(diff).intValue());
                    }).
                collect(Collectors.toList());
        return ImmutableList.copyOf(sortedDataTypes);
    }

    /** @return the lazy loading data model */
    public LazyDataModel<UserEnumerationView> getLazyModel() {
        return lazyModel;
    }

    /** @return <code>true</code> if no data was found, <code>false</code> otherwise */
    public boolean isDataTableEmpty() {
        return lazyModel.isEmpty();
    }

    /** @return The {@link DataType}s selected in the table */
    public List<UserEnumerationView> getSelectedEnums() {
        return selectedEnums;
    }
    /** @param selectedEnums The {@link DataType}s selected in the table */
    public void setSelectedEnums(List<UserEnumerationView> selectedEnums) {
        this.selectedEnums = selectedEnums;
    }

    /** @return The sub {@link List} of selected {@link DataType}s that are in use in the database */
    public List<UserEnumerationView> getUsedEnums() {
        return usedEnums;
    }

    @Override
    public ExportSimpleTableDialog getSimpleTableDialog() {
        return simpleTableExporterDialog;
    }

    /** @return the filteredDialogEnums */
    public List<UserEnumerationView> getFilteredDialogEnums() {
        return filteredDialogEnums;
    }

    /** @param filteredDialogEnums the filteredDialogEnums to set */
    public void setFilteredDialogEnums(List<UserEnumerationView> filteredDialogEnums) {
        this.filteredDialogEnums = filteredDialogEnums;
    }

    /** @return the dialogEnum */
    public UserEnumerationView getDialogEnum() {
        return dialogEnum;
    }
}
