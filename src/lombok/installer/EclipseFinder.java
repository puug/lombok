/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.installer;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Lombok;

/** Utility class for doing various OS-specific operations related to finding Eclipse installations. */
class EclipseFinder {
	private EclipseFinder() {
		//Prevent instantiation.
	}
	
	/**
	 * Returns a File object pointing to our own jar file. Will obviously fail if the installer was started via
	 * a jar that wasn't accessed via the file-system, or if its started via e.g. unpacking the jar.
	 */
	static File findOurJar() {
		try {
			URI uri = EclipseFinder.class.getResource("/" + EclipseFinder.class.getName().replace('.', '/') + ".class").toURI();
			Pattern p = Pattern.compile("^jar:file:([^\\!]+)\\!.*\\.class$");
			Matcher m = p.matcher(uri.toString());
			if ( !m.matches() ) return new File("lombok.jar");
			String rawUri = m.group(1);
			return new File(URLDecoder.decode(rawUri, Charset.defaultCharset().name()));
		} catch ( Exception e ) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	/**
	 * Returns all drive letters on windows, regardless of what kind of drive is represented.
	 * 
	 * Relies on the 'fsutil.exe' program. I have no idea if you can call it without triggering a gazillion
	 * security warnings on Vista. I did a cursory test on an out-of-the-box Windows XP and that seems to work.
	 * 
	 * @return A List of drive letters, such as ["A", "C", "D", "X"].
	 */
	static List<String> getDrivesOnWindows() throws IOException {
		ProcessBuilder builder = new ProcessBuilder("c:\\windows\\system32\\fsutil.exe", "fsinfo", "drives");
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream in = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
		
		List<String> drives = new ArrayList<String>();
		
		String line;
		while ( (line = br.readLine()) != null ) {
			if ( line.startsWith("Drives: ") ) {
				line = line.substring(8);
				for ( String driveLetter : line.split("\\:\\\\\\s*") ) drives.add(driveLetter.trim());
			}
		}
		
		Iterator<String> it = drives.iterator();
		while ( it.hasNext() ) {
			if ( !isLocalDriveOnWindows(it.next()) ) it.remove();
		}
		
		return drives;
	}
	
	/**
	 * @return true if the letter represents a local fixed disk, false if its a disk drive, optical drive,
	 *   USB stick, network drive, or any other kind of drive. Substed (virtual) drives that are an alias to
	 *   a directory on a local disk cause a 'return true', but this is intentional.
	 */
	static boolean isLocalDriveOnWindows(String driveLetter) {
		if ( driveLetter == null || driveLetter.length() == 0 ) return false;
		try {
			ProcessBuilder builder = new ProcessBuilder("c:\\windows\\system32\\fsutil.exe", "fsinfo", "drivetype", driveLetter + ":");
			builder.redirectErrorStream(true);
			Process process = builder.start();
			InputStream in = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
			
			String line;
			while ( (line = br.readLine()) != null ) {
				if ( line.substring(5).equalsIgnoreCase("Fixed Drive") ) return true;
			}
			
			return false;
		} catch ( Exception e ) {
			return false;
		}
	}
	
	/**
	 * Returns a list of paths of Eclipse installations.
	 * Eclipse installations are found by checking for the existence of 'eclipse.exe' in the following locations:
	 * 
	 * X:\*Program Files*\*Eclipse*
	 * X:\*Eclipse*
	 * 
	 * Where 'X' is tried for all local disk drives, unless there's a problem calling fsutil, in which case only
	 * C: is tried.
	 * 
	 * @return A List of directories that contain 'eclipse.exe'.
	 */
	static List<String> findEclipseOnWindows() {
		List<String> eclipses = new ArrayList<String>();
		List<String> driveLetters = asList("C");
		
		try {
			driveLetters = getDrivesOnWindows();
		} catch ( IOException ignore ) {}
		
		for ( String letter : driveLetters ) {
			File f = new File(letter + ":\\");
			for ( File dir : f.listFiles() ) {
				if ( !dir.isDirectory() ) continue;
				if ( dir.getName().toLowerCase().contains("eclipse") ) {
					String eclipseLocation = findEclipseOnWindows1(dir);
					if ( eclipseLocation != null ) eclipses.add(eclipseLocation);
				}
				if ( dir.getName().toLowerCase().contains("program files") ) {
					for ( File dir2 : dir.listFiles() ) {
						if ( !dir2.isDirectory() ) continue;
						if ( dir.getName().toLowerCase().contains("eclipse") ) {
							String eclipseLocation = findEclipseOnWindows1(dir);
							if ( eclipseLocation != null ) eclipses.add(eclipseLocation);
						}
					}
				}
			}
		}
		
		return eclipses;
	}
	
	/** Checks if the provided directory contains 'eclipse.exe', and if so, returns the directory, otherwise null. */
	private static String findEclipseOnWindows1(File dir) {
		if ( new File(dir, "eclipse.exe").isFile() ) return dir.getAbsolutePath();
		return null;
	}
	
	/**
	 * Calls the OS-dependent 'find Eclipse' routine. If the local OS doesn't have a routine written for it,
	 * null is returned.
	 * 
	 * @return List of directories that contain the Eclipse executable.
	 */
	static List<String> findEclipses() {
		switch ( getOS() ) {
		case WINDOWS:
			return findEclipseOnWindows();
		case MAC_OS_X:
			return findEclipseOnMac();
		default:
		case UNIX:
			return null;
		}
	}
	
	static enum OS {
		MAC_OS_X, WINDOWS, UNIX;
	}
	
	static OS getOS() {
		String prop = System.getProperty("os.name", "").toLowerCase();
		if ( prop.matches("^.*\\bmac\\b.*$") ) return OS.MAC_OS_X;
		if ( prop.matches("^.*\\bwin(dows)\\b.*$") ) return OS.WINDOWS;
		
		return OS.UNIX;
	}
	
	/**
	 * Returns the proper name of the executable for the local OS.
	 * 
	 * @return 'Eclipse.app' on OS X, 'eclipse.exe' on Windows, and 'eclipse' on other OSes.
	 */
	static String getEclipseExecutableName() {
		switch ( getOS() ) {
		case WINDOWS:
			return "eclipse.exe";
		case MAC_OS_X:
			return "Eclipse.app";
		default:
		case UNIX:
			return "eclipse";
		}
	}
	
	/**
	 * Scans /Applications for any folder named 'Eclipse'
	 */
	static List<String> findEclipseOnMac() {
		List<String> eclipses = new ArrayList<String>();
		for ( File dir : new File("/Applications").listFiles() ) {
			if ( !dir.isDirectory() ) continue;
			if ( dir.getName().toLowerCase().equals("eclipse.app") ) {
				//This would be kind of an unorthodox Eclipse installation, but if Eclipse ever
				//moves to this more maclike installation concept, our installer can still handle it.
				eclipses.add("/Applications");
			}
			if ( dir.getName().toLowerCase().contains("eclipse") ) {
				if ( new File(dir, "Eclipse.app").exists() ) eclipses.add(dir.toString());
			}
		}
		return eclipses;
	}
}