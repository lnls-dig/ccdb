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
import org.openepics.discs.ccdb.model.cl.Approval;
import org.openepics.discs.ccdb.model.cl.ProcessStatus;
import org.openepics.discs.ccdb.model.cl.Process;

/**
 * Representation of a process's status
 *
 * @author <a href="mailto:vuppala@msu.edu">Vasu Vuppala</a>
 */
@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApprovalRep {
   
   private static final String CLPROCESS = "Checklist";
   
   private String process;
   private Boolean approved;
    
    public ApprovalRep() { }

    public static ApprovalRep newInstance(Process process, Approval approval) {
        ApprovalRep psr = new ApprovalRep();

        psr.process = process.getName();
        psr.approved = approval == null? false: approval.getApproved();  
        
        return psr;
    }
    
    public static ApprovalRep newInstance(ProcessStatus status) {
        ApprovalRep psr = new ApprovalRep();

        psr.process = CLPROCESS;
        
        psr.approved = status == null? false: status.getStatus().getLogicalValue();
        
        return psr;
    }
    // -- getters and setters

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }   
}
