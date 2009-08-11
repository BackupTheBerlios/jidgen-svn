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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.Defaults;
import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.io.FileAccessor;

/**
 * A filter for IDs that are already in use within the system's passwd file.<br/>
 * <i>Intended for use within the FilterChain class.</i>
 * 
 * @author unrza249
 */
public class PasswdFilter extends AbstractFilter implements IFilter {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(PasswdFilter.class);

	/**
	 * The location of the passwd file. This should usually be /etc/passwd, at
	 * least for the local system.<br/>
	 * The connection via this accessor is managed by the filter class alone.
	 */
	private FileAccessor pwFileAccessor;

	/**
	 * A list that stores the forbidden logins
	 */
	private List<String> loginList;

	/**
	 * Default constructor
	 */
	public PasswdFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName()
				+ ".INIT_MESSAGE"));

		this.loginList = new ArrayList<String>();
		
		this.loadDefaults();
	}

	
	private void loadDefaults() {
		this.setDefaultProp("filename", Defaults.PASSWD_FILE);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");

		if (this.pwFileAccessor == null)
			this.connect();

		if (this.loginList.contains(id)) {
			logger.trace(Messages.getString("IFilter.FILTER_NAME") + " \""
					+ this.getID() + "\" "
					+ Messages.getString("IFilter.SKIPPED_ID") + " \"" + id
					+ "\"");

			logger.debug(Messages.getString("IFilter.REASON") + " \""
					+ this.getProp("filename") + "\"" + " "
					+ Messages.getString("IFilter.CONTAINS") + " \"" + id
					+ "\"");

			return null;
		} else {
			return id;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.AbstractFilter#update()
	 */
	public boolean update() {
		logger.trace("Update called.");

		if (this.pwFileAccessor == null)
			this.connect();
			
		// reset the blacklist file
		this.pwFileAccessor.reset();

		// clear buffered login list
		this.loginList.clear();

		// re-read passwd file and fill list
		String line;
		while ((line = this.pwFileAccessor.getLine()) != null) {
			String userID = line.substring(0, line.indexOf(':'));
			this.loginList.add(userID);
			logger.trace("Added user: \"" + userID + "\"");
		}

		return true;
	}

	/**
	 * "Connects" to the passwd file
	 */
	private void connect() {
		this.pwFileAccessor = new FileAccessor(this.getProp("filename"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#autosetID()
	 */
	public void autosetID() {
		this.setID(this.getClass().getSimpleName() + "-" + this.getProp("filename"));
	}
}
