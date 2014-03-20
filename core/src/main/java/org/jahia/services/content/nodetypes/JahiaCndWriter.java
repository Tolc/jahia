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
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
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
package org.jahia.services.content.nodetypes;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.version.OnParentVersionAction;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Prints node type defs in a compact notation
 * Print Format:
 * <ex = "http://apache.org/jackrabbit/example">
 * [ex:NodeType] > ex:ParentType1, ex:ParentType2
 * orderable mixin
 *   - ex:property (STRING) = 'default1', 'default2'
 *     primary mandatory autocreated protected multiple VERSION
 *     < 'constraint1', 'constraint2'
 *   + ex:node (ex:reqType1, ex:reqType2) = ex:defaultType
 *     mandatory autocreated protected multiple VERSION
 */
public class JahiaCndWriter {

    /**
     * the indention string
     */
    private static final String INDENT = " ";

    private static final HashSet<String> ALL_OPERATORS = Sets.newHashSet(Lexer.ALL_OPERATORS);

    /**
     * the underlying writer
     */
    private Writer out;

    public JahiaCndWriter(Writer out) {
        this.out = out;
    }

    public JahiaCndWriter(NodeTypeIterator nti, Map<String,String> namespaces, Writer out) throws IOException {
        this.out = out;
        writeNamespaces(namespaces);
        while (nti.hasNext()) {
            ExtendedNodeType ntd = (ExtendedNodeType) nti.nextNodeType();
            write(ntd);
        }
    }

    public JahiaCndWriter(Collection<ExtendedNodeType> l, Writer out) throws IOException {
        this.out = out;

        for (ExtendedNodeType aL : l) {
            write(aL);
        }
    }

    /**
     * Flushes all pending write operations and Closes this writer. please note,
     * that the underlying writer remains open.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        out.flush();
        out.close();
        out = null;
    }

    private void writeNamespaces(Map<String,String> namespaces) throws IOException {
        for (Map.Entry<String,String> k : namespaces.entrySet()) {
            out.write("<" + k.getKey() + " = '" + k.getValue() + "'>\n");
        }
        out.write("\n");
    }

    /**
     * Write one NodeTypeDefExtension to this writer
     *
     * @param d
     * @throws java.io.IOException
     */
    public void write(ExtendedNodeType d) throws IOException {
        if (d.getName().startsWith("nt:") || d.getName().startsWith("mix:")) {
            return;
        }
        writeName(d);
        writeSupertypes(d);
        writeOptions(d);
        writeItemDefExtensions(d);
        out.write("\n\n");
    }

    /**
     * write name
     */
    private void writeName(ExtendedNodeType ntd) throws IOException {
        out.write("[");
        out.write(ntd.getName());
        out.write("]");
    }

    /**
     * write supertypes
     */
    private void writeSupertypes(ExtendedNodeType ntd) throws IOException {
        ExtendedNodeType[] sta = ntd.getDeclaredSupertypes();
        String delim = " > ";
        for (int i = 0; i < sta.length; i++) {
            out.write(delim);
            out.write(sta[i].getName());
            delim = ", ";
        }
    }

    /**
     * write options
     */
    private void writeOptions(ExtendedNodeType ntd) throws IOException {
        if (ntd.hasOrderableChildNodes()) {
            out.write(" orderable");
        }
        if (ntd.isMixin()) {
            out.write(" mixin");
        }
        if (ntd.isAbstract()) {
            out.write(" abstract");
        }
        if (!ntd.isQueryable()) {
            out.write(" noquery");
        }
        List<ExtendedNodeType> mixinExtends = ntd.getMixinExtends();
        if (mixinExtends != null && !mixinExtends.isEmpty()) {
            out.write("\n" + INDENT);
            out.write("extends = ");
            Iterator<ExtendedNodeType> it = mixinExtends.iterator();
            while (it.hasNext()) {
                out.write(it.next().getName());
                if (it.hasNext()) {
                    out.write(", ");
                }
            }
        }
        String itemsType = ntd.getItemsType();
        if (itemsType != null) {
            out.write("\n" + INDENT);
            out.write("itemtype = ");
            out.write(itemsType);
        }
    }

    /**
     * write prop defs
     */
    private void writeItemDefExtensions(ExtendedNodeType ntd) throws IOException {
        List items = ntd.getDeclaredItems(true);
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            ExtendedItemDefinition itemDefExtension = (ExtendedItemDefinition) iterator.next();
            if (itemDefExtension.isNode()) {
                writeNodeDefExtension(ntd, (ExtendedNodeDefinition) itemDefExtension);
            } else {
                writePropertyDefExtension(ntd, (ExtendedPropertyDefinition) itemDefExtension);
            }
        }
    }

    /**
     * write prop def
     * @param pd
     */
    private void writePropertyDefExtension(ExtendedNodeType ntd, ExtendedPropertyDefinition pd) throws IOException {
        out.write("\n" + INDENT + "- ");
        out.write(pd.getName());
        out.write(" (");
        out.write(PropertyType.nameFromValue(pd.getRequiredType()).toLowerCase());

        if (SelectorType.nameFromValue(pd.getSelector()) != null &&
                (SelectorType.defaultSelectors.get(pd.getRequiredType()) == null || pd.getSelector() != SelectorType.defaultSelectors.get(pd.getRequiredType()) || !pd.getSelectorOptions().isEmpty())) {
            out.write(", ");
            out.write(SelectorType.nameFromValue(pd.getSelector()).toLowerCase());
            Map<String, String> selectorOptions = pd.getSelectorOptions();
            if (!selectorOptions.isEmpty()) {
                out.write("[");
                Iterator<Map.Entry<String, String>> keys = selectorOptions.entrySet().iterator();
                while (keys.hasNext()) {
                    Map.Entry<String, String> entry = keys.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    out.write(key);
                    if (StringUtils.isNotBlank(value)) {
                        out.write("='" + value + "'");
                    }
                    if (keys.hasNext()) {
                        out.write(",");
                    }
                }
                out.write("]");
            }
        }
        out.write(")");
        writeDefaultValues(pd.getDefaultValuesAsUnexpandedValue());
        out.write(ntd.getPrimaryItemName() != null && ntd.getPrimaryItemName().equals(pd.getName()) ? " primary" : "");
        if (pd.isMandatory()) {
            out.write(" mandatory");
        }
        if (pd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (pd.isProtected()) {
            out.write(" protected");
        }
        if (pd.isMultiple()) {
            out.write(" multiple");
        }
        if (pd.isHidden()) {
            out.write(" hidden");
        }
        if (pd.isInternationalized()) {
            out.write(" internationalized");
        }
        if (pd.getOnConflict() != OnConflictAction.USE_LATEST) {
            out.write(" onconflict=" + OnConflictAction.nameFromValue(pd.getOnConflict()));
        }

        if (pd.getIndex() != IndexType.TOKENIZED) {
            out.write(" indexed=" + IndexType.nameFromValue(pd.getIndex()));
        }

        if (pd.getScoreboost() != 1.) {
            out.write(" scoreboost=" + pd.getScoreboost());
        }
        if (pd.getAnalyzer() != null) {
            out.write(" analyzer=" + pd.getAnalyzer());
        }
        if (pd.isFacetable()) {
            out.write(" facetable");
        }
        if (pd.isHierarchical()) {
            out.write(" hierarchical");
        }
        if (!pd.isQueryOrderable()) {
            out.write(" noqueryorder");
        }
        if (!pd.isFullTextSearchable()) {
            out.write(" nofulltext");
        }
        if (pd.getLocalItemType() != null) {
            out.write(" itemtype = " + pd.getLocalItemType());
        }
        if (pd.getOnParentVersion() != OnParentVersionAction.VERSION) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(pd.getOnParentVersion()).toLowerCase());
        }
        String[] availableQueryOperators = pd.getAvailableQueryOperators();
        if (availableQueryOperators != null && availableQueryOperators.length > 0) {
            writeQueryOperators(availableQueryOperators);
        }

        writeValueConstraints(pd.getValueConstraintsAsValue());
    }

    private void writeQueryOperators(String[] availableQueryOperators) throws IOException {
        if (ALL_OPERATORS.equals(Sets.newHashSet(availableQueryOperators))) {
            return;
        }
        out.write(" queryops '");
        for (int i = 0; i < availableQueryOperators.length; i++) {
            if (i > 0) {
                out.write(",");
            }
            String op = availableQueryOperators[i];
            if (QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(op)) {
                out.write(Lexer.QUEROPS_EQUAL);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO.equals(op)) {
                out.write(Lexer.QUEROPS_NOTEQUAL);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN.equals(op)) {
                out.write(Lexer.QUEROPS_LESSTHAN);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO.equals(op)) {
                out.write(Lexer.QUEROPS_LESSTHANOREQUAL);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN.equals(op)) {
                out.write(Lexer.QUEROPS_GREATERTHAN);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO.equals(op)) {
                out.write(Lexer.QUEROPS_GREATERTHANOREQUAL);
            } else if (QueryObjectModelConstants.JCR_OPERATOR_LIKE.equals(op)) {
                out.write(Lexer.QUEROPS_LIKE);
            }
        }
        out.write("'");
    }

    /**
     * write default values
     * @param dva
     */
    private void writeDefaultValues(Value[] dva) throws IOException {
        if (dva != null && dva.length > 0) {
            out.write(" = ");
            out.write(StringUtils.join(getValuesAsString(dva), ", "));
        }
    }

    /**
     * write value constraints
     * @param vca
     */
    private void writeValueConstraints(Value[] vca) throws IOException {
        if (vca != null && vca.length > 0) {
            out.write(" < ");
            out.write(StringUtils.join(getValuesAsString(vca), ", "));
        }
    }
    public static List<String> getValuesAsString(Value[] values) throws IOException {
        List<String> valuesAsString = new ArrayList<String>();
        for (Value value : values) {
            StringBuilder sb = new StringBuilder();
            try {
                if (value instanceof DynamicValueImpl) {
                    DynamicValueImpl dynamicValue = (DynamicValueImpl) value;
                    sb.append(escape(dynamicValue.getFn()));
                    sb.append("(");
                    List<String> p = dynamicValue.getParams();
                    Iterator<String> it = p.iterator();
                    while (it.hasNext()) {
                        sb.append("'");
                        sb.append(escape(it.next()));
                        sb.append("'");
                        if (it.hasNext()) {
                            sb.append(" ");
                        }
                    }
                    sb.append(")");
                } else if (value.getString() != null) {
                    sb.append("'");
                    sb.append(escape(value.getString()));
                    sb.append("'");
                }
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
            valuesAsString.add(sb.toString());
        }
        return valuesAsString;
    }


    /**
     * write node def
     * @param nd
     */
    private void writeNodeDefExtension(ExtendedNodeType ntd, ExtendedNodeDefinition nd) throws IOException {
        out.write("\n" + INDENT + "+ ");

        String name = nd.getName();
        out.write(name);
        writeRequiredTypes(nd.getRequiredPrimaryTypes());
        writeDefaultType(nd.getDefaultPrimaryType());
        out.write(ntd.getPrimaryItemName() != null && ntd.getPrimaryItemName().equals(nd.getName()) ? " primary" : "");
        if (nd.isMandatory()) {
            out.write(" mandatory");
        }
        if (nd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (nd.isProtected()) {
            out.write(" protected");
        }
        if (nd.allowsSameNameSiblings()) {
            out.write(" multiple");
        }
        if (nd.getOnParentVersion() != OnParentVersionAction.VERSION) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(nd.getOnParentVersion()).toLowerCase());
        }
    }

    /**
     * write required types
     * @param reqTypes
     */
    private void writeRequiredTypes(NodeType[] reqTypes) throws IOException {
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (int i = 0; i < reqTypes.length; i++) {
                out.write(delim);
                out.write(reqTypes[i].getName());
                delim = ", ";
            }
            out.write(")");
        }
    }

    /**
     * write default types
     * @param defType
     */
    private void writeDefaultType(NodeType defType) throws IOException {
        if (defType != null && !defType.getName().equals("*")) {
            out.write(" = ");
            out.write(defType.getName());
        }
    }

    /**
     * escape
     * @param s
     * @return the escaped string
     */
    private static String escape(String s) {
        if (s.indexOf('\\') == -1 && s.indexOf('\'') == -1) {
            return s;
        }
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\' || sb.charAt(i) == '\'') {
                sb.insert(i, '\\');
                i++;
            }
        }
        return sb.toString();
    }
}