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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.Ldap;
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
	 * LDAP search -- the ldap search filter to use<br>
	 * The string <b>{ID}</b> will be replaced by the ID which is currently being checked.
	 */
	private String searchFilter = "(sn={ID})";
	
	/**
	 * The LDAP connection object for this filter
	 */
	private Ldap ldap;
	

	
	
	
	/**
	 * Default constructor
	 */
	public LdapFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName() + ".INIT_MESSAGE"));
	}
	
	/**
	 * Constructor with filter ID
	 * 
	 * @param id
	 * 		A unique ID to identify this filter object within the filter chain
	 */
	public LdapFilter(String id) {
		super(id);
	}

	/**
	 * Constructor with filter ID and description
	 * 
	 * @param id
	 * 		A unique ID to identify this filter object within the filter chain
	 * @param description
	 * 		A textual description for this filter object to be printed on usage
	 */
	public LdapFilter(String id, String description) {
		super(id, description);
	}


	
	
	
	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id)	{
		logger.trace("Checking ID '" + id + "'");
		
		// Specify the search filter to match
		this.ldap.setSearchFilter(this.searchFilter.replace("{ID}", id));

		// execute the search request
		if (this.ldap.doSearch()) {
			return null;
		}
		else {
			return id;
		}
	}

	/**
	 * @return
	 */
	public String getSearchFilter() {
		return searchFilter;
	}

	/**
	 * @param searchFilter
	 */
	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	/**
	 * @return
	 */
	public Ldap getLdap() {
		return ldap;
	}

	/**
	 * @param ldap
	 */
	public void setLdap(Ldap ldap) {
		this.ldap = ldap;
	}
}
