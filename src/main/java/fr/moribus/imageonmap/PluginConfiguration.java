/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap;

import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;

import java.io.*;
import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;


public final class PluginConfiguration extends Configuration
{
	static public Locale LANG = Locale.ENGLISH;

	static public Boolean COLLECT_DATA = false;

	static public Integer MAP_GLOBAL_LIMIT = 0;
	static public Integer MAP_PLAYER_LIMIT = 0;

	static public Integer LIMIT_SIZE_X = 0;
	static public Integer LIMIT_SIZE_Y= 0;
	static public Boolean SAVE_FULL_IMAGE = false;
	FileInputStream in = null;

	public static void initialize() {

		try {
			FileInputStream fis = new FileInputStream(new File(ImageOnMap.getPlugin().getDataFolder(), "config.yml"));
			 
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			line = br.readLine();//2
			line = br.readLine();//3
			line = br.readLine();//4
			line = br.readLine();//5
			line = br.readLine(); //We're now at line 6.
			line = br.readLine(); //lang is in Line.
			
			if(line.contains("en_US")) LANG = Locale.ENGLISH;
			if(line.contains("fr_FR")) LANG = Locale.FRENCH;
			else LANG = Locale.ENGLISH;

			line = br.readLine();//8
			line = br.readLine();//9
			line = br.readLine();//10
			line = br.readLine();//11
			line = br.readLine();//Collect-data in line.
			
			if(line.contains("true")) COLLECT_DATA = true;
			
			line = br.readLine();//13
			line = br.readLine();//14
			line = br.readLine();//15
			line = br.readLine();//16
			line = br.readLine();//17
			line = br.readLine();//map-global-limit
			
			line = line.substring(17, line.length() - 1);
			try{MAP_GLOBAL_LIMIT = Integer.parseInt(line);}
			catch(NumberFormatException e) {MAP_GLOBAL_LIMIT = 0;}
			
			//It's even the same line length! Nothing new needs to be done.
			line = br.readLine();//map-player-limit
			//map-player-limit: 172
			//012345678901234567890
			line = line.substring(17, line.length() - 1);
			try{MAP_PLAYER_LIMIT = Integer.parseInt(line);}
			catch(NumberFormatException e) {MAP_PLAYER_LIMIT = 0;}

			line = br.readLine();//20
			line = br.readLine();//21
			line = br.readLine();//22

			line = br.readLine();//limit-map-size-x
			//Lucky break! same code. Again!
			line = line.substring(17, line.length() - 1);
			try{LIMIT_SIZE_X = Integer.parseInt(line);}
			catch(NumberFormatException e) {LIMIT_SIZE_X = 0;}
			
			line = br.readLine();//limit-map-size-x
			//Lucky break! same code. Again!
			line = line.substring(17, line.length() - 1);
			try{LIMIT_SIZE_Y = Integer.parseInt(line);}
			catch(NumberFormatException e) {LIMIT_SIZE_Y = 0;}
			
			line = br.readLine();//25
			line = br.readLine();//26
			line = br.readLine();//27
			
			line = br.readLine();//line holds save-full-image.
			if(line.contains("true")) {
				SAVE_FULL_IMAGE = true;
			}
			br.close();
			fis.close();
		}
		catch(IOException e){

		}
	}
	
}
