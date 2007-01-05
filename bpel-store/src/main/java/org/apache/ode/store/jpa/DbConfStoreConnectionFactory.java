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

package org.apache.ode.store.jpa;

import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.HashMap;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class DbConfStoreConnectionFactory implements ConfStoreConnectionFactory {

    private DataSource _ds;
    private EntityManagerFactory _emf;

    public DbConfStoreConnectionFactory(DataSource ds) {
        _ds = ds;
        HashMap propMap = new HashMap();
        propMap.put("javax.persistence.nonJtaDataSource", ds);
        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");
//        propMap.put("openjpa.MetaDataFactory",
//                "Types=org.apache.ode.store.jpa.ProcessConfDaoImpl;" +
//                        "org.apache.ode.store.jpa.ProcessConfPropertyDaoImpl;" +
//                        "org.apache.ode.store.jpa.DeploymentUnitDaoImpl");
        _emf = Persistence.createEntityManagerFactory("ode-store", propMap);
    }

    public ConfStoreConnection getConnection() {
        return new ConfStoreConnectionJpa(_emf.createEntityManager());
    }
}