/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.compiler.bom;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common interface to all BPEL object model (BOM) objects. Provides for
 * location information (i.e. line numbers) and namespace context (XML namespace
 * prefix maps).
 */
public class BpelObject {

    public static final QName ATTR_LINENO = new QName("urn:org.apache.ode.bpel.compiler", "lineno");

    private final Element _element;

    private final QName _type;

    private final NSContext _nsContext;

    private List<BpelObject> _children = null;

    public BpelObject(Element el) {
        _element = el;
        _type = new QName(el.getNamespaceURI(), el.getLocalName());
        _nsContext = new NSContext();
        initNSContext(el);
    }

    public QName getType() {
        return _type;
    }
    
    public Element getElement() {
        return _element;
    }

    /**
     * Get the line number in the BPEL source document where this object is
     * defined.
     * 
     * @return line number
     */
    public int getLineNo() {
        return Integer.valueOf(getAttribute(ATTR_LINENO, "-1"));
    }

    /**
     * Get the namespace context for this BPEL object (i.e. prefix-to-namespace
     * mapping).
     * 
     * @return namespace context
     */
    public NSContext getNamespaceContext() {
        return _nsContext;
    }

 
    /**
     * Return the declared extensibility elements. The extensibility elements
     * declared as subelements of this BpelObject will be returned with a value
     * type of org.w3c.dom.Element. The ones declared as extensibility
     * attributes will be returned as a value type of String.
     * 
     * @return extensibility qualified names and the full elements value (String
     *         or Element)
     */
    public Map<QName, Object> getExtensibilityElements() {
        // TODO: implement
        return new HashMap<QName, Object>();

    }


    public Element getExtensibilityElement(QName extElName) {
        BpelObject e = getFirstChild(extElName);
        if (e == null)
            return null;
        return e.getElement();
    }

    
    /**
     * Is this a BPEL 1.1 object?
     * @return
     */
    protected boolean is11() {
        return getType().getNamespaceURI() != null && 
            (getType().getNamespaceURI().equals(Bpel11QNames.NS_BPEL4WS_2003_03)
                    || getType().getNamespaceURI().equals(Bpel11QNames.NS_BPEL4WS_PARTNERLINK_2003_05)); 
    }
    
    protected boolean isAttributeSet(String attrname) {
        return null != getAttribute(attrname, null);
    }
    
    protected <T extends BpelObject> List<T> getChildren(Class<T> cls) {
        return CollectionsX.filter(new ArrayList<T>(), getChildren(), cls);
    }

    protected <T extends BpelObject> T getFirstChild(Class<T> cls) {
        List<T> children = getChildren(cls);
        if (children.size() == 0)
            return null;
        return children.get(0);
    }
    
    protected List<BpelObject> getChildren(final QName type) {
        return CollectionsX.filter(new ArrayList<BpelObject>(), getChildren(), new MemberOfFunction<BpelObject>() {
            @Override
            public boolean isMember(BpelObject o) {
                return o.getType().equals(type);
            }
        });
    }

    protected BpelObject getFirstChild(final QName type) {
        return CollectionsX.find_if(getChildren(), new MemberOfFunction<BpelObject>() {
            @Override
            public boolean isMember(BpelObject o) {
                return o.getType().equals(type);
            }
        });
    }

    protected List<BpelObject> getChildren() {
        if (_children == null) {
            _children = new ArrayList<BpelObject>();
            NodeList nl = _element.getChildNodes();
            for (int i = 0; i < nl.getLength(); ++i) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                    _children.add(createBpelObject((Element) node));
            }
        }

        return _children;

    }

    protected BpelObject createBpelObject(Element element) {
        return BpelObjectFactory.getInstance().createBpelObject(element);
    }

    protected String getAttribute(QName name, String dflt) {
        String val = _element.getAttributeNS(name.getNamespaceURI(), name.getLocalPart());
        if (val == null || "".equals(val))
            return dflt;
        return val;
    }
    
    protected String getAttribute(String name, String dflt) {
        String val = _element.getAttribute(name);
        if (val == null || "".equals(val))
            return dflt;
        return val;
    }
    
    protected String getAttribute(String name) {
        return getAttribute(name, null);
    }
    
    protected <T> T getAttribute(String attrName, Map<String, T> suppressJoinFailure, T notset) {
        String val = getAttribute(attrName, null);
        if (val == null || "".equals(val))
            return notset;
        return suppressJoinFailure.get(val);
    }
   
    
    
    /**
     * Initialize object's namespace context (recursively).
     * 
     * @param el
     *            object's associated element.
     */
    private void initNSContext(Element el) {
        if (el.getParentNode() != null && el.getParentNode().getNodeType() == Node.ELEMENT_NODE)
            initNSContext((Element) el.getParentNode());
        NamedNodeMap attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            Attr attr = (Attr) attrs.item(i);
            if (!attr.getName().startsWith("xmlns:"))
                continue;
            String prefix = attr.getLocalName();
            String uri = attr.getValue();

            _nsContext.register(prefix, uri);
        }
        
        Attr dflt = el.getAttributeNode("xmlns");
        if (dflt != null) {
            _nsContext.register("", dflt.getTextContent());
        }
        
    }
    
    @Override
    public String toString() {
        return DOMUtils.domToString(_element);
    }

}