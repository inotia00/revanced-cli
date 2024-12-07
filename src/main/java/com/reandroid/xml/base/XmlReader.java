package com.reandroid.xml.base;

import org.xmlpull.v1.XmlPullParsers;
import org.xmlpull.v1.XmlPullParsersException;

import java.io.IOException;

public interface XmlReader {
    void parse(XmlPullParsers parser) throws XmlPullParsersException, IOException;
}
