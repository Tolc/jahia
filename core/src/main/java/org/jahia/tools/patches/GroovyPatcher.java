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
package org.jahia.tools.patches;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

/**
 * Simple patch service that monitors specified folder (by default <code>WEB-INF/var/patches/groovy/</code>) for Groovy scripts, executes
 * them and renames the executed files.
 * 
 * @author Sergiy Shyrkov
 */
public class GroovyPatcher implements JahiaAfterInitializationService, DisposableBean, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GroovyPatcher.class);

    private static final String PATCHES_BASE = "/WEB-INF/var/patches/groovy";

    private static final Comparator<Resource> RESOURCE_COMPARATOR = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            try {
                return o1.getURI().compareTo(o2.getURI());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return 0;
        }
    };
    public static void executeScripts(Resource[] scripts) {
        long timer = System.currentTimeMillis();
        logger.info("Found new patch scripts {}. Executing...", StringUtils.join(scripts));

        for (Resource script : scripts) {
            try {
                long timerSingle = System.currentTimeMillis();
                String scriptContent = getContent(script);
                if (StringUtils.isNotEmpty(scriptContent)) {
                    ScriptEngine engine = getEngine();
                    ScriptContext ctx = new SimpleScriptContext();
                    ctx.setWriter(new StringWriter());
                    Bindings bindings = engine.createBindings();
                    bindings.put("log",
                            new LoggerWrapper(logger, logger.getName(), ctx.getWriter()));
                    ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    engine.eval(scriptContent, ctx);
                    String result = ((StringWriter) ctx.getWriter()).getBuffer().toString();
                    logger.info(
                            "Execution of script {} took {} ms with result:\n{}",
                            new String[] { script.toString(),
                                    String.valueOf(System.currentTimeMillis() - timerSingle),
                                    result });
                } else {
                    logger.warn("Content of the script {} is either empty or cannot be read. Skipping.");
                }
                rename(script, ".installed");
            } catch (Exception e) {
                logger.error(
                        "Execution of script " + script + " failed with error: " + e.getMessage(),
                        e);
                rename(script, ".failed");
            }
        }

        logger.info("Execution took {} ms", (System.currentTimeMillis() - timer));
    }

    public static void executeScripts(ServletContext ctx, String lifecyclePhase) {
        try {
            String realPath = ctx.getRealPath(PATCHES_BASE);
            File lookupFolder = realPath != null ? new File(realPath) : null;
            if (lookupFolder == null || !lookupFolder.isDirectory()) {
                return;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Looking up patches in the folder {}", lookupFolder);
            }
            List<File> patches = new LinkedList<File>(FileUtils.listFiles(lookupFolder,
                    new SuffixFileFilter(new String[] { "." + lifecyclePhase + ".groovy" },
                            IOCase.INSENSITIVE), TrueFileFilter.INSTANCE));

            if (patches == null || patches.isEmpty()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No patches were found");
                }
                return;
            }
            Collections.sort(patches);

            Resource[] resources = new Resource[patches.size()];
            for (int i = 0; i < patches.size(); i++) {
                resources[i] = patches.get(i) == null ? null : new FileSystemResource(patches.get(i));
            }

            executeScripts(resources);
        } catch (Exception e) {
            logger.error("Error executing patches", e);
        }
    }

    protected static String getContent(Resource r) throws IOException {
        InputStream in = null;
        try {
            in = r.getInputStream();
            return IOUtils.toString(in, "UTF-8");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    protected static ScriptEngine getEngine() throws ScriptException {
        try {
            return ScriptEngineUtils.getInstance().scriptEngine("groovy");
        } catch (ScriptException e) {
            if (e.getMessage() != null
                    && e.getMessage().startsWith("Script engine not found for extension")) {
                return null;
            } else {
                throw e;
            }
        }
    }

    protected static void rename(Resource script, String suffix) {
        File scriptFile;
        try {
            scriptFile = script.getFile();
            File dest = new File(scriptFile.getParentFile(), scriptFile.getName() + suffix);
            if (dest.exists()) {
                FileUtils.deleteQuietly(dest);
            }
            if (!scriptFile.renameTo(dest)) {
                logger.warn("Unable to rename script file {} to {}. Skip renaming.", script
                        .getFile().getPath(), dest.getPath());
            }
        } catch (IOException e) {
            logger.warn("Unable to rename the script file for resurce " + script
                    + " due to an error: " + e.getMessage(), e);
        }
    }

    private long interval = 5 * 60000L; // 5 minutes interval by default

    private String patchesLookup = PATCHES_BASE + "/**/*.groovy";

    private ServletContext servletContext;

    private Timer watchdog;

    public void destroy() throws Exception {
        if (watchdog != null) {
            watchdog.cancel();
        }
    }

    public void executeScripts(String lifecyclePhase) {
        executeScripts(servletContext, lifecyclePhase);
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        if (!SettingsBean.getInstance().isProcessingServer()) {
            logger.info("Script watchdog is disabled on a non-processing Jahia server");
            return;
        }
        
        if (interval > 5000 && SettingsBean.getInstance().isDevelopmentMode()) {
            // in development mode reduce monitoring interval to 5 seconds
            interval = 5000;
        }

        if (interval <= 0) {
            logger.info("The interval for the Groovy patcher is <= 0. Skip starting file watcher.");
            return;
        }

        if (StringUtils.isEmpty(patchesLookup)) {
            logger.info("The patches lookup path is not set. Skip starting file watcher.");
            return;
        }

        try {
            if (getEngine() == null) {
                logger.error("The Groovy engine is not evailable. Skip starting file watcher.");
                return;
            }
        } catch (ScriptException e) {
            throw new JahiaInitializationException(e.getMessage(), e);
        }
        
        // execute scripts right now
        perform();

        // start watchdog for monitoring
        watchdog = new Timer(true);
        watchdog.schedule(new TimerTask() {
            @Override
            public void run() {
                perform();
            }
        }, 0, interval);
    }

    private void perform() {
        if (logger.isTraceEnabled()) {
            logger.trace("Checking for avilable Groovy patches in {}", patchesLookup);
        }
        Resource[] resources = null;
        try {
            resources = SpringContextSingleton.getInstance().getResources(patchesLookup,
                    false);
        } catch (IOException e) {
            logger.error(
                    "Error looking up patches in " + patchesLookup + ". Cause: "
                            + e.getMessage(), e);
        }
        if (resources == null || resources.length == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("No new Groovy patches found in {}. Sleeping...",
                        patchesLookup);
            }
            return;
        }

        Arrays.sort(resources, RESOURCE_COMPARATOR);
        executeScripts(resources);
    }
    
    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setPatchesLookup(String patchesLookup) {
        this.patchesLookup = patchesLookup;
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
