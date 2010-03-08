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
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.JDBCContext;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.dao.jpa.JPADAOOperator;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.GUID;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.EntityManagerImpl;

/**

 */
public class HibernateBpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

  static final Log __log = LogFactory.getLog(HibernateBpelDAOConnectionFactoryImpl.class);
  static Map _defaultProperties = new HashMap();
  static JPADAOOperator _operator = new HibernateDaoOperator();
  EntityManagerFactory _emf;
  JDBCContext _ctx;

  static {
    _defaultProperties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
  }

  public void init(Properties odeConfig, Object env) {
    this._ctx = (JDBCContext) env;
    Map emfProperties = buildConfig(OdeConfigProperties.PROP_DAOCF + ".", odeConfig, _ctx);
    _emf = Persistence.createEntityManagerFactory("ode-bpel", emfProperties);

  }

  public BpelDAOConnection getConnection() {
    final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();

    if (currentConnection.get() != null && isOpen(currentConnection.get().getEntityManager())) {
      return currentConnection.get();
    } else {
      EntityManager em = _emf.createEntityManager();
      BpelDAOConnectionImpl conn = new BpelDAOConnectionImpl(em, _operator);
      currentConnection.set(conn);
      return conn;
    }
  }

  public void shutdown() {
    _emf.close();
  }

  static Map buildConfig(String prefix, Properties odeConfig, JDBCContext ctx) {
    Map props = new HashMap(_defaultProperties);

    String guid = new GUID().toString();
    if (ctx.getDataSource() != null) {
      props.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
      HibernateUtil.registerDatasource(guid, ctx.getDataSource());
    }
    if (ctx.getTransactionManager() != null) {
      props.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
      props.put(Environment.TRANSACTION_MANAGER_STRATEGY, HibernateTransactionManagerLookup.class.getName());
      HibernateUtil.registerTransactionManager(guid, ctx.getTransactionManager());
      props.put("javax.persistence.transactionType", "JTA");
    } else {
      props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
    }


    if (ctx.getDataSource() != null || ctx.getTransactionManager() != null) {
      props.put(HibernateUtil.PROP_GUID, guid);
    }

    if (ctx.isCreateModel()) {
      props.put(Environment.HBM2DDL_AUTO, "create-drop");
    }

    // Isolation levels override; when you use a ConnectionProvider, this has no effect
    //String level = System.getProperty("ode.connection.isolation", "2");
    //props.put(Environment.ISOLATION, level);

    addEntries(prefix, odeConfig, props);

    return props;
  }

  public static void addEntries(String prefix, Properties odeConfig, Map props) {
    if (odeConfig != null) {
      for (Map.Entry me : odeConfig.entrySet()) {
        String key = (String) me.getKey();
        if (key.startsWith(prefix)) {
          String jpaKey = key.substring(prefix.length() - 1);
          String val = (String) me.getValue();
          if (val == null || val.trim().length() == 0) {
            props.remove(jpaKey);
          } else {
            props.put(jpaKey, me.getValue());
          }
        }
      }
    }
  }

  /*
   * For some reason Hibernate does not mark an EntityManager as being closed when
   * the EntityManagerFactory that created it is closed. This method performs a
   * deep introspection to determin if the EntityManager is still viable.
   */
  public static boolean isOpen(EntityManager mgr) {
    if (mgr instanceof EntityManagerImpl) {
      EntityManagerImpl mgrImpl = (EntityManagerImpl) mgr;
      return !mgrImpl.getSession().getSessionFactory().isClosed();
    } else {
      return mgr.isOpen();
    }
  }
}

