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

import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Jdbc {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(Jdbc.class);
	
	Connection db;

	ResultSet lastResult = null;

	boolean connected = false;
	

	/**
	 * 
	 */
	public Jdbc() {

	}

	/**
	 * 
	 * @param f
	 */
	public Jdbc(File f) {

	}


	/**
	 * 
	 */
	public void load() {
		String className = "com.mysql.jdbc.Driver";
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
		//String url = "jdbc:<subprotocol>:<subname>";

		// Define URL of database server for
		// database named mysql on the localhost
		// with the default port number 3306.
		String url = "jdbc:mysql://localhost:3306/jidgen";

		try {
			//this.db = DriverManager.getConnection(url, "loginName", "Password");
			this.db = DriverManager.getConnection(url, "jidgen", "jidgen");

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
