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
package org.apache.ode.dao.jpa.openjpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.JDBCContext;
import org.apache.ode.dao.bpel.BpelDAOConnection;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.JPADAOOperator;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.il.config.OdeConfigProperties;

/**

 */
public class OpenJPABpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

  static final Log __log = LogFactory.getLog(OpenJPABpelDAOConnectionFactoryImpl.class);
  static Map _defaultProperties = new HashMap();
  static JPADAOOperator _operator = new OpenJPADaoOperator();
  EntityManagerFactory _emf;
  JDBCContext _ctx;

  static {
    _defaultProperties.put("javax.persistence.provider", "org.apache.openjpa.persistence.PersistenceProviderImpl");
    _defaultProperties.put("openjpa.Log", "log4j");
    _defaultProperties.put("openjpa.FlushBeforeQueries", "false");
    _defaultProperties.put("openjpa.FetchBatchSize", 1000);

    // _defaultProperties.put("openjpa.Log", "DefaultLevel=TRACE");
  }

  public void init(Properties odeConfig, Object env) {
    this._ctx = (JDBCContext) env;
    Map emfProperties = buildConfig(OdeConfigProperties.PROP_DAOCF + ".", odeConfig, _ctx);
    _emf = Persistence.createEntityManagerFactory("ode-bpel", emfProperties);
  }

  public BpelDAOConnection getConnection() {
    final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();

    if (_ctx.getTransactionManager() != null) {
      try {
        _ctx.getTransactionManager().getTransaction().registerSynchronization(new Synchronization() {
          // OpenJPA allows cross-transaction entity managers, which we don't want

          public void afterCompletion(int i) {
            if (currentConnection.get() != null) {
              currentConnection.get().getEntityManager().close();
            }
            currentConnection.set(null);
          }

          public void beforeCompletion() {
          }
        });
      } catch (RollbackException e) {
        throw new RuntimeException("Coulnd't register synchronizer!");
      } catch (SystemException e) {
        throw new RuntimeException("Coulnd't register synchronizer!");
      }
    }

    if (currentConnection.get() != null && currentConnection.get().getEntityManager().isOpen()) {
      //currentConnection.get().getEntityManager().joinTransaction();
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
    if (ctx.getTransactionManager() != null) {
      props.put("openjpa.ConnectionFactoryMode", "managed");
      props.put("openjpa.jdbc.TransactionIsolation", "read-committed");
      props.put("openjpa.ManagedRuntime", new JpaTxMgrProvider(ctx.getTransactionManager()));
      props.put("javax.persistence.transactionType", "JTA");
    } else {
      props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
    }
    if (ctx.getDataSource() != null) {
      props.put("openjpa.ConnectionFactory", ctx.getDataSource());
    }

    //props.put("openjpa.jdbc.DBDictionary", dictionary);

    if (ctx.isCreateModel()) {
      props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(SchemaAction=drop,SchemaAction=add,ForeignKeys=true)");
    }

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
}
