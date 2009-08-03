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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.cli.IdGenOptions;
import de.rrze.idmone.utils.jidgen.filter.BlacklistFilter;
import de.rrze.idmone.utils.jidgen.filter.FilterChain;
import de.rrze.idmone.utils.jidgen.filter.LdapFilter;
import de.rrze.idmone.utils.jidgen.filter.PasswdFilter;
import de.rrze.idmone.utils.jidgen.filter.ShellCmdFilter;
import de.rrze.idmone.utils.jidgen.template.Template;

/**
 * class IdGenerator
 * <p>
 * 		<b>Used external packages</b>
 * 		<br />
 * 		For further infos about the cli package see <a href="http://commons.apache.org/cli/">
 * 		org.apache.commons.cli</a>.
 * 		<br />
 * 		For further infos about the logging package see 
 * 		<a href="http://commons.apache.org/logging/commons-logging-1.1/index.html">
 * 		org.apache.commons.logging</a>
 * </p>
 *  
 * @author unrza249
 * @author unrz205
 */
public class IdGenerator
{
	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(IdGenerator.class);

	/**
	 * The options manager for IdGen 
	 */
	private IdGenOptions options;

	/**
	 * The option string array as passed to the options parser
	 */
	private String[] cliArgs;
	
	/**
	 * Flag that indicates whether or not an update of the
	 * options object is needed.
	 * <b>Only used internally!</b>
	 */
	private boolean cliArgsDirtyFlag = true;


	/**
	 * A filter chain for the IdGenerator<br />
	 * This chain contains all filters that should be applied to
	 * the generated ids.
	 */
	private FilterChain filterChain; 

	
	/* 
	 * CONSTANTS 
	 */

	/**
	 * Default terminal width in characters
	 */
	public static final int DEFAULT_TERM_WIDTH = 80;
	
	/**
	 * Default for output in columns
	 */
	public static final boolean DEFAULT_ENABLE_COLUMN_OUTPUT = false;

	/**
	 * Default number of id proposals to be generated after one 
	 * invocation of jidgen. 
	 */
	public static final int DEFAULT_NUM_IDs = 1;
	
	/**
	 * This is meant to be the emergency exit if the
	 * id generation loop does not exit.
	 * If this happens the loop is broken after MAX_ATTEMPTS
	 * loops and a proper error message is displayed.
	 */
	private static final int MAX_ATTEMPTS = 10000;
	
	/**
	 * Default blacklist file
	 */
	public static final String DEFAULT_BLACKLIST_FILE = "blacklist";
	
	
	/**
	 * Default passwd file
	 */
	public static final String DEFAULT_PASSWD_FILE = "/etc/passwd";
	
	
	/**
	 * Default shell command
	 */
	public static final String DEFAULT_SHELLCMD = "./filter.sh %s";
	

	/**
	 * Default configuration file for the LDAP filter
	 */
	public static final String DEFAULT_LDAP_CONFIGURATION_FILE="ldapFilter.properties";

	/**
	 * Special characters that can be included.
	 * <i>UNUSED</i>
	 */
	//public static final String SPECIAL_SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	/**
	 * List of ambiguous characters that can look alike and can confuse users.
	 * <i>UNUSED</i>
	 */
	//public static final String AMBIGUOUS_SYMBOLS = "B8G6I1l0OQDS5Z2";
	
	/* 
	 * END: CONSTANTS 
	 */
	
	
	/**
	 * Default constructor of the IdGenerator
	 */
	public IdGenerator() {
		logger.trace("Invoked default constructor.");

		// create a new filter chain
		this.filterChain = new FilterChain();
		
		// create and fill options manager for CLI and library usage
		this.options = new IdGenOptions(DEFAULT_TERM_WIDTH);
		addOptions(this.options);
	}


	/**
	 * Constructor with argument <b>array</b>
	 * @param args Array of string arguments in CLI notation
	 */
	public IdGenerator(String[] args) {
		this(); // Call default constructor
		
		this.setCLIArgs(args);
		this.init();
	}


	/**
	 * Constructor with argument <b>string</b> 
	 * @param args Argument string in CLI notation
	 */
	public IdGenerator(String args) {
		this(); // Call default constructor
	
		this.setCLIArgs(args);
		this.init();
	}

	
	
	/**
	 * Entry point of the program (CLI)
	 * @param args
	 *            the program arguments
	 */
	public static void main(String[] args) {
		logger.info(Messages.getString("IdGenerator.WELCOME"));

		// create an instance of the IdGenerator and start the generation process
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
		int termWidth = DEFAULT_TERM_WIDTH;
		if (generator.options.hasOptionValue("W")) {
			termWidth = Integer.parseInt(generator.options.getOptionValue("W"));
		}
		generator.options.setTermWidth(termWidth);
		logger.trace("Set terminal width to " + termWidth + ".");

		// check for -h option or call with no options at all
		if (generator.options.hasOptionValue("h") || generator.options.getNum() == 0) {
			generator.printUsage();
			System.exit(0);
		}

		// check for -hh option
		if (generator.options.hasOptionValue("hh")) {
			generator.printHelp();
			System.exit(0);
		}

		// set number of ids
		int numIds = DEFAULT_NUM_IDs;
		if (generator.options.hasOptionValue("N")) {
			numIds = Integer.parseInt(generator.options.getOptionValue("N"));
		}
		logger.trace("Set number of ids to generate to " + numIds + ".");

		// enable column output
		boolean enableColumnOutput = DEFAULT_ENABLE_COLUMN_OUTPUT;
		if (generator.options.hasOptionValue("C")) {
			enableColumnOutput = true;
		}
		logger.trace("Column output " + (enableColumnOutput?"enabled":"disabled") + ".");

		
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
			}
			else {
				generator.print(ids);
			}
		}
	}

	/**
	 * Init the IdGenerator object<br />
	 * This means:<br/>
	 * - reading or re-reading the options data
	 * from the the specified CLI arguments, if needed<br/>
	 * - setting up the specified filters and adding them to the
	 * filter chain<br/>
	 * <i>This got a seperate method, so that it can
	 * be called more flexibly and especially also from the main
	 * method for CLI usage.</i>
	 * @return true if no errors occurred, false otherwise 
	 */
	private boolean init() {
		
		logger.trace("Init called.");
		
		// at first: update the options data (-> cliArgs) if needed
		if (this.cliArgsDirtyFlag) {
			this.cliArgsDirtyFlag = false;

			if (!this.options.parseOptions(this.cliArgs)) {
				logger.error(Messages.getString("IdGenerator.ERROR_OPTIONS_UPDATE") + " " + Arrays.toString(this.cliArgs));
				return false;
			}
		}

		logger.trace("Processing CLI arguments...");

		/*
		 * FILTER SETUP
		 */
		// blacklist filter
		if (this.options.hasOptionValue("B")) {
			logger.trace("Enabling blacklist filter.");
			BlacklistFilter blacklistFilter = new BlacklistFilter();

			// source file
			if (this.options.hasOptionValue("Bf")) {
				blacklistFilter.setFile(new File(this.options.getOptionValue("Bf")));
			}
			else {
				blacklistFilter.setFile(new File(DEFAULT_BLACKLIST_FILE));
			}
			
			// set a unique ID for this filter
			blacklistFilter.setID(blacklistFilter.getClass().getSimpleName() + "-" + blacklistFilter.getBlacklistFile());
			
			this.filterChain.addFilter(blacklistFilter);
		}

		// passwd filter
		if (this.options.hasOptionValue("P")) {
			logger.trace("Enabling passwd filter.");
			PasswdFilter passwdFilter = new PasswdFilter();

			// source file
			if (this.options.hasOptionValue("Pf")) {
				passwdFilter.setFile(new File(this.options.getOptionValue("Pf")));
			}
			else {
				passwdFilter.setFile(new File(DEFAULT_PASSWD_FILE));
			}

			// set a unique ID for this filter
			passwdFilter.setID(passwdFilter.getClass().getSimpleName() + "-" + passwdFilter.getPasswdFile());

			this.filterChain.addFilter(passwdFilter);
		}

		// shellcmd filter
		if (this.options.hasOptionValue("S")) {
			logger.trace("Enabling shellcmd filter.");
			ShellCmdFilter shellCmdFilter = new ShellCmdFilter();
			
			// file to execute
			// TODO introduce Shell class similar to File/Ldap classes
			if (this.options.hasOptionValue("Sf")) {
				shellCmdFilter.setCmd(this.options.getOptionValue("Sf"));
			}
			else {
				shellCmdFilter.setCmd(DEFAULT_SHELLCMD);
			}
			
			// set a unique ID for this filter
			shellCmdFilter.setID(shellCmdFilter.getClass().getSimpleName() + "-" + shellCmdFilter.getCmd());
			
			this.filterChain.addFilter(shellCmdFilter);	
		}
		
		// LDAP filter
		if (this.options.hasOptionValue("L")) {
			logger.trace("Enabling LDAP filter.");
			LdapFilter ldapFilter = new LdapFilter();

			// LDAP connection to use
			if (this.options.hasOptionValue("Lf")) {
				ldapFilter.setLdap(new Ldap(new File(this.options.getOptionValue("Lf"))));
			}
			else {
				ldapFilter.setLdap(new Ldap(new File(DEFAULT_LDAP_CONFIGURATION_FILE)));
			}
			
			// set a unique ID for this filter
			ldapFilter.setID(ldapFilter.getClass().getSimpleName() + "-" + ldapFilter.getLdap());
			
			
			this.filterChain.addFilter(ldapFilter);
		}
		
		return true;
	}


	/**
	 * Initializes the CLI (Command Line Interface) options of the IdGenerator. 
	 * @return the CLI options
	 */
	private void addOptions(IdGenOptions opts)	{
		logger.trace("Building CLI options...");
	
		// id template string
		opts.add(
				"T",
				"template", 
				Messages.getString("IIdGenCommandLineOptions.CL_TEMPLATE_DESC"), 
				1,
				"template",
				' '
		);
		
		// terminal width
		opts.add(
				"W",
				"terminal-width",
				Messages.getString("IIdGenCommandLineOptions.CL_TERMINAL_WIDTH_DESC") + " (Default: " + DEFAULT_TERM_WIDTH + ")",
				1,
				"number",
				' '
		);
	
		// number of ids
		opts.add(
				"N",
				"number-ids",
				Messages.getString("IIdGenCommandLineOptions.CL_NUMBER_IDS_DESC"),
				1,
				"number",
				' '
		);
	
		// print in columns flag
		opts.add(
				"C",
				"print-in-columns",
				Messages.getString("IIdGenCommandLineOptions.CL_PRINT_IN_COLUMNS_DESC")
		);
	
		// shellcmd filter command
		opts.add(
				"Sf",
				"shellcmd-command",
				Messages.getString("IIdGenCommandLineOptions.CL_SHELLCMD_COMMAND_DESC"),
				1,
				"command",
				' '
		);
	
		// shellcmd filter enable
		opts.add(
				"S",
				"enable-shellcmd-filter",
				Messages.getString("IIdGenCommandLineOptions.CL_SHELLCMD_DESC") + " (Default: " + DEFAULT_SHELLCMD + ")"
		);		
		
		// passwd filter file
		opts.add(
				"Pf",
				"passwd-file",
				Messages.getString("IIdGenCommandLineOptions.CL_PASSWD_FILE_DESC"),
				1,
				"file",
				' '
		);
	
		// passwd filter enable
		opts.add(
				"P",
				"enable-passwd-filter",
				Messages.getString("IIdGenCommandLineOptions.CL_PASSWD_DESC") + " (Default: " + DEFAULT_PASSWD_FILE + ")"
		);
	
		// blacklist filter file
		opts.add(
				"Bf",
				"blacklist-file",
				Messages.getString("IIdGenCommandLineOptions.CL_BLACKLIST_FILE_DESC"),
				1,
				"file",
				' '
		);
	
		// blacklist filter enable
		opts.add(
				"B",
				"enable-blacklist-filter",
				Messages.getString("IIdGenCommandLineOptions.CL_BLACKLIST_DESC") + " (Default: " + DEFAULT_BLACKLIST_FILE + ")"
		);
	
		
		// ldap filter enable
		opts.add(
				"L",
				"enable-ldap-filter",
				Messages.getString("IIdGenCommandLineOptions.CL_LDAP_DESC") + " (Default: " + DEFAULT_LDAP_CONFIGURATION_FILE + ")"
		);
	
		// ldap filter configuration file
		opts.add(
				"Lf",
				"ldap-properties-file",
				Messages.getString("IIdGenCommandLineOptions.CL_LDAP_FILE_DESC"),
				1,
				"file",
				' '
		);
	
		
		// create all "T[a-z]" options as invisible and a dummy option for them
		for (char currentChar = 'a'; currentChar < 'z'; currentChar++) {
			opts.addInvisible(
					"T" + currentChar,
					"template-variable-" + currentChar, 
					1,
					"data",
					' '
			);
		}
		opts.addDummy(
				"T[a-z]",
				"template-variable-[a-z]",
				Messages.getString("IIdGenCommandLineOptions.CL_TEMPLATE_VARIABLE_DESC"),
				1,
				"data"
		);
	
	
		// display usage (short help)
		opts.add(
				"h",
				"help", 
				Messages.getString("IIdGenCommandLineOptions.CL_HELP")
		);
	
		// display help page (long help)
		opts.add(
				"hh",
				"help-page", 
				Messages.getString("IIdGenCommandLineOptions.CL_HELP_PAGE")
		);
	}


	/**
	 * Converts the given string into an array
	 * and calls setCLIArgs(String[]).
	 * 
	 * @param args
	 * 			the argument string
	 */
	public void setCLIArgs(String args) {
		logger.trace("Converting argument string to array...");
		String[] argsArr = args.split(" ");
		this.setCLIArgs(argsArr);
	}

	/**
	 * Sets the stored argument array for
	 * later parsing.<br />
	 * An update of the data array can be 
	 * done explicitly by calling the
	 * updateOptions() method or automatically
	 * on the next call to generateIds(int). 
	 * 
	 * @param args
	 * 			the new argument array
	 */
	public void setCLIArgs(String[] args) {
		this.cliArgsDirtyFlag = true;
		this.cliArgs = args;
		logger.trace("Set cliArgs to " + Arrays.toString(this.cliArgs));
	}

	/**
	 * Set an option with it's value
	 * @param opt
	 * 			the short option parameter to be set
	 * @param value
	 * 			the value to be associated with the parameter
	 */
	public void setOption(String opt, String value) {
		this.options.setOptionValue(opt, value);
	}


	/**
	 * Clears the filter chain and re-initializes it.<br/>
	 * Also re-reads the CLI options if indicated by the updateOptions
	 * member variable.
	 * @return true on success, false otherwise
	 */
	public boolean updateOptions() {
		this.filterChain.clear();
		return this.init();
	}


	/**
	 * This method tries to generate the given number of ids. 
	 * The method returns an empty list if it does 
	 * not manage to create any suitable id within the <i>MAX_ATTEMPTS</i>
	 * or null if an error occurs.
	 * 
	 * @param num
	 * 			target number of ids to generate
	 * @return a suitable id list, an empty list if such could not be
	 *         generated or null on error
	 */
	public List<String> generateIDs(int num) {
		
		if (this.cliArgsDirtyFlag) {
			this.updateOptions();
		}
	
		/*
		 * Update the filter chain's data
		 * 
		 * It is VERY important to update all filters inside the chain before every
		 * generation run. After this all data from external files is re-read and 
		 * buffered in memory for fast access.
		 * 
		 * !!! If you forget this your IDs are not guaranteed to be valid !!!
		 */
		this.filterChain.update();
		
		logger.info(Messages.getString("IdGenerator.START_GENERATION") + num);
	
		// the result array
		ArrayList<String> validIDs = new ArrayList<String>();
	
		// the template object, responsible for building id suggestions
		Template template = new Template(this.options.getData());
	
		// id generation loop
		int i = 0;
		while (template.hasAlternatives() && (validIDs.size() < num)) {
			if (i++ == MAX_ATTEMPTS) {
				logger.fatal(Messages.getString("IdGenerator.MAX_ATTEMPTS_REACHED") + " (" + MAX_ATTEMPTS + ")");
				System.exit(152);
			}
			String idCandidate = null;
			idCandidate = template.buildString();
			logger.trace(Messages.getString("IdGenerator.TRACE_ID_CANDIDATE") + " " + idCandidate);
	
			// apply the filter chain to the generated id
			// add to list if we got a valid, unique id 
			if (	(this.filterChain.apply(idCandidate) != null)
					&& (!validIDs.contains(idCandidate)))
			{
				validIDs.add(idCandidate);
			}
			else { 
				// log some info about the failed attempt 
				logger.trace(Messages.getString("IdGenerator.TRACE_ATTEMPT_GENERATE") + " " + idCandidate);
			}
		}
	
		logger.debug(Messages.getString("IdGenerator.NUMBER_OF_ITERATIONS") + i);
	
		if (validIDs.size() < num) {
			logger.warn(Messages.getString("IdGenerator.FAILED_TO_REACH_TARGET_NUM") + validIDs.size());
		}
	
		if (validIDs.size() == 0) {
			logger.fatal(Messages.getString("IdGenerator.NO_ALTERNATIVES_LEFT"));
		}
	
		return validIDs;
	}


	/**
	 * Prints ids into columns with a predefined terminal width (to
	 * System.out). The number of columns is calculated from the terminal width.
	 * 
	 * @param ids
	 *            a list of ids to be printed
	 */
	public void printColumns(List<String> ids, int termWidth)
	{
		int idLength = ids.get(0).length();
		int numberOfColumns = termWidth / (idLength + 1);
		if (numberOfColumns == 0)
			numberOfColumns = 1;

		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));

		int i;
		int column = 0;
		for (i = 0; i < ids.size(); i++)
		{
			column++;
			String id = (String) ids.get(i);
			if (((column % numberOfColumns) == 0))
			{
				System.out.print(id + Messages.getString("IdGenerator.NEW_LINE"));
			} else
			{
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
	public void print(List<String> ids)
	{
		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));

		for (int i = 0; i < ids.size(); i++)
		{
			String id = (String) ids.get(i);
			System.out.print(id	+ Messages.getString("IdGenerator.NEW_LINE"));
		}
		logger.debug(Messages.getString("IdGenerator.N_SEPARATOR"));
	}

	/**
	 * Prints the usage info
	 * and the available CLI options
	 */
	public void printUsage() {
		System.out.println(options.getHelp(false));
	}

	/**
	 * Prints a very verbose help page
	 * with more detailed info
	 */
	public void printHelp() {
		System.out.println(options.getHelp(true));		
	}

}