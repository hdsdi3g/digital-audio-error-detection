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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Open, read and parse a wav file for values analyses.
 */
public class WavFile {
	
	/**
	 * Wav file channel count.
	 */
	private int channelcount;
	
	/**
	 * Wav sample frequency, like 48000.
	 */
	private int freq;
	
	/**
	 * Bytes count for one sample : 1 = 8 bits, 2 = 16 bits, 3 = 24 bits
	 */
	private int samplesize;
	
	/**
	 * Raw position in file (in sample), without channel.
	 */
	private int rawsampleposition;
	
	/**
	 * Current channel position (0 = left, 1 = right ...)
	 */
	private int currentchannel;
	
	/**
	 * Stream to read values.
	 */
	private DataInputSteamLittleEndian datainputstream;
	
	/**
	 * Wav data block size (bytes).
	 */
	private int datasize;
	
	/**
	 * File to read.
	 */
	private File file;
	
	/**
	 * Errors events.
	 */
	private ArrayList<WavDefaultevent> wavdefaultevents;
	
	/**
	 * New line separator.
	 */
	public static final String LINESEPARATOR = System.getProperty("line.separator");
	
	/**
	 * INTEGER_MAX_VALUE in double format.
	 */
	private static double INTEGER_MAX_VALUE = Integer.MAX_VALUE;
	
	/**
	 * If there is this count of sample of a Digital Silence, raise an error.
	 */
	private int same_sample_threshold_silence = ConfigurationProperties.getP("daed.same_sample_threshold_silence", 10);
	
	/**
	 * If there is this count of sample of a Hold value, raise an error.
	 */
	private int same_sample_threshold_hold = ConfigurationProperties.getP("daed.same_sample_threshold_hold", 5);
	
	/**
	 * During this time (in seconds), no warning, when a new error is detect.
	 * This helps not to go too close to too many errors.
	 */
	private float no_warning_duration = ConfigurationProperties.getP("daed.no_warning_duration", 1.0f);
	
	/**
	 * If Hold value error is detected, the value must to be more high like this value, to raise an error.
	 */
	private double level_threshold_hold = ConfigurationProperties.getP("daed.level_threshold_hold", -50f);
	
	/**
	 * The most high sample value.
	 */
	private double peak_value_dbfs = Double.MIN_VALUE;
	
	/**
	 * peak_value_dbfs position in wav file.
	 */
	private int peak_position_sample = -1;
	
	/**
	 * peak_value_dbfs position channel.
	 */
	private int peak_channel_id = -1;
	
	/**
	 * Number format for round, and clean number values.
	 */
	public static NumberFormat currentnumberformat = DecimalFormat.getNumberInstance();
	
	/**
	 * Start publish error in MKR from this time (seconds).
	 */
	public static float startpositionresultvalues = ConfigurationProperties.getP("daed.startpositionresultvalues", 0f);
	
	/**
	 * Stop publish error in MKR to this time (seconds).
	 */
	public static float endpositionresultvalues = ConfigurationProperties.getP("daed.endpositionresultvalues", Float.MAX_VALUE);
	
	/**
	 * @param file File to read from.
	 */
	public WavFile(File file) throws IOException {
		this.file = file;
		if (file == null) {
			throw new NullPointerException("\"file\" can't to be null");
		}
		FileInputStream fileinputstream = new FileInputStream(file);
		/**
		 * BufferedInputStream for speed up the reading byte after byte .
		 */
		BufferedInputStream bis = new BufferedInputStream(fileinputstream, 10 * 1024 * 1024);
		datainputstream = new DataInputSteamLittleEndian(bis);
		wavdefaultevents = new ArrayList<WavDefaultevent>();
	}
	
	/**
	 * Read the 4 next bytes, like wav label data.
	 */
	private byte[] readNext4bytes() throws IOException {
		byte[] result = new byte[4];
		
		int realread = datainputstream.read(result);
		if (realread != 4) {
			throw new EOFException(String.valueOf(realread));
		}
		return result;
	}
	
	/**
	 * Read the first bytes in wav file for get informations, and position to the start of wav datas (samples).
	 * @see http://www.sonicspot.com/guide/wavefiles.html to understand the wonders of wav riff files.
	 */
	private void parseWavHeader() throws IOException {
		
		byte[] label;
		
		/**
		 * FileTypeBlocID (4 bytes) : RIFF (0x52,0x49,0x46,0x46)
		 */
		validByteValue(0x52, "RIFF");
		validByteValue(0x49, "RIFF");
		validByteValue(0x46, "RIFF");
		validByteValue(0x46, "RIFF");
		
		/**
		 * FileSize (4 bytes) : file size minus 8 bytes
		 */
		readNext4bytes();
		
		/**
		 * FileFormatID (4 bytes) : Format = «WAVE» (0x57,0x41,0x56,0x45)
		 */
		validByteValue(0x57, "WAVE");
		validByteValue(0x41, "WAVE");
		validByteValue(0x56, "WAVE");
		validByteValue(0x45, "WAVE");
		
		while (true) {
			label = readNext4bytes();
			
			if (((label[0] == 0x66) & (label[1] == 0x6D) & (label[2] == 0x74) & (label[3] == 0x20))) {
				/**
				 * "fmt "
				 * Wav file informations
				 */
				
				/**
				 * BlocSize (4 bytes) : bytes count for block - 8 (0x10)
				 */
				int blocsize = datainputstream.readInt(); // 16
				
				/**
				 * AudioFormat (2 bytes) : Storage type (1: PCM, ...)
				 */
				validByteValue(0x01, "PCM");
				validByteValue(0x00, "PCM");
				
				/**
				 * NbrCanaux (2 bytes) : number of channels (1 -> 6)
				 */
				channelcount = datainputstream.readUnsignedShort();
				
				/**
				 * Sample frequency in Hz (4 bytes) : 11025, 22050, 44100, 48000 and 96000
				 */
				freq = datainputstream.readInt(); // 48000
				
				/**
				 * BytePerSec (4 bytes) : bytes count to read by seconds (== freq * BytePerBloc).
				 */
				datainputstream.readInt();// 288000
				
				/**
				 * BytePerBloc (2 bytes) : bytes count by sample block (for all channels : channelcount * BitsPerSample/8).
				 */
				datainputstream.readUnsignedShort();
				
				/**
				 * BitsPerSample (2 bytes) : bytes count used for one sample (8, 16, 24)
				 */
				samplesize = datainputstream.readUnsignedShort() / 8;
				
				if (blocsize > 16) {
					datainputstream.skipBytes(blocsize - 16);
				}
				
				continue;
			}
			
			if (((label[0] == 0x64) & (label[1] == 0x61) & (label[2] == 0x74) & (label[3] == 0x61))) {
				/**
				 * "data", this is what we seek !
				 */
				break;
			}
			
			/**
			 * The rest : "iXML", "PAD " ...
			 */
			datainputstream.skipBytes(datainputstream.readInt());
		}
		
		// 0x20 0xCC 0x2F -> 24/st
		// 0x20 0xCC 0x0F -> 16/mo
		
		/**
		 * DataSize (4 bytes) : bytes count for data : samples
		 */
		datasize = datainputstream.readInt();
		
		/**
		 * Let's start to read this file.
		 */
		currentchannel = channelcount - 1;
		rawsampleposition = -1;
	}
	
	/**
	 * If the next reading byte is not the expected value, throw a new IOException, whis testname to cause.
	 */
	private void validByteValue(int expectedvalue, String testname) throws IOException {
		if (datainputstream.read() != expectedvalue) {
			throw new IOException("Bad header const for " + testname);
		}
	}
	
	/**
	 * The number of bits for a sample like 8, 16, 24
	 */
	public int getBitspersample() {
		return samplesize * 8;
	}
	
	/**
	 * The number of sample for this file.
	 */
	public int getSampleCount() {
		return datasize / (samplesize * channelcount);
	}
	
	/**
	 * The duration for this audio file.
	 */
	public float getDuration() {
		return (float) getSampleCount() / (float) freq;
	}
	
	/**
	 * Close the stream.
	 * Don't forget to do !
	 */
	public void close() throws IOException {
		datainputstream.close();
	}
	
	/**
	 * Read next sample (channel after channel) and return the reading value converted in signed 32b integer.
	 */
	private int getNextRawSampleValue() throws IOException {
		if (currentchannel + 1 == channelcount) {
			currentchannel = 0;
		} else {
			currentchannel++;
		}
		rawsampleposition++;
		
		if (samplesize == 1) {
			return datainputstream.readSimpleByte();
		}
		if (samplesize == 2) {
			return datainputstream.readDoubleByte();
		}
		if (samplesize == 3) {
			return datainputstream.readTripleByte();
		}
		return -1;
	}
	
	/**
	 * @return false is the end of data block is reached.
	 */
	private boolean isHaveNextSample() {
		return (((rawsampleposition * samplesize) + samplesize) < datasize);
	}
	
	/**
	 * Convert sample value (integer 32 bits signed) to lovely dB full scale in double.
	 */
	private static double get_dBValue(int linear_int_value) {
		return 20 * Math.log10(Math.abs(linear_int_value) / INTEGER_MAX_VALUE);
	}
	
	/**
	 * Reads the wav file and analyzes its values.
	 */
	public void process() throws IOException {
		parseWavHeader();
		
		int maxvalue = 0;
		int currentvalue;
		int lastmaxvalue = 0;
		
		int rawsamplepos = 0;
		int realsamplepos = -1;
		int no_warning_duration_realsample = Math.round((freq * no_warning_duration));
		
		int[] lastvalue = new int[channelcount];
		int[] samevaluecount = new int[channelcount];
		
		int lastovermodulationrealsample = -no_warning_duration_realsample;
		int lastsilencezonerealsample = -no_warning_duration_realsample;
		int[] silencesamplecount = new int[channelcount];
		int lastholdrealsample = -no_warning_duration_realsample;
		boolean samesampleisnotsilenceorovermodulation = true;
		
		try {
			while (isHaveNextSample()) {
				currentvalue = getNextRawSampleValue();
				realsamplepos = rawsamplepos / channelcount;
				
				/**
				 * Max value compute.
				 */
				maxvalue = Math.max(Math.abs(currentvalue), maxvalue);
				if (lastmaxvalue != maxvalue) {
					peak_position_sample = realsamplepos;
					peak_channel_id = currentchannel;
				}
				lastmaxvalue = maxvalue;
				
				samesampleisnotsilenceorovermodulation = true;
				
				if (lastvalue[currentchannel] == currentvalue) {
					/**
					 * the previous sample for this channel is duplicated
					 */
					samevaluecount[currentchannel]++;
					
					if (currentvalue == 0) {
						samesampleisnotsilenceorovermodulation = false;
						silencesamplecount[currentchannel]++;
						if (silencesamplecount[currentchannel] == same_sample_threshold_silence) {
							if ((realsamplepos > lastsilencezonerealsample + no_warning_duration_realsample)) {
								System.err.print("Digital silence\t");
								System.err.print(realsamplepos - same_sample_threshold_silence);
								System.err.print("\t");
								System.err.println(currentchannel + 1);
								lastsilencezonerealsample = realsamplepos;
								wavdefaultevents.add(new WavDefaulteventDigitalsilence(realsamplepos - same_sample_threshold_silence, currentchannel + 1));
							}
						}
					} else {
						silencesamplecount[currentchannel] = 0;
					}
					
				} else {
					/**
					 * new sample different
					 */
					samevaluecount[currentchannel] = 0;
					
					if ((currentvalue == Integer.MIN_VALUE) | (currentvalue >= 0x7FFF0000)) {
						samesampleisnotsilenceorovermodulation = false;
						if ((realsamplepos > lastovermodulationrealsample + no_warning_duration_realsample)) {
							System.err.print("Overmodulation\t");
							System.err.print(realsamplepos);
							System.err.print("\t");
							System.err.println(currentchannel + 1);
							lastovermodulationrealsample = realsamplepos;
							wavdefaultevents.add(new WavDefaulteventOvermodulation(realsamplepos, currentchannel + 1));
						}
					}
				}
				
				if (samesampleisnotsilenceorovermodulation) {
					if ((samevaluecount[currentchannel] == same_sample_threshold_hold) & ((realsamplepos > lastholdrealsample + no_warning_duration_realsample))) {
						double hold_value = get_dBValue(currentvalue);
						if (hold_value > level_threshold_hold) {
							System.err.print("Hold value\t");
							System.err.print(currentnumberformat.format(hold_value));
							System.err.print("\t");
							System.err.print(1 + realsamplepos - same_sample_threshold_hold);
							System.err.print("\t");
							System.err.println(currentchannel + 1);
							wavdefaultevents.add(new WavDefaulteventHold(1 + realsamplepos - same_sample_threshold_hold, currentchannel + 1, get_dBValue(currentvalue)));
						}
						/**
						 * in all cases were recorded, although there was no warning, so that this does not pass a previous if.
						 */
						lastholdrealsample = realsamplepos;
					}
				}
				
				lastvalue[currentchannel] = currentvalue;
				
				rawsamplepos++;
			}
		} catch (EOFException e) {
			e.printStackTrace();
		}
		
		peak_value_dbfs = get_dBValue(maxvalue);
		
	}
	
	/**
	 * @return peak_channel_id
	 */
	public int getPeak_channel_id() {
		return peak_channel_id;
	}
	
	/**
	 * @return peak_position_sample
	 */
	public int getPeak_position_sample() {
		return peak_position_sample;
	}
	
	/**
	 * @return peak_value_dbfs
	 */
	public double getPeak_value_dbfs() {
		return peak_value_dbfs;
	}
	
	/**
	 * The tabbed text file name.
	 */
	private File tableresultfile = ConfigurationProperties.getP("daed.tableresultfilename", new File("results.txt"));
	
	/**
	 * Convert an sample number to a second value.
	 */
	public float getPositionFromRealSample(int realsampleposition) {
		return (float) realsampleposition / (float) freq;
	}
	
	/**
	 * Write result table file, wav txt information, and WaveLab MRK file.
	 */
	public void writeInformationFiles() throws IOException {
		boolean createheader;
		createheader = (tableresultfile.exists() == false);
		/**
		 * Append if file exists
		 */
		FileOutputStream tableout = new FileOutputStream(tableresultfile, true);
		
		/**
		 * Text tabulated (table)
		 */
		StringBuffer tableline;
		if (createheader) {
			tableline = new StringBuffer();
			tableline.append("File name\t");
			tableline.append("Bits per sample\t");
			tableline.append("Channel count\t");
			tableline.append("Sample freq\t");
			tableline.append("Sample count\t");
			tableline.append("File duration\t");
			tableline.append("File size\t");
			tableline.append("Peak channel\t");
			tableline.append("Peak position\t");
			tableline.append("Peak value");
			tableline.append(LINESEPARATOR);
			tableout.write(tableline.toString().getBytes());
		}
		
		tableline = new StringBuffer();
		tableline.append(file.getName());
		tableline.append("\t");
		tableline.append(getBitspersample());
		tableline.append("\t");
		tableline.append(channelcount);
		tableline.append("\t");
		tableline.append(freq);
		tableline.append("\t");
		tableline.append(getSampleCount());
		tableline.append("\t");
		tableline.append(currentnumberformat.format(getDuration()));
		tableline.append("\t");
		tableline.append(file.length());
		tableline.append("\t");
		tableline.append(getPeak_channel_id() + 1);
		tableline.append("\t");
		tableline.append(getPeak_position_sample());
		tableline.append("\t");
		tableline.append(currentnumberformat.format(getPeak_value_dbfs()));
		tableline.append(LINESEPARATOR);
		tableout.write(tableline.toString().getBytes());
		tableout.flush();
		tableout.close();
		
		/**
		 * Text in line.
		 */
		String infofilename = file.getPath() + ".txt";
		/**
		 * create new file
		 */
		PrintStream infoout = new PrintStream(new File(infofilename));
		infoout.print("File name\t");
		infoout.println(file.getName());
		infoout.print("Bits per sample\t");
		infoout.println(getBitspersample());
		infoout.print("Channel count\t");
		infoout.println(channelcount);
		infoout.print("Sample freq\t");
		infoout.println(freq);
		infoout.print("Sample count\t");
		infoout.println(getSampleCount());
		infoout.print("File duration\t");
		infoout.println(currentnumberformat.format(getDuration()));
		infoout.print("File size\t");
		infoout.println(file.length());
		infoout.print("Peak channel\t");
		infoout.println(getPeak_channel_id() + 1);
		infoout.print("Peak position\t");
		infoout.println(getPeak_position_sample());
		infoout.print("Peak value\t");
		infoout.println(currentnumberformat.format(getPeak_value_dbfs()));
		infoout.flush();
		infoout.close();
		
		/**
		 * Wavelab's Mrk file
		 */
		if (wavdefaultevents.size() > 0) {
			String mrkfilename = file.getPath().substring(0, file.getPath().lastIndexOf(".")) + ".mrk";
			/**
			 * Create / overwrite
			 */
			PrintStream mrkout = new PrintStream(new File(mrkfilename));
			
			mrkout.println("Markers");
			mrkout.println("{");
			
			int currentposition;
			for (int pos = 0; pos < wavdefaultevents.size(); pos++) {
				currentposition = wavdefaultevents.get(pos).getPosition();
				if (pos + 1 < wavdefaultevents.size()) {
					if (currentposition == wavdefaultevents.get(pos + 1).getPosition()) {
						/**
						 * If the next event is at the same place like now, go next.
						 */
						continue;
					}
				}
				/**
				 * don't show events before time
				 */
				if (getPositionFromRealSample(currentposition) < startpositionresultvalues) {
					continue;
				}
				
				/**
				 * don't show events after time
				 */
				if (getPositionFromRealSample(currentposition) > (getDuration() - endpositionresultvalues)) {
					break;
				}
				
				mrkout.print("\tMarker");
				mrkout.println(pos + 1);
				mrkout.println("\t{");
				mrkout.print("\t\tName=");
				mrkout.println(wavdefaultevents.get(pos).getLabel());
				mrkout.print("\t\tPos=");
				mrkout.println(currentposition);
				mrkout.println("\t\tType=0");
				mrkout.println("\t\tFlags=0");
				mrkout.println("\t\tExtra=0");
				mrkout.println("\t}");
			}
			mrkout.println("}");
			
			mrkout.flush();
			mrkout.close();
		}
	}
	
}
