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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.io.File;

/**
 * A filter for IDs that are already in use within the system's passwd file.
 * 
 * @author unrza249
 */
public class PasswdFilter 
extends AbstractFilter
implements IFilter 
{
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(PasswdFilter.class);

	/**
	 * The location of the passwd file. This should usually be /etc/passwd, at
	 * least for the local system
	 */
	private File passwdFile;

	
	/**
	 *  A list that stores the forbidden logins
	 */
	private List<String> loginList = new ArrayList<String>();
	
	
	
	
	
	
	/**
	 * Default constructor
	 */
	public PasswdFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName() + ".INIT_MESSAGE"));
	}

	/**
	 * @param id
	 */
	public PasswdFilter(String id) {
		super(id);
	}

	/**
	 * @param id
	 * @param description
	 */
	public PasswdFilter(String id, String description) {
		super(id, description);
	}

	
	

	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");
	
		if (this.loginList.contains(id)) {
			logger.trace(Messages.getString("IFilter.FILTER_NAME") 
					+ " \"" + this.getID() + "\" "
					+ Messages.getString("IFilter.SKIPPED_ID") 
					+ " \"" + id
					+ "\"");
			
			logger.debug(Messages.getString("IFilter.REASON")
					+ " \"" + this.getPasswdFile().getFilename() + "\""
					+ " " + Messages.getString("IFilter.CONTAINS")
					+ " \"" + id + "\"");


			return null;
		}
		else {
			return id;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.AbstractFilter#update()
	 */
	public boolean update() {
		logger.trace("Update called.");
		
		// reset the blacklist file
		this.passwdFile.reset();
		
		// clear buffered login list
		this.loginList.clear();
		
		// re-read passwd file and fill list
		String line;
		while ((line = this.passwdFile.getLine()) != null) {
			String userID = line.substring(0, line.indexOf(':'));
			this.loginList.add(userID);
			logger.trace("Added user: \"" + userID + "\"");
		}
		
		
		return true;
	}
	
	/**
	 * @param passwdFile
	 */
	public void setFile(File passwdFile) {
		logger.debug("passwdFile = " + passwdFile);
		this.passwdFile = passwdFile;
	}

	/**
	 * @return
	 */
	public File getPasswdFile() {
		return passwdFile;
	}


}
