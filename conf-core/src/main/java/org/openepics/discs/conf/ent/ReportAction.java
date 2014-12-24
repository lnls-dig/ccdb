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
package org.openepics.discs.conf.ent;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

/**
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
@Entity
@Table(name = "report_action")
public class ReportAction extends ConfigurationEntity {
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    private ReportFilterAction operation;

    @Basic
    @Column(name = "field")
    private String field;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Basic
    @Column(name = "value")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report parentReport;

    protected ReportAction() {
    }

    public ReportAction(ReportFilterAction operation, String field, String value,
            Report parentReport, String modifiedBy) {
        this.operation = Preconditions.checkNotNull(operation);
        this.field = Preconditions.checkNotNull(field);
        this.value = Preconditions.checkNotNull(value);
        this.parentReport = Preconditions.checkNotNull(parentReport);
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public ReportAction(ReportFilterAction operation, Property property, String value,
            Report parentReport, String modifiedBy) {
        this.operation = Preconditions.checkNotNull(operation);
        this.property = Preconditions.checkNotNull(property);
        this.value = Preconditions.checkNotNull(value);
        this.parentReport = Preconditions.checkNotNull(parentReport);
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public ReportAction(ReportFilterAction operation, Tag tag, Report parentReport, String modifiedBy) {
        if (operation != ReportFilterAction.IS)
            throw new IllegalArgumentException("Tag can only have an \"IS\" operation.");
        this.operation = operation;
        this.tag = Preconditions.checkNotNull(tag);
        this.parentReport = Preconditions.checkNotNull(parentReport);
        this.modifiedBy = modifiedBy;
        this.modifiedAt = new Date();
    }

    public ReportFilterAction getOperation() {
        return operation;
    }
    public void setOperation(ReportFilterAction operation) {
        if (tag != null && operation != ReportFilterAction.IS)
            throw new IllegalArgumentException("Tag can only have an \"IS\" operation.");
        this.operation = operation;
    }

    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
        this.property = null;
        this.tag = null;
    }

    public Property getProperty() {
        return property;
    }
    public void setProperty(Property property) {
        this.property = property;
        this.field = null;
        this.tag = null;
    }

    public Tag getTag() {
        return tag;
    }
    public void setTag(Tag tag) {
        this.tag = tag;
        this.field = null;
        this.property = null;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        if (tag != null)
            throw new IllegalStateException("Cannot set a value on a filter by tag.");
        this.value = value;
    }

    public Report getParentReport() {
        return parentReport;
    }
    public void setParentReport(Report parentReport) {
        this.parentReport = parentReport;
    }

    @Override
    public String toString() {
        if (field != null) {
            return "[ Field: " + field + " " + operation.toString() + " " + value + " ]";
        }

        if (property != null) {
            return "[ Property: " + property.getName() + " " + operation.toString() + " " + value + " ]";
        }

        if (tag != null) {
            return "[ Tag IS " + tag.getName() + " ]";
        }

        return "Error! Invalid report filter definition.";
    }
}
