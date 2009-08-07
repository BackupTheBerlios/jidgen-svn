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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.io.Jdbc;

/**
 * This a basic filter template class that implements most of the 
 * common filter functions. All real filter implementations should
 * extend this class to avoid duplicate code.
 * 
 * @author unrza249
 *
 */
public class JdbcFilter 
	extends AbstractFilter
{
	/**
	 * A static logger instance
	 */
	private static final Log logger = LogFactory.getLog(JdbcFilter.class);

	private String searchQuery = "(sn={ID})";
	
	private Jdbc jdbc;

	/**
	 * Default constructor
	 */
	public JdbcFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName() + ".INIT_MESSAGE"));
	}

	/**
	 * Constructor with filter ID
	 * 
	 * @param id
	 * 		A unique ID to identify this filter object within the filter chain
	 */
	public JdbcFilter(String id) {
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
	public JdbcFilter(String id, String description) {
		super(id, description);
	}





	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id)	{
		logger.trace("Checking ID '" + id + "'");

		String query = this.searchQuery.replace("{ID}", id);

		// execute the search request
		this.jdbc.query("SELECT `user_name` FROM `users` WHERE `user_name` = '" + id + "';");
		int numRows = this.jdbc.getNumRows();
		
		if (numRows > 0) {
			logger.debug(Messages.getString("IFilter.FILTER_NAME") 
					+ " \"" + this.getID() + "\" "
					+ Messages.getString("IFilter.SKIPPED_ID") 
					+ " \"" + id
					+ "\"");		
			logger.debug(Messages.getString("IFilter.REASON")
					+ " \"" + this.getSearchQuery() + "\""
					+ " " + Messages.getString("IFilter.MATCHED")
					+ " \"" + id + "\"");

			return null;
		}
		else {
			return id;
		}
	}

	/**
	 * @return
	 */
	public String getSearchQuery() {
		return searchQuery;
	}

	/**
	 * @param searchQuery
	 */
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	/**
	 * @return the jdbc
	 */
	public Jdbc getJdbc() {
		return jdbc;
	}

	/**
	 * @param jdbc the jdbc to set
	 */
	public void setJdbc(Jdbc jdbc) {
		this.jdbc = jdbc;
	}

}
