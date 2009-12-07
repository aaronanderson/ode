/**
 * 
 */
package org.apache.ode.dao.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * This will look up the properties from bpel.properties file,
 * the property would be like : jpa.dao.operator=org.apache.ode.dao.jpa.hibernate.HibernateDaoOperator
 * 
 * @author Jeff Yu
 *
 */
public class JPADaoOperatorFactory {
	
	public static final String DAO_OPERATOR = "jpa.dao.operator";
	
	private static final Log log = LogFactory.getLog(JPADaoOperatorFactory.class);
	
	private static JPADaoOperator operator;
	
	private JPADaoOperatorFactory(){
		
	}
	
	private static void createJPADaoOperator(){		
		Properties props = new Properties();
		InputStream is=JPADaoOperatorFactory.class.getClassLoader().getResourceAsStream("bpel.properties");
		String daoOperatorClass = "org.apache.ode.dao.jpa.openjpa.OpenJPADaoOperator";
	    try {
			props.load(is);
			daoOperatorClass = props.getProperty(DAO_OPERATOR);
		} catch (IOException e) {
			log.error("error in finding the bpel.properties file", e);
		}
		
	    log.debug("The jpa.dao.operator class is [" + daoOperatorClass + "]");
	
	    try {
	        operator = (JPADaoOperator) Class.forName(daoOperatorClass).newInstance();
	    } catch (Exception e) {
	    	log.error("exception in create instance for " + daoOperatorClass, e);
	    	throw new RuntimeException(e);
	    }
		
	}	
	
	public static JPADaoOperator getJPADaoOperator() {
		if (operator == null) {
			createJPADaoOperator();
		}
		return operator;
	}

}
