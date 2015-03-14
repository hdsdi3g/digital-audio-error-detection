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
 * A Digital Silence event for a wav file.
 */
public class WavDefaulteventDigitalsilence implements WavDefaultevent {
	
	protected int position;
	protected int channel;
	
	public WavDefaulteventDigitalsilence(int position, int channel) {
		this.position = position;
		this.channel = channel;
	}
	
	public String getLabel() {
		return "Silence";
	}
	
	public int getPosition() {
		return position;
	}
	
}
