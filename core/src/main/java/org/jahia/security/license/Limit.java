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
/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jahia.security.license;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jahia.utils.xml.XmlWriter;
import org.jahia.resourcebundle.ResourceMessage;

/**
 * Generic limit
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Limit {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(Limit.class);

    private String klass;
    private String name;
    private String valueStr;
    private LimitValue value;
    private Validator validator;
    private License license;
    private boolean initialized = false;

    /**
     * @param name
     * @param value
     */
    public Limit (String klass, String name, String value, License license) {
        this.klass = klass;
        this.name = name;
        this.valueStr = value;
        if (ListLimitValue.isListValue(value)) {
            this.value = new ListLimitValue(value);
        } else if (RangeLimitValue.isRangeValue(value)) {
            this.value = new RangeLimitValue(value);
        } else {
            this.value = new LimitValue(value);
        }
        this.license = license;
    }

    private void init (String klass, String name, String value) {
        String className = klass;
        try {
            Class validatorClass = Class.forName(className);
            Class[] parameterTypes = new Class[3];
            parameterTypes[0] = String.class;
            parameterTypes[1] = String.class;
            parameterTypes[2] = License.class;
            Constructor validatorConstructor = validatorClass.getConstructor(
                parameterTypes);
            Object[] initArgs = new Object[3];
            initArgs[0] = name;
            initArgs[1] = value;
            initArgs[2] = license;
            validator = (Validator) validatorConstructor.newInstance(initArgs);
        } catch (ClassNotFoundException cnfe) {
            logger.error("Couldn't find validator class " + klass, cnfe);
            validator = null;
        } catch (IllegalAccessException iae) {
            logger.error("Error accessing validator class " + klass, iae);
            validator = null;
        } catch (InstantiationException ie) {
            logger.error("Validation class " + klass +
                         " instantiation exception", ie);
            validator = null;
        } catch (InvocationTargetException ite) {
            logger.error(
                "Invocation target exception while invoking validator class " +
                klass, ite);
            validator = null;
        } catch (NoSuchMethodException nsme) {
            logger.error(
                "No such method exception while creating instance of validator class " +
                klass, nsme);
            validator = null;
        }
        initialized = true;
    }

    /**
     * @return
     */
    public String getName () {
        return name;
    }

    /**
     * @return
     */
    public LimitValue getValue () {
        return value;
    }

    public void toXML (XmlWriter xmlWriter)
        throws IOException {
        xmlWriter.writeEntity("limit");
        xmlWriter.writeAttribute("class", klass);
        xmlWriter.writeAttribute("name", name);
        xmlWriter.writeAttribute("value", value.toXMLString());
        xmlWriter.endEntity();
    }

    public boolean check () {
        init(klass, name, valueStr);
        if (validator == null) {
            logger.warn(
                "Failing check because we couldn't instantiate the validator class " +
                klass);
            return false;
        }
        if (!value.check(validator)) {
            logger.debug("Validator [" + name + "] value=[" + valueStr +
                         "] check returned false.");
            return false;
        }
        return true;
    }

    public ResourceMessage getErrorMessage () {
        if (validator != null) {
            return value.getErrorMessage(validator);
        } else {
            return null;
        }
    }

    public String toSignatureData () {
        StringBuffer signDataBuf = new StringBuffer();
        signDataBuf.append(klass);
        signDataBuf.append("\n");
        signDataBuf.append(name);
        signDataBuf.append("\n");
        signDataBuf.append(valueStr);
        signDataBuf.append("\n");
        return signDataBuf.toString();
    }

    public String getValueStr () {
        return valueStr;
    }

    public License getLicense () {
        return license;
    }
    public Validator getValidator() {
        if (!initialized) {
            init(klass, name, valueStr);
        }
        return validator;
    }

}