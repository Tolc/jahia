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
package org.jahia.services.render.filter;

import net.htmlparser.jericho.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simple regular expression filter
 *
 * Make replacements of all entries of the regexp table. Examples of use :
 *
 * <bean class="org.jahia.services.render.filter.RegexpFilter" >
 *   <property name="regexp">
 *      <map>
 *         <entry key="(banana)">
 *             <value><![CDATA[<em>fried $1</em>]]></value>
 *         </entry>
 *         <entry key="\[([a-zA-Z]*)\]">
 *             <value><![CDATA[<em>$1</em>]]></value>
 *         </entry>
 *       </map>
 *   </property>
 * </bean>
 */
public class RegexpFilter extends AbstractFilter {
    private Map<Pattern, String> regexp;
    private boolean contentOnly;

    public void setRegexp(Map<String, String> regexp) {
        if (!regexp.isEmpty()) {
            // compile patterns
            this.regexp = new LinkedHashMap<Pattern, String>();
            for (Map.Entry<String, String> replacement : regexp.entrySet()) {
                this.regexp.put(Pattern.compile(replacement.getKey()), replacement.getValue());
            }
        }
    }

    public void setContentOnly(boolean contentOnly) {
        this.contentOnly = contentOnly;
    }


    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (regexp == null) {
            return previousOut;
        }

        if (!contentOnly) {
            for (Map.Entry<Pattern, String> replacement : regexp.entrySet()) {
                previousOut = replacement.getKey().matcher(previousOut).replaceAll(replacement.getValue());
            }
            return previousOut;
        } else {
            StringBuffer buffer = new StringBuffer(previousOut);
            Source source = new Source(previousOut);
            List<Tag> tags = source.getAllTags();
            int offset = 0;
            for (Tag tag : tags) {
                int begin = tag.getEnd() + offset;
                int end = tag.getNextTag() != null ? tag.getNextTag().getBegin() + offset : previousOut.length() + offset - 1;

                String repl = buffer.substring(begin, end);
                for (Map.Entry<Pattern, String> replacement : regexp.entrySet()) {
                     repl = replacement.getKey().matcher(repl).replaceAll(replacement.getValue());
                }

                buffer.replace(begin, end, repl);
                offset += repl.length() - (end-begin);
            }
            return buffer.toString();
        }
    }

}
