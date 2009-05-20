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
package org.jahia.operations.valves;

import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.SourceFormatter;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

import org.apache.log4j.Category;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.operations.PageGeneratorQueue;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.cache.SkeletonCache;
import org.jahia.services.cache.SkeletonCacheEntry;

import java.util.*;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2004</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class SkeletonParseAndStoreValve implements Valve {
    // ------------------------------ FIELDS ------------------------------

    private static Category logger = org.apache.log4j.Logger.getLogger(SkeletonParseAndStoreValve.class);
    private PageGeneratorQueue generatorQueue;
    public static final String SESSION_ID_REGEXP = ";jsessionid=[^?\"']*([\"'])";

    public static final String ESI_INCLUDE_STARTTAG_REGEXP = "<!-- cache:include src=\\\"(.*)\\\" -->";
    public static final String ESI_INCLUDE_STOPTAG_REGEXP = "<!-- /cache:include -->";

    public static final String ESI_VAR_STARTTAG_REGEXP = "<!-- cache:vars var=\\\"(.*)\\\" -->";
    public static final String ESI_VAR_STOPTAG_REGEXP = "<!-- /cache:vars -->";

    public void setGeneratorQueue(PageGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }
// --------------------------- CONSTRUCTORS ---------------------------

    public SkeletonParseAndStoreValve() {
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface Valve ---------------------


    public void initialize() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        if (ProcessingContext.GET_METHOD == processingContext.getHttpMethod() && processingContext.settings().isOutputContainerCacheActivated()) {
            try {
                // Let's retrieve the generated content from the response wrapper object.
                String generatedOutput = processingContext.getGeneratedOutput();
                String curContentType = processingContext.getContentType();
                // Get the HTML cache instance
                SkeletonCache<GroupCacheKey, SkeletonCacheEntry> skeletonCache;
                try {
                    skeletonCache = ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance();
                } catch (JahiaInitializationException ex) {
                    throw new PipelineException(ex);
                }
                // extract the workflow state out of the parameters bean
                int workflowState = processingContext.getEntryLoadRequest().
                        getWorkflowState();
                // Get the language code
                String curLanguageCode = processingContext.getLocale().toString();
                parseAndStoreInCache(processingContext,
                                     skeletonCache,
                                     workflowState,
                                     curLanguageCode,
                                     generatedOutput,
                                     curContentType);
            } catch (java.io.IOException ioe) {
                if (logger.isDebugEnabled()) logger.debug("Error while retrieving generated output: " + ioe.getMessage(), ioe);
            }
        }
        ValveContext.valveResources.set(null);
        valveContext.invokeNext(context);
    }

    // -------------------------- OTHER METHODS --------------------------

    /**
     * Stores the generated HTML content into the specified HTML cache.
     *
     * @param context         the parameter bean
     * @param skeletonCache   the HTML cache instance
     * @param workflowState   the workflow state
     * @param languageCode    the language code
     * @param generatedOutput the html content body to be stored
     * @param contentType     the html content type
     */
    private void parseAndStoreInCache(ProcessingContext context,
                                       SkeletonCache<GroupCacheKey, SkeletonCacheEntry> skeletonCache,
                                       int workflowState,
                                       String languageCode,
                                       String generatedOutput,
                                       String contentType) {
        if (logger.isDebugEnabled()) logger.debug("Storing generated content into the HTML Cache...");
        // Cannot proceed if the user information is not available
        if (context.getUser() == null) return;
        final Map<GroupCacheKey, GroupCacheKey> notCacheablePage = generatorQueue.getNotCacheablePage();
        //        if (ProcessingContext.EDIT.equals(context.getOperationMode()) &&
        //            context.settings().isOutputCacheExpirationOnly()) {
        //            doWeCache = false;
        //        }
        if ("core".equals(context.getEngine()) && !"debug".equals(context.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER))) {
            if (context.getCacheExpirationDate() != null) {
                if (logger.isDebugEnabled()) logger.debug("Using default expiration date");
                Date nowDate = new Date();
                if (nowDate.compareTo(context.getCacheExpirationDate()) >= 0) {
                    // cache will expire immediately -> we don't cache the
                    // page.
                    return;
                }
            }
            final Set<ContentObjectKey> keyHashSet = new HashSet<ContentObjectKey>();
            keyHashSet.add(new ContentPageKey(context.getPageID()));
            // Add the entry to the cache
            String queryString = context.getQueryString();
            String aesMode = (String) context.getSessionState().getAttribute("aesMode");
            if (aesMode == null) {
                aesMode = "";
            }
            if (queryString == null) {
                queryString = "";
            }
            // this key is to create a skeleton for each display option (query string + advanced edit settings)
            final StringBuilder builder = new StringBuilder(queryString);
            if (!ProcessingContext.NORMAL.equals(context.getOperationMode()) &&
                    !ProcessingContext.EDIT.equals(context.getOperationMode())) {
                builder.append(context.getPathInfo());
            }
            builder.append(aesMode);
            String userAgentViewKey = (String) context.getAttribute(UserAgentViewSwitcherValve.VIEW_SWITCHING_VALVE_KEY_REQUEST_ATTRIBUTE_NAME);
            if (userAgentViewKey != null) {
                builder.append(userAgentViewKey);
            }
            String additionalKey = builder.toString();
            GroupCacheKey entryKey = ServicesRegistry.getInstance()
                    .getCacheKeyGeneratorService()
                    .computeSkeletonEntryKeyWithGroups(context.getPage(),
                                                       additionalKey,
                                                       context.getUser(),
                                                       languageCode,
                                                       context.getOperationMode(),
                                                       context.getScheme(),
                                                       keyHashSet);
            boolean doWeCache = !notCacheablePage.containsKey(entryKey);
            if (generatedOutput != null &&
                generatedOutput.length() > 0 &&
                doWeCache) {
                // let's update the cache...
                if (logger.isDebugEnabled()) {
                    StringBuffer buffer = new StringBuffer("Storing output in cache for page=");
                    buffer.append(context.getPageID());
                    buffer.append(" user=").append(context.getUser().getUsername());
                    buffer.append(" languageCode=");
                    buffer.append(languageCode);
                    buffer.append(" workflowState=");
                    buffer.append(workflowState);
                    buffer.append(" userAgent=");
                    buffer.append(context.getUserAgent());
                    buffer.append("...");
                    logger.debug(buffer.toString());
                }
                // first we will quickly replace all the HTML comments with real tags, this is a hack to avoid
                // generating tags to the browser that causes DOM parsing problems. Can we find a better way to do this ?
                generatedOutput = generatedOutput.replaceAll(ESI_INCLUDE_STOPTAG_REGEXP, "</esi:include>");
                generatedOutput = generatedOutput.replaceAll(ESI_INCLUDE_STARTTAG_REGEXP, "<esi:include src=\"$1\">");
                generatedOutput = generatedOutput.replaceAll(ESI_VAR_STOPTAG_REGEXP, "</esi:vars>");
                generatedOutput = generatedOutput.replaceAll(ESI_VAR_STARTTAG_REGEXP, "<esi:vars var=\"$1\">");

                Source source = new Source(cleanHtml(generatedOutput));
                // This will remove all blank line and drastically reduce data in memory
                source = new Source((new SourceFormatter(source)).toString());
                List<StartTag> esiIncludeTags = source.findAllStartTags("esi:include");
                if (logger.isDebugEnabled()) displaySegments(esiIncludeTags);
                // We will remove container content here has we do not want to store them twice in memory
                OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);
                final String output = outputDocument.toString();
                if (!output.equals("")) {
                    generatedOutput = output;
                    if (logger.isDebugEnabled()) System.out.println("generatedOutput = " + generatedOutput);
                    SkeletonCacheEntry htmlEntry = new SkeletonCacheEntry();
                    htmlEntry.setContentType(contentType);
                    htmlEntry.setContentBody(generatedOutput);
                    CacheEntry<SkeletonCacheEntry> newEntry = new CacheEntry<SkeletonCacheEntry>(htmlEntry);
                    newEntry.setOperationMode(context.getOperationMode());

                    if (context.getCacheExpirationDelay() == 0 && doWeCache) {
                        notCacheablePage.put(entryKey, entryKey);
                    }
                    // compute the entry's expiration date
                    final long expirationDelay = context.settings().getContainerCacheDefaultExpirationDelay()*1000;
                    if (expirationDelay != -1) {
                        Date nowDate = new Date();

                        // create the expiration date by adding an expiration timeout to the
                        // current system date.
                        Date expirationDate = new Date(nowDate.getTime() + expirationDelay);
                        if (logger.isDebugEnabled()) logger.debug("Set the expiration date");
                        newEntry.setExpirationDate(expirationDate);
                    }

                    // if a default expiration date has been set, us this instead.
                    if (context.getCacheExpirationDate() != null) {
                        if (logger.isDebugEnabled()) logger.debug("Using default expiration date");
                        Date nowDate = new Date();
                        if (nowDate.compareTo(context.getCacheExpirationDate()) >= 0) {
                            // cache will expire immediately -> we don't cache the
                            // page.
                            return;
                        }
                        newEntry.setExpirationDate(context.getCacheExpirationDate());
                    }

                    if (!notCacheablePage.containsKey(entryKey)) {
                        skeletonCache.putCacheEntry(entryKey, newEntry, true);
                    }
                    if (logger.isDebugEnabled()) logger.debug("Added HTML page into the cache.");
                }
            }
            else {
                if (logger.isDebugEnabled()) logger.debug("Bypassing HTML cache storage");
                if (skeletonCache != null && context.getUser() != null) {
                    if (context.getCacheExpirationDelay() == 0 && doWeCache) {
                        notCacheablePage.put(entryKey, entryKey);
                    }
                }
            }
        }
    }

    private static void displaySegments(Iterable<StartTag> segments) {
        for (StartTag segment : segments) {
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println(segment.getDebugInfo());
            System.out.println(segment.getElement().getContent());
            System.out.println(segment.getAttributeValue("src"));
            System.out.println(segment.isUnregistered());
        }
        System.out.println("\n*******************************************************************************\n");
    }

    private static OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : segments) {
            outputDocument.replace(segment.getElement().getContent(), "");
        }
        return outputDocument;
    }

    private String cleanHtml(String bodyContent) {
        // Try to remove all cache/(o|b).*/ and also jessionid
        String cleanBodyContent = bodyContent.replaceAll("cache/(o|b)[a-z]*/","");
        cleanBodyContent = cleanBodyContent.replaceAll(SESSION_ID_REGEXP,"$1");
        return cleanBodyContent;
    }
}