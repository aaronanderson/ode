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
package org.apache.ode.dao.jpa.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Query;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JPADaoOperator;
import org.apache.ode.utils.GUID;
import org.hibernate.cfg.Environment;

/**
 * 
 * @author Jeff Yu
 */
public class HibernateDaoOperator implements JPADaoOperator {
	
	private static final Log __log = LogFactory.getLog(HibernateDaoOperator.class);

    public <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName) {
    	//TODO
    }

	public Map<String, Object> getInitializeProperties(DataSource ds, boolean createDatamodel, TransactionManager tx) {
        HashMap<String, Object> properties = new HashMap<String,Object>();
        
        String guid = new GUID().toString();
        HibernateUtil.registerDatasource(guid, ds);
        HibernateUtil.registerTransactionManager(guid, tx);
        
        properties.put(HibernateUtil.PROP_GUID, guid);
        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        properties.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
        
        
        if (createDatamodel) {
        	properties.put(Environment.HBM2DDL_AUTO, "create-drop");
        }
        
        // Isolation levels override; when you use a ConnectionProvider, this has no effect
        String level = System.getProperty("ode.connection.isolation", "2");
        properties.put(Environment.ISOLATION, level);

        if (__log.isDebugEnabled()) {
        	__log.debug("Properties passed to Hibernate:");
        	for (String key : properties.keySet()) {
        		__log.debug(key + "=" + properties.get(key));
        	}
        }

        return properties;
	}

	public void setBatchSize(Query query, int limit) {
        //TODO
	}
	
	
}