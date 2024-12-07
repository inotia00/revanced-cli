/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)
package org.xmlpull.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"deprecation", "unchecked"})
public class XmlPullParsersFactory {
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected ArrayList parserClasses;
    protected ArrayList serializerClasses;
    protected String classNamesLocation = null;
    protected HashMap<String, Boolean> features = new HashMap<String, Boolean>();
    protected XmlPullParsersFactory() {
        parserClasses = new ArrayList<String>();
        serializerClasses = new ArrayList<String>();
        try {
            parserClasses.add(Class.forName("com.reandroid.xml.kxml2.KXmlParser"));
            serializerClasses.add(Class.forName("com.reandroid.xml.kxml2.KXmlSerializers"));
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        }
    }
    public void setFeature(String name, boolean state) throws XmlPullParsersException {
        features.put(name, state);
    }
    public boolean getFeature(String name) {
        Boolean value = features.get(name);
        return value != null ? value.booleanValue() : false;
    }
    public void setNamespaceAware(boolean awareness) {
        features.put (XmlPullParsers.FEATURE_PROCESS_NAMESPACES, awareness);
    }
    public boolean isNamespaceAware() {
        return getFeature(XmlPullParsers.FEATURE_PROCESS_NAMESPACES);
    }
    public void setValidating(boolean validating) {
        features.put(XmlPullParsers.FEATURE_VALIDATION, validating);
    }

    public boolean isValidating() {
        return getFeature(XmlPullParsers.FEATURE_VALIDATION);
    }
    public XmlPullParsers newPullParser() throws XmlPullParsersException {
        final XmlPullParsers pp = getParserInstance();
        for (Map.Entry<String, Boolean> entry : features.entrySet()) {
            if (entry.getValue()) {
                pp.setFeature(entry.getKey(), entry.getValue());
            }
        }
        return pp;
    }
    private XmlPullParsers getParserInstance() throws XmlPullParsersException {
        ArrayList<Exception> exceptions = null;
        if (parserClasses != null && !parserClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : parserClasses) {
                try {
                    if (o != null) {
                        Class<?> parserClass = (Class<?>) o;
                        return (XmlPullParsers) parserClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }
        throw newInstantiationException("Invalid parser class list", exceptions);
    }
    private XmlSerializers getSerializerInstance() throws XmlPullParsersException {
        ArrayList<Exception> exceptions = null;
        if (serializerClasses != null && !serializerClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : serializerClasses) {
                try {
                    if (o != null) {
                        Class<?> serializerClass = (Class<?>) o;
                        return (XmlSerializers) serializerClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }
        throw newInstantiationException("Invalid serializer class list", exceptions);
    }
    private static XmlPullParsersException newInstantiationException(String message,
                                                                     ArrayList<Exception> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return new XmlPullParsersException(message);
        } else {
            XmlPullParsersException exception = new XmlPullParsersException(message);
            for (Exception ex : exceptions) {
                exception.addSuppressed(ex);
            }
            return exception;
        }
    }

    public XmlSerializers newSerializer() throws XmlPullParsersException {
        return getSerializerInstance();
    }
    public static XmlPullParsersFactory newInstance () throws XmlPullParsersException {
        return new XmlPullParsersFactory();
    }
    public static XmlPullParsersFactory newInstance (String unused, Class unused2)
            throws XmlPullParsersException {
        return newInstance();
    }
}
