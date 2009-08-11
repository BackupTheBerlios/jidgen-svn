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

package de.rrze.idmone.utils.jidgen.cli;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;

/**
 * This class is an extension to the org.apache.commons.cli.Option class with
 * some modifications to make it fit to the needs of jidgen.
 * 
 * @see <a href=
 *      "http://commons.apache.org/cli/api-release/org/apache/commons/cli/Option.html"
 *      >http://commons.apache.org/cli/api-release/org/apache/commons/cli/Option

 *      .html</a>
 * 
 * @author unrza249
 */
// TODO remove dependency to apache.commons.cli because of lacking functionality
public class IdGenOption extends Option {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(IdGenOption.class);

	/**
	 * If this option shoud be displayed in the help page
	 */
	private boolean visible = true;

	/**
	 * If this option should be processed by the parser when reading the command
	 * line.
	 */
	private boolean dummy = false;

	/**
	 * The options's short identifier
	 */
	private String shortOpt = "";

	/**
	 * A list of default values for this option. Multiple values do not mean
	 * that there are multiple default values to choose from but that this
	 * option has a multi-valued default value.<br/>
	 * E.g. <i>defaultValueList = ['bla', 'blub']</i> stands for <i>-OPTION
	 * bla,blub</i> where "bla,blub" - involving <b>both</b> values - is the
	 * defaultValue.
	 */
	private List<String> defaultValueList;

	/**
	 * A rather complex constructor with the possibility to set most of the
	 * available configuration options at once
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param description
	 *            the description of the CLI option
	 * @param numArgs
	 *            specifies whether the option has arguments and how many
	 * @param argName
	 *            sets the argument name to be displayed
	 * @param valueSeparator
	 *            sets the value separator
	 * @param required
	 *            specifies whether the option is required
	 */
	public IdGenOption(String shortOption, String longOption,
			String description, int numArgs, String argName,
			char valueSeparator, boolean required) {
		super(shortOption, description);
		this.setLongOpt(longOption);

		this.setArgs(numArgs);
		this.setArgName(argName);
		this.setValueSeparator(valueSeparator);

		this.setRequired(required);
	}

	/**
	 * Set, if this option should be processed by the parser when reading the
	 * command line.<br/>
	 * Dummy options are only displayed in the help page, but ignored by the
	 * parser. This is useful if one option should be added as a representative
	 * of multiple other options.<br/>
	 * E.g. options <i>-La</i> ... <i>-Lz</i> are added but not displayed
	 * (invisible). Instead one dummy option <i>-L[a-z]</i> is added to
	 * represent the others in the help page.
	 * 
	 * @param dummy
	 *            if this option should be processed by the parser when reading
	 *            the command line
	 */
	public void setDummy(boolean dummy) {
		this.dummy = dummy;
	}

	/**
	 * Returns true if this option should be ignored by the parser when reading
	 * the command line
	 * 
	 * @return if this option should be ignored by the parser when reading the
	 *         command line
	 */
	public boolean isDummy() {
		return this.dummy;
	}

	/**
	 * Set, if this option is visible in the help page.
	 * 
	 * @param visible
	 *            if this option is visible in the help page
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns true if this option is visible in the help page, false otherwise.
	 * 
	 * @return true if this option is visible in the help page, false otherwise
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/**
	 * Sets the short option identifier for this option.
	 * 
	 * @param shortOpt
	 *            the short option identifier for this option
	 */
	public void setShortOpt(String shortOpt) {
		if (this.isDummy()) {
			this.shortOpt = shortOpt;
		} else {
			logger.warn(Messages.getString("IdGenOption.NO_SET_SHORT_OPT"));
		}
	}

	/**
	 * Return the short option identifier for this option.
	 * 
	 * @returnthe short option identifier for this option
	 */
	public String getShortOpt() {
		if (this.isDummy()) {
			return this.shortOpt;
		} else {
			return super.getOpt();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.cli.Option#getOpt()
	 */
	public String getOpt() {
		return this.getShortOpt();
	}

	/**
	 * Returns true if <i>at least</i> one default value is set for this option.
	 * 
	 * @return true if <i>at least</i> one default value is available, false
	 *         otherwise
	 */
	public boolean hasDefaultValue() {
		return (this.defaultValueList != null);
	}

	/**
	 * Set a new default value for this option.<br/>
	 * The old default value is beeing overwritten.
	 * 
	 * @param defaultValue
	 *            the option's new default value
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValueList = new ArrayList<String>();
		this.defaultValueList.add(defaultValue);
	}

	/**
	 * Set a new default values for this option.<br/>
	 * The old default value is beeing overwritten.
	 * 
	 * @param defaultValue
	 *            the option's new default value list
	 */
	public void setDefaultValues(List<String> defaultValues) {
		this.defaultValueList = defaultValues;
	}

	/**
	 * Returns the default value of the requested option.<br/>
	 * If the requested option has no default value <i>null</i> is returned.<br/>
	 * If the option has multiple default values only the first value is
	 * returned. Use getDefaultValues() to get all default values.
	 * 
	 * @return the options <u>first</u> default value or <i>null</i> if none is
	 *         available
	 */
	public String getDefaultValue() {
		if (this.hasDefaultValue()) {
			return this.getDefaultValues().get(0);
		} else {
			return null;
		}
	}

	/**
	 * Returns the option's default value list (if multiple values act as
	 * default) or <i>null</i> if no default value list is available.
	 * 
	 * @return the options default value list or <i>null</i> if none is
	 *         available
	 */
	public List<String> getDefaultValues() {
		if (this.hasDefaultValue()) {
			return this.defaultValueList;
		} else {
			logger.error(this.getShortOpt() + " "
					+ Messages.getString("IdGenOption.NO_DEFAULT_VALUE"));
			return null;
		}
	}

}
