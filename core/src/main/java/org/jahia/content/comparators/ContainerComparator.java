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
 package org.jahia.content.comparators;

import org.jahia.params.ProcessingContext;

/**
 * <p>Title: Container comparator </p>
 * <p>Description: This sub-class of the ordered comparator hard-codes it's
 * sub-comparators in an order that is logic for containers.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 * @todo Can we remove the hard-coded configuration here and put it maybe in
 * a configuration file ?
 */

public class ContainerComparator extends OrderedCompositeComparator {
    public ContainerComparator(ProcessingContext processingContext) {
        super();
        super.addComparator(new ContainerByContainerListComparator());
        if (processingContext != null) {
            super.addComparator(new ContainerRankingComparator(processingContext));
        }
    }

}