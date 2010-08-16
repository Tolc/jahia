/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:30:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RightsTabItem extends EditEngineTabItem {
    private AclEditor rightsEditor;

    public RightsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.rights", "Rights"), engine);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() != null) {
            setProcessed(true);
            final GWTJahiaNode node = engine.getNode();
            getACL(node);
        } else if (engine.getParentNode() != null)  {
            setProcessed(true);
            final GWTJahiaNode node = engine.getParentNode();
            getACL(node);
        }
    }

    private void getACL(final GWTJahiaNode node) {
        mask("Loading ACL ...");
        JahiaContentManagementService.App.getInstance().getACL(node.getPath(), new BaseAsyncCallback<GWTJahiaNodeACL>() {
            /**
             * onsuccess
             * @param gwtJahiaNodeACL
             */
            public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                unmask();
                // auth. editor
                rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
                rightsEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL); //todo parameterize
                rightsEditor.setCanBreakInheritance(false);
                if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                    rightsEditor.setReadOnly(true);
                } else {
                    rightsEditor.setReadOnly(!node.isWriteable() || node.isLocked());
                }
                Button saveButton = rightsEditor.getSaveButton();
                if (toolbarEnabled) {
                    saveButton.setVisible(true);
                    saveButton.addSelectionListener(new SaveAclSelectionListener(engine.getNode()));
                }


                setLayout(new FitLayout());
                rightsEditor.addNewAclPanel(RightsTabItem.this);
                layout();
            }

            /**
             * On failure
             * @param throwable
             */
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot retrieve acl", throwable);
            }
        });
    }

    protected class SaveAclSelectionListener extends SelectionListener<ButtonEvent> {
        private GWTJahiaNode selectedNode;
        private GWTJahiaNodeACL acl;

        private SaveAclSelectionListener(GWTJahiaNode selectedNode) {
            this.selectedNode = selectedNode;
            this.acl = rightsEditor.getAcl();
        }

        public void componentSelected(ButtonEvent event) {
            JahiaContentManagementService.App.getInstance().setACL(selectedNode.getPath(), acl, new BaseAsyncCallback() {
                public void onSuccess(Object o) {
                    Info.display("", "ACL saved");
                    rightsEditor.setSaved();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("acl save failed", throwable);
                }
            });
        }
    }


    public AclEditor getRightsEditor() {
        return rightsEditor;
    }

}
