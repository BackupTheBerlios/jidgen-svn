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

import java.util.List;

/**
 * Interface representing the basic functionality that should be supported by an
 * id filter class. Implementations of this class can be registered for usage by
 * the FilterChain class.
 * 
 * @author unrza249
 * @author unrz205
 */
public interface IFilter {

	/**
	 * Returns the property value with the given name. If the the property is
	 * not speciefied a default value is returned instead - if available.
	 * 
	 * @param propName
	 *            the name of the property - aka "key"
	 * @return the property's value
	 */
	public abstract String getProp(String propName);

	/**
	 * Sets a property with the specified name and value.
	 * 
	 * @param propName
	 *            the property name to use - aka "key"
	 * @param propValue
	 *            the property's value
	 */
	public abstract void setProp(String propName, String propValue);

	/**
	 * Sets a default property with the specified name and value.<br/>
	 * This value will only be returned by the getProp() method if there was no
	 * property with the same named set by the setProp(String,String) method.<br/>
	 * <i>Although this method is public it is mostly used internally. However
	 * if you think you must use it, you are free to do so.</i>
	 * 
	 * @param propName
	 *            the property name to use - aka "key"
	 * @param propValue
	 *            the property's <b>default</b> value to fall back on
	 */
	public abstract void setDefaultProp(String propName, String propValue);

	/**
	 * Loads a set of properties from the given filename.<br/>
	 * The properties file must be formatted according to the Java Propteries
	 * File specifications.
	 * 
	 * @param filename
	 *            the name of the properties file without the ".properties"
	 *            ending
	 */
	public abstract void loadPropFile(String filename);

	/**
	 * This method must return the unique identifier of the filter. A unique
	 * identifier is needed for correct registration of the filter.
	 * 
	 * @return the filter identifier
	 */
	public abstract String getID();

	/**
	 * Sets the identifier of this filter. A filter should have a predefined
	 * identifier. A good idea is to use the class.getName() method.
	 * 
	 * @param id
	 */
	public abstract void setID(String id);

	/**
	 * This method returns a short description of what the filter is doing and
	 * how.
	 * 
	 * @return description
	 */
	public abstract String getDescription();

	/**
	 * This method sets the description of the filter.
	 * 
	 * @param description
	 */
	public abstract void setDescription(String description);

	/**
	 * This method does the actual filtering. It implements the main logic of
	 * the filter.
	 * 
	 * @param id
	 *            the id to be checked
	 * @return <em>null</em> if the id should be filtered and the id if it
	 *         satisfies the rules.
	 */
	public abstract String apply(String id);

	/**
	 * This method checks a whole list of ids. It should return a list of
	 * suitable ids or an empty list if none of the ids fits the rules.
	 * 
	 * @param ids
	 *            a list of ids to be checked
	 * @return the list with filtered ids
	 */
	public abstract List<String> apply(List<String> ids);

	/**
	 * Returns the filter type (simple class name)
	 * 
	 * @return filter type
	 */
	public abstract String getType();

	/**
	 * Re-reads data needed for the filter process, e.g. an externally stored
	 * blacklist
	 * 
	 * @return true on success, false on failure
	 */
	public abstract boolean update();

	/**
	 * Try to automatically assemble a verbose and unique id for this filter.<br/>
	 * <i>This should only be used after setting the filter's properties</i>
	 */
	public abstract void autosetID();
}
