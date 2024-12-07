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

import android.content.res.XmlResourceParsers;
import org.xmlpull.v1.XmlPullParsers;
import org.xmlpull.v1.XmlPullParsersException;
import org.xmlpull.v1.XmlSerializers;

import java.io.Closeable;
import java.io.IOException;

public class XmlParserToSerializer {

    private final XmlPullParsers parser;
    private XmlSerializers serializer;
    private boolean enableIndent;
    boolean processNamespace;
    boolean reportNamespaceAttrs;

    public XmlParserToSerializer(XmlPullParsers parser, XmlSerializers serializer){
        this.parser = parser;
        this.serializer = XmlIndentingSerializers.create(serializer);
        this.enableIndent = true;
        XMLUtil.setFeatureSafe(parser, XmlPullParsers.FEATURE_PROCESS_NAMESPACES, true);
        XMLUtil.setFeatureSafe(parser, XmlPullParsers.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, true);
    }

    public void setEnableIndent(boolean enableIndent) {
        if(enableIndent == this.enableIndent) {
            return;
        }
        this.enableIndent = enableIndent;
        if (enableIndent) {
            this.serializer = XmlIndentingSerializers.create(this.serializer);
        } else if(this.serializer instanceof XmlIndentingSerializers) {
            this.serializer = ((XmlIndentingSerializers) this.serializer).getBaseSerializer();
        }
    }

    public void write() throws IOException, XmlPullParsersException {
        XmlPullParsers parser = this.parser;

        this.processNamespace = XMLUtil.getFeatureSafe(parser,
                XmlPullParsers.FEATURE_PROCESS_NAMESPACES, false);

        this.reportNamespaceAttrs = XMLUtil.getFeatureSafe(parser,
                XmlPullParsers.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, false);

        int event = parser.next();
        while (nextEvent(event)){
            event = parser.next();
        }
        close();
    }
    private void close() throws IOException {
        XmlPullParsers parser = this.parser;
        if(parser instanceof Closeable){
            ((Closeable)parser).close();
        }
        XmlSerializers serializer = this.serializer;
        if(serializer instanceof Closeable){
            ((Closeable)serializer).close();
        }
    }
    private boolean nextEvent(int event) throws IOException, XmlPullParsersException {
        boolean hasNext = true;
        switch (event){
            case XmlResourceParsers.START_DOCUMENT:
                onStartDocument();
                break;
            case XmlResourceParsers.START_TAG:
                onStartTag();
                break;
            case XmlResourceParsers.TEXT:
                onText();
                break;
            case XmlResourceParsers.ENTITY_REF:
                onEntityRef();
                break;
            case XmlResourceParsers.CDSECT:
                onCdsect();
                break;
            case XmlResourceParsers.COMMENT:
                onComment();
                break;
            case XmlResourceParsers.END_TAG:
                onEndTag();
                break;
            case XmlResourceParsers.END_DOCUMENT:
                onEndDocument();
                hasNext = false;
                break;
        }
        return hasNext;
    }

    private void onStartDocument() throws IOException{
        serializer.startDocument("utf-8", null);
    }
    private void onStartTag() throws IOException, XmlPullParsersException {
        XmlPullParsers parser = this.parser;
        XmlSerializers serializer = this.serializer;

        boolean processNamespace = this.processNamespace;
        boolean countNamespaceAsAttribute = processNamespace && reportNamespaceAttrs;

        if(!countNamespaceAsAttribute){
            int nsCount = parser.getNamespaceCount(parser.getDepth());
            for(int i=0; i<nsCount; i++){
                String prefix = parser.getNamespacePrefix(i);
                String namespace = parser.getNamespaceUri(i);
                serializer.setPrefix(prefix, namespace);
            }
        }
        serializer.startTag(parser.getNamespace(), parser.getName());
        int attrCount = parser.getAttributeCount();
        for(int i=0; i<attrCount; i++){
            String namespace = processNamespace ?
                    parser.getAttributeNamespace(i) : null;

            serializer.attribute(namespace,
                    parser.getAttributeName(i),
                    parser.getAttributeValue(i));
        }
    }
    private void onText() throws IOException{
        serializer.text(parser.getText());
    }
    private void onEntityRef() throws IOException{
        serializer.entityRef(parser.getText());
    }
    private void onCdsect() throws IOException{
        serializer.cdsect(parser.getText());
    }
    private void onComment() throws IOException{
        serializer.comment(parser.getText());
    }
    private void onEndTag() throws IOException{
        serializer.endTag(parser.getNamespace(), parser.getName());
    }
    private void onEndDocument() throws IOException{
        serializer.endDocument();
    }
}
