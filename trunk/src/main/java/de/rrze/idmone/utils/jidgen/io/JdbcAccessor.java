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
 * 
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
public class JdbcAccessor {

	/**
	 * The class logger
	 */
	private static final Log logger = LogFactory.getLog(JdbcAccessor.class);

	Connection db;

	ResultSet lastResult = null;

	boolean connected = false;
	
	private String driver;
	private String url;
	private String user;
	private String password;

	/**
	 * Default constructor
	 */
	public JdbcAccessor() {
	}

	/**
	 * 
	 */
	private void load() {
		try {
			
			URLClassLoader classLoader = new URLClassLoader( new URL[] { new URL("jar:file://" + "/usr/share/jdbc-mysql/lib/jdbc-mysql.jar" + "!/") });
	        Driver d = (Driver) classLoader.loadClass("com.mysql.jdbc.Driver").newInstance();
	        DriverManager.registerDriver(new DriverShim(d));

		} catch (ClassNotFoundException e) {
			logger.fatal("Unable to load the driver class: Class not found \""
					+ this.driver + "\".");
			System.exit(-1); // TODO error code
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
			System.exit(-1); // TODO error code
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(-1); // TODO error code
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(-1); // TODO error code
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1); // TODO error code
		}
	}

	/**
	 * Establish a connection to the configured JDBC endpoint, e.g. a mysql
	 * database
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
	 * 
	 * @param q
	 * @return
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
	 * 
	 * @return
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

}
