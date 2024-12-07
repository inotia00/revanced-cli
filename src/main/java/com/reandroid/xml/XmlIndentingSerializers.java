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

import org.xmlpull.v1.XmlSerializers;

import java.io.IOException;

public class XmlIndentingSerializers extends XmlSerializersWrapper {

    public XmlIndentingSerializers(XmlSerializers baseSerializer) {
        super(baseSerializer);
    }

    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        super.startDocument(encoding, standalone);
        setIndentFeature();
    }

    @Override
    public XmlSerializers startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        setIndentFeature();
        return super.startTag(namespace, name);
    }
    private void setIndentFeature() {
        XMLUtil.setFeatureSafe(this, XMLUtil.FEATURE_INDENT_OUTPUT, true);
    }

    public static XmlSerializers create(XmlSerializers serializer) {
        if(containsIndenting(serializer)) {
            return serializer;
        }
        return new XmlIndentingSerializers(serializer);
    }
    private static boolean containsIndenting(XmlSerializers serializer) {
        while (!(serializer instanceof XmlIndentingSerializers) &&
                (serializer instanceof XmlSerializersWrapper)) {
            serializer = ((XmlSerializersWrapper) serializer).getBaseSerializer();
        }
        return serializer instanceof XmlIndentingSerializers;
    }
}
