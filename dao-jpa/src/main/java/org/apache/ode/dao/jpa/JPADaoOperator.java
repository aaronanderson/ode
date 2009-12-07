package org.apache.ode.dao.jpa;

import java.util.Iterator;
import java.util.Map;

import javax.persistence.Query;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * this is interface that will include the methods that will be used in JPA DAO,
 * But the implementation should be different from various JPA vendor, like OpenJPA, Hibernate etc.
 * 
 * @author Jeff Yu
 *
 */
public interface JPADaoOperator {
	
	public <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName);
	
	public void setBatchSize(Query query, int limit);
	
	public Map<String, Object> getInitializeProperties(DataSource ds, boolean createDatamodel, TransactionManager tx);

}