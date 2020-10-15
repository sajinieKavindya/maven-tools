/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.maven.registry;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.wso2.maven.core.model.AbstractXMLDoc;
import org.wso2.maven.registry.beans.RegistryCollection;
import org.wso2.maven.registry.beans.RegistryElement;
import org.wso2.maven.registry.beans.RegistryItem;
import org.wso2.maven.registry.beans.RegistryProperty;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.xml.stream.FactoryConfigurationError;

public abstract class RegistryInfoProvider extends AbstractXMLDoc implements Observer {

    private File source;

    public void update(Observable o, Object arg) {
    }

    RegistryItem getRegistryItem(OMElement omElement) {
        RegistryItem item = new RegistryItem();
        item.setFile(getChildElements(omElement, "file").get(0).getText());
        item.setPath(getChildElements(omElement, "path").get(0).getText());
        List<OMElement> mediaTypeElements = getChildElements(omElement, "mediaType");
        if (!mediaTypeElements.isEmpty()) {
            item.setMediaType(mediaTypeElements.get(0).getText());
        }
        setPropertiesToRegistryElement(omElement, item);
        return item;
    }

    RegistryCollection getRegistryCollection(OMElement omElement2) {
        RegistryCollection collection = new RegistryCollection();
        collection.setDirectory(getChildElements(omElement2, "directory").get(0).getText());
        collection.setPath(getChildElements(omElement2, "path").get(0).getText());
        setPropertiesToRegistryElement(omElement2, collection);
        return collection;
    }

    private void setPropertiesToRegistryElement(OMElement omElement, RegistryElement element) {
        OMElement properties = getChildElements(omElement, "properties").get(0);
        List<OMElement> propertyList = getChildElements(properties, "property");
        for (OMElement property : propertyList) {
            element.addProperty(new RegistryProperty(getAttribute(property, "key"), getAttribute(property, "value")));
        }
    }

    OMElement getRegistryElementInfo(RegistryElement item) {
        OMElement element = null;
        if (item instanceof RegistryItem) {
            element = getElement("item", "");
            OMElement element2 = getElement("file", ((RegistryItem) item).getFile());
            OMElement element3 = getElement("path", item.getPath());
            OMElement element4 = getElement("mediaType", ((RegistryItem) item).getMediaType());
            element.addChild(element2);
            element.addChild(element3);
            element.addChild(element4);
        } else if (item instanceof RegistryCollection) {
            element = getElement("collection", "");
            OMElement element2 = getElement("directory", ((RegistryCollection) item).getDirectory());
            OMElement element3 = getElement("path", item.getPath());
            element.addChild(element2);
            element.addChild(element3);
        }
        if (element != null) {
            OMElement propertiesElement = getElement("properties", "");

            List<RegistryProperty> registryPropertyList = item.getProperties();
            for (RegistryProperty registryProperty : registryPropertyList) {
                OMElement propertyElement = getElement("property", "");
                addAttribute(propertyElement, "key", registryProperty.getKey());
                addAttribute(propertyElement, "value", registryProperty.getValue());
                propertiesElement.addChild(propertyElement);
            }
            element.addChild(propertiesElement);
        }
        return element;
    }

    protected abstract void deserialize(OMElement documentElement) throws Exception;

    protected String serialize() throws Exception {
        String result = null;
        OMDocument document = factory.createOMDocument();
        OMElement documentElement = getDocumentElement();
        document.addChild(documentElement);
        try {
            result = getPretifiedString(documentElement);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    protected String getDefaultName() {
        return null;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public File getSource() {
        return source;
    }

    public File toFile() throws Exception {
        return new File(toFile(getSource()).toString());
    }

    public void fromFile(File file) throws FactoryConfigurationError, Exception {
        setSource(file);
        if (getSource().exists()) {
            deserialize(getSource());
        }
    }

    public abstract OMElement getDocumentElement();
}
