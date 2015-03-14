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

/**
 * Wav Default event
 */
interface WavDefaultevent {
	
	/**
	 * The text to be displayed.
	 */
	String getLabel();
	
	/**
	 * The real sample position for wav.
	 */
	int getPosition();
	
}
