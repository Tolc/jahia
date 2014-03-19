/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.compare;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:27:04 AM
 */
public class CompareEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    private GWTJahiaNode node;
    private Linker linker = null;
    private String locale;

    //private LayoutContainer mainComponent;
    private VersionViewer leftPanel;
    protected ButtonBar buttonBar;
    private String uuid;
    private String path;
    private Date versionDate = null;
    private NodeHolder engine = null;
    private String workspace = null;
    private String versionLabel = null;
    private boolean refreshOpener = false;
    private VersionViewer rightPanel;

    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public CompareEngine(GWTJahiaNode node, String locale, Linker linker, boolean displayVersionSelector, boolean displayTwoPanels) {
        this.linker = linker;
        this.node = node;
        this.locale = locale;
        init(displayVersionSelector, displayTwoPanels);
    }

    public CompareEngine(String uuid, String locale, boolean displayVersionSelector, String path) {
        this.locale = locale;
        this.uuid = uuid;
        this.path = path;
        init(displayVersionSelector, true);
    }

    public CompareEngine(String uuid, String locale, boolean displayVersionSelector, String path, Date versionDate,
                         NodeHolder engine, String workspace, String versionLabel) {
        this.locale = locale;
        this.uuid = uuid;
        this.path = path;
        this.versionDate = versionDate;
        this.engine = engine;
        this.workspace = workspace;
        this.versionLabel = versionLabel;
        init(displayVersionSelector, true);
    }

    protected void init(boolean displayVersionSelector, boolean displayTwoPanels) {
        setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        setBodyBorder(false);
        int windowHeight = com.google.gwt.user.client.Window.getClientHeight() - 10;
        int windowWidth = com.google.gwt.user.client.Window.getClientWidth() - 10;
        setSize(windowWidth, windowHeight);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        String title = Messages.get("label.compare","Compare ");
        if (node != null) {
            setHeadingHtml(title + node.getPath());
        } else {
            setHeadingHtml(title + path);
        }
        //live version
        if (node != null) {
            leftPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                    linker, "live", false, displayVersionSelector, this);
        } else {
            leftPanel = new VersionViewer(uuid, locale, workspace != null ? workspace : "live", false, displayVersionSelector, versionDate, this, versionLabel);
        }

        add(leftPanel, new RowData(displayTwoPanels ? 0.5 : 1,1));
        if (displayTwoPanels) {
            if (node != null) {
                rightPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                        linker, displayVersionSelector ? "live" : "default", true, displayVersionSelector, this) {
                    @Override
                    public String getCompareWith() {
                        return leftPanel.getInnerHTML();
                    }
                };
            } else {
                rightPanel = new VersionViewer(uuid, locale, "default", true, displayVersionSelector, null, this, null) {
                    @Override
                    public String getCompareWith() {
                        return leftPanel.getInnerHTML();
                    }
                };
            }
            add(rightPanel, new RowData(0.5, 1));
        }
        layout();
    }


    @Override
    protected void onHide() {
        super.onHide();
        if (refreshOpener) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(Linker.REFRESH_ALL, true);
            if (engine != null) {
                engine.close();
                engine.getLinker().refresh(data);
            }
            if (linker != null) {
                linker.refresh(data);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        maximize();
    }

    public void setRefreshOpener(boolean refreshOpener) {
        this.refreshOpener = refreshOpener;
    }
    
    protected VersionViewer getLeftPanel() {
        return leftPanel;
    }
    
    protected VersionViewer getRightPanel() {
        return rightPanel;
    }
}


