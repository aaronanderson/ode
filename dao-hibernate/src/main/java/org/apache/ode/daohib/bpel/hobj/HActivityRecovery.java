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

package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HLargeData;
import org.apache.ode.daohib.hobj.HObject;
import java.util.Date;

/**
 * Persistent representation of activity recovery information.
 * @hibernate.class table="BPEL_ACTIVITY_RECOVERY"
 */
public class HActivityRecovery extends HObject {

  /** Process instance to which this scope belongs. */
  private HProcessInstance _instance;
  private int         _activityId;
  private String      _channel;
  private String      _reason;
  private Date        _dateTime;
  private HLargeData  _data;
  private String      _actions;

  /**
   * Get the {@link HProcessInstance} to which this scope object belongs.
   * @hibernate.many-to-one
   *  column="PIID"
   */
	public HProcessInstance getInstance() {
		return _instance;
	}

  /** @see #getInstance() */
  public void setInstance(HProcessInstance instance) {
		_instance = instance;
	}

  /**
   * @hibernate.property column="AID"
   */
  public int getActivityId() {
    return _activityId;
  }

  public void setActivityId(int activityId) {
    _activityId = activityId;
  }

  /**
   * @hibernate.property column="CHANNEL"
   */
	public String getChannel() {
		return _channel;
	}

  public void setChannel(String channel) {
		_channel = channel;
	}

  /**
   * @hibernate.property column="REASON"
   */
	public String getReason() {
		return _reason;
	}

  public void setReason(String reason) {
		_reason = reason;
	}

  /**
   * @hibernate.property column="DATE_TIME"
   */
	public Date getDateTime() {
		return _dateTime;
	}

  public void setDateTime(Date dateTime) {
		_dateTime = dateTime;
	}

  /**
   * @hibernate.many-to-one column="LDATA_ID" cascade="delete"
   */
  public HLargeData getData() {
    return _data;
  }

  public void setData(HLargeData data) {
    _data = data;
  }

  /**
   * @hibernate.property column="ACTIONS"
   */
	public String getActions() {
		return _actions;
	}

  public void setActions(String actions) {
		_actions = actions;
	}

}