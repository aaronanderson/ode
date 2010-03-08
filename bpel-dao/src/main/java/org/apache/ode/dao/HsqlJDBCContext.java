package org.apache.ode.dao;

import java.sql.SQLException;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;

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
/**
 * are based on J2EE/JDBC data sources. 
 * 
 */
public class HsqlJDBCContext extends JDBCContext {

  /** GUID used to create a unique in-memory db. */
  private String _guid;
  
  public void init() {
    _guid = new GUID().toString();
    jdbcDataSource hsqlds = new jdbcDataSource();
    hsqlds.setDatabase("jdbc:hsqldb:mem:" + _guid);
    hsqlds.setUser("sa");
    hsqlds.setPassword("");
    setDataSource(hsqlds);
    setCreateModel(true);

  }

  public void shutdown() throws SQLException {
    getDataSource().getConnection().createStatement().execute("SHUTDOWN;");
  }
}
