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

package de.rrze.idmone.utils.jidgen.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads the ResourceBundle associated with the IdGenerator. This ResourceBundle
 * contains the localized (external) strings used by the classes.
 * 
 * @author unrz205
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 * 
 */
public class Messages
{
	
	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(Messages.class);
	
	/**
	 * The identifier of the ResourceBundle.
	 */
	public static final String BUNDLE_NAME = "messages";

	/**
	 *  The ResourceBundle instance
	 */
	private static ResourceBundle RESOURCE_BUNDLE = null;

	
	
	
	
	/**
	 * Returns the localized message for this key. If the key is not found its
	 * value is returned surrounded by exclamation marks.
	 * 
	 * @param key the key to be searched for in the resource bundle
	 * @return the localized value for the key
	 */
	public static String getString(String key) {
		if (RESOURCE_BUNDLE == null) {
			loadBundle();
		}
		
		try
		{
			String ret = RESOURCE_BUNDLE.getString(key);
			return ret;
		} catch (MissingResourceException e)
		{
			logger.debug(e.getLocalizedMessage());
			return '!' + key + '!';
		}
	}

	
	/**
	 * Try to load the correct, locale specific, resource bundle
	 * or fall back to the default bundle if the correct one is not available
	 */
	private static void loadBundle() {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		}
		catch (MissingResourceException e) {
			// Try to fall back to the default locale, e.g. "en" (see ISO-639 for two-letter codes)
			logger.debug(e.getMessage());
			logger.debug("Falling back to default locale 'en'.");
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en"));
		}
	}
}
