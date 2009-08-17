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

package de.rrze.idmone.utils.jidgen.filterChain.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.io.LdapAccessor;

/**
 * This class filters IDs against a configurable LDAP directory server via JNDI.<br/>
 * <i>Intended for use within the FilterChain class.</i>
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 * 
 */
public class LdapFilter extends AbstractFilter {
	/**
	 * A static logger instance
	 */
	private static final Log logger = LogFactory.getLog(LdapFilter.class);

	/**
	 * The LDAP access object for this filter.<br/>
	 * The LDAP connection via this accessor is managed by the filter class
	 * alone.
	 */
	private LdapAccessor ldapAccessor;

	/**
	 * Default constructor
	 */
	public LdapFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName()
				+ ".INIT_MESSAGE"));

		this.loadDefaults();
	}

	/**
	 * Loads the default properties for this filter.<br/>
	 * <br/>
	 * At the moment the following properties are recognized:<br />
	 * <ul>
	 * <li><b>host</b> -- the hostname to connect to (Default: localhost)</li>
	 * <li><b>port</b> -- the port to connect to (Default: 389)</li>
	 * <li><b>namingContext</b> -- the naming context to use (Default:
	 * dc=example,dc=com)</li>
	 * <li><b>user</b> -- the user DN to bind to (Default:
	 * cn=jidgen,ou=people,dc=example,dc=com)</li>
	 * <li><b>password</b> -- the bind-user's password (Default: jidgen)</li>
	 * <li><b>searchFilter</b> -- the search filter to use (Default: null)</li>
	 * <li><b>searchBase</b> -- the search base to use (Default: ou=people)</li>
	 * </ul>
	 */
	private void loadDefaults() {
		// TODO This should be located in the Defaults class
		this.setDefaultProp("host", "localhost");
		this.setDefaultProp("port", "389");
		this.setDefaultProp("namingContext", "dc=example,dc=com");
		this.setDefaultProp("user", "cn=jidgen,ou=people,dc=example,dc=com");
		this.setDefaultProp("password", "jidgen");

		/*
		 * The ldap search filter to use.<br> The string <b>{ID}</b> will be
		 * replaced by the ID which is currently being checked.
		 */
		this.setDefaultProp("searchFilter", "(sn={ID})");

		this.setDefaultProp("searchBase", "ou=people");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");

		if (this.ldapAccessor == null) {
			this.connect();
		}

		// Update the search filter to the current id.
		// This has to be updated for every id that must be checked!
		this.ldapAccessor.setSearchFilter(this.getProp("searchFilter").replace(
				"{ID}", id));

		// execute the search request
		if (this.ldapAccessor.doSearch()) {
			logger.debug(Messages.getString("IFilter.FILTER_NAME") + " \""
					+ this.getID() + "\" "
					+ Messages.getString("IFilter.SKIPPED_ID") + " \"" + id
					+ "\"");
			logger
					.debug(Messages.getString("IFilter.REASON") + " \""
							+ this.getProp("searchFilter") + "\"" + " "
							+ Messages.getString("IFilter.MATCHED") + " \""
							+ id + "\"");

			return null;
		} else {
			return id;
		}
	}

	/**
	 * Instantiates the LDAP accessor with the given properties and connects to
	 * the directory server.
	 */
	private void connect() {
		// get an ldap accessor instance
		this.ldapAccessor = new LdapAccessor();

		// configure the ldap accessor
		this.ldapAccessor.setHost(this.getProp("host"));
		this.ldapAccessor.setPort(this.getProp("port"));
		this.ldapAccessor.setUser(this.getProp("user"));
		this.ldapAccessor.setPassword(this.getProp("password"));
		this.ldapAccessor.setSearchBase(this.getProp("searchBase"));
		this.ldapAccessor.setNamingContext(this.getProp("namingContext"));

		// connect
		this.ldapAccessor.connect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#autosetID()
	 */
	public void autosetID() {
		this.setID(LdapFilter.class.getSimpleName() + "-" + getProp("host")
				+ ":" + getProp("port") + "/" + getProp("namingContext"));
	}
}
