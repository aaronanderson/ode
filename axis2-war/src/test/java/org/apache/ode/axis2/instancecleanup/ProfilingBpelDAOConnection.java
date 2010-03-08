package org.apache.ode.axis2.instancecleanup;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessInstanceProfileDAO;
import org.apache.ode.dao.bpel.ProcessProfileDAO;

public interface ProfilingBpelDAOConnection extends BpelDAOConnection {
      ProcessProfileDAO createProcessProfile(ProcessDAO instance);
      
      ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance);
}
