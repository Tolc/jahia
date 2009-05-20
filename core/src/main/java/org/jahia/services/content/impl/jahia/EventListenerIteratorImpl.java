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
package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.EventListener;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 2, 2008
 * Time: 2:13:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventListenerIteratorImpl extends RangeIteratorImpl implements EventListenerIterator {

    public EventListenerIteratorImpl(Iterator iterator, long size) {
        super(iterator, size);
    }

    public EventListener nextEventListener() {
        return (EventListener) next();
    }
}
