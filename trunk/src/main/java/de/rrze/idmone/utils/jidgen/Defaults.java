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

public class Defaults {
	
	/**
	 * Character seperating multiple values within one CLI option.
	 */
	public static final char CLI_VALUE_SEPERATOR = ',';
	
	/**
	 * Default terminal width in characters
	 */
	public static final int TERM_WIDTH = 80;
	
	/**
	 * Default for output in columns
	 */
	public static final boolean ENABLE_COLUMN_OUTPUT = false;

	/**
	 * Default number of id proposals to be generated after one 
	 * invocation of jidgen. 
	 */
	public static final int NUM_IDs = 1;
	
	/**
	 * This is meant to be the emergency exit if the
	 * id generation loop does not exit.
	 * If this happens the loop is broken after MAX_ATTEMPTS
	 * loops and a proper error message is displayed.
	 */
	public static final int MAX_ATTEMPTS = 10000;
	
	/**
	 * Default blacklist file
	 */
	public static final String BLACKLIST_FILE = "blacklist";
	
	
	/**
	 * Default passwd file
	 */
	public static final String PASSWD_FILE = "/etc/passwd";
	
	
	/**
	 * Default shell command
	 */
	public static final String SHELLCMD = "./filter.sh %s";
	

	/**
	 * Default configuration file for the LDAP filter
	 */
	public static final String LDAP_CONFIGURATION_FILE="ldapFilter.properties";


	/**
	 * Default configuration file for the LDAP filter
	 */
	public static final String JDBC_CONFIGURATION_FILE="jdbcFilter.properties";

}
