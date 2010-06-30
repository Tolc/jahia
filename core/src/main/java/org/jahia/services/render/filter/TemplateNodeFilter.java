package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * WrapperFilter
 * <p/>
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 */
public class TemplateNodeFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(TemplateNodeFilter.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("skipWrapper") == null) {
            Wrapper wrapper = null;
            Wrapper previousWrapper = null;
            if (renderContext.getRequest().getAttribute("wrapperSet") == null) {
                wrapper = pushBodyWrappers(resource);
                renderContext.getRequest().setAttribute("wrapperSet", Boolean.TRUE);
            } else {
                previousWrapper = (Wrapper) renderContext.getRequest().getAttribute("previouswrapper");
                if (previousWrapper != null) {
                    wrapper = previousWrapper.next;
                }
            }

            if (wrapper != null && !renderContext.isAjaxRequest()) {
                try {
                    JCRNodeWrapper wrapperNode = wrapper.node;
                    renderContext.getRequest().setAttribute("previouswrapper", wrapper);
//                chain.set(renderContext.getRequest(), "previouswrapper", wrapper);
                    renderContext.getRequest().setAttribute("wrappedResource", resource);
                    renderContext.getRequest().setAttribute("wrapperNode", wrapperNode);
                    Resource wrapperResource = new Resource(wrapperNode,
                            resource.getTemplateType().equals("edit") ? "html" : resource.getTemplateType(), wrapper.template, Resource.CONFIGURATION_WRAPPER);
                    if (service.hasTemplate(wrapperNode.getPrimaryNodeType(), wrapper.template)) {
                        chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);
                        String output = RenderService.getInstance().render(wrapperResource, renderContext);
                        if (renderContext.isEditMode()) {
                            output = "<div jahiatype=\"linkedContentInfo\" linkedNode=\"" +
                                    resource.getNode().getIdentifier() + "\"" +

                                    " node=\"" + wrapperNode.getIdentifier() + "\" type=\"template\">" + output +
                                    "</div>";
                        }

                        renderContext.getRequest().setAttribute("previouswrapper", previousWrapper);

                        return output;
                    } else {
                        logger.warn("Cannot get wrapper " + wrapper);
                    }
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + wrapper, e);
                } catch (RenderException e) {
                    logger.error("Cannot execute wrapper " + wrapper, e);
                }
            }
        }
        chain.pushAttribute(renderContext.getRequest(), "inWrapper",
                (renderContext.isAjaxRequest()) ? Boolean.TRUE : Boolean.FALSE);
        return null;
    }


    public Wrapper pushBodyWrappers(Resource resource) {
        final JCRNodeWrapper node = resource.getNode();
        String template = resource.getTemplate();
        if ("default".equals(template)) {
            template = null;
        }
        JCRNodeWrapper current = node;
        Wrapper wrapper = null;
        try {
            while (current != null) {
                if (current.hasProperty("j:templateNode")) {
                    JCRNodeWrapper wrapperNode = (JCRNodeWrapper) current.getProperty("j:templateNode").getNode();
                    wrapper = addWrapper(node, template, wrapper, wrapperNode);

                    Query q = current.getSession().getWorkspace().getQueryManager().createQuery(
                            "select * from [jnt:template] as w where ischildnode(w, ['" + wrapperNode.getPath() + "'])",
                            Query.JCR_SQL2);
                    QueryResult result = q.execute();
                    NodeIterator ni = result.getNodes();
                    while (ni.hasNext()) {
                        wrapper = addWrapper(node, template, wrapper, (JCRNodeWrapper) ni.nextNode());
                    }
                    current = wrapperNode;
                    template = null;
                } else {
                    current = current.getParent();
                    if (current.isNodeType("jnt:templatesFolder")) {
                        current = null;
                    }
                }
            }
        } catch (ItemNotFoundException e) {
            // default
            wrapper = new Wrapper("bodywrapper", node, null);
        } catch (RepositoryException e) {
            logger.error("Cannot find wrapper", e);
        }
        return wrapper;
    }

    private Wrapper addWrapper(JCRNodeWrapper node, String template, Wrapper wrapper, JCRNodeWrapper wrapperNode)
            throws RepositoryException {
        boolean ok = true;
        if (wrapperNode.hasProperty("j:applyOn")) {
            ok = false;
            Value[] values = wrapperNode.getProperty("j:applyOn").getValues();
            for (Value value : values) {
                if (node.isNodeType(value.getString())) {
                    ok = true;
                    break;
                }
            }
            if (values.length == 0) {
                ok = true;
            }
        }
        if (template == null) {
            ok &= !wrapperNode.hasProperty("j:templateKey");
        } else {
            ok &= wrapperNode.hasProperty("j:templateKey") && template.equals(wrapperNode.getProperty("j:templateKey").getString());
        }
        if (ok) {
            wrapper = new Wrapper(
                    wrapperNode.hasProperty("j:template") ? wrapperNode.getProperty("j:template").getString() :
                            "default", wrapperNode, wrapper);
        }
        return wrapper;
    }

    public class Wrapper {
        public String template;
        public JCRNodeWrapper node;
        public Wrapper next;

        Wrapper(String template, JCRNodeWrapper node, Wrapper next) {
            this.template = template;
            this.node = node;
            this.next = next;
        }

        @Override
        public String toString() {
            return template + " for node " + node.getPath();
        }
    }

}