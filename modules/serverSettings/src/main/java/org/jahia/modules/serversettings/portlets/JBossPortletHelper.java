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
package org.jahia.modules.serversettings.portlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Helper class for preparing portlet WAR files to be deployed on JBoss Application Server.
 * 
 * @author Sergiy Shyrkov
 */
final class JBossPortletHelper {

    private static void addToJar(String resource, JarOutputStream jos) throws IOException {
        jos.putNextEntry(new JarEntry("WEB-INF/" + resource));
        InputStream is = JBossPortletHelper.class.getClassLoader().getResourceAsStream(
                "META-INF/jboss-resources/" + resource);
        try {
            IOUtils.copy(is, jos);
            jos.closeEntry();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static JarEntry cloneEntry(JarEntry originalJarEntry) {
        final JarEntry newJarEntry = new JarEntry(originalJarEntry.getName());
        newJarEntry.setComment(originalJarEntry.getComment());
        newJarEntry.setExtra(originalJarEntry.getExtra());
        newJarEntry.setMethod(originalJarEntry.getMethod());
        newJarEntry.setTime(originalJarEntry.getTime());

        // Must set size and CRC for STORED entries
        if (newJarEntry.getMethod() == ZipEntry.STORED) {
            newJarEntry.setSize(originalJarEntry.getSize());
            newJarEntry.setCrc(originalJarEntry.getCrc());
        }

        return newJarEntry;
    }

    private static void copyEntries(JarInputStream source, JarOutputStream dest) throws IOException {
        JarEntry originalJarEntry = source.getNextJarEntry();
        while (originalJarEntry != null) {
            final JarEntry newJarEntry = cloneEntry(originalJarEntry);
            dest.putNextEntry(newJarEntry);
            IOUtils.copy(source, dest);
            dest.closeEntry();
            dest.flush();
            originalJarEntry = source.getNextJarEntry();
        }
    }

    /**
     * Returns a file descriptor for the modified (prepared) portlet WAR file.
     * 
     * @param sourcePortletWar
     *            the source portlet WAR file
     * @return a file descriptor for the modified (prepared) portlet WAR file
     * @throws IOException
     *             in case of processing error
     */
    public static File process(File sourcePortletWar) throws IOException {
        JarFile jar = new JarFile(sourcePortletWar);
        File dest = new File(FilenameUtils.getFullPathNoEndSeparator(sourcePortletWar.getPath()),
                FilenameUtils.getBaseName(sourcePortletWar.getName()) + ".war");
        try {
            boolean hasPortletTld = jar.getEntry("WEB-INF/portlet.tld") != null;
            boolean hasPortlet2Tld = jar.getEntry("WEB-INF/portlet_2_0.tld") != null;
            jar.close();
            final JarInputStream jarIn = new JarInputStream(new FileInputStream(sourcePortletWar));
            final Manifest manifest = jarIn.getManifest();
            final JarOutputStream jarOut;
            if (manifest != null) {
                jarOut = new JarOutputStream(new FileOutputStream(dest), manifest);
            } else {
                jarOut = new JarOutputStream(new FileOutputStream(dest));
            }

            try {
                copyEntries(jarIn, jarOut);

                addToJar("jboss-deployment-structure.xml", jarOut);

                if (!hasPortletTld) {
                    addToJar("portlet.tld", jarOut);
                }
                if (!hasPortlet2Tld) {
                    addToJar("portlet_2_0.tld", jarOut);
                }
            } finally {
                jarIn.close();
                jarOut.close();
                FileUtils.deleteQuietly(sourcePortletWar);
            }
            return dest;
        } finally {
            jar.close();
        }
    }

    private JBossPortletHelper() {
        super();
    }

}
