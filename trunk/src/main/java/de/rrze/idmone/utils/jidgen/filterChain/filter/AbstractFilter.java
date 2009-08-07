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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;

/**
 * This a basic filter template class that implements most of the common filter
 * functions. All real filter implementations should extend this class to avoid
 * duplicate code.
 * 
 * @author unrza249
 * 
 */
public abstract class AbstractFilter implements IFilter {

	/**
	 * A static logger instance
	 */
	private static final Log logger = LogFactory.getLog(AbstractFilter.class);

	/**
	 * The filter id defaults to the class name
	 */
	private String id = this.getClass().getSimpleName();

	/**
	 * The filter description is retrieved from the external messages file by
	 * default. The identifier is {ClassName}.DESCR
	 */
	private String description = Messages.getString(this.id.substring(this.id
			.lastIndexOf('.') + 1)
			+ ".DESC");

	/**
	 * Default properties to fall back on when no appropriate property is
	 * available elsewhere
	 */
	private Properties defaultProps = new Properties();

	/**
	 * User-set properties with the default properties to fall back on.
	 */
	private Properties filterProps = new Properties(defaultProps);

	/**
	 * Default constructor
	 */
	public AbstractFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#getProp(java.lang
	 * .String)
	 */
	public String getProp(String propName) {
		return this.filterProps.getProperty(propName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#setProp(java.lang
	 * .String, java.lang.String)
	 */
	public void setProp(String propName, String propValue) {
		Log logger = LogFactory.getLog(this.getClass());
		logger.debug(propName + " = " + propValue);
		this.filterProps.setProperty(propName, propValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#setDefaultProp
	 * (java.lang.String, java.lang.String)
	 */
	public void setDefaultProp(String propName, String propValue) {
		Log logger = LogFactory.getLog(this.getClass());
		logger.debug("[DEFAULT] " + propName + " = " + propValue);
		this.defaultProps.setProperty(propName, propValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#loadProps(java
	 * .lang.String)
	 */
	public void loadPropFile(String filename) {
		Log logger = LogFactory.getLog(this.getClass());
		try {
			this.filterProps.load(new FileReader(filename));
		} catch (FileNotFoundException e) {
			logger.fatal("", e);
			System.exit(-1); // TODO
		} catch (IOException e) {
			logger.fatal("", e);
			System.exit(-1); // TODO
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#getID()
	 */
	public String getID() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.rrze.idmone.utils.jidgen.filter.IFilter#setDescription(java.lang.String
	 * )
	 */
	public void setDescription(String new_description) {
		this.description = new_description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#setID(java.lang.String)
	 */
	public void setID(String new_id) {
		this.id = new_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#getType()
	 */
	public String getType() {
		return this.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.util.List)
	 */
	public List<String> apply(List<String> id) {
		List<String> suitable = new ArrayList<String>();

		for (Iterator<String> iter = id.iterator(); iter.hasNext();) {
			String element = (String) iter.next();

			// add to suitable array if not filtered
			if (apply(element) != null)
				suitable.add(element);
		}
		return suitable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#update()
	 */
	public boolean update() {
		/*
		 * This is just a convenience stub and should be overridden by all
		 * subclasses that actually have something that is worth updating.
		 */
		logger.trace("Nothing to update.");
		return true;
	}
	

}
