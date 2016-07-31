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
package org.openepics.discs.conf.webservice;


import java.io.Serializable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.openepics.discs.conf.ejb.ComptypeEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.jaxb.InstallationSlotName;
import org.openepics.discs.conf.jaxb.lists.InstallationSlotNameList;
import org.openepics.discs.conf.jaxrs.InstallationSlotNameResource;
import org.openepics.discs.conf.util.Utility;

import com.google.common.base.Strings;

/**
 * An implementation of the InstallationSlotBasicResource interface.
 *
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class InstallationSlotNameResourceImpl implements InstallationSlotNameResource, Serializable {
    private static final long serialVersionUID = -1014268826571453590L;

    @Inject private SlotEJB slotEJB;
    @Inject private ComptypeEJB comptypeEJB;

    @Override
    public InstallationSlotNameList getAllInstallationSlotNames(String deviceTypeName) {
        return new InstallationSlotNameList(
                Strings.isNullOrEmpty(deviceTypeName)
                            ? slotEJB.findAll().stream().
                                    filter(s -> !s.getComponentType().getName().equals(SlotEJB.ROOT_COMPONENT_TYPE)
                                                && !s.getComponentType().getName().equals(SlotEJB.GRP_COMPONENT_TYPE)).
                                    map(InstallationSlotName::new).collect(Collectors.toList())
                            : Utility.nullableToStream(comptypeEJB.findByName(deviceTypeName)).
                                    flatMap(compType -> slotEJB.findByComponentType(compType).stream()).
                                    filter(s -> !s.getComponentType().getName().equals(SlotEJB.ROOT_COMPONENT_TYPE)
                                            && !s.getComponentType().getName().equals(SlotEJB.GRP_COMPONENT_TYPE)).
                                    map(InstallationSlotName::new).collect(Collectors.toList())
                );
    }
}
