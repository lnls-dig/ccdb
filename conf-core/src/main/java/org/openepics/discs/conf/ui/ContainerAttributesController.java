/**
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any newer version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.openepics.discs.conf.ejb.PropertyEJB;
import org.openepics.discs.conf.ejb.SlotEJB;
import org.openepics.discs.conf.ent.ComponentType;
import org.openepics.discs.conf.ent.ComptypePropertyValue;
import org.openepics.discs.conf.ent.Property;
import org.openepics.discs.conf.ent.PropertyAssociation;
import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.ent.SlotArtifact;
import org.openepics.discs.conf.ent.SlotPropertyValue;
import org.openepics.discs.conf.ent.Tag;
import org.openepics.discs.conf.ui.common.AbstractAttributesController;
import org.openepics.discs.conf.views.EntityAttributeView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * @author Andraz Pozar <andraz.pozar@cosylab.com>
 *
 */
@Named
@ViewScoped
public class ContainerAttributesController extends AbstractAttributesController<SlotPropertyValue, SlotArtifact> {
    @Inject private SlotEJB slotEJB;
    @Inject private PropertyEJB propertyEJB;

    private Slot slot;
    private String parentContainer;

    @PostConstruct
    public void init() {
        final Long id = Long.parseLong(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("id"));
        slot = slotEJB.findById(id);
        super.setArtifactClass(SlotArtifact.class);
        super.setPropertyValueClass(SlotPropertyValue.class);
        super.setDao(slotEJB);

        parentProperties = slot.getComponentType().getComptypePropertyList();

        populateAttributesList();
        filterProperties();
        parentContainer = slot.getChildrenSlotsPairList().size() > 0 ? slot.getChildrenSlotsPairList().get(0).getParentSlot().getName() : null;
    }

    /**
     * Redirection back to view of all {@link ComponentType}s
     */
    public void containerRedirect() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("containers-manager.xhtml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void populateAttributesList() {
        attributes = new ArrayList<>();
        slot = slotEJB.findById(slot.getId());

        for (ComptypePropertyValue parentProp : parentProperties) {
            if (parentProp.getPropValue() != null) attributes.add(new EntityAttributeView(parentProp));
        }

        for (SlotPropertyValue prop : slot.getSlotPropertyList()) {
            attributes.add(new EntityAttributeView(prop));
        }

        for (SlotArtifact art : slot.getSlotArtifactList()) {
            attributes.add(new EntityAttributeView(art));
        }

        for (Tag tag : slot.getTags()) {
            attributes.add(new EntityAttributeView(tag));
        }
    }

    @Override
    protected void filterProperties() {
        filteredProperties = ImmutableList.copyOf(Collections2.filter(propertyEJB.findAll(), new Predicate<Property>() {
            @Override
            public boolean apply(Property property) {
                final PropertyAssociation propertyAssociation = property.getAssociation();
                return propertyAssociation == PropertyAssociation.ALL || propertyAssociation == PropertyAssociation.SLOT || propertyAssociation == PropertyAssociation.SLOT_DEVICE || propertyAssociation == PropertyAssociation.TYPE_SLOT;
            }
        }));
    }

    /**
     * Returns {@link Slot} for which attributes are being manipulated
     * @return the {@link Slot}
     */
    public Slot getSlot() {
        return slot;
    }

    @Override
    protected void setPropertyValueParent(SlotPropertyValue child) {
        child.setSlot(slot);
    }

    @Override
    protected void setArtifactParent(SlotArtifact child) {
        child.setSlot(slot);
    }

    public String getParentContainer() { return parentContainer; }

    @Override
    protected void setTagParent(Tag tag) {
        final Set<Tag> existingTags = slot.getTags();
        if (!existingTags.contains(tag)) {
            existingTags.add(tag);
            slotEJB.save(slot);
        }
    }

    @Override
    protected void deleteTagFromParent(Tag tag) {
        slot.getTags().remove(tag);
        slotEJB.save(slot);
    }
}
