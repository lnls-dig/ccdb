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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openepics.discs.conf.ejb.AuditRecordEJB;
import org.openepics.discs.conf.ent.AuditRecord;
import org.openepics.discs.conf.ent.ConfigurationEntity;
import org.openepics.discs.conf.ent.EntityType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author vuppala
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
@Named(value = "auditManager")
@ViewScoped
public class AuditManager implements Serializable {
    private static final long serialVersionUID = 4650841685917081962L;

    private static final Logger LOGGER = Logger.getLogger(AuditManager.class.getCanonicalName());

    @Inject transient private AuditRecordEJB auditRecordEJB;

    private List<AuditRecord> auditRecordsForEntity;
    private AuditRecord displayRecord;

    /**
     * Creates a new instance of AuditManager
     */
    public AuditManager() {
    }

    /**
     * This method is called from xhtml to set the audit record for which the details will be shown in the dialog.
     * The audit record is selected by its database ID.
     * @param id - the database id of the audit log record
     */
    public void chooseDisplayRecord(final Long id) {
        this.displayRecord = auditRecordEJB.findById(id);
    }

    /**
     * @return The audit record used in the <i>display details</i> dialog.
     */
    public AuditRecord getDisplayRecord() {
        return displayRecord;
    }

    /**
     * @return A pretty printed representation of the log entry JSON.
     */
    public String getDisplayRecordEntry() {
        if (displayRecord == null) {
            return "";
        }

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final Object json = mapper.readValue(displayRecord.getEntry(), Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return "";
        }
    }

    /**
     * The method sets the audit log list for the selected entity. This method is called from the table button "i" in
     * the xhtml file.
     * @param selectedEntity - the entity to set the audit log list for.
     * @param entityType - the type of the entity. To set this parameter from xhtml, use a string representation of
     * the enumeration constant.
     */
    public void selectEntityForLog(final ConfigurationEntity selectedEntity, final EntityType entityType) {
        auditRecordsForEntity = auditRecordEJB.findByEntityIdAndType(selectedEntity.getId(), entityType);
    }

    /**
     * @return A list of audit log entries for a selected entity to show in the table.
     */
    public List<AuditRecord> getAuditRecordsForEntity() {
        return auditRecordsForEntity;
    }


}
