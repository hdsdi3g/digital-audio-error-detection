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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Java DataInputSteam is Big Endian number format. Wav file is Little Endian number format.
 * @see DataInputStream
 */
public class DataInputSteamLittleEndian extends InputStream {
	
	/**
	 * The InputStream to read.
	 */
	private InputStream in;
	
	/**
	 * No used for the moment, can work, but to tests before.
	 */
	private long position;
	
	public DataInputSteamLittleEndian(InputStream inputstream) {
		super();
		in = inputstream;
		position = 0;
	}
	
	public int read() throws IOException {
		position++;
		return in.read();
	}
	
	public final short readShort() throws IOException {
		position += 2;
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (short) ((ch1 << 0) + (ch2 << 8));
	}
	
	public final int readUnsignedShort() throws IOException {
		position += 2;
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (ch1 << 0) + (ch2 << 8);
	}
	
	public final int readInt() throws IOException {
		position += 4;
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
	}
	
	/**
	 * Read 3 bytes for define one 32 bits signed integer.
	 */
	public final int readTripleByte() throws IOException {
		position += 3;
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		if ((ch1 | ch2 | ch3) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 8) + (ch2 << 16) + (ch3 << 24));
		// return ((ch1 << 8) + (ch2 << 16) + (ch3 << 24));
		// return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16));
	}
	
	/**
	 * Read 2 bytes for define one 32 bits signed integer.
	 */
	public final int readDoubleByte() throws IOException {
		position += 2;
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 16) + (ch2 << 24));
	}
	
	/**
	 * Read 1 bytes for define one 32 bits signed integer.
	 */
	public final int readSimpleByte() throws IOException {
		position += 1;
		int ch1 = in.read();
		if (ch1 < 0) {
			throw new EOFException();
		}
		return (ch1 << 24);
	}
	
	private byte readBuffer[] = new byte[8];
	
	public final long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return (((long) readBuffer[0] << 0) + ((long) (readBuffer[1] & 255) << 8) + ((long) (readBuffer[2] & 255) << 16) + ((long) (readBuffer[3] & 255) << 24) + ((long) (readBuffer[4] & 255) << 32)
				+ (((long) readBuffer[5] & 255) << 40) + (((long) readBuffer[6] & 255) << 48) + (((long) readBuffer[7] & 255) << 56));
		
	}
	
	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}
	
	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}
	
	public final int read(byte b[]) throws IOException {
		position += b.length;
		return in.read(b, 0, b.length);
	}
	
	public final int read(byte b[], int off, int len) throws IOException {
		position += len;
		return in.read(b, off, len);
	}
	
	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}
	
	public final void readFully(byte b[], int off, int len) throws IOException {
		position += len;
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}
	
	public final int skipBytes(int n) throws IOException {
		int total = 0;
		int cur = 0;
		
		while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
			total += cur;
		}
		position += n;
		return total;
	}
	
	public final byte readByte() throws IOException {
		position++;
		int ch = in.read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (byte) (ch);
	}
	
	public final int readUnsignedByte() throws IOException {
		position++;
		int ch = in.read();
		if (ch < 0) {
			throw new EOFException();
		}
		return ch;
	}
	
	public void close() throws IOException {
		super.close();
		in.close();
	}
	
}
