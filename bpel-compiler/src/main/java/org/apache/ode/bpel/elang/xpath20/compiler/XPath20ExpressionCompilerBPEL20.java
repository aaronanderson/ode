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
package org.apache.ode.bpel.elang.xpath20.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionCompiler;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath10.compiler.XPathMessages;
import org.apache.ode.bpel.elang.xpath10.compiler.XslCompilationErrorListener;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Node;


/**
 * XPath compiler based on the SAXON implementation.
 * 
 * @author mriou <mriou at apache dot org>
 */
public class XPath20ExpressionCompilerBPEL20 implements ExpressionCompiler {

    private static final Log __log = LogFactory.getLog(XPath20ExpressionCompilerBPEL20.class);

    private static final QName _qnLinkStatus = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETLINKSTATUS);

    private static final QName _qnVarProp = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEPROPRTY);

    private static final QName _qnVarData = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEDATA);

    private static final QName _qnXslTransform = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_DOXSLTRANSFORM);

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    private CompilerContext _compilerContext;

    private Map<String, String> _properties = new HashMap<String, String>();

    public XPath20ExpressionCompilerBPEL20() {
        super();
        _properties.put("runtime-class", "org.apache.ode.bpel.elang.xpath20.runtime.XPath20ExpressionRuntime");
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
        XslCompilationErrorListener xe = new XslCompilationErrorListener(compilerContext);
        XslTransformHandler.getInstance().setErrorListener(xe);

    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileJoinCondition(java.lang.Object)
     */
    public OExpression compileJoinCondition(Object source) throws CompilationException {
        return _compile((Expression) source, true);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compile(java.lang.Object)
     */
    public OExpression compile(Object source) throws CompilationException {
        return _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileLValue(java.lang.Object)
     */
    public OLValueExpression compileLValue(Object source) throws CompilationException {
        return (OLValueExpression) _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
     */
    private OExpression _compile(org.apache.ode.bpel.compiler.bom.Expression xpath, boolean isJoinCondition)
            throws CompilationException {
        OXPath20ExpressionBPEL20 oexp = new OXPath20ExpressionBPEL20(_compilerContext.getOProcess(), _qnVarData,
                _qnVarProp, _qnLinkStatus, _qnXslTransform, isJoinCondition);
        oexp.namespaceCtx = xpath.getNamespaceContext();
        doJaxpCompile(oexp, xpath);
        return oexp;
    }

    private void doJaxpCompile(OXPath20ExpressionBPEL20 out, Expression source) throws CompilationException {
        String xpathStr;
        Node node = source.getExpression();
        if (node == null) {
            throw new IllegalStateException("XPath string and xpath node are both null");
        }
        if (node.getNodeType() != Node.TEXT_NODE) {
            throw new CompilationException(__msgs.errUnexpectedNodeTypeForXPath(DOMUtils.domToString(node)));
        }
        xpathStr = node.getNodeValue();
        xpathStr = xpathStr.trim();

        out.xpath = xpathStr;
        try {
            __log.debug("Compiling expression " + xpathStr);
            XPathFactoryImpl xpf = new net.sf.saxon.xpath.XPathFactoryImpl();
            JaxpFunctionResolver funcResolver = new JaxpFunctionResolver(
                    _compilerContext, out, source.getNamespaceContext(), Constants.BPEL20_NS);
            xpf.setXPathFunctionResolver(funcResolver);
            JaxpVariableResolver varResolver = new JaxpVariableResolver(_compilerContext, out);
            xpf.setXPathVariableResolver(varResolver);

            XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
            xpe.setStaticContext(new SaxonContext(xpf.getConfiguration(), varResolver, funcResolver));
            xpe.setXPathFunctionResolver(funcResolver);
            xpe.setNamespaceContext(source.getNamespaceContext());
            xpe.compile(xpathStr);

            // Here we're "faking" an evaluation to parse properly variables and
            // functions and
            // detect all possible mistakes. To do so we're using specific
            // resolvers that always
            // return guessed appropriate values from variable types.
//            expr.evaluate(DOMUtils.newDocument());

            // Fishing for predicates
            // TODO Clean that up
//            if (xpathStr.indexOf("[$") > 0) {
//                String rightStr = xpathStr.substring(xpathStr.indexOf("[$") + 2, xpathStr.length());
//                String varStr = rightStr.substring(0, rightStr.indexOf("]"));
//                varResolver.resolveVariable(new QName(null, varStr));
//            }
        } catch (XPathExpressionException e) {
            __log.debug(e);
            __log.info("Couldn't validate properly expression " + xpathStr);
        } catch (WrappedResolverException wre) {
            if (wre._compilationMsg != null)
                throw new CompilationException(wre._compilationMsg, wre);
            if (wre.getCause() instanceof CompilationException)
                throw (CompilationException) wre.getCause();
            throw wre;
        }
    }

    public Map<String, String> getProperties() {
        return _properties;
    }
}