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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.ode.dao.JDBCContext;
import org.apache.ode.dao.jpa.store.ConfStoreDAOConnectionImpl;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import static org.apache.ode.dao.jpa.openjpa.OpenJPABpelDAOConnectionFactoryImpl.buildConfig;
import static org.apache.ode.dao.jpa.openjpa.OpenJPABpelDAOConnectionFactoryImpl._operator;

public class OpenJPAConfStoreDAOConnectionFactoryImpl implements ConfStoreDAOConnectionFactory {

  EntityManagerFactory _emf;
  JDBCContext _ctx;

  public void init(Properties odeConfig, Object env) {
    this._ctx = (JDBCContext) env;
    Map emfProperties = buildConfig(OdeConfigProperties.PROP_DAOCF_STORE + ".", odeConfig, _ctx);
    _emf = Persistence.createEntityManagerFactory("ode-store", emfProperties);

  }

  public ConfStoreDAOConnection getConnection() {
    final ThreadLocal<ConfStoreDAOConnectionImpl> currentConnection = ConfStoreDAOConnectionImpl.getThreadLocal();
    if (currentConnection.get() != null && currentConnection.get().getEntityManager().isOpen() ) {
      //currentConnection.get().getEntityManager().joinTransaction();
      return currentConnection.get();
    } else {
      EntityManager em = _emf.createEntityManager();
      ConfStoreDAOConnectionImpl conn = new ConfStoreDAOConnectionImpl(em,_operator);
      if (_ctx.getTransactionManager()!=null){
        conn.setTransactionManager(_ctx.getTransactionManager());
      }
      currentConnection.set(conn);
      return conn;
    }
  }

  public void shutdown() {
    _emf.close();
  }
}
