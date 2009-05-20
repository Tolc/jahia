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
package org.jahia.hibernate.manager;

import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jahia.hibernate.dao.SubscriptionDAO;
import org.jahia.hibernate.model.SubscriptionData;
import org.jahia.services.notification.Subscription;
import org.jahia.services.notification.Subscription.Channel;
import org.jahia.services.notification.Subscription.Type;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Hibernate manager class for handling the persistence of user subscriptions.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionManager {

    private static final XStream PROPERTIES_SERIALIZER = new XStream(
            new XppDriver() {
                @Override
                public HierarchicalStreamWriter createWriter(Writer out) {
                    return new CompactWriter(out, xmlFriendlyReplacer());
                }
            });

    private static Map<String, String> toProperties(String value) {
        Map<String, String> properties = new HashMap<String, String>();
        if (value != null && value.length() > 0) {
            properties = (Map<String, String>) PROPERTIES_SERIALIZER
                    .fromXML(value);
        }
        return properties;
    }

    private static String toValue(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }

        return PROPERTIES_SERIALIZER.toXML(properties);
    }

    private SubscriptionDAO subscriptionDao;

    public Subscription confirm(int subscriptionId) {
        SubscriptionData subscription = subscriptionDao
                .findById(subscriptionId);
        if (subscription != null && !subscription.isEnabled()) {
            subscription.setEnabled(true);
            subscriptionDao.update(subscription);
        }
        return subscription != null ? toBusinessObject(subscription) : null;
    }

    public void delete(int subscriptionId) {
        subscriptionDao.delete(subscriptionId);
    }

    public void delete(Subscription subscription) {
        subscriptionDao.delete(toDataObject(subscription));
    }

    public void deleteAll(Subscription subscription) {
        subscriptionDao
                .deleteAll(subscriptionDao.findAll(toDataObject(subscription),
                        "includeChildren", "enabled", "suspended",
                        "confirmationKey", "confirmationRequestTimestamp"));
    }

    /**
     * Return a list of subscriptions to parent objects matching the specified
     * criteria.
     * 
     * @param criteria
     *            the subscription data object template with the available
     *            search criteria
     * @param parentObjectKeys
     *            and array of parent object keys
     * @return list of subscription data objects matching the criteria
     */
    public List<Subscription> getParentSubscriptions(Subscription criteria,
            String[] parentObjectKeys) {
        SubscriptionData template = toDataObject(criteria);
        template.setIncludeChildren(true);
        List<SubscriptionData> subscriptions = subscriptionDao.findAllParents(
                template, parentObjectKeys, "confirmationKey",
                "confirmationRequestTimestamp");
        return toBusinessObjects(subscriptions);
    }

    /**
     * Find subscription by ID.
     * 
     * @param id
     *            the subscription ID
     * @return the requested subscription or <code>null</code> if the
     *         subscription with the specified ID could not be found
     */
    public Subscription getSubscription(int id) {
        return toBusinessObject(subscriptionDao.findById(id));
    }

    /**
     * Find subscriptions "by example".
     * 
     * @param criteria
     *            the subscription data object template with the available
     *            search criteria
     * @param excludedProperties
     *            properties to ignore in the criteria
     * @return list of subscription data objects matching the criteria
     */
    public List<Subscription> getSubscriptions(Subscription criteria,
            String... excludedProperties) {
        return toBusinessObjects(subscriptionDao.findAll(
                toDataObject(criteria), excludedProperties));
    }

    public void setSubscriptionDAO(SubscriptionDAO subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public Subscription suspend(int subscriptionId, boolean doSuspend) {
        SubscriptionData subscription = subscriptionDao
                .findById(subscriptionId);
        if (subscription != null
                && (doSuspend && !subscription.isSuspended() || !doSuspend
                        && subscription.isSuspended())) {
            subscription.setSuspended(doSuspend);
            subscriptionDao.update(subscription);
        }
        return subscription != null ? toBusinessObject(subscription) : null;
    }

    private Subscription toBusinessObject(SubscriptionData dataObj) {

        return dataObj != null ? toBusinessObject(dataObj, new Subscription())
                : null;
    }

    private Subscription toBusinessObject(SubscriptionData dataObj,
            Subscription businessObj) {

        if (null == dataObj) {
            return null;
        }

        businessObj.setId(dataObj.getId());
        businessObj.setObjectKey(dataObj.getObjectKey());
        businessObj.setIncludeChildren(dataObj.isIncludeChildren());
        businessObj.setEventType(dataObj.getEventType());
        businessObj.setUsername(dataObj.getUsername());
        businessObj.setUserRegistered(dataObj.isUserRegistered());
        businessObj.setSiteId(dataObj.getSiteId());
        businessObj.setChannel(Channel.valueOf(dataObj.getChannel()));
        businessObj.setType(Type.valueOf(dataObj.getType()));
        businessObj.setEnabled(dataObj.isEnabled());
        businessObj.setSuspended(dataObj.isSuspended());
        businessObj.setConfirmationKey(dataObj.getConfirmationKey());
        businessObj.setConfirmationRequestTimestamp(dataObj
                .getConfirmationRequestTimestamp());
        businessObj.setProperties(toProperties(dataObj.getProperties()));

        return businessObj;
    }

    private List<Subscription> toBusinessObjects(
            List<SubscriptionData> dataObjects) {

        if (dataObjects.isEmpty()) {
            return Collections.emptyList();
        }
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        for (SubscriptionData subscriptionData : dataObjects) {
            subscriptions.add(toBusinessObject(subscriptionData));
        }
        return subscriptions;
    }

    private SubscriptionData toDataObject(Subscription businessObj) {

        SubscriptionData dataObj = null;
        if (businessObj != null) {
            dataObj = new SubscriptionData(businessObj.getId(), businessObj
                    .getObjectKey(), businessObj.isIncludeChildren(),
                    businessObj.getEventType(), businessObj.getUsername(),
                    businessObj.isUserRegistered(), businessObj.getSiteId(),
                    businessObj.getChannel().toChar(), businessObj.getType()
                            .toChar(), businessObj.isEnabled(), businessObj
                            .isSuspended());
            dataObj.setConfirmationKey(businessObj.getConfirmationKey());
            dataObj.setConfirmationRequestTimestamp(businessObj
                    .getConfirmationRequestTimestamp());
            dataObj.setProperties(toValue(businessObj.getProperties()));
        }
        return dataObj;
    }

    public Subscription update(Subscription subscription) {
        return toBusinessObject(subscriptionDao
                .update(toDataObject(subscription)), subscription);
    }

}
