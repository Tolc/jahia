/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.opensearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 10 sept. 2008
 * Time: 17:01:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchEngineGroupBean {

    private String name;

    private List<String> engineNames = new ArrayList<String>();

    public SearchEngineGroupBean() {
    }

    public SearchEngineGroupBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEngineNames() {
        return engineNames;
    }

    public void setEngineNames(List<String> engineNames) {
        this.engineNames = engineNames;
    }

    public void addEngineName(String searchEngineName){
        if (searchEngineName!= null && !"".equals(searchEngineName.trim())){
            this.engineNames.add(searchEngineName);
        }
    }
}