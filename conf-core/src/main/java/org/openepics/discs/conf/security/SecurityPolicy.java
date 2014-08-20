/**
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any newer version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openepics.discs.conf.security;

import javax.ejb.Local;

import org.openepics.discs.conf.ent.EntityTypeOperation;


/**
 * Abstract SecurityPolicy interface. Implementations should contain all needed a&a functionality.
 * 
 * @author mpavleski
 *
 */
@Local
public interface SecurityPolicy {
    /**
     * Returns the user id (user-name) for the current user
     * 
     * @return
     */
    public String getUserId();


    /**
     * Method used to login (authenticate)
     * 
     * @return
     */
    public void login(String userName, String password);

    
    /**
     * Method used for logout
     */
    public void logout();
    
    
    /**
     * Checks if user is authorized to do opeeration operationType on entity of entityType
     * 
     * @param entity The target entity
     * @param operationType The operation type
     */
    public void checkAuth(Object entity, EntityTypeOperation operationType);
    
    /**
     * Returns UI hints for the JSF/ManagedBeans layer
     * 
     * @param param
     * @return
     */
    public boolean getUIHint(String param);            
}
