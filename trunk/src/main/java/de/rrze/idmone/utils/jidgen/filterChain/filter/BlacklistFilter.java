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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.Defaults;
import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.io.FileAccessor;

/**
 * This class is used for filtering IDs from a blacklist.<br/>
 * If the proposed id is contained within the blacklist <em>null</em> is
 * returned to indicate the password is not suitable. Otherwise the password
 * itself is returned. <br/>
 * <i>Intended for use within the FilterChain class.</i>
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 * @author unrz205
 */
public class BlacklistFilter extends AbstractFilter implements IFilter {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(BlacklistFilter.class);

	/**
	 * The file containing the forbidden words<br/>
	 * The connection via this accessor is managed by the filter class alone.
	 */
	private FileAccessor blFileAccessor;

	/**
	 * A list that caches the forbidden words read from the blacklist file
	 */
	private List<String> bl;

	/**
	 * Default constructor.
	 */
	public BlacklistFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName()
				+ ".INIT_MESSAGE"));

		this.bl = new ArrayList<String>();

		this.loadDefaults();
	}

	private void loadDefaults() {
		this.setDefaultProp("filename", Defaults.BLACKLIST_FILE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");

		if (this.blFileAccessor == null)
			connect();

		// Iterate over the list and check whether it contains the word
		for (Iterator<String> iter = bl.iterator(); iter.hasNext();) {
			String blackword = iter.next();

			// filter on match
			if (id.contains(blackword)) {
				logger.debug(Messages.getString("IFilter.FILTER_NAME") + " \""
						+ this.getID() + "\" "
						+ Messages.getString("IFilter.SKIPPED_ID") + " \"" + id
						+ "\"");

				logger.debug(Messages.getString("IFilter.REASON") + " \""
						+ this.getProp("filename") + "\"" + " "
						+ Messages.getString("IFilter.CONTAINS") + " \"" + id
						+ "\"");

				return null;
			}
		}

		return id;
	}

	/**
	 * "Connects" to the blacklist file
	 */
	private void connect() {
		this.blFileAccessor = new FileAccessor(this.getProp("filename"));
	}

	/**
	 * Returns a reference of the blacklist used by this filter.
	 * 
	 * @return list of forbidden words as read from the specified blacklist file
	 */
	public List<String> getBlacklist() {
		return this.bl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#update()
	 */
	public boolean update() {
		logger.trace("Update called.");

		if (this.blFileAccessor == null)
			connect();

		// reset the blacklist file
		this.blFileAccessor.reset();

		// clear the stored blacklist
		this.bl.clear();

		// re-fill the blacklist from file
		String word;
		while ((word = this.blFileAccessor.getLine()) != null) {
			this.bl.add(word);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#autosetID()
	 */
	public void autosetID() {
		this.setID(this.getClass().getSimpleName() + "-"
				+ this.getProp("filename"));
	}
}
