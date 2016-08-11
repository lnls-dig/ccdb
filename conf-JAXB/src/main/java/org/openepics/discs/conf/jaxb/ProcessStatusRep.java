/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Cable Database.
 * Cable Database is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 2 of the License, or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.ccdb.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;

/**
 * Representation of a process's status
 *
 * @author <a href="mailto:vuppala@msu.edu">Vasu Vuppala</a>
 */
@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessStatusRep {
   private String process;
   private String processDescription;
   private String status;
   private String statusDescription;
   private Boolean approved;
    
    public ProcessStatusRep() { }

    public static ProcessStatusRep newInstance(ProcessStatus procStat) {
        ProcessStatusRep psr = new ProcessStatusRep();

        psr.process = procStat.getField().getProcess().getName();
        psr.processDescription = procStat.getField().getProcess().getDescription();
        if (procStat.getStatus() != null) {
            psr.status = procStat.getStatus().getName();
            psr.statusDescription = procStat.getStatus().getDescription();
            psr.approved = procStat.getStatus().getLogicalValue();
        } else {
            psr.status = "NA";
            psr.statusDescription = "Not Applicable";
            psr.approved  = null;
        }

        return psr;
    }
    // -- getters and setters

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(String processDescription) {
        this.processDescription = processDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    
}
