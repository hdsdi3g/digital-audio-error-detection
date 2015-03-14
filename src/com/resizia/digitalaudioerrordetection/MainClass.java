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
import java.util.ArrayList;

public class MainClass {
	
	public static final float app_version = 0.12f;
	
	public static void main(String[] args) {
		
		System.out.print("Digital Audio Error Detection v");
		System.out.println(app_version);
		System.out.println();
		
		if ((WavFile.startpositionresultvalues > 0f) | (WavFile.endpositionresultvalues < Float.MAX_VALUE)) {
			System.out.print("Partial MRK result : ");
			System.out.print(WavFile.startpositionresultvalues);
			System.out.print(" sec to ");
			System.out.print(WavFile.endpositionresultvalues);
			System.out.println(" sec");
			System.out.println();
		}
		
		ArrayList<String> files = new ArrayList<String>();
		
		if (args.length > 0) {
			for (int pos = 0; pos < args.length; pos++) {
				files.add(args[pos]);
			}
		}
		
		// long start = System.nanoTime();
		
		File currentfile;
		File[] subfiles;
		for (int pos = 0; pos < files.size(); pos++) {
			currentfile = new File(files.get(pos));
			if (currentfile.exists() == false) {
				continue;
			} else {
				if (currentfile.isDirectory()) {
					subfiles = currentfile.listFiles();
					for (int possf = 0; possf < subfiles.length; possf++) {
						files.add(subfiles[possf].getPath());
					}
					continue;
				}
			}
			if ((files.get(pos).endsWith("wav") == false) & (files.get(pos).endsWith("WAV") == false)) {
				System.err.print(files.get(pos));
				System.err.println(" is not a wav, go next");
				continue;
			}
			System.out.println(files.get(pos));
			try {
				WavFile wavfile = new WavFile(currentfile);
				wavfile.process();
				wavfile.writeInformationFiles();
				wavfile.close();
				
				System.out.print("Peak value:\t");
				System.out.print(WavFile.currentnumberformat.format(wavfile.getPeak_value_dbfs()));
				System.out.print("\t");
				System.out.print(wavfile.getPeak_position_sample());
				System.out.print("\t");
				System.out.println(wavfile.getPeak_channel_id() + 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
