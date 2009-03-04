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

package de.rrze.idmone.utils.jidgen;

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
 * project.
 * 
 * @author unrza249
 *
 */
public class Ldap {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(Ldap.class);

	/**
	 * LDAP connection -- the hostname to connect to
	 */
	private String 	ldapHost = "acer";
	
	/**
	 * LDAP connection -- the port to connect to<br>
	 * Defaults to <b>389</b>
	 */
	private int 	ldapPort = 389;
	
	/**
	 * LDAP connection -- the naming context to use
	 */
	private String 	ldapNamingContext = "dc=example,dc=com";
	
	/**
	 * LDAP authentication -- the DN to bind to
	 */
	private String 	ldapUser = "cn=jidgen,ou=people,dc=example,dc=com";
	/**
	 * LDAP authentication -- the password to use for the binding user
	 */
	private String 	ldapPassword = "jidgen";
	
	/**
	 * LDAP search -- the ldap search filter to use<br>
	 * The string <b>{ID}</b> will be replaced by the ID which is currently being checked.
	 */
	private String 	ldapSearchFilter = null;
	
	/**
	 * LDAP search -- attributes to return for the search results
	 */
	private String[] ldapSearchAttributes = {};
	
	/**
	 * LDAP search -- the search scope<br/>
	 * Defaults to <b>SUBTREE</b> 
	 */
	private int 	ldapSearchScope = SearchControls.SUBTREE_SCOPE;
	
	/**
	 * LDAP search -- the search base to use<br />
	 * Defaults to <b>ou=people</b>
	 */
	private String 	ldapSearchBase = "ou=people";
	
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
		env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort + "/" + ldapNamingContext);
		env.put(Context.SECURITY_PRINCIPAL, ldapUser);
		env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

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
		
		if (this.ldapSearchAttributes != null) {
			this.ctls.setReturningAttributes(this.ldapSearchAttributes);
		}
		
		if (this.ldapSearchFilter != null) {
			this.ctls.setSearchScope(this.ldapSearchScope);
		}
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
			this.lastResult = this.ctx.search(this.ldapSearchBase, this.ldapSearchFilter, ctls);
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
	 * @return
	 */
	public String getHost() {
		return ldapHost;
	}

	/**
	 * @param ldapHost
	 */
	public void setHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}


	/**
	 * @return
	 */
	public int getPort() {
		return ldapPort;
	}


	/**
	 * @param ldapPort
	 */
	public void setPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}


	/**
	 * @return
	 */
	public String getNamingContext() {
		return ldapNamingContext;
	}


	/**
	 * @param ldapNamingContext
	 */
	public void setNamingContext(String ldapNamingContext) {
		this.ldapNamingContext = ldapNamingContext;
	}


	/**
	 * @return
	 */
	public String getUser() {
		return ldapUser;
	}


	/**
	 * @param ldapUser
	 */
	public void setUser(String ldapUser) {
		this.ldapUser = ldapUser;
	}


	/**
	 * @return
	 */
	public String getPassword() {
		return ldapPassword;
	}


	/**
	 * @param ldapPassword
	 */
	public void setPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}


	/**
	 * @return
	 */
	public String getSearchFilter() {
		return ldapSearchFilter;
	}


	/**
	 * @param ldapSearchFilter
	 */
	public void setSearchFilter(String ldapSearchFilter) {
		logger.debug("ldapSearchFilter = " + ldapSearchFilter);
		this.ldapSearchFilter = ldapSearchFilter;
	}


	/**
	 * @return
	 */
	public int getSearchScope() {
		return ldapSearchScope;
	}


	/**
	 * @param ldapSearchScope
	 */
	public void setSearchScope(int ldapSearchScope) {
		this.ldapSearchScope = ldapSearchScope;
	}


	/**
	 * @return
	 */
	public String getSearchBase() {
		return ldapSearchBase;
	}


	/**
	 * @param ldapSearchBase
	 */
	public void setSearchBase(String ldapSearchBase) {
		this.ldapSearchBase = ldapSearchBase;
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
