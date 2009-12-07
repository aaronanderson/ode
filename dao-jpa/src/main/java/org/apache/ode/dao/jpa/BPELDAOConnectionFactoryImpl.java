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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 * @author Jeff Yu
 */
public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {
    static final Log __log = LogFactory.getLog(BPELDAOConnectionFactoryImpl.class);

    protected EntityManagerFactory _emf;
    private TransactionManager _tm;
    private DataSource _ds;
    private Object _dbdictionary;
    private JPADaoOperator _operator;

    static ThreadLocal<BPELDAOConnectionImpl> _connections = new ThreadLocal<BPELDAOConnectionImpl>();

    public BPELDAOConnectionFactoryImpl() {
    	_operator = JPADaoOperatorFactory.getJPADaoOperator();
    }

    public BpelDAOConnection getConnection() {
        try {
            _tm.getTransaction().registerSynchronization(new Synchronization() {
                // OpenJPA allows cross-transaction entity managers, which we don't want
                public void afterCompletion(int i) {
                    if (_connections.get() != null)
                        _connections.get().getEntityManager().close();
                    _connections.set(null);
                }
                public void beforeCompletion() { }
            });
        } catch (RollbackException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        } catch (SystemException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        }
        if (_connections.get() != null) {
            return _connections.get();
        } else {
            HashMap propMap2 = new HashMap();
            propMap2.put("openjpa.TransactionMode", "managed");
            EntityManager em = _emf.createEntityManager(propMap2);
            BPELDAOConnectionImpl conn = createBPELDAOConnection(em);
            _connections.set(conn);
            return conn;
        }
    }

    protected BPELDAOConnectionImpl createBPELDAOConnection(EntityManager em) {
        return new BPELDAOConnectionImpl(em);
    }
    
    @SuppressWarnings("unchecked")
    public void init(Properties properties) {
        
        Map<String, Object> propMap = _operator.getInitializeProperties(_ds, false, _tm);

        if (_dbdictionary != null)
            propMap.put("openjpa.jdbc.DBDictionary", _dbdictionary);

        if (properties != null) {
            for (Map.Entry me : properties.entrySet()){
                propMap.put((String)me.getKey(),me.getValue());
            }
        }
        _emf = Persistence.createEntityManagerFactory("ode-dao", propMap);
    }

    public void setTransactionManager(TransactionManager tm) {
        _tm = tm;
    }

    public void setDataSource(DataSource datasource) {
        _ds = datasource;

    }

    public void setDBDictionary(String dbd) {
        _dbdictionary = dbd;
    }

    public void setTransactionManager(Object tm) {
        _tm = (TransactionManager) tm;

    }

    public void setUnmanagedDataSource(DataSource ds) {
    }

    public void shutdown() {
        _emf.close();
    }
    
}
