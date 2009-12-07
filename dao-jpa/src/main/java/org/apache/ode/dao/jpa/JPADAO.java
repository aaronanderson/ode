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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.ode.bpel.dao.BpelDAOConnection;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 * @author Jeff Yu
 */
public class JPADAO {
	
	private JPADaoOperator _operator;
	
	public JPADAO() {
		_operator = JPADaoOperatorFactory.getJPADaoOperator();
	}

    protected BpelDAOConnection getConn() {
    	return BPELDAOConnectionFactoryImpl._connections.get();
    }

    protected EntityManager getEM() {
    	//FIXME: Am I right here??
    	//return OpenJPAPersistence.getEntityManager(this);
    	return BPELDAOConnectionFactoryImpl._connections.get().getEntityManager();
    }

    /**
     * javax.persistence.Query either let you query for a collection or a single
     * value throwing an exception if nothing is found. Just a convenient shortcut
     * for single results allowing null values
     * @param qry query to execute
     * @return whatever you assign it to
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSingleResult(Query qry) {
        List res = qry.getResultList();
        if (res.size() == 0) {
        	return null;
        }
        return (T) res.get(0);
    }
    
    
    protected <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName) {
    	_operator.batchUpdateByIds(ids, query, parameterName);
    }
}