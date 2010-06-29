package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;

/**
 * Interface defining what is a mdoule on a rendered page in edit mode
 * A module could be selected, edited, dragged etc.
 */
public abstract class Module extends LayoutContainer {

    protected String id;
    protected GWTJahiaNode node;
    protected HTML html;
    protected String path;
    protected String template;
    protected String scriptInfo;
    protected Map<String,String> moduleParams;
    protected Module parentModule;
    protected MainModule mainModule;
    protected String nodeTypes;
    protected String referenceTypes;
    protected boolean isDraggable = false;
    protected int depth;
    protected boolean selectable;
    protected Header head;

    public Module() {
    }

    protected Module(String id, String path, String template, String scriptInfo, String nodeTypes, String referenceTypes,
                     MainModule mainModule) {
        super();
        this.id = id;
        this.path = path;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.nodeTypes = nodeTypes;
        this.referenceTypes = referenceTypes;
        this.mainModule = mainModule;
    }

    protected Module(String id, String path, String template, String scriptInfo, String nodeTypes, String referenceTypes, MainModule mainModule, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.nodeTypes = nodeTypes;
        this.referenceTypes = referenceTypes;
        this.mainModule = mainModule;
    }

    public void onParsed() {

    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public String getModuleId() {
        return id;
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public String getReferenceTypes() {
        return referenceTypes;
    }

    public String getTemplate() {
        return template;
    }

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public Header getHeader() {
        return head;
    }

    protected void setHeaderText(String headerText) {
        head.setText(headerText);
    }

    public String getScriptInfo() {
        return scriptInfo;
    }
}
