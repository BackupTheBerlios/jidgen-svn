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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.Defaults;
import de.rrze.idmone.utils.jidgen.i18n.Messages;

/**
 * A filter that calls the given shell command with the id or subsequently with
 * all ids, if there are more than one, to be tested and filters the id on exit
 * code 0 (success). Any exit code other than 0 will not filter the id.
 * 
 * @author unrza249
 */
public class ShellCmdFilter extends AbstractFilter implements IFilter {
	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(ShellCmdFilter.class);

	// TODO this class does not have a ShellAccessor, yet

	/**
	 * Default construct.
	 */
	public ShellCmdFilter() {
		logger.info(Messages.getString(this.getClass().getSimpleName()
				+ ".INIT_MESSAGE"));

		this.loadDefaults();
	}

	private void loadDefaults() {
		this.setDefaultProp("shellCommand", Defaults.SHELLCMD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filter.IFilter#apply(java.lang.String)
	 */
	public String apply(String id) {
		logger.trace("Checking ID '" + id + "'");

		String cmd = this.getProp("shellCommand").replace("%s", id);

		logger.trace("Executing command: " + cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process proc = run.exec(cmd);

			// read stdout and log it to the debug level
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String stdOutput = "";
			while ((stdOutput = stdOut.readLine()) != null) {
				logger.debug("STDOUT: " + stdOutput);
			}
			stdOut.close();

			// read stderr and log it to the error level
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(
					proc.getErrorStream()));
			String errOutput = "";
			while ((errOutput = stdErr.readLine()) != null) {
				logger.error("STDERR: " + errOutput);
			}
			stdErr.close();

			int exitCode = proc.waitFor();
			proc.destroy();

			if (exitCode == 0) {
				logger.trace("Filtered!");
				return null;
			} else {
				return id;
			}

		} catch (IOException e) {
			logger.fatal(e.toString());
			System.exit(120);
		} catch (InterruptedException e) {
			logger.fatal(e.toString());
			System.exit(121);
		}

		logger.debug(Messages.getString("IFilter.TRACE_FILTER_NAME") + " \""
				+ this.getID() + "\" "
				+ Messages.getString("IFilter.TRACE_SKIPPED_ID") + " \"" + id
				+ "\"");

		logger.debug(Messages.getString("IFilter.REASON") + " \""
				+ this.getProp("shellCommand") + "\"" + " "
				+ Messages.getString("IFilter.DENIED") + " \"" + id + "\"");

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.rrze.idmone.utils.jidgen.filterChain.filter.IFilter#autosetID()
	 */
	public void autosetID() {
		this.setID(this.getClass().getSimpleName() + "-"
				+ this.getProp("shellCommand"));
	}

}
