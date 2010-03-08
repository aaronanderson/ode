package org.apache.ode.dao;

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
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * are based on J2EE/JDBC data sources. 
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 */
public class JDBCContext {

  private DataSource _ds;
  private TransactionManager _tm;
  private boolean _createModel;

  public JDBCContext() {
  }

  public JDBCContext(DataSource ds, TransactionManager tm, boolean createModle) {
    this._ds = ds;
    this._tm = tm;
    this._createModel = createModle;
  }

  /**
   * Set the managed data source (transactions tied to transaction manager).
   * @param ds
   */
  public void setDataSource(DataSource ds) {
    this._ds = ds;
  }

  /**
   * Set the transaction manager.
   * @param tm
   */
  public void setTransactionManager(TransactionManager tm) {
    this._tm = tm;
  }

  /**
   * @return the DataSource
   */
  public DataSource getDataSource() {
    return _ds;
  }

  /**
   * @return the TransactionManager
   */
  public TransactionManager getTransactionManager() {
    return _tm;
  }

  /**
   * @return the createModel
   */
  public boolean isCreateModel() {
    return _createModel;
  }

  /**
   * @param createModel value of createModel
   */
  public void setCreateModel(boolean createModel) {
    this._createModel = createModel;
  }
}
