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

package de.rrze.idmone.utils.jidgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.cli.IdGenOption;
import de.rrze.idmone.utils.jidgen.cli.IdGenOptions;
import de.rrze.idmone.utils.jidgen.filterChain.FilterChain;
import de.rrze.idmone.utils.jidgen.filterChain.filter.BlacklistFilter;
import de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter;
import de.rrze.idmone.utils.jidgen.filterChain.filter.JdbcFilter;
import de.rrze.idmone.utils.jidgen.filterChain.filter.LdapFilter;
import de.rrze.idmone.utils.jidgen.filterChain.filter.PasswdFilter;
import de.rrze.idmone.utils.jidgen.filterChain.filter.ShellCmdFilter;
import de.rrze.idmone.utils.jidgen.i18n.Messages;
import de.rrze.idmone.utils.jidgen.template.Template;

/**
 * Main class of the jidgen IdGenerator.
 * <p>
 * <b>Used external packages</b> <br />
 * For further infos about the cli package see <a
 * href="http://commons.apache.org/cli/"> org.apache.commons.cli</a>. <br />
 * For further infos about the logging package see <a
 * href="http://commons.apache.org/logging/commons-logging-1.1/index.html">
 * org.apache.commons.logging</a>
 * </p>
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 * @author unrz205
 */
public class IdGenerator {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(IdGenerator.class);

	/**
	 * The CLI options manager for IdGen
	 */
	private IdGenOptions options;

	/**
	 * The option string array as passed to the options parser
	 */
	private String[] cliArgs;

	/**
	 * Flag that indicates whether or not an update of the options object is
	 * needed. <b>Only used internally!</b>
	 */
	private boolean cliArgsDirtyFlag = true;

	/**
	 * A filter chain for the IdGenerator<br />
	 * This chain contains all filters that should be applied to the generated
	 * ids.
	 */
	private FilterChain filterChain;

	/**
	 * The template object for generating IDs according to the template string
	 */
	private Template template;

	/**
	 * Default constructor of the IdGenerator
	 */
	public IdGenerator() {
		logger.trace("Invoked default constructor.");

		// create a new filter chain
		this.filterChain = new FilterChain();

		// create and fill options manager for CLI and library usage
		this.options = new IdGenOptions();
		addOptions(this.options);
	}

	/**
	 * Constructor with argument <b>array</b>
	 * 
	 * @param args
	 *            Array of string arguments in CLI notation
	 */
	public IdGenerator(String[] args) {
		this(); // Call default constructor

		this.setCLIArgs(args);
		this.init();
	}

	/**
	 * Constructor with argument <b>string</b>
	 * 
	 * @param args
	 *            Argument string in CLI notation
	 */
	public IdGenerator(String args) {
		this(); // Call default constructor

		this.setCLIArgs(args);
		this.init();
	}

	/**
	 * Entry point of the program (CLI)
	 * 
	 * @param args
	 *            the program arguments
	 */
	public static void main(String[] args) {
		logger.info(Messages.getString("IdGenerator.WELCOME"));

		// create an instance of the IdGenerator and start the generation
		// process
		IdGenerator generator = new IdGenerator();

		// pass on the CLI options array
		generator.setCLIArgs(args);

		// initialize the generator options manually
		// so we can print the usage on error
		if (!generator.init()) {
			generator.printUsage();
			System.exit(150);
		}

		/*
		 * PROCESS CLI-ONLY OPTIONS
		 */

		// set terminal width
		int termWidth = Defaults.TERM_WIDTH;
		if (generator.options.isSpecified("W")) {
			termWidth = Integer.parseInt(generator.options.getValue("W"));
		}
		generator.options.setTermWidth(termWidth);
		logger.trace("Set terminal width to " + termWidth + ".");

		// check for -h option or call with no options at all
		if (generator.options.isSpecified("h")
				|| generator.options.getCount() == 0) {
			generator.printUsage();
			System.exit(0);
		}

		// check for -hh option
		if (generator.options.isSpecified("hh")) {
			generator.printHelp();
			System.exit(0);
		}

		// set number of ids
		int numIds = Defaults.NUM_IDs;
		if (generator.options.isSpecified("N")) {
			numIds = Integer.parseInt(generator.options.getValue("N"));
		}
		logger.trace("Set number of ids to generate to " + numIds + ".");

		// enable column output
		boolean enableColumnOutput = Defaults.ENABLE_COLUMN_OUTPUT;
		if (generator.options.isSpecified("C")) {
			enableColumnOutput = true;
		}
		logger.trace("Column output "
				+ (enableColumnOutput ? "enabled" : "disabled") + ".");

		/*
		 * START WORKING
		 */

		// generate ids
		List<String> ids = generator.generateIDs(numIds);

		// output the generated ids
		if (ids != null && !ids.isEmpty()) {
			logger.info(Messages.getString("IdGenerator.ID"));
			if (enableColumnOutput) {
				generator.printColumns(ids, termWidth);
			} else {
				generator.print(ids);
			}
		}
	}

	/**
	 * Init the IdGenerator object<br />
	 * This means:<br/>
	 * - reading or re-reading the options data from the the specified CLI
	 * arguments, if needed<br/>
	 * - setting up the specified filters and adding them to the filter chain<br/>
	 * <i>This got a seperate method, so that it can be called more flexibly and
	 * especially also from the main method for CLI usage.</i>
	 * 
	 * @return true if no errors occurred, false otherwise
	 */
	private boolean init() {

		logger.trace("Init called.");

		// at first: update the options data (-> cliArgs) if needed
		if (this.cliArgsDirtyFlag) {
			this.cliArgsDirtyFlag = false;

			if (!this.options.parseCommandLine(this.cliArgs)) {
				logger.error(Messages
						.getString("IdGenerator.ERROR_OPTIONS_UPDATE")
						+ " " + Arrays.toString(this.cliArgs));
				return false;
			}
		}

		logger.trace("Processing CLI arguments...");

		/*
		 * FILTER SETUP
		 */

		// blacklist filter
		initFilter("B", BlacklistFilter.class, "BLACKLIST", "filename",
				Defaults.BLACKLIST_FILE);

		// passwd filter
		initFilter("P", PasswdFilter.class, "PASSWD", "filename",
				Defaults.PASSWD_FILE);

		// shellcmd filter
		initFilter("S", ShellCmdFilter.class, "SHELLCMD", "shellCommand",
				Defaults.SHELLCMD);

		// JDBC filter
		initFilter("D", JdbcFilter.class, "JDBC", null,
				Defaults.JDBC_CONFIGURATION_FILE);

		// LDAP filter
		initFilter("L", LdapFilter.class, "LDAP", null,
				Defaults.LDAP_CONFIGURATION_FILE);

		return true;
	}

	/**
	 * Convenience function to initialize all supported filters as requested by
	 * the cli options.<br/>
	 * <b>!!INTENDED FOR INTERNAL USE ONLY!!</b>
	 * 
	 * @param shortOpt
	 *            the one letter short option identifier
	 * @param filterClass
	 *            the class to instantiate for this filter
	 * @param name
	 *            filter name for logging purposes
	 * @param propName
	 *            name of the property to set from the cli value(s) or null if a
	 *            properties file should be loaded instead
	 * @param defaultValue
	 *            the default value for the specified propterty or the default
	 *            properties file to load if the propName is null
	 */
	@SuppressWarnings("unchecked")
	private void initFilter(String shortOpt, Class filterClass, String name,
			String propName, String defaultValue) {

		try {
			if (this.options.isSpecified(shortOpt)) {
				logger.trace("Enabling " + name + " filter " + "(Default: "
						+ defaultValue + ").");
				IFilter filter = (IFilter) filterClass.newInstance();

				if (propName == null) {
					filter.loadPropFile(defaultValue);
				} else {
					filter.setProp(propName, defaultValue);
				}
				filter.autosetID();
				this.filterChain.addFilter(filter);
			}

			if (this.options.isSpecified(shortOpt + "f")) {
				Iterator<String> valueIt = this.options.getValues(
						shortOpt + "f").iterator();

				if (propName == null) {
					while (valueIt.hasNext()) {
						IFilter filter = (IFilter) filterClass.newInstance();

						String propFile = valueIt.next();
						logger.trace("Enabling " + name + " filter " + "("
								+ propFile + ").");

						filter.loadPropFile(propFile);
						filter.autosetID();

						this.filterChain.addFilter(filter);
					}
				} else {
					while (valueIt.hasNext()) {
						IFilter filter = (IFilter) filterClass.newInstance();

						String propValue = valueIt.next();
						logger.trace("Enabling " + name + " filter " + "("
								+ propValue + ").");

						filter.setProp(propName, propValue);
						filter.autosetID();

						this.filterChain.addFilter(filter);
					}

				}
			}
		} catch (IllegalAccessException e) {
			// FIXME
		} catch (InstantiationException e) {
			// FIXME
		}

	}

	/**
	 * Initializes the CLI (Command Line Interface) options of the IdGenerator.
	 * 
	 * @return the CLI options
	 */
	private void addOptions(IdGenOptions opts) {
		// TODO find a more abstract way of initialising the options object
		logger.trace("Building CLI options...");

		// id template string
		opts.add("T", "template", Messages
				.getString("IIdGenCommandLineOptions.CL_TEMPLATE_DESC"), 1,
				"template", ' ');

		// terminal width
		opts.add("W", "terminal-width", Messages
				.getString("IIdGenCommandLineOptions.CL_TERMINAL_WIDTH_DESC")
				+ " (Default: " + Defaults.TERM_WIDTH + ")", 1, "number", ' ');

		// number of ids
		opts.add("N", "number-ids", Messages
				.getString("IIdGenCommandLineOptions.CL_NUMBER_IDS_DESC"), 1,
				"number", ' ');

		// print in columns flag
		opts
				.add(
						"C",
						"print-in-columns",
						Messages
								.getString("IIdGenCommandLineOptions.CL_PRINT_IN_COLUMNS_DESC"));

		// shellcmd filter command
		opts
				.add(
						"Sf",
						"shellcmd-command",
						Messages
								.getString("IIdGenCommandLineOptions.CL_SHELLCMD_COMMAND_DESC"),
						IdGenOption.UNLIMITED_VALUES, "command(s)",
						Defaults.CLI_VALUE_SEPERATOR);

		// shellcmd filter enable
		opts.add("S", "enable-shellcmd-filter", Messages
				.getString("IIdGenCommandLineOptions.CL_SHELLCMD_DESC")
				+ " (Default: " + Defaults.SHELLCMD + ")");

		// passwd filter file
		opts.add("Pf", "passwd-file", Messages
				.getString("IIdGenCommandLineOptions.CL_PASSWD_FILE_DESC"),
				IdGenOption.UNLIMITED_VALUES, "file(s)",
				Defaults.CLI_VALUE_SEPERATOR);

		// passwd filter enable
		opts.add("P", "enable-passwd-filter", Messages
				.getString("IIdGenCommandLineOptions.CL_PASSWD_DESC")
				+ " (Default: " + Defaults.PASSWD_FILE + ")");

		// blacklist filter file
		opts.add("Bf", "blacklist-file", Messages
				.getString("IIdGenCommandLineOptions.CL_BLACKLIST_FILE_DESC"),
				IdGenOption.UNLIMITED_VALUES, "file(s)",
				Defaults.CLI_VALUE_SEPERATOR);

		// blacklist filter enable
		opts.add("B", "enable-blacklist-filter", Messages
				.getString("IIdGenCommandLineOptions.CL_BLACKLIST_DESC")
				+ " (Default: " + Defaults.BLACKLIST_FILE + ")");

		// ldap filter enable
		opts.add("L", "enable-ldap-filter", Messages
				.getString("IIdGenCommandLineOptions.CL_LDAP_DESC")
				+ " (Default: " + Defaults.LDAP_CONFIGURATION_FILE + ")");

		// ldap filter configuration file
		opts.add("Lf", "ldap-properties-file", Messages
				.getString("IIdGenCommandLineOptions.CL_LDAP_FILE_DESC"),
				IdGenOption.UNLIMITED_VALUES, "file(s)",
				Defaults.CLI_VALUE_SEPERATOR);

		// jdbc filter enable
		opts.add("D", "enable-jdbc-filter", Messages
				.getString("IIdGenCommandLineOptions.CL_JDBC_DESC")
				+ " (Default: " + Defaults.JDBC_CONFIGURATION_FILE + ")");

		// jdbc filter configuration file
		opts.add("Df", "jdbc-properties-file", Messages
				.getString("IIdGenCommandLineOptions.CL_JDBC_FILE_DESC"),
				IdGenOption.UNLIMITED_VALUES, "file(s)",
				Defaults.CLI_VALUE_SEPERATOR);

		// create all "T[a-z]" options as invisible and a dummy option for them
		for (char currentChar = 'a'; currentChar < 'z'; currentChar++) {
			opts.addInvisible("T" + currentChar, "template-variable-"
					+ currentChar, 1, "data", ' ');
		}
		opts
				.addDummy(
						"T[a-z]",
						"template-variable-[a-z]",
						Messages
								.getString("IIdGenCommandLineOptions.CL_TEMPLATE_VARIABLE_DESC"),
						1, "data");

		// display usage (short help)
		opts.add("h", "help", Messages
				.getString("IIdGenCommandLineOptions.CL_HELP"));

		// display help page (long help)
		opts.add("hh", "help-page", Messages
				.getString("IIdGenCommandLineOptions.CL_HELP_PAGE"));
	}

	/**
	 * Converts the given string into an array and calls setCLIArgs(String[]).
	 * 
	 * @param args
	 *            the argument string
	 */
	public void setCLIArgs(String args) {
		logger.trace("Converting argument string to array...");
		String[] argsArr = args.split(" ");
		this.setCLIArgs(argsArr);
	}

	/**
	 * Sets the stored argument array for later parsing.<br />
	 * An update of the data array can be done explicitly by calling the
	 * updateOptions() method or automatically on the next call to
	 * generateIds(int).
	 * 
	 * @param args
	 *            the new argument array
	 */
	public void setCLIArgs(String[] args) {
		this.cliArgsDirtyFlag = true;
		this.cliArgs = args;
		logger.trace("Set cliArgs to " + Arrays.toString(this.cliArgs));
	}

	/**
	 * Set an option with it's value<br/>
	 * <i>Convenience function</i>
	 * 
	 * @param opt
	 *            the short option parameter to be set
	 * @param value
	 *            the value to be associated with the parameter
	 */
	public void setOption(String opt, String value) {
		this.options.setValue(opt, value);
	}

	/**
	 * Clears the filter chain and re-initializes it.<br/>
	 * Also re-reads the CLI options if indicated by the updateOptions member
	 * variable.
	 * 
	 * @return true on success, false otherwise
	 */
	public boolean updateOptions() {
		this.filterChain.clear();
		return this.init();
	}

	/**
	 * This method tries to generate the given number of ids. The method returns
	 * an empty list if it does not manage to create any suitable id within the
	 * <i>MAX_ATTEMPTS</i> or null if an error occurs.
	 * 
	 * @param num
	 *            target number of ids to generate
	 * @return a suitable id list, an empty list if such could not be generated
	 *         or null on error
	 */
	public List<String> generateIDs(int num) {
		return this.getIDs(num, true);
	}

	/**
	 * 
	 * @param num
	 * @return
	 */
	public List<String> getNextIDs(int num) {
		return this.getIDs(num, false);
	}

	/**
	 * This method tries to generate the given number of ids. The method returns
	 * an empty list if it does not manage to create any suitable id within the
	 * <i>MAX_ATTEMPTS</i> or null if an error occurs.
	 * 
	 * @param num
	 * @param resetTemplate
	 * @return
	 */
	private List<String> getIDs(int num, boolean resetTemplate) {

		if (this.cliArgsDirtyFlag) {
			this.updateOptions();
		}

		/*
		 * Update the filter chain's data
		 * 
		 * It is VERY important to update all filters inside the chain directly
		 * before every generation run. After this all data from external files
		 * is re-read and buffered in memory for fast access.
		 * 
		 * !!! If you forget this your IDs are not guaranteed to be valid !!!
		 */
		this.filterChain.update();

		logger.info(Messages.getString("IdGenerator.START_GENERATION") + num);

		// the result array
		ArrayList<String> validIDs = new ArrayList<String>();

		// the template object, responsible for building id suggestions
		if (this.template == null || resetTemplate)
			this.template = new Template(this.options);

		// id generation loop
		int i = 0;
		while (template.hasAlternatives() && (validIDs.size() < num)) {
			if (i++ == Defaults.MAX_ATTEMPTS) {
				logger.fatal(Messages
						.getString("IdGenerator.MAX_ATTEMPTS_REACHED")
						+ " (" + Defaults.MAX_ATTEMPTS + ")");
				System.exit(152);
			}

			String idCandidate = null;
			idCandidate = template.buildString();
			logger.trace(Messages.getString("IdGenerator.TRACE_ID_CANDIDATE")
					+ " " + idCandidate);

			// apply the filter chain to the generated id
			// add to list if we got a valid, unique id
			if ((this.filterChain.apply(idCandidate) != null)
					&& (!validIDs.contains(idCandidate))) {
				validIDs.add(idCandidate);
			} else {
				// log some info about the failed attempt
				logger.trace(Messages
						.getString("IdGenerator.TRACE_ATTEMPT_GENERATE")
						+ " " + idCandidate);
			}

		}

		logger
				.debug(Messages.getString("IdGenerator.NUMBER_OF_ITERATIONS")
						+ i);

		if (validIDs.size() < num) {
			logger.warn(Messages
					.getString("IdGenerator.FAILED_TO_REACH_TARGET_NUM")
					+ validIDs.size());
		}

		if (validIDs.size() == 0) {
			logger
					.fatal(Messages
							.getString("IdGenerator.NO_ALTERNATIVES_LEFT"));
		}

		return validIDs;
	}

	/**
	 * Prints ids into columns with a predefined terminal width (to System.out).
	 * The number of columns is calculated from the terminal width.
	 * 
	 * @param ids
	 *            a list of ids to be printed
	 */
	public void printColumns(List<String> ids, int termWidth) {
		int idLength = ids.get(0).length();
		int numberOfColumns = termWidth / (idLength + 1);
		if (numberOfColumns == 0)
			numberOfColumns = 1;

		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));

		int i;
		int column = 0;
		for (i = 0; i < ids.size(); i++) {
			column++;
			String id = (String) ids.get(i);
			if (((column % numberOfColumns) == 0)) {
				System.out.print(id
						+ Messages.getString("IdGenerator.NEW_LINE"));
			} else {
				System.out.print(id + ' ');
			}
		}
		if ((column % numberOfColumns) != 0) {
			System.out.print(Messages.getString("IdGenerator.NEW_LINE"));
		}
		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));
	}

	/**
	 * Prints a list of ids to a terminal(System.out)
	 * 
	 * @param ids
	 *            a list of ids to be printed
	 */
	public void print(List<String> ids) {
		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));

		for (int i = 0; i < ids.size(); i++) {
			String id = (String) ids.get(i);
			System.out.print(id + Messages.getString("IdGenerator.NEW_LINE"));
		}
		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));
	}

	/**
	 * Prints the usage info and the available CLI options
	 */
	public void printUsage() {
		System.out.println(options.getFormattedOptionsHelp(false));
	}

	/**
	 * Prints a very verbose help page with more detailed info
	 */
	public void printHelp() {
		System.out.println(options.getFormattedOptionsHelp(true));
	}

}