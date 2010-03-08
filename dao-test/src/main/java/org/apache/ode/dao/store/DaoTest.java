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
package org.apache.ode.dao.store;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import junit.framework.TestCase;
import javax.xml.namespace.QName;
import org.apache.ode.dao.HsqlJDBCContext;
import org.apache.ode.il.config.OdeConfigProperties;

public class DaoTest extends TestCase {

  HsqlJDBCContext _ctx;
  ConfStoreDAOConnectionFactory _cf;

  public void setUp() throws Exception {
    OdeConfigProperties props = new OdeConfigProperties(new File(getClass().getResource("/ode.properties").toURI().getPath()), "test.");
    props.load();
    _cf = (ConfStoreDAOConnectionFactory) Class.forName(props.getDAOConfStoreConnectionFactory()).newInstance();
    _ctx = new HsqlJDBCContext();
    _ctx.init();
    _cf.init(props.getProperties(), _ctx);
  }

  public void tearDown() throws Exception {
    _cf.shutdown();
    _ctx.shutdown();

  }

  public void testEmpty() {
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    assertEquals(0, conn.getDeploymentUnits().size());
    assertNull(conn.getDeploymentUnit("foobar"));
    conn.commit();
    conn.close();
  }

  public void testCreateDU() {
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
      assertNotNull(du);
      assertEquals("foo", du.getName());
      assertNotNull(du.getDeployDate());
    } finally {
      conn.commit();
      conn.close();
    }

    conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
      assertNotNull(du);
      assertEquals("foo", du.getName());
    } finally {
      conn.commit();
    }

  }

  public void testRollback() {
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.createDeploymentUnit("foo");
      assertNotNull(du);
      assertEquals("foo", du.getName());
      assertNotNull(du.getDeployDate());
    } finally {
      conn.rollback();
      conn.close();
    }

    conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.getDeploymentUnit("foo");
      assertNull(du);
    } finally {
      conn.commit();
    }

  }

  public void testGetDeploymentUnits() {
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    try {
      conn.createDeploymentUnit("foo1");
      conn.createDeploymentUnit("foo2");
      conn.createDeploymentUnit("foo3");
      conn.createDeploymentUnit("foo4");
    } finally {
      conn.commit();
      conn.close();
    }
    conn = _cf.getConnection();
    conn.begin();
    try {
      assertNotNull(conn.getDeploymentUnit("foo1"));
      assertNotNull(conn.getDeploymentUnit("foo2"));
      assertNotNull(conn.getDeploymentUnit("foo3"));
      assertNotNull(conn.getDeploymentUnit("foo4"));
      assertNull(conn.getDeploymentUnit("foo5"));
    } finally {
      conn.commit();
    }
  }

  public void testCreateProcess() {
    QName foobar = new QName("foo", "bar");
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
      ProcessConfDAO p = du.createProcess(foobar, foobar, 1);
      assertEquals(foobar, p.getPID());
      assertEquals(foobar, p.getType());
      assertNotNull(p.getDeploymentUnit());
      assertEquals("foo1", p.getDeploymentUnit().getName());
    } finally {
      conn.commit();
      conn.close();
    }

    conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
      ProcessConfDAO p = du.getProcess(foobar);
      assertNotNull(p);
      assertNotNull(du.getProcesses());

      assertEquals(foobar, p.getPID());
      assertEquals(foobar, p.getType());

    } finally {
      conn.commit();
      conn.close();
    }

  }

  public void testProcessProperties() {
    QName foobar = new QName("foo", "bar");
    ConfStoreDAOConnection conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.createDeploymentUnit("foo1");
      ProcessConfDAO p = du.createProcess(foobar, foobar, 1);
      p.setProperty(foobar, "baz");
    } finally {
      conn.commit();
      conn.close();
    }

    conn = _cf.getConnection();
    conn.begin();
    try {
      DeploymentUnitDAO du = conn.getDeploymentUnit("foo1");
      ProcessConfDAO p = du.getProcess(foobar);
      assertNotNull(p.getProperty(foobar));
      assertEquals("baz", p.getProperty(foobar));
      assertNotNull(p.getPropertyNames());
      assertTrue(p.getPropertyNames().contains(foobar));
    } finally {
      conn.commit();
      conn.close();
    }


  }
}
