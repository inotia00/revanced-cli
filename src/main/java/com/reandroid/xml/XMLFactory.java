/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.xml;

import com.reandroid.common.FileChannelInputStream;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.xml.kxml2.KXmlParser;
import com.reandroid.xml.kxml2.KXmlSerializers;
import org.xmlpull.v1.XmlPullParsers;
import org.xmlpull.v1.XmlPullParsersException;
import org.xmlpull.v1.XmlSerializers;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XMLFactory {

    public static XmlPullParsers newPullParser(String xmlContent) throws XmlPullParsersException {
        XmlPullParsers parser = newPullParser();
        StringReader reader = new StringReader(xmlContent);
        parser.setInput(reader);
        return parser;
    }
    public static XmlPullParsers newPullParser(File file) throws XmlPullParsersException {
        XmlPullParsers parser = newPullParser();
        try {
            parser.setInput(new FileChannelInputStream(file), StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            throw new XmlPullParsersException(ex.getMessage());
        }
        return parser;
    }
    public static XmlPullParsers newPullParser(Reader reader) throws XmlPullParsersException {
        XmlPullParsers parser = newPullParser();
        parser.setInput(reader);
        return parser;
    }
    public static XmlPullParsers newPullParser(InputStream inputStream) throws XmlPullParsersException {
        XmlPullParsers parser = newPullParser();
        parser.setInput(inputStream, StandardCharsets.UTF_8.name());
        return parser;
    }
    public static XmlPullParsers newPullParser(){
        XmlPullParsers parser = new CloseableParser();
        try {
            parser.setFeature(XmlPullParsers.FEATURE_PROCESS_NAMESPACES, true);
        } catch (Throwable ignored) {
        }
        return parser;
    }

    public static XmlSerializers newSerializer(Writer writer) throws IOException{
        XmlSerializers serializer = newSerializer();
        serializer.setOutput(writer);
        return serializer;
    }
    public static XmlSerializers newSerializer(File file) throws IOException {
        return newSerializer(FileUtil.outputStream(file));
    }
    public static XmlSerializers newSerializer(OutputStream outputStream) throws IOException{
        XmlSerializers serializer = newSerializer();
        serializer.setOutput(outputStream, StandardCharsets.UTF_8.name());
        return serializer;
    }
    public static XmlSerializers newSerializer(){
        return new CloseableSerializers();
    }

    public static void setOrigin(XmlPullParsers parser, Object origin) {
        if (parser instanceof KXmlParser) {
            ((KXmlParser) parser).setOrigin(origin);
        }
    }
    public static void setEnableIndentAttributes(XmlSerializers serializer, boolean indentAttributes) {
        KXmlSerializers kXmlSerializer = getKXmlSerializer(serializer);
        if (kXmlSerializer != null) {
            kXmlSerializer.setEnableIndentAttributes(indentAttributes);
        }
    }
    private static KXmlSerializers getKXmlSerializer(XmlSerializers serializer) {
        if (serializer instanceof KXmlSerializers) {
            return (KXmlSerializers) serializer;
        }
        while (serializer instanceof XmlSerializersWrapper) {
            serializer = ((XmlSerializersWrapper) serializer).getBaseSerializer();
            if (serializer instanceof KXmlSerializers) {
                return (KXmlSerializers) serializer;
            }
        }
        return null;
    }
}
