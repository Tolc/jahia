package org.jahia.bin;

import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Node;
import javax.jcr.ItemNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 13, 2009
 * Time: 6:52:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Render extends HttpServlet {
    private static Logger logger = Logger.getLogger(Render.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getPathInfo();


        try {
            ProcessingContext ctx = Jahia.createParamBean(req, resp, req.getSession());

            int index = path.indexOf('/', 1);
            String workspace = path.substring(1, index);
            path = path.substring(index);

            StringBuffer out = render(workspace, path, ctx, req, resp);

            resp.setContentType("text/html");
            resp.setContentLength(out.length());

            PrintWriter writer = resp.getWriter();
            writer.print(out.toString());
            writer.close();
        } catch (Exception e) {

            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new ServletException(e);
        }

    }

    public StringBuffer render(String workspace, String path, ProcessingContext ctx, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, IOException {
        Resource r = resolveResource(workspace, path, ctx.getUser());
        try {
            if (workspace.equals("default")) {
                ctx.setOperationMode("edit");
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        Node current = r.getNode();
        try {
            while (true) {
                if (current.isNodeType("jnt:jahiaVirtualsite") || current.isNodeType("jnt:virtualsite")) {
                    String sitename = current.getName();
                    try {
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitename);
                        ctx.setSite(site);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    break;
                }
                current = current.getParent();
            }
        } catch (ItemNotFoundException e) {
            // no site
        }

        return RenderService.getInstance().render(r, request, response);
    }

    private Resource resolveResource(String workspace, String path, JahiaUser user) throws RepositoryException {
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession(user, workspace);

        JCRNodeWrapper node = null;

        String ext = null;
        String tpl = null;

        while (true) {
            int i = path.lastIndexOf('.');
            if (i > path.lastIndexOf('/')) {
                if (ext == null) {
                    ext = path.substring(i+1);
                } else {
                    tpl = path.substring(i+1);
                }
                path = path.substring(0,i);
            } else {
                throw new PathNotFoundException("not found");
            }
            try {
                node = session.getNode(path);
                break;
            } catch (PathNotFoundException e) {
            }
        }
        Resource r = new Resource(node, ext, tpl);
        return r;
    }


}
