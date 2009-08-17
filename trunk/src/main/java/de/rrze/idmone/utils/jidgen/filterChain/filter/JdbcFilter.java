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
import de.rrze.idmone.utils.jidgen.io.JdbcAccessor;

/**
 * This class filters IDs against a generic database backend connected via a
 * configurable JDBC driver.<br/>
 * <i>Intended for use within the FilterChain class.</i>
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 * 
 */
public class JdbcFilter extends AbstractFilter {
	/**
	 * A static logger instance
	 */
	private static final Log logger = LogFactory.getLog(JdbcFilter.class);

	/**
	 * The JDBC accessor object that provides access to the database<br/>
	 * The connection via this accessor is managed by the filter class alone.
	 */
	private JdbcAccessor jdbcAccessor;

	/**
	 * Default constructor
	 */
	public JdbcFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName()
				+ ".INIT_MESSAGE"));

		this.loadDefaults();
	}

	private void loadDefaults() {
		this.setDefaultProp("driverClassPath", ".");
		this.setDefaultProp("driver", "com.mysql.jdbc.Driver");
		// URL: jdbc:<subprotocol>:<subname>
		this.setDefaultProp("url", "jdbc:mysql://localhost:3306/jidgen");
		this.setDefaultProp("user", "jidgen");
		this.setDefaultProp("password", "jidgen");
		this.setDefaultProp("query",
				"SELECT `user_name` FROM `users` WHERE `user_name` = '{ID}';");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");

		if (this.jdbcAccessor == null)
			this.connect();

		// execute the search request
		this.jdbcAccessor.query(this.getProp("query").replace("{ID}", id));
		int numRows = this.jdbcAccessor.getNumRows();

		if (numRows > 0) {
			logger.debug(Messages.getString("IFilter.FILTER_NAME") + " \""
					+ this.getID() + "\" "
					+ Messages.getString("IFilter.SKIPPED_ID") + " \"" + id
					+ "\"");
			logger
					.debug(Messages.getString("IFilter.REASON") + " \""
							+ this.getProp("query") + "\"" + " "
							+ Messages.getString("IFilter.MATCHED") + " \""
							+ id + "\"");

			return null;
		} else {
			return id;
		}
	}

	/**
	 * Instantiates the JDBC accessor with the given properties and connects to
	 * the database.
	 */
	private void connect() {
		// get a jdbc accessor instance
		this.jdbcAccessor = new JdbcAccessor();

		// configure the jdbc accessor
		this.jdbcAccessor.setDriverClassPath(this.getProp("driverClassPath"));
		this.jdbcAccessor.setDriver(this.getProp("driver"));
		this.jdbcAccessor.setUrl(this.getProp("url"));
		this.jdbcAccessor.setUser(this.getProp("user"));
		this.jdbcAccessor.setPassword(this.getProp("password"));

		// connect to the database
		this.jdbcAccessor.connect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#autosetID()
	 */
	public void autosetID() {
		this.setID(this.getClass().getSimpleName() + "-" + this.getProp("url"));
	}

}
