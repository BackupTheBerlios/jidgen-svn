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

import java.util.Hashtable;

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
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
public class LdapAccessor {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(LdapAccessor.class);


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

	
	
	private String host;
	private String port;
	private String user;
	private String password;
	private String namingContext;
	private String searchFilter;
	private String searchBase;





	/**
	 * Default constructor
	 */
	public LdapAccessor() {	
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
		env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port + "/" + namingContext);
		env.put(Context.SECURITY_PRINCIPAL, user);
		env.put(Context.SECURITY_CREDENTIALS, password);

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
			this.lastResult = this.ctx.search(searchBase, searchFilter, ctls);
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
		return host + ":" + port + "/" + namingContext;
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

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the namingContext
	 */
	public String getNamingContext() {
		return namingContext;
	}

	/**
	 * @param namingContext the namingContext to set
	 */
	public void setNamingContext(String namingContext) {
		this.namingContext = namingContext;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return user;
	}

	/**
	 * @param username the username to set
	 */
	public void setUser(String username) {
		this.user = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the searchFilter
	 */
	public String getSearchFilter() {
		return searchFilter;
	}

	/**
	 * @param searchFilter the searchFilter to set
	 */
	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	/**
	 * @return the searchBase
	 */
	public String getSearchBase() {
		return searchBase;
	}

	/**
	 * @param searchBase the searchBase to set
	 */
	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

}
