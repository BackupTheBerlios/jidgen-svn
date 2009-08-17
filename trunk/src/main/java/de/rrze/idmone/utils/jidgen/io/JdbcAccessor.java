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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.DriverShim;

/**
 * This class was written for easy handling of database operations.<br/>
 * The database connection is established using a user configurable JDBC driver
 * class.
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
public class JdbcAccessor {

	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(JdbcAccessor.class);

	/**
	 * The database connection object
	 */
	private Connection db;

	/**
	 * The result of the last database query
	 */
	private ResultSet lastResult = null;

	/**
	 * Flag to indicate, whether the connection to the database was already
	 * established
	 */
	private boolean connected = false;

	/**
	 * The path of the external driver classes, e.g. the driver JAR file
	 */
	private String driverClassPath;

	/**
	 * The name of the class containing the driver to use for connecting to the
	 * database
	 */
	private String driver;

	/**
	 * The JDBC URL to connect to
	 */
	private String url;

	/**
	 * Username for connecting to the database
	 */
	private String user;

	/**
	 * Password for connecting to the database
	 */
	private String password;

	/**
	 * Default constructor
	 */
	public JdbcAccessor() {
	}

	/**
	 * Loads the driver class, instantiates and registers it at the JDBC
	 * DriverManager for later use.
	 */
	private void load() {
		try {

			URLClassLoader classLoader = new URLClassLoader(
					new URL[] { new URL("jar:file://" + this.driverClassPath
							+ "!/") });
			Driver d = (Driver) classLoader.loadClass(this.driver)
					.newInstance();
			DriverManager.registerDriver(new DriverShim(d));

		} catch (ClassNotFoundException e) {
			logger.fatal("Unable to load the driver class.");
			logger
					.fatal("Check \"driverClassPath\" and \"driverClass\" parameters in the properties file.");
			logger.fatal(e.toString());
			System.exit(-1); // TODO error code
		} catch (MalformedURLException e) {
			logger.fatal("Unable to parse the \"driverClassPath\" property: "
					+ this.driverClassPath);
			logger.fatal(e.toString());
			System.exit(-1); // TODO error code
		} catch (InstantiationException e) {
			logger.fatal("Unable to instantiate the driver class: "
					+ this.driver);
			logger.fatal(e.toString());
			System.exit(-1); // TODO error code
		} catch (IllegalAccessException e) {
			logger.fatal("Driver class or constructor not accessible: "
					+ this.driver);
			logger.fatal(e.toString());
			System.exit(-1); // TODO error code
		} catch (SQLException e) {
			logger
					.fatal("Unable to register the driver because of a database error: "
							+ this.driver);
			logger.fatal(e.toString());
			System.exit(-1); // TODO error code
		}
	}

	/**
	 * Establish a connection to the configured JDBC endpoint, e.g. a MySQL
	 * database.
	 */
	public void connect() {
		this.load();

		try {
			this.db = DriverManager.getConnection(this.url, this.user,
					this.password);

			if (this.db != null) {
				DatabaseMetaData meta = this.db.getMetaData();
				logger.debug("Got Connection - Driver Name: "
						+ meta.getDriverName() + ", Driver Version: "
						+ meta.getDriverVersion());
			} else {
				logger.fatal("Could not Get Connection");
				System.exit(-1); // TODO error code
			}
		} catch (SQLException e) {
			logger.fatal("Error while connecting: " + e.toString());
			System.exit(-1); // TODO error code
		}

		this.connected = true;
	}

	/**
	 * Executes a query against the configured database.<br/>
	 * If the connection to the database was not yet established, this will be
	 * done automatically.
	 * 
	 * @param q
	 *            the SQL query string
	 * @return the query result
	 */
	public ResultSet query(String q) {
		if (!this.connected)
			this.connect();

		ResultSet rs = null;
		try {
			Statement statement = this.db.createStatement();

			rs = statement.executeQuery(q);
			// while (rs.next()) System.out.println(rs.getString(1));
		} catch (SQLException e) {
			logger.fatal("Error while executing query: " + e.toString());
			System.exit(-1); // TODO error code
		}

		this.lastResult = rs;

		return rs;
	}

	/**
	 * Returns the number of result "rows" of the last database query.
	 * 
	 * @return number of result "rows" of the last database query
	 */
	public int getNumRows() {
		try {
			if (this.lastResult != null) {
				this.lastResult.last();
				return this.lastResult.getRow();
			}
		} catch (SQLException e) {
			logger.fatal("Error while retrieving result meta-data: "
					+ e.toString());
			System.exit(-1); // TODO error code
		}

		return 0;
	}

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver
	 *            the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the username
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the username to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the driverClassPath
	 */
	public String getDriverClassPath() {
		return driverClassPath;
	}

	/**
	 * @param driverClassPath
	 *            the driverClassPath to set
	 */
	public void setDriverClassPath(String driverClassPath) {
		this.driverClassPath = driverClassPath;
	}

}
