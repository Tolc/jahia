/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.services.query;

import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.utils.TestHelper;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.*;

/**
 * Unit test for checking different index options
 * 
 * @author Benjamin Papez
 * 
 */
public class IndexOptionsIT extends AbstractJUnitTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(IndexOptionsIT.class);
    private final static String TESTSITE_NAME = "jcrIndexOptionsTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, null);
            assertNotNull(site);

            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory()
                    .getCurrentUserSession();
            InputStream importStream = IndexOptionsIT.class.getClassLoader()
                    .getResourceAsStream("imports/importIndexOptionNodes.xml");
            session.importXML(SITECONTENT_ROOT_NODE + "/home", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }
    
    @Before
    public void setUp() {

    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testNonIndexedFields() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'nonindexed')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                
                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));

                query = "//element(*, test:fieldsWithIndexOptions)[jcr:like(@nonIndexedSmallText, 'n%')]";
                q = queryManager.createQuery(query, Query.XPATH);
                queryResult = q.execute();

                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }
    
    private long getResultSize(NodeIterator nodes) {
        long resultSize = nodes.getSize();
        if (resultSize == -1) {
            resultSize = 0;
            for (NodeIterator it = nodes; it.hasNext(); ) {
                it.next();
                resultSize++;
            }
        }
        return resultSize;
    }
    
    @Test
    public void testNoFulltextIndexedField() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ZXY')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();

                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));                

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where indexFields.nofulltextSmallText like 'ZXY%'";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();

                assertTrue("Query did not return correct number of results", getResultSize(queryResult.getNodes()) > 0);
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }
    
    @Test
    public void testSorting() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[sortableFloat] asc";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                Node previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        double previousDouble = 0;
                        double currentDouble = 0;
                        try {
                            previousDouble = previousNode.getProperty(
                                    "sortableFloat").getDouble();
                        } catch (Exception ex) {
                        }
                        try {
                            currentDouble = currentNode.getProperty(
                                    "sortableFloat").getDouble();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousDouble <= currentDouble);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[nofulltextSmallText] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        String previousString = "";
                        String currentString = "";
                        try {
                            previousString = previousNode.getProperty(
                                    "nofulltextSmallText").getString();
                        } catch (Exception ex) {
                        }
                        try {
                            currentString = currentNode.getProperty(
                                    "nofulltextSmallText").getString();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousString.compareTo(currentString) < 0);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[simpleSmallText] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        String previousString = "";
                        String currentString = "";
                        try {
                            previousString = previousNode.getProperty(
                                    "simpleSmallText").getString();
                        } catch (Exception ex) {
                        }
                        try {
                            currentString = currentNode.getProperty(
                                    "simpleSmallText").getString();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousString.compareTo(currentString) < 0);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[untokenizedDate] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        Calendar previousDate = null;
                        Calendar currentDate = null;
                        try {
                            previousDate = previousNode.getProperty(
                                    "untokenizedDate").getDate();
                        } catch (Exception ex) {
                        }
                        try {
                            currentDate = currentNode.getProperty(
                                    "untokenizedDate").getDate();
                        } catch (Exception ex) {
                        }
                        if (previousDate != null && currentDate != null) {
                            assertTrue(previousDate.compareTo(currentDate) < 0);
                        }
                    }
                    previousNode = currentNode;
                }
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }
    
    @Test
    public void testFulltextAndNonIndexedField() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ABBA')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                NodeIterator it = queryResult.getNodes();
                assertEquals(2, it.getSize());
                Set<String> results = new HashSet<String>();
                results.add(it.nextNode().getIdentifier());
                results.add(it.nextNode().getIdentifier());
                assertTrue(results.containsAll(Arrays.asList("8c467cc3-a42c-4252-84b7-0b20ecc0ce30", 
                        "225162ba-69ac-4128-a141-fd95bd8c792e")));
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}