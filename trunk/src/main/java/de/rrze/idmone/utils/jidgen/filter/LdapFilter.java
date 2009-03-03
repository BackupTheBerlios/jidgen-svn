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

package de.rrze.idmone.utils.jidgen.filter;

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

import de.rrze.idmone.utils.jidgen.Messages;

/**
 * This a basic filter template class that implements most of the 
 * common filter functions. All real filter implementations should
 * extend this class to avoid duplicate code.
 * 
 * @author unrza249
 *
 */
public class LdapFilter 
extends AbstractFilter
{
	/**
	 * A static logger instance
	 */
	private static final Log logger = LogFactory.getLog(LdapFilter.class);


	/**
	 * Default constructor
	 */
	public LdapFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName() + ".INIT_MESSAGE"));
	}

	/**
	 * @param id
	 */
	public LdapFilter(String id) {
		this.setID(id);
	}

	/**
	 * @param id
	 * @param description
	 */
	public LdapFilter(String id, String description) {
		this(id);
		this.setDescription(description);
	}




	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id)	{
		
		// CONFIG
		String 	ldapHost = "acer";
		String 	ldapNamingContext = "dc=example,dc=com";
		int 	ldapPort = 389;
		String 	ldapUser = "cn=jidgen,ou=people,dc=example,dc=com";
		String 	ldapPassword = "jidgen";
//		String 	ldapUser = "cn=Manager,dc=example,dc=com";
//		String 	ldapPassword = "acer";
		String 	ldapSearchFilter = "(sn={ID})";
		int 	ldapSearchScope = SearchControls.SUBTREE_SCOPE;
		String 	ldapSearchBase = "ou=people";
		
		// START
		logger.trace("Connecting to LDAP server...");		
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");	
		env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort + "/" + ldapNamingContext);
		env.put(Context.SECURITY_PRINCIPAL, ldapUser);
		env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

		try {
			// Create initial context
			DirContext ctx = new InitialDirContext(env);

			// Specify the ids of the attributes to return
			String[] attrIDs = {};

			// Set search controls
			SearchControls ctls = new SearchControls();
			ctls.setReturningAttributes(attrIDs);
			ctls.setSearchScope(ldapSearchScope);

			// Specify the search filter to match
			String filter = ldapSearchFilter.replace("{ID}", id);
			logger.trace("Using search filter: " + filter);
			
			// Search for objects that have those matching attributes
			NamingEnumeration<SearchResult> answer = ctx.search(ldapSearchBase, filter, ctls);

			// Close the context when we're done
			ctx.close();

			if (answer.hasMore()) {
				logger.trace("Found entries:");
				while(answer.hasMore()) {
					logger.trace(answer.next());
				}
				return null;
			}
			else {
				return id;
			}
		}
		catch (NamingException e) {
			logger.fatal("Exception", e);
			System.exit(150);
		}

		return null;
	}
}
