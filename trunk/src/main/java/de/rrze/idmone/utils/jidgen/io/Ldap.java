/*
 * jidgen, developed as a part of the IDMone project at RRZE.
 * Copyright 2008, RRZE, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. This
 * product includes software developed by the Apache Software Foundation
 * http://www.apache.org/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.rrze.idmone.utils.jidgen.io;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * This class was written for easy handling
 * of LDAP operations within the jidgen
 * project.<br/>
 * It utilizes the JNDI - Java Naming and Directory Interface
 * to cannect to a specified LDAP directory server.
 * 
 * @author unrza249
 */
public class Ldap {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(Ldap.class);

	/**
	 * Default properties to fall back on when no appropriate 
	 * property is available elsewhere
	 */
	private Properties defaultProperties = new Properties();

	/**
	 * User-set properties with the default properties 
	 * to fall back on.
	 */
	private Properties props = new Properties(defaultProperties);

	/**
	 * LDAP search controls
	 */
	private SearchControls ctls;

	/**
	 * LDAP connection context
	 */
	private DirContext ctx;

	/**
	 * True if there is a valid connection context, false otherwise
	 */
	private boolean connected = false;

	/**
	 * The raw search result of the last search operation
	 */
	private NamingEnumeration<SearchResult> lastResult = null;

	/**
	 * Number of entries in the last search result
	 */
	private int lastResultSize = 0;






	/**
	 * Default constructor
	 */
	public Ldap() {	
		this.setDefaultProperties();
	}

	
	/**
	 * Constructor with external properties
	 * 
	 * @param props
	 * 		The external properties to use for initialization
	 */
	public Ldap(Properties props) {
		this();
		this.setProperties(props);
	}
	
	
	public Ldap(File f) { 
		this();
		try {
			logger.debug("ldapConfigurationFile = " + f.getFilename());
			this.props.load(f.getReader());
		}
		catch (IOException e) {
			logger.fatal("Exception", e);
		}
	}
	
	
	/**
	 * Initializes the defaultProperties member
	 * with the internal presets
	 */
	private void setDefaultProperties() {
		this.defaultProperties.setProperty("host", "localhost");
		this.defaultProperties.setProperty("port", "389");
		this.defaultProperties.setProperty("namingContext", "dc=example,dc=com");
		this.defaultProperties.setProperty("user", "cn=jidgen,ou=people,dc=example,dc=com");
		this.defaultProperties.setProperty("password", "jidgen");
		this.defaultProperties.setProperty("searchFilter", "");
		this.defaultProperties.setProperty("searchBase", "ou=people");
	}
	

	/**
	 * Connect to the LDAP server<br/>
	 * This initializes the connection context object and opens the connection
	 * with the parameters specified.
	 */
	public void connect() {
		if (this.connected) {
			logger.debug("Already connected to LDAP!");
			return;
		}

		logger.trace("Connecting to LDAP server...");
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");	
		env.put(Context.PROVIDER_URL, "ldap://" + this.getHost() + ":" + this.getPort() + "/" + this.getNamingContext());
		env.put(Context.SECURITY_PRINCIPAL, this.getUser());
		env.put(Context.SECURITY_CREDENTIALS, this.getPassword());

		try {
			// Create initial context
			this.ctx = new InitialDirContext(env);
			this.connected = true;
		}
		catch (NamingException e) {
			logger.fatal("Exception", e);
			System.exit(150);
		}
	}


	public void disconnect() {
		if (!this.connected) {
			logger.debug("Not connected to LDAP!");
			return;
		}

		logger.trace("Disconnecting from LDAP server...");
		try {
			// Close the context when we're done
			this.ctx.close();
			this.connected = false;
		}
		catch (NamingException e) {
			logger.fatal("Exception", e);
			System.exit(150);
		}	
	}



	private void updateSearchControls() {
		logger.trace("Updating search controls...");

		// Set search controls
		this.ctls = new SearchControls();

		String[] attributes = {};
		this.ctls.setReturningAttributes(attributes);

		this.ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	}

	/**
	 * Executes the search with the stored parameters and returns the results.
	 * 
	 * @return
	 */
	private void executeSearch() {
		// check for open connection
		if (!this.connected) {
			this.connect();
		}

		// init the search controls
		this.updateSearchControls();

		// do the search
		logger.trace("Executing search...");
		this.lastResult = null;
		try {
			// Search for objects that have those matching attributes
			this.lastResult = this.ctx.search(this.getSearchBase(), this.getSearchFilter(), ctls);
			this.countLastSearchResult();
		}
		catch (NamingException e) {
			logger.fatal("Exception", e);
			System.exit(150);
		}		
	}

	/**
	 * Counts the number of entries in the last search result and
	 * stores the size internally.
	 */
	private void countLastSearchResult() {
		try {
			this.lastResultSize = 0;
			while (this.lastResult.hasMore()) {
				this.lastResult.next();
				this.lastResultSize++;
			}
		}
		catch (NamingException e) {
			logger.fatal("Exception", e);
			System.exit(150);
		}		
	}

	/**
	 * Executes the search with the stored parameters and
	 * return true if the search got at least one result, false
	 * if no results were returned.<br />
	 * The 
	 * 
	 * @return
	 * 		true if at least one result was returned, false if none were returned
	 */
	public boolean doSearch() {
		this.executeSearch();

		if (this.lastResultSize > 0) {
			return true;
		}
		else {
			return false;
		}
	}



	/**
	 * Returns hostname, port and naming context of the LDAP
	 * connection managed by this object
	 */
	public String toString() {
		return this.getHost() + ":" + this.getPort() + "/" + this.getNamingContext();
	}

	/**
	 * Takes the given properties object and copies the contained values
	 * to the internal properties object. Values that are not contained (as far as
	 * the containsKey() method says) are not copied.
	 * <p>
	 * At the moment the following properties are recognized:<br />
	 * <ul>
	 * <li><b>host</b> -- the hostname to connect to (Default: localhost)</li>
	 * <li><b>port</b> -- the port to connect to (Default: 389)</li>
	 * <li><b>namingContext</b> -- the naming context to use (Default: dc=example,dc=com)</li>
	 * <li><b>user</b> -- the user DN to bind to (Default: cn=jidgen,ou=people,dc=example,dc=com)</li>
	 * <li><b>password</b> -- the bind-user's password (Default: jidgen)</li>
	 * <li><b>searchFilter</b> -- the search filter to use (Default: null)</li>
	 * <li><b>searchBase</b> -- the search base to use (Default: ou=people)</li>
	 * </ul>
	 * <p>
	 * For use with jidgen <i>no attributes</i> are retrieved and the search scope defaults
	 * to <i>subtree</i>.
	 * 
	 * 
	 * @param p
	 * 		properties object to copy values from
	 */
	public void setProperties(Properties p) {
		if (p.containsKey("host")) 
			this.setHost(p.getProperty("host"));

		if (p.containsKey("port")) 
			this.setPort(p.getProperty("port"));

		if (p.containsKey("namingContext")) 
			this.setNamingContext(p.getProperty("namingContext"));

		if (p.containsKey("user")) 
			this.setUser(p.getProperty("user"));

		if (p.containsKey("password")) 
			this.setPassword(p.getProperty("password"));

		if (p.containsKey("searchFilter")) 
			this.setSearchFilter(p.getProperty("searchFilter"));

		if (p.containsKey("searchBase")) 
			this.setSearchBase(p.getProperty("searchBase"));
	}




	/**
	 * @return
	 */
	public String getHost() {
		return this.props.getProperty("host");
	}

	/**
	 * @param ldapHost
	 */
	public void setHost(String ldapHost) {
		this.props.setProperty("host", ldapHost);
	}


	/**
	 * @return
	 */
	public String getPort() {
		return this.props.getProperty("port");
	}


	/**
	 * @param ldapPort
	 */
	public void setPort(String ldapPort) {
		this.props.setProperty("port", ldapPort);
	}


	/**
	 * @return
	 */
	public String getNamingContext() {
		return this.props.getProperty("namingContext");
	}


	/**
	 * @param ldapNamingContext
	 */
	public void setNamingContext(String ldapNamingContext) {
		this.props.setProperty("namingContext", ldapNamingContext);
	}


	/**
	 * @return
	 */
	public String getUser() {
		return this.props.getProperty("user");
	}


	/**
	 * @param ldapUser
	 */
	public void setUser(String ldapUser) {
		this.props.setProperty("user", ldapUser);
	}


	/**
	 * @return
	 */
	public String getPassword() {
		return this.props.getProperty("password");
	}


	/**
	 * @param ldapPassword
	 */
	public void setPassword(String ldapPassword) {
		this.props.setProperty("password", ldapPassword);
	}


	/**
	 * @return
	 */
	public String getSearchFilter() {
		return this.props.getProperty("searchFilter");
	}


	/**
	 * @param ldapSearchFilter
	 */
	public void setSearchFilter(String ldapSearchFilter) {
		logger.debug("ldapSearchFilter = " + ldapSearchFilter);
		this.props.setProperty("searchFilter", ldapSearchFilter);
	}


	/**
	 * @return
	 */
	public String getSearchScope() {
		return this.props.getProperty("searchScope");
	}


	/**
	 * @param ldapSearchScope
	 */
	public void setSearchScope(String ldapSearchScope) {
		this.props.setProperty("searchScope", ldapSearchScope);
	}


	/**
	 * @return
	 */
	public String getSearchBase() {
		return this.props.getProperty("searchBase");
	}


	/**
	 * @param ldapSearchBase
	 */
	public void setSearchBase(String ldapSearchBase) {
		this.props.setProperty("searchBase", ldapSearchBase);
	}


	/**
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}


	/**
	 * @return
	 * 		The result of the last search operation
	 */
	public NamingEnumeration<SearchResult> getResult() {
		return lastResult;
	}


	/**
	 * @return
	 * 		The number of entries of the last search operation
	 */
	public int getResultSize() {
		return lastResultSize;
	}

}
