/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.button.Button;

import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:22
 */
public class PortletModesCard extends MashupWizardCard {
    private AclEditor modeMappingEditor;


    public PortletModesCard() {
        super(Messages.get("org.jahia.engines.MashupsManager.wizard.modesperm.label", "Modes permissions"), Messages.get("org.jahia.engines.MashupsManager.wizard.modesperm.description.label", "Set modes permissions"));
    }

    public void next() {
        if (modeMappingEditor != null) {
            getGwtJahiaNewPortletInstance().setModes(modeMappingEditor.getAcl());
        }
    }

    public void createUI() {
        removeAll();
        super.createUI();
        // update
        final GWTJahiaNodeACL acl = getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl().cloneObject();
        List<String> permissions = acl.getAvailablePermissions().get(JCRClientUtils.MODES_ACL);
        acl.setBreakAllInheritance(true);
        if (permissions != null && permissions.size() > 0) {
            JahiaContentManagementService.App.getInstance().createDefaultUsersGroupACE(permissions, true, new BaseAsyncCallback<GWTJahiaNodeACE>() {
                public void onSuccess(GWTJahiaNodeACE gwtJahiaNodeACE) {
                    Log.debug("Add group ACE");
                    removeAll();
                    List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();
                    aces.add(gwtJahiaNodeACE);
                    acl.setAce(aces);
                    initModeMappingEditor(acl);
                    modeMappingEditor.addNewAclPanel(PortletModesCard.this);
                    getPortletWizardWindow().updateWizard();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Unable to Add group ACE");
                    removeAll();
                    initModeMappingEditor(acl);
                    modeMappingEditor.addNewAclPanel(PortletModesCard.this);
                }
            });
        } else {
            add(new Label(Messages.get("org.jahia.engines.MashupsManager.wizard.modesperm.onlyViewMode", "The selected portlets contains only view mode.")));
        }
    }

    private void initModeMappingEditor(GWTJahiaNodeACL acl) {
        modeMappingEditor = new AclEditor(acl, getPortletWizardWindow().getParentNode().getAclContext());
        modeMappingEditor.setAclGroup(JCRClientUtils.MODES_ACL);
        modeMappingEditor.setAddUsersLabel(Messages.get("org.jahia.engines.MashupsManager.wizard.modes.adduser.label", "Add mode-user permission"));
        modeMappingEditor.setAddGroupsLabel(Messages.get("org.jahia.engines.MashupsManager.wizard.modes.addgroup.label", "Add mode-group permission"));
        Button saveButton = modeMappingEditor.getSaveButton();
        saveButton.setVisible(false);
        Button restoreButton = modeMappingEditor.getRestoreButton();
        restoreButton.setVisible(false);
    }

    @Override
    public void refreshLayout() {
        // do nothing
    }
}