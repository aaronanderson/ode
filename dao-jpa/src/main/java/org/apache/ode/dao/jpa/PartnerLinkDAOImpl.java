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

package org.apache.ode.dao.jpa;


import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.ContextValueDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

@Entity
@Table(name="ODE_PARTNER_LINK")
@NamedQueries({
    @NamedQuery(name=PartnerLinkDAOImpl.DELETE_PARTNER_LINKS_BY_SCOPE_IDS, query="delete from PartnerLinkDAOImpl as l where l._scopeId in (:scopeIds)")
})
public class PartnerLinkDAOImpl extends JPADAO implements PartnerLinkDAO {
    public final static String DELETE_PARTNER_LINKS_BY_SCOPE_IDS = "DELETE_PARTNER_LINKS_BY_SCOPE_IDS";

	@Id @Column(name="PARTNER_LINK_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
    @SuppressWarnings("unused")
	private Long _id;
	@Lob @Column(name="MY_EPR")
    private String _myEPR;
	@Transient
    private Element _myEPRElement;
	@Basic @Column(name="MY_ROLE_NAME")
    private String _myRoleName;
	@Basic @Column(name="MY_ROLE_SERVICE_NAME")
    private String _myRoleServiceName;
	@Basic @Column(name="MY_SESSION_ID")
    private String _mySessionId;
	@Lob @Column(name="PARTNER_EPR")
    private String _partnerEPR;
	@Transient
    private Element _partnerEPRElement;
	@Basic @Column(name="PARTNER_LINK_MODEL_ID")
    private int _partnerLinkModelId;
	@Basic @Column(name="PARTNER_LINK_NAME")
    private String _partnerLinkName;
	@Basic @Column(name="PARTNER_ROLE_NAME")
    private String _partnerRoleName;
	@Basic @Column(name="PARTNER_SESSION_ID")
    private String _partnerSessionId;

    @Basic @Column(name="SCOPE_ID", nullable=true, insertable=false, updatable=false)
    @SuppressWarnings("unused")
    private Long _scopeId;
    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @JoinColumn(name="SCOPE_ID")
    @SuppressWarnings("unused")
    private ScopeDAOImpl _scope;
    
    @OneToMany(targetEntity=ContextValueDAOImpl.class,mappedBy="_partnerLink",fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    private Collection<ContextValueDAO> _contextValues = new ArrayList<ContextValueDAO>();

    public PartnerLinkDAOImpl() {}
	public PartnerLinkDAOImpl(int modelId, String name, String myRole, String partnerRole) {
		_partnerLinkModelId = modelId;
		_partnerLinkName = name;
		_myRoleName = myRole;
		_partnerRoleName = partnerRole;
	}

	public Element getMyEPR() {
		if ( _myEPRElement == null && _myEPR != null && !"".equals(_myEPR)) {
			try {
				_myEPRElement = DOMUtils.stringToDOM(_myEPR);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}
		
		return _myEPRElement;
	}

	public String getMyRoleName() {
		return _myRoleName;
	}

	public QName getMyRoleServiceName() {
		return _myRoleServiceName == null ? null : QName.valueOf(_myRoleServiceName);
	}

	public String getMySessionId() {
		return _mySessionId;
	}

	public Element getPartnerEPR() {
		if ( _partnerEPRElement == null && _partnerEPR != null && !"".equals(_partnerEPR)) {
			try {
				_partnerEPRElement = DOMUtils.stringToDOM(_partnerEPR);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}
		return _partnerEPRElement;
	}

	public int getPartnerLinkModelId() {
		return _partnerLinkModelId;
	}

	public String getPartnerLinkName() {
		return _partnerLinkName;
	}

	public String getPartnerRoleName() {
		return _partnerRoleName;
	}

	public String getPartnerSessionId() {
		return _partnerSessionId;
	}

	public void setMyEPR(Element val) {
		_myEPRElement = val;
		_myEPR = DOMUtils.domToString(val);

	}

	public void setMyRoleServiceName(QName svcName) {
		_myRoleServiceName = svcName.toString();

	}

	public void setMySessionId(String sessionId) {
		_mySessionId = sessionId;

	}

	public void setPartnerEPR(Element val) {
		_partnerEPRElement = val;
		_partnerEPR = DOMUtils.domToString(val);

	}

	public void setPartnerSessionId(String session) {
		_partnerSessionId = session;

	}

    public void setScope(ScopeDAOImpl scope) {
        _scope = scope;
    }
    
	public Collection<ContextValueDAO> getContextValues() {
		return _contextValues;
	}

	public void removeContextValue(String namespace, String key) {
		Query q = getEM().createNamedQuery(ContextValueDAOImpl.DELETE_CONTEXT_VALUES_BY_KEYS);
		q.setParameter("namespace", namespace);
		q.setParameter("key", key);
	}

	public void setContextValue(String namespace, String key, String value) {
        ContextValueDAO val = null;
        for (ContextValueDAO v : getContextValues()) {
            if (v.getNamespace().equals(namespace) && v.getKey().equals(key)) {
                val = v;
            }
        }
		
        if (val == null) {
        	val = new ContextValueDAOImpl(this, namespace, key);
        	getEM().persist(val);
            _contextValues.add(val);
        }
        
        val.setValue(value);
	}

}
