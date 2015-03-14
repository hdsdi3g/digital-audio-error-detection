/*
 * 
 * ***** BEGIN LICENSE BLOCK *****
 *
 * This file is part of Digital Audio Error Detection.
 * 
 * Digital Audio Error Detection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Digital Audio Error Detection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Digital Audio Error Detection.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */

package com.resizia.digitalaudioerrordetection;

import java.io.File;

/**
 * Get clean values from System Properties.
 */
public class ConfigurationProperties {
	
	public static String getP(String name, String defaultvalue) {
		return System.getProperty(name, defaultvalue);
	}
	
	public static int getP(String name, int defaultvalue) {
		try {
			return Integer.parseInt(System.getProperty(name, String.valueOf(defaultvalue)));
		} catch (Exception e) {
			e.printStackTrace();
			return defaultvalue;
		}
	}
	
	public static float getP(String name, float defaultvalue) {
		try {
			return Float.parseFloat(System.getProperty(name, String.valueOf(defaultvalue)));
		} catch (Exception e) {
			e.printStackTrace();
			return defaultvalue;
		}
	}
	
	public static long getP(String name, long defaultvalue) {
		try {
			return Long.parseLong(System.getProperty(name, String.valueOf(defaultvalue)));
		} catch (Exception e) {
			e.printStackTrace();
			return defaultvalue;
		}
	}
	
	public static double getP(String name, double defaultvalue) {
		try {
			return Double.parseDouble(System.getProperty(name, String.valueOf(defaultvalue)));
		} catch (Exception e) {
			e.printStackTrace();
			return defaultvalue;
		}
	}
	
	/**
	 * Don't test if file exists.
	 */
	public static File getP(String name, File defaultvalue) {
		String filename = System.getProperty(name, "");
		if (filename != "") {
			return new File(filename);
		} else {
			return defaultvalue;
		}
	}
	
}
