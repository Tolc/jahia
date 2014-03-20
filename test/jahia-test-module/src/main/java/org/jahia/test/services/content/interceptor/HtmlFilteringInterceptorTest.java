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
package org.jahia.test.services.content.interceptor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.interceptor.HtmlFilteringInterceptor;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for the {@link HtmlFilteringInterceptor}.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlFilteringInterceptorTest extends HtmlFilteringInterceptor {
	private static JCRNodeWrapper node;
	private static JCRSessionWrapper session;

	private static String loadContent(String resource) throws IOException {
		String content = null;

		InputStream is = HtmlFilteringInterceptorTest.class.getResourceAsStream(resource);
		try {
			content = IOUtils.toString(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		assertNotNull("Cannot read content from resource '" + resource+ "'", content);

		return content;
	}

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		TestHelper.createSite("html-filtering");
		session = JCRSessionFactory.getInstance().getCurrentUserSession();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		TestHelper.deleteSite("html-filtering");
	}

	@Before
	public void setUp() throws RepositoryException {

		JCRNodeWrapper shared = session.getNode("/sites/html-filtering/contents");
		if (!shared.isCheckedOut()) {
			session.checkout(shared);
		}
		if (shared.hasNode("html-filtering")) {
			shared.getNode("html-filtering").remove();
		}

		node = shared.addNode("html-filtering", "jnt:mainContent");
		session.save();
	}

	@After
	public void tearDown() throws Exception {
		node.remove();
		session.save();
	}

	@Test
	public void testFilteringDisabled() throws Exception {
		String source = "abc";
		assertEquals("Filtering should nor be done as the tag set is empty", source,
		        HtmlFilteringInterceptor.filterTags(source, Collections.EMPTY_SET, false));
	}

	@Test
	public void testHr() throws Exception {
		String source = loadContent("hr.txt");
		
		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("script", "object", "hr")), false);
		assertFalse("<hr/> tag was not removed", out.contains("<hr"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text 2"));
	}

	@Test
	public void testFormatting() throws Exception {
		String source = loadContent("formatting.txt");
		
		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("b", "i", "strong")), false);
		assertFalse("<strong/> tag was not removed", out.contains("<strong"));
		assertFalse("<i/> tag was not removed", out.contains("<i"));
		assertFalse("<b/> tag was not removed", out.contains("<b"));
		assertTrue("other elements were incorrectly removed", out.contains("video") && out.contains("here:") && out.contains("require") && out.contains("market") && out.contains("Jahia Solutions"));
	}

}
