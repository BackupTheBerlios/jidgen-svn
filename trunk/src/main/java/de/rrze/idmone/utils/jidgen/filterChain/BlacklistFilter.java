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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.File;
import de.rrze.idmone.utils.jidgen.Messages;


/**
 * This class is used for filtering IDs from a blacklist. If the proposed
 * id is contained within the blacklist, <em>null</em> is returned to
 * indicate the password is not suitable. Otherwise the password itself is
 * returned.
 * 
 * @author unrza249
 * @author unrz205
 */
public class BlacklistFilter 
extends AbstractFilter
implements IFilter
{
	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(BlacklistFilter.class);

	/**
	 * The file containing the forbidden words 
	 */
	private File blacklistFile;
	
	
	/**
	 *  A list that stores the forbidden words
	 */
	private List<String> blacklist = new ArrayList<String>();
	
	
	/**
	 * Default constructor.
	 */
	public BlacklistFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName() + ".INIT_MESSAGE"));
	}

	/**
	 * @param id
	 */
	public BlacklistFilter(String id) {
		super(id);
	}
	
	/**
	 * @param id
	 * @param description
	 */
	public BlacklistFilter(String id, String description) {
		super(id, description);
	}	
	

	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");
		
		// Iterate over the list and check whether it contains the word
		for (Iterator<String> iter = blacklist.iterator(); iter.hasNext();)	{
			String blackword = iter.next();

			// filter on match
			if (id.contains(blackword)) {
				logger.debug(Messages.getString("IFilter.TRACE_FILTER_NAME") 
						+ " \"" + this.getID() + "\" "
						+ Messages.getString("IFilter.TRACE_SKIPPED_ID") 
						+ " \"" + id
						+ "\"");
				
				return null;
			}
		}

		return id;
	}

	/**
	 * Returns a reference of the blacklist used by this filter and
	 * <em>null</em> if the filters is purely procedural and checks
	 * ids against rule.
	 * 
	 * @return the blacklist of the filter or <em>null</em> if one is not
	 *         used.
	 */
	public List<String> getBlacklist() {
		return this.blacklist;
	}

	/**
	 * Sets the blacklist of the filter.
	 * 
	 * @param blacklist
	 */
	public void setBlacklist(List<String> blacklist) {
		this.blacklist = blacklist;
	}

	/**
	 * Adds a password to the list of forbidden ids.
	 * 
	 * @param blackWord
	 *            the forbidden word
	 */
	public void add(String blackWord) {
			logger.trace("Added blackword: \"" + blackWord + "\"");
			this.blacklist.add(blackWord);
	}

	/**
	 * Removes a word from the blacklist.
	 * 
	 * @param blackWord
	 *            the word to be removed from the blacklist
	 */
	public void remove(String blackWord) {
		this.blacklist.remove(blackWord);
	}

	/**
	 * Removes all words from the blacklist leaving it empty.
	 */
	public void clear() {
		this.blacklist.clear();
	}
	
	
	/* (non-Javadoc)
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#update()
	 */
	public boolean update() {
		logger.trace("Update called.");
		
		// reset the blacklist file
		this.blacklistFile.reset();
		
		// clear the stored blacklist
		this.clear();
		
		// re-fill the blacklist from file
		String word;
		while((word = this.blacklistFile.getLine()) != null) {
			this.add(word);
		}
		
		return true;
	}
	
	
	/**
	 * @return
	 */
	public File getBlacklistFile() {
		return blacklistFile;
	}

	/**
	 * @param blacklistFile
	 */
	public void setFile(File blacklistFile) {
		logger.debug("blacklistFile = " + blacklistFile);
		this.blacklistFile = blacklistFile;
	}

}
