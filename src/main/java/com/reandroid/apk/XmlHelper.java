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
package com.reandroid.apk;

import org.xmlpull.v1.XmlPullParsers;
import org.xmlpull.v1.XmlPullParsersException;
import org.xmlpull.v1.XmlSerializers;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XmlHelper {

    public static Map<String, String> readAttributes(XmlPullParsers parser, String elementName) throws IOException, XmlPullParsersException {
        if(!findElement(parser, elementName)){
            return null;
        }
        return mapAttributes(parser);
    }
    public static Map<String, String> mapAttributes(XmlPullParsers parser){
        Map<String, String> map = new HashMap<>();
        int count = parser.getAttributeCount();
        for(int i = 0; i < count; i++){
            String name = parser.getAttributeName(i);
            int index = name.indexOf(':');
            if(index > 0 && index < name.length() && !name.startsWith("xmlns:")){
                index++;
                name = name.substring(index);
            }
            map.put(name,
                    parser.getAttributeValue(i));
        }
        return map;
    }
    private static boolean findElement(XmlPullParsers parser, String elementName) throws IOException, XmlPullParsersException {
        int event;
        while ((event = parser.next()) != XmlPullParsers.END_DOCUMENT){
            if(event != XmlPullParsers.START_TAG){
                continue;
            }
            if(elementName.equals(parser.getName())){
                return true;
            }
        }
        return false;
    }
    public static String toXMLTagName(String typeName){
        // e.g ^attr-private
        if(typeName.length()>0 && typeName.charAt(0)=='^'){
            typeName = typeName.substring(1);
        }
        return typeName;
    }

    public static void closeSilent(Object obj){
        if(!(obj instanceof Closeable)){
            return;
        }
        Closeable closeable = (Closeable) obj;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static void setIndent(XmlSerializers serializer, boolean state){
        setFeatureSafe(serializer, FEATURE_INDENT, state);
    }
    public static void setFeatureSafe(XmlSerializers serializer, String name, boolean state){
        try {
            serializer.setFeature(name, state);
        } catch (Throwable ignored) {
        }
    }

    public static final String RESOURCES_TAG = "resources";
    public static final String FEATURE_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
}
