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
package org.apache.ode.dao.jpa.store;

import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.DeploymentUnitDAO;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JPAConnection;
import org.apache.ode.dao.jpa.JPADAOOperator;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ConfStoreDAOConnectionImpl extends JPAConnection implements ConfStoreDAOConnection {

    private static final Log __log = LogFactory.getLog(ConfStoreDAOConnectionImpl.class);
    static final ThreadLocal<ConfStoreDAOConnectionImpl> _connections = new ThreadLocal<ConfStoreDAOConnectionImpl>();

    private TransactionManager _mgr;

    public ConfStoreDAOConnectionImpl(EntityManager mgr, JPADAOOperator operator){
      super(mgr, operator);
    }

    public void setTransactionManager(TransactionManager mgr){
      this._mgr=mgr;
    }

    public void begin() {
      if (_mgr!=null){
        try{
        _mgr.begin();
        }catch (Exception ne){
          __log.error(ne);
        }
      }else{
        _em.getTransaction().begin();
      }
    }

    public void close() {
    }

    public void commit() {
      if (_mgr!=null){
        try{
        _mgr.commit();
        }catch (Exception ne){
          __log.error(ne);
        }
      }else{
        _em.getTransaction().commit();
      }
    }

    public DeploymentUnitDAO createDeploymentUnit(String name) {
        DeploymentUnitDaoImpl du = new DeploymentUnitDaoImpl();
        du.setName(name);
        du.setDeployDate(new Date());
        _em.persist(du);
        return du;
    }

    public DeploymentUnitDAO getDeploymentUnit(String name) {
        return _em.find(DeploymentUnitDaoImpl.class, name);
    }

    public Collection<DeploymentUnitDAO> getDeploymentUnits() {
        return _em.createQuery("SELECT du from DeploymentUnitDaoImpl du").getResultList();
    }

    public void rollback() {
      if (_mgr!=null){
        try{
        _mgr.rollback();
        }catch (Exception ne){
          __log.error(ne);
        }
      }else{
        _em.getTransaction().rollback();
      }
    }

    public long getNextVersion() {
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        if (res.size() == 0) {
            return 1;
        } else {
            VersionTrackerDAOImpl vt = res.get(0);
            return vt.getVersion() + 1;
        }
    }

    public void setVersion(long version) {
        List<VersionTrackerDAOImpl> res = _em.createQuery("select v from VersionTrackerDAOImpl v").getResultList();
        VersionTrackerDAOImpl vt;
        if (res.size() == 0) {
            vt = new VersionTrackerDAOImpl();
        } else {
            vt = res.get(0);
        }
        vt.setVersion(version);
        _em.persist(vt);
    }

    public static ThreadLocal<ConfStoreDAOConnectionImpl> getThreadLocal() {
        return _connections;
    }

}
