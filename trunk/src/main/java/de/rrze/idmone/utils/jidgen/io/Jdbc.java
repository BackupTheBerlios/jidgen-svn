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

package de.rrze.idmone.utils.jidgen.io;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
public class Jdbc {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(Jdbc.class);
	
	private Properties defaultProperties = new Properties();
	
	private Properties props = new Properties(defaultProperties);
	
	Connection db;

	ResultSet lastResult = null;

	boolean connected = false;



	/**
	 * 
	 */
	public Jdbc() {
		this.setDefaultProperties();
	}

	public Jdbc(Properties props) {
		this();
		this.setProperties(props);
	}
	
	/**
	 * 
	 * @param f
	 */
	public Jdbc(File f) {
		this();
		try {
			logger.debug("jndiConfigurationFile = " + f.getFilename());
			this.props.load(f.getReader());
		}
		catch (IOException e) {
			logger.fatal("Exception", e);
		}
	}


	
	/**
	 * Initializes the defaultProperties member
	 * with the internal presets
	 */
	private void setDefaultProperties() {
		this.defaultProperties.setProperty("driver", "com.mysql.jdbc.Driver");
		// URL: jdbc:<subprotocol>:<subname>
		this.defaultProperties.setProperty("url", "jdbc:mysql://localhost:3306/jidgen");
		this.defaultProperties.setProperty("username", "jidgen");
		this.defaultProperties.setProperty("password", "jidgen");
	}
	
	/**
	 * Takes the given properties object and copies the contained values
	 * to the internal properties object. Values that are not contained (as far as
	 * the containsKey() method says) are not copied.
	 * <p>
	 * At the moment the following properties are recognized:<br />
	 * <ul>
	 * <li><b>host</b> -- the hostname to connect to (Default: localhost)</li>
	 * <li><b>port</b> -- the port to connect to (Default: 389)</li>
	 * <li><b>namingContext</b> -- the naming context to use (Default: dc=example,dc=com)</li>
	 * <li><b>user</b> -- the user DN to bind to (Default: cn=jidgen,ou=people,dc=example,dc=com)</li>
	 * <li><b>password</b> -- the bind-user's password (Default: jidgen)</li>
	 * <li><b>searchFilter</b> -- the search filter to use (Default: null)</li>
	 * <li><b>searchBase</b> -- the search base to use (Default: ou=people)</li>
	 * </ul>
	 * <p>
	 * For use with jidgen <i>no attributes</i> are retrieved and the search scope defaults
	 * to <i>subtree</i>.
	 * 
	 * 
	 * @param p
	 * 		properties object to copy values from
	 */
	public void setProperties(Properties p) {
		
		/*
		if (p.containsKey("host")) 
			this.setHost(p.getProperty("host"));

		if (p.containsKey("port")) 
			this.setPort(p.getProperty("port"));

		if (p.containsKey("namingContext")) 
			this.setNamingContext(p.getProperty("namingContext"));

		if (p.containsKey("user")) 
			this.setUser(p.getProperty("user"));

		if (p.containsKey("password")) 
			this.setPassword(p.getProperty("password"));

		if (p.containsKey("searchFilter")) 
			this.setSearchFilter(p.getProperty("searchFilter"));

		if (p.containsKey("searchBase")) 
			this.setSearchBase(p.getProperty("searchBase"));
		*/
	}
	
	
	/**
	 * 
	 */
	public void load() {
		String className = this.props.getProperty("driver");
		try {
			Class.forName(className);
		}
		catch(ClassNotFoundException e){
			logger.fatal("Unable to load the driver class: Class not found \"" + className + "\".");
			System.exit(-1); // TODO error code
		}
	}

	/**
	 * 
	 */
	public void connect() {
		// Define URL of database server for
		// database named mysql on the localhost
		// with the default port number 3306.
		String url = this.props.getProperty("url");

		try {
			//this.db = DriverManager.getConnection(url, "loginName", "Password");
			this.db = DriverManager.getConnection(url, this.props.getProperty("username"), this.props.getProperty("password"));

			if(this.db != null){
				DatabaseMetaData meta = this.db.getMetaData();
				logger.debug("Got Connection - Driver Name: " + meta.getDriverName() + ", Driver Version: " + meta.getDriverVersion());
			} else {
				logger.fatal("Could not Get Connection");
				System.exit(-1); // TODO error code
			}
		}
		catch(SQLException e){
			logger.fatal("Error while connecting: " + e.toString());
			System.exit(-1); // TODO error code
		}

		this.connected = true;
	}

	/**
	 * 
	 * @param q
	 * @return
	 */
	public ResultSet query(String q) {
		if (!this.connected) {
			this.load();
			this.connect();
		}
		
		
		ResultSet rs = null;
		try {
			Statement statement = this.db.createStatement();

			rs = statement.executeQuery(q);
			//while (rs.next()) System.out.println(rs.getString(1));
		}
		catch (SQLException e) {
			logger.fatal("Error while executing query: " + e.toString());
			System.exit(-1); // TODO error code
		}

		this.lastResult = rs;

		return rs;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumRows() {
		try {
			if (this.lastResult != null) {
				this.lastResult.last();
				return this.lastResult.getRow();
			}
		}
		catch (SQLException e) {
			logger.fatal("Error while retrieving result meta-data: " + e.toString());
			System.exit(-1); // TODO error code
		}
	
		return 0;
	}

}
