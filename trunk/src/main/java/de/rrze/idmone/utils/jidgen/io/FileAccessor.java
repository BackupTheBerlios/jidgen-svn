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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rrze.idmone.utils.jidgen.i18n.Messages;



/**
 * This class was written for easy handling
 * of file I/O operations within the jidgen
 * project.
 *  
 * @author Florian Löffler <florian.loeffler@rrze.uni-erlangen.de>
 */
public class FileAccessor {

	/**
	 *  The class logger
	 */
	private static final Log logger = LogFactory.getLog(FileAccessor.class);

	/**
	 * The complete path to the file.
	 */
	private String file;

	/**
	 * The file reader for this object's file
	 */
	private Reader reader;

	/**
	 * The buffered file reader for this object's file
	 */
	private BufferedReader bufferedReader;

	/**
	 * Indicates whether the file is opened or not
	 */
	private boolean open = false;

	/**
	 * Default constructor
	 */
	public FileAccessor() {
	}

	/**
	 * Constructor with file location
	 * 
	 * @param file
	 * 			the location of the file to process
	 */
	public FileAccessor(String file) {
		this.file = file;
	}


	/**
	 * Closes the file reader
	 */
	public void close() {
		if (this.isOpen()) {
			try {
				this.bufferedReader.close();
			}
			catch (IOException e) {
				logger.fatal(e.toString());
				System.exit(202);
			}
			this.setOpen(false);
		}
	}

	/**
	 * Opens the file reader
	 */
	public void open() {
		if (!this.isOpen()) {
			this.reader = this.openFile(this.file);
			this.bufferedReader = new BufferedReader(this.reader);
			this.setOpen(true);
		}
	}


	/**
	 * Tries to get a buffered reader for the specified file
	 * 
	 * @param file
	 * 			the file we want to have a reader for
	 * @return	
	 * 			the buffered reader for the specified file or null on error
	 */
	private Reader openFile(String file) {
		try {
			Reader r = new FileReader(file);
			return r;
		}
		catch (FileNotFoundException e) {
			logger.fatal(Messages.getString("File.FILE_NOT_FOUND") + file);
			System.exit(200);
		}
	
		return null;
	}

	/**
	 * Reads one line from the file into a string and
	 * returns it
	 * 
	 * @return the next line of the file or null on EOF/error
	 */
	public String getLine() {
		if (!this.isOpen()) {
			this.open();
		}

		String line = null;
		try {
			line = this.bufferedReader.readLine();
		}
		catch (IOException e) {
			logger.fatal("Exception",e);
			System.exit(201);
		}
		return line;
	}

	/**
	 * Reads the whole file into a string and returns it.
	 * Lines are separated by '\n'.
	 * 
	 * @return the whole file content as a string
	 */
	public String getContents() {
		String contents = "";
		while ((contents += this.getLine() + "\n") != null) {
			// just loop
		}
		return contents;
	}


	/**
	 * Resets the file reader by closing and re-opening it.
	 */
	public void reset() {
		if (this.isOpen()) {
			this.close();
		}
		this.open();
	}

	/**
	 * The toString() method returns the filename.
	 * 
	 * @return
	 * 		the name of the file managed by this object
	 */
	public String toString() {
		return this.file;
	}

	/**
	 * @return
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Returns the raw file reader object for direct access
	 * @return 
	 * 		the raw file reader object
	 */
	public Reader getReader() {
		if (!this.isOpen()) {
			this.open();
		}
		return reader;
	}

	/**
	 * @return
	 */
	public BufferedReader getBufferedReader() {
		if (!this.isOpen()) {
			this.open();
		}
		return bufferedReader;
	}


	/**
	 * @param open
	 */
	private void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return file;
	}

	
	/**
	 * 
	 * @return
	 */
	public boolean exists() {
		return (new File(this.file)).exists();
	}
}
