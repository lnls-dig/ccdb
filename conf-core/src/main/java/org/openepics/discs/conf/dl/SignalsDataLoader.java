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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;
import org.openepics.discs.conf.dl.annotations.SignalsLoader;
import org.openepics.discs.conf.dl.common.AbstractDataLoader;
import org.openepics.discs.conf.dl.common.DataLoader;
import org.openepics.discs.conf.dl.common.ErrorMessage;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.values.StrValue;
import org.openepics.discs.conf.util.PropertyValueUnassignedException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Implementation of data loader for signals.
 *
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 *
 */
@Stateless
@SignalsLoader
public class SignalsDataLoader extends AbstractDataLoader implements DataLoader {
    private static final Logger LOGGER = Logger.getLogger(SignalsDataLoader.class.getCanonicalName());

    // Header column name constants
    private static final String HDR_DEVICE = "DEVICE";
    private static final String HDR_NUMBER = "SIGNAL NUMBER";
    private static final String HDR_NAME = "SIGNAL NAME";
    private static final String HDR_DESC = "SIGNAL DESCRIPTION";

    private static final int COL_INDEX_DEVICE = 1;
    private static final int COL_INDEX_NUMBER = 2;
    private static final int COL_INDEX_NAME = 3;
    private static final int COL_INDEX_DESC = 4;
    
    private static final String PROPERTY_NAME_PREFIX = "SignalName";
    private static final String PROPERTY_DESC_PREFIX = "SignalDescription";

    private static final Set<String> REQUIRED_COLUMNS = new HashSet<>(
                                                Arrays.asList(HDR_DEVICE, HDR_NUMBER, HDR_NAME, HDR_DESC));

    @Inject private SlotEJB slotEJB; 
    
    private String deviceFld, nameFld, descFld;
    private int numberFld;
    
	@Override
	public int getDataWidth() {
		return 5;
	}

	@Override
	protected Set<String> getRequiredColumnNames() {
		return REQUIRED_COLUMNS;
	}

	@Override
	protected @Nullable Integer getUniqueColumnIndex() {
		return null;
	}

	@Override
	protected void assignMembersForCurrentRow() {
        nameFld = readCurrentRowCellForHeader(COL_INDEX_NAME);
        deviceFld = readCurrentRowCellForHeader(COL_INDEX_DEVICE);
        descFld = readCurrentRowCellForHeader(COL_INDEX_DESC);
        numberFld = Integer.valueOf(readCurrentRowCellForHeader(COL_INDEX_NUMBER));
	}

	@Override
	protected void handleUpdate(String actualCommand) {
	    try {
            Slot installationSlot = slotEJB.findByName(deviceFld);
            if (!isSlotOK(installationSlot)) {
                return;
            }
            // If both values are defined update is possible
            if (!isPropertyValueDefined(installationSlot, PROPERTY_NAME_PREFIX + numberFld)) {
                result.addRowMessage(ErrorMessage.MODIFY_VALUE_MISSING, PROPERTY_NAME_PREFIX + numberFld);
            }
            if (!isPropertyValueDefined(installationSlot, PROPERTY_DESC_PREFIX + numberFld)) {
                result.addRowMessage(ErrorMessage.MODIFY_VALUE_MISSING, PROPERTY_DESC_PREFIX + numberFld);
            }
            // Proceed if no errors
            if (!result.isRowError()) {
                handleCreateUpdate(installationSlot);
            }
	    } catch (PropertyValueUnassignedException e) {
	        // property not found
	        LOGGER.log(Level.FINE, "Signals import - property not found: " + e.getMessage());
	    }
	}
	
	@Override
	protected void handleDelete(String actualCommand) {
        Slot installationSlot = slotEJB.findByName(deviceFld);
        if (!isSlotOK(installationSlot)) {
            return;
        }
        
        try {
            setPropertyValue(installationSlot, PROPERTY_NAME_PREFIX + numberFld, null);
            setPropertyValue(installationSlot, PROPERTY_DESC_PREFIX + numberFld, null);
            if (!result.isRowError()) {
                slotEJB.save(installationSlot);
            }
        } catch (EJBTransactionRolledbackException e) {
            handleLoadingError(LOGGER, e);
        }
	}

	@Override
	protected void handleRename() {
		throw new NotImplementedException();
	}

	@Override
	protected void handleCreate(String actualCommand) {
	    try {
            Slot installationSlot = slotEJB.findByName(deviceFld);
            if (!isSlotOK(installationSlot)) {
                return;
            }
            // If both values are undefined creation is possible
            if (isPropertyValueDefined(installationSlot, PROPERTY_NAME_PREFIX + numberFld)) {
                result.addRowMessage(ErrorMessage.CREATE_VALUE_EXISTS, PROPERTY_NAME_PREFIX + numberFld);
            }
            if (isPropertyValueDefined(installationSlot, PROPERTY_DESC_PREFIX + numberFld)) {
                result.addRowMessage(ErrorMessage.CREATE_VALUE_EXISTS, PROPERTY_DESC_PREFIX + numberFld);
            }
            // Proceed if no errors
            if (!result.isRowError()) {
                handleCreateUpdate(installationSlot);
            }
        } catch (PropertyValueUnassignedException e) {
            // property not found
            LOGGER.log(Level.FINE, "Signals import - property not found: " + e.getMessage());
        }
	}

	@Override
	protected void setUpIndexesForFields() {
        final Builder<String, Integer> mapBuilder = ImmutableMap.builder();

        mapBuilder.put(HDR_NAME, COL_INDEX_NAME);
        mapBuilder.put(HDR_DESC, COL_INDEX_DESC);
        mapBuilder.put(HDR_NUMBER, COL_INDEX_NUMBER);
        mapBuilder.put(HDR_DEVICE, COL_INDEX_DEVICE);

        indicies = mapBuilder.build();
	}
	
    @Override
    public int getImportDataStartIndex() {
        // index of the first import data Excel row is 9 (0 based 8)
        return 8;
    }
    
    private void handleCreateUpdate(final Slot installationSlot) {
        try {
            setPropertyValue(installationSlot, PROPERTY_NAME_PREFIX + numberFld, nameFld);
            setPropertyValue(installationSlot, PROPERTY_DESC_PREFIX + numberFld, descFld);
            if (!result.isRowError()) {
                slotEJB.save(installationSlot);
            }
        } catch (EJBTransactionRolledbackException e) {
            handleLoadingError(LOGGER, e);
        }
    }
    
    private boolean isPropertyValueDefined(final Slot slot, final String propertyValueName) {
        final List<SlotPropertyValue> propValues = slot.getSlotPropertyList();
        for (SlotPropertyValue propValue : propValues) {
            if (propValue.getProperty().getName().equalsIgnoreCase(propertyValueName)) {
                return propValue.getPropValue() != null;
            }
        }
        result.addRowMessage(ErrorMessage.PROPERTY_NOT_FOUND, propertyValueName);
        throw new PropertyValueUnassignedException(propertyValueName);
    }
    
    private void setPropertyValue(final Slot slot, final String propertyValueName, final @Nullable String value) {
        final List<SlotPropertyValue> propValues = slot.getSlotPropertyList();
        for (SlotPropertyValue propValue : propValues) {
            if (propValue.getProperty().getName().equalsIgnoreCase(propertyValueName)) {
                propValue.setPropValue(value == null ? null : new StrValue(value));
                return;
            }
        }
        // property not found
        LOGGER.log(Level.FINE, "Signals import - property not found: " + propertyValueName);
        result.addRowMessage(ErrorMessage.PROPERTY_NOT_FOUND, propertyValueName);
    }
    
    private boolean isSlotOK(Slot installationSlot) {
        if (installationSlot == null) {
            LOGGER.log(Level.FINE, "Signals import - installation slot not found: " + deviceFld);
            result.addRowMessage(ErrorMessage.ENTITY_NOT_FOUND, HDR_DEVICE);
            return false;
        }
        if (!installationSlot.isHostingSlot()) {
            LOGGER.log(Level.FINE, "Signal import - trying to add a signal to container: " + deviceFld);
            result.addRowMessage(ErrorMessage.INSTALLATION_SLOT_REQUIRED, HDR_DEVICE);
            return false;
        }
        return true;
    }
}
