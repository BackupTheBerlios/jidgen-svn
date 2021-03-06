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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.Defaults;
import de.rrze.idmone.utils.jidgen.i18n.Messages;

/**
 * An options manager class for jidgen extending the
 * org.apache.commons.cli.Options class.
 * 
 * @see <a href=
 *      "http://commons.apache.org/cli/api-release/org/apache/commons/cli/Options.html"
 *      >http://commons.apache.org/cli/api-release/org/apache/commons/cli/

 *      Options.html</a>
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
// TODO remove dependency to apache.commons.cli because of lacking functionality
public class IdGenOptions extends Options {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(IdGenOptions.class);

	/**
	 * The terminal width in number of characters
	 */
	private int termWidth;

	/**
	 * The internal data array for storing all parsed CLI options with their
	 * arguments.
	 */
	// TODO Make the stored data generic (->Object) and ensure typesafety
	// through own checks
	private HashMap<String, List<String>> data;

	/**
	 * List of dummy options that are displayed but not processed by the parser.
	 * This is useful if one option should be added as a representative of
	 * multiple other options.<br/>
	 * E.g. options <i>-La</i> ... <i>-Lz</i> are added but not displayed
	 * (invisible). Instead one dummy option <i>-L[a-z]</i> is added to
	 * represent the others in the help page.
	 */
	private HashMap<String, IdGenOption> dummyOptions;

	/**
	 * Default constructor
	 */
	public IdGenOptions() {
		this.data = new HashMap<String, List<String>>();
		this.dummyOptions = new HashMap<String, IdGenOption>();
		this.termWidth = Defaults.TERM_WIDTH;
	}

	/**
	 * A rather complex function with the possibility to set most of the
	 * available configuration options at once.<br />
	 * This also handles the special dummy options.
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param description
	 *            the description of the cCLI option
	 * @param numArgs
	 *            specifies whether the option has arguments and how many
	 * @param argName
	 *            sets the argument name to be displayed
	 * @param valueSeparator
	 *            sets the value separator
	 * @param required
	 *            specifies whether the option is required
	 * @param visible
	 *            specifies whether the option should be displayed in the help
	 *            string
	 * @param dummy
	 *            specifies whether the option should be interpreted by the
	 *            parser<br/>
	 *            This is useful for creating options only for output in the
	 *            help string that describe one or more hidden options in one.
	 */
	protected IdGenOption add(String shortOption, String longOption,
			String description, int numArgs, String argName,
			char valueSeparator, boolean required, boolean visible,
			boolean dummy, String defaultValue) {

		IdGenOption option;

		if (dummy) {
			option = new IdGenOption("dummy_" + this.dummyOptions.size(),
					longOption, description, numArgs, argName, valueSeparator,
					required);
		} else {
			option = new IdGenOption(shortOption, longOption, description,
					numArgs, argName, valueSeparator, required);
		}

		option.setVisible(visible);
		option.setDummy(dummy);

		if (!defaultValue.isEmpty()) {
			option.setDefaultValue(defaultValue);
		}

		if (dummy) {
			option.setShortOpt(shortOption);
		}

		this.addOption(option);

		return option;
	}

	/**
	 * A rather complex function with the possibility to set most of the
	 * available configuration options at once.<br />
	 * This just adds a regular option.<br />
	 * The option is not required by default.
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param description
	 *            the description of the cCLI option
	 * @param numArgs
	 *            specifies whether the option has arguments and how many
	 * @param argName
	 *            sets the argument name to be displayed
	 * @param valueSeparator
	 *            sets the value separator
	 */
	public IdGenOption add(String shortOption, String longOption,
			String description, int numArgs, String argName, char valueSeparator) {
		return this.add(shortOption, longOption, description, numArgs, argName,
				valueSeparator, false, true, false, "");
	}

	/**
	 * A simple function for adding just a regular option without argument. The
	 * option is not required by default.
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param description
	 *            the description of the CLI option
	 */
	public IdGenOption add(String shortOption, String longOption,
			String description) {
		return this.add(shortOption, longOption, description, 0, "", ' ',
				false, true, false, "");
	}

	/**
	 * A rather complex function with the possibility to set most of the
	 * available configuration options at once.<br />
	 * This adds a hidden option that will not be displayed in the help string
	 * but still be parsed when specified.<br />
	 * The option is not required by default.
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param numArgs
	 *            specifies whether the option has arguments and how many
	 * @param argName
	 *            sets the argument name to be displayed
	 * @param valueSeparator
	 *            sets the value separator
	 */
	public IdGenOption addInvisible(String shortOption, String longOption,
			int numArgs, String argName, char valueSeparator) {
		return this.add(shortOption, longOption, "", numArgs, argName,
				valueSeparator, false, false, false, "");
	}

	/**
	 * A rather complex function with the possibility to set most of the
	 * available configuration options at once.<br />
	 * This adds a dummy option that will be displayed in the help string but is
	 * <b>not</b> processed by the parser.<br />
	 * This is useful for creating options only for output in the help string
	 * that describe one or more hidden options in one line.
	 * 
	 * @param shortOption
	 *            a one letter flag
	 * @param longOption
	 *            long flag
	 * @param description
	 *            the description of the cCLI option
	 * @param numArgs
	 *            specifies whether the option has arguments and how many
	 * @param argName
	 *            sets the argument name to be displayed
	 */
	public IdGenOption addDummy(String shortOption, String longOption,
			String description, int numArgs, String argName) {
		return this.add(shortOption, longOption, description, numArgs, argName,
				' ', false, true, true, "");
	}

	/**
	 * Adds a single option object to this options object
	 * 
	 * @param option
	 *            the option to be added
	 * @return the added option
	 */
	public IdGenOption addOption(IdGenOption option) {
		super.addOption(option);

		if (option.isDummy()) {
			this.dummyOptions.put(option.getShortOpt(), option);
		}
		return option;
	}

	/**
	 * Fill the internal variable data by parsing a given array of command line
	 * options.
	 * 
	 * @param args
	 *            the String array containing all command line options
	 * @return the data collection
	 * @throws ParseException
	 */
	public void parse(String[] args) throws ParseException {
		// init the parser
		BasicParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(this, args);

		// iterate over all command line options
		Iterator<IdGenOption> iter = this.getOptions().iterator();
		while (iter.hasNext()) {

			IdGenOption currentOption = iter.next();
			// logger.trace("Processing option \"" + currentOption.getShortOpt()
			// + "\"");

			if (commandLine.hasOption(currentOption.getShortOpt())) {
				// option was specified
				String[] values = commandLine.getOptionValues(currentOption
						.getShortOpt());
				if (values != null) {
					this.setValues(currentOption.getShortOpt(), values);
					logger.debug(currentOption.getShortOpt() + " = "
							+ this.getValues(currentOption.getShortOpt()));
				} else if (currentOption.hasArg()) {
					/*
					 * Option does NOT have a value but should have one -->
					 * missing argument
					 */
					logger
							.fatal(currentOption.getShortOpt()
									+ " "
									+ Messages
											.getString("IdGenOptions.MISSING_ARGUMENT"));
					System.out.println(this.getFormattedOptionsHelp(false));
					System.exit(-1);
				} else {
					/*
					 * Option does NOT have a value and that's ok --> option
					 * without an argument --> switch. At least put an entry
					 * with an empty string in the data array to mark that the
					 * option was specified
					 */
					this.addSwitch(currentOption.getShortOpt());
				}
			} else {
				// option was NOT specified, so use default if available
				if (currentOption.hasDefaultValue()) {
					// has default
					logger.info(currentOption.getShortOpt() + " "
							+ Messages.getString("IdGenOptions.USING_DEFAULT")
							+ " " + currentOption.getDefaultValue());
					this.addValue(currentOption.getShortOpt(), currentOption
							.getDefaultValue());
				}
			}
		}
	}

	/**
	 * This adds an entry with a value to the recognized options. If the entry
	 * already exists the value is <b>added</b> to the existing argument list,
	 * which makes the entry multi-valued.
	 * 
	 * @param shortOpt
	 *            the short name of the option
	 * @param value
	 *            the value to add to the argument list of the specified option
	 */
	public void addValue(String shortOpt, String value) {
		if (this.data.containsKey(shortOpt)) {
			List<String> l = this.data.get(shortOpt);
			l.add(value);
		} else {
			List<String> l = new ArrayList<String>();
			l.add(value);
			this.data.put(shortOpt, l);
		}
	}

	/**
	 * This adds an entry without any values to the recognized options. This
	 * marks the presence of an option without an argument --> switch.
	 * 
	 * @param shortOpt
	 *            the short name of the switch
	 */
	public void addSwitch(String shortOpt) {
		this.data.put(shortOpt, null);
	}

	/**
	 * This adds an entry with a value to the recognized options. If the entry
	 * already exists the value is <b>overwritten</b> with the specified value.
	 * 
	 * @param shortOpt
	 *            the short name of the option
	 * @param value
	 *            the value to set for the specified option
	 */
	public void setValue(String shortOpt, String value) {
		List<String> l = new ArrayList<String>();
		l.add(value);
		this.data.put(shortOpt, l);
	}

	/**
	 * This adds an entry with a value list to the recognized options. If the
	 * entry already exists the value is <b>overwritten</b> with the specified
	 * value list.
	 * 
	 * @param shortOpt
	 *            the short name of the option
	 * @param valueList
	 *            a list of values to be set for the specified option
	 */
	public void setValues(String shortOpt, List<String> valueList) {
		this.data.put(shortOpt, valueList);
	}

	/**
	 * This adds an entry with a value list to the recognized options. If the
	 * entry already exists the value is <b>overwritten</b> with the specified
	 * value list.
	 * 
	 * @param shortOpt
	 *            the short name of the option
	 * @param valueArray
	 *            an array of values to be set for the specified option
	 */
	public void setValues(String shortOpt, String[] valueArray) {
		ArrayList<String> valueList = new ArrayList<String>();
		for (int i = 0; i < valueArray.length; i++) {
			valueList.add(valueArray[i]);
		}
		this.setValues(shortOpt, valueList);
	}

	/**
	 * Returns the value of the requested option.<br/>
	 * If the requested option has no value or the option is not present
	 * <i>null</i> is returned.<br/>
	 * If the option has multiple values only the first value is returned. Use
	 * getOptionValues(shortOpt) to get a list of all available values.
	 * 
	 * @param shortOpt
	 *            the option for which the value is requested
	 * @return the first value (if multiple available) of the requested option
	 *         or null if no value is available
	 */
	public String getValue(String shortOpt) {
		return this.getValues(shortOpt).get(0);
	}

	/**
	 * Returns the value list of the requested option.<br/>
	 * If the requested option has no value or the option is not present
	 * <i>null</i> is returned.<br/>
	 * 
	 * @param shortOpt
	 *            the option for which the value is requested
	 * @return the value list of the requested option or null if no value is
	 *         available
	 */
	public List<String> getValues(String shortOpt) {
		if (this.data != null) {
			if (this.data.containsKey(shortOpt)) {
				return this.data.get(shortOpt);
			} else {
				logger.debug(Messages.getString("IdGenOptions.NO_DATA_ENTRY")
						+ " " + shortOpt + ".\n");
				return null;
			}
		} else {
			logger
					.warn(Messages
							.getString("IdGenOptions.DATA_NOT_INITIALIZED"));
			return null;
		}
	}

	/**
	 * Checks if the requested short option was specified in the arguments
	 * string.
	 * 
	 * @param shortOpt
	 *            short option name to check for
	 * @return true if the option was specified, false otherwise
	 */
	public boolean isSpecified(String shortOpt) {
		return (this.data.containsKey(shortOpt));
	}

	/**
	 * Converts the given string into an array and calls parseOptions(String[]).
	 * 
	 * @param args
	 *            argument string
	 * @return true on success, false otherwise
	 */
	public boolean parseCommandLine(String args) {
		String[] argsArr = args.split(" ");
		return this.parseCommandLine(argsArr);
	}

	/**
	 * Fills the data array inside the options object with the arguments
	 * specified in the array.
	 * 
	 * @param args
	 *            the argument array
	 * @return true on success, false otherwise
	 */
	public boolean parseCommandLine(String[] args) {
		try {
			logger.trace("Parsing cliArgs...");
			this.parse(args);
		} catch (ParseException e) {
			logger.debug(e.toString());
			return false;
		} catch (NumberFormatException e) {
			logger.debug(e.toString());
			return false;
		}

		return true;
	}

	/**
	 * @return the termWidth
	 */
	public int getTermWidth() {
		return termWidth;
	}

	/**
	 * @param termWidth
	 *            the termWidth to set
	 */
	public void setTermWidth(int termWidth) {
		this.termWidth = termWidth;
	}

	/**
	 * Returns an IdGenOption collection of all stored options.<br/>
	 * <i>Dummy options are not included in the returned options list.</i>
	 * 
	 * @return a collection of all stored options excluding dummy options
	 */
	@SuppressWarnings("unchecked")
	public Collection<IdGenOption> getOptions() {
		/*
		 * Compile a list of all stored option objects to be processed excluding
		 * all dummy options
		 */
		Collection<IdGenOption> optionsList = new HashSet<IdGenOption>();
		optionsList.addAll((Collection<IdGenOption>) super.getOptions());
		optionsList.removeAll(this.dummyOptions.values());

		return optionsList;
	}

	/**
	 * Return the number of option values.
	 * 
	 * @return number of option values
	 */
	public int getCount() {
		return this.data.size();
	}

	/**
	 * Build a formatted help string for the command line options managed by
	 * this options object.
	 * 
	 * @return the formatted help string, ready for output
	 */
	public String getFormattedOptionsHelp(boolean longHelp) {
		IdGenHelpFormatter formatter = new IdGenHelpFormatter();
		formatter.setWidth(this.termWidth);
		return formatter.getHelpString(this, longHelp);
	}

	/**
	 * returns a string representation of the stored options, e.g. the help
	 * string
	 * 
	 * @return the (long) help string
	 */
	public String toString() {
		return this.getFormattedOptionsHelp(true);
	}

}
