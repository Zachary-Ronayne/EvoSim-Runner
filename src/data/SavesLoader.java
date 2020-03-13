package data;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import sim.Track;

/**
 * Handles loading in all the save files for the simulations and the tracks
 */
public final class SavesLoader{

	/**
	 * The names of all the save files currently available to open
	 */
	private static String[] saveNames;
	/**
	 * The names of all the track files currently available to open
	 */
	private static String[] trackNames;
	
	/**
	 * Scans the folders for all files in the file list and updates the data in this class. If no default track is found, a default track file is added
	 */
	public static void scanFiles(){
		//load in saves
		File file = new File("./data/saves");
		File[] files = file.listFiles();
		
		ArrayList<String> ss = new ArrayList<String>();
		//make sure each of these files is a valid file
		for(File f : files){
			if(f.isFile()) ss.add(f.getName());
		}
		
		saveNames = new String[ss.size()];
		for(int i = 0; i < saveNames.length; i++) saveNames[i] = ss.get(i);
		
		//load in tracks
		file = new File("./data/tracks");
		files = file.listFiles();
		
		ss = new ArrayList<String>();
		//make sure each of these files is a valid file
		for(File f : files){
			if(f.isFile()) ss.add(f.getName());
		}
		
		trackNames = new String[ss.size()];
		for(int i = 0; i < trackNames.length; i++) trackNames[i] = ss.get(i);
		
		sortNames();
	}
	
	/**
	 * Adds the default track to the tracks list, overriding the file if one already exists
	 * @param settings the settings to load the track in with
	 */
	public static void saveDefaultTrack(Settings settings){
		try{
			Track t = new Track(settings, new Point2D.Double(0, 0));
			t.setDefault();
			PrintWriter write = new PrintWriter(new File("./data/tracks/DefaultTrack.txt"));
			t.save(write);
			write.close();
			scanFiles();
		}catch(Exception e){}
	}
	
	public static String[] getSaveNames(){
		return saveNames;
	}
	
	public static String[] getTrackNames(){
		return trackNames;
	}
	
	/**
	 * Sorts the file lists of the saves and tracks
	 */
	public static void sortNames(){
		for(int i = 0; i < saveNames.length; i++){
			int low = -1;
			for(int j = i; j < saveNames.length; j++){
				if(low == -1 || saveNames[j].toLowerCase().compareTo(saveNames[low].toLowerCase()) < 0) low = j;
			}
			String temp = saveNames[i];
			saveNames[i] = saveNames[low];
			saveNames[low] = temp;
		}

		for(int i = 0; i < trackNames.length; i++){
			int low = -1;
			for(int j = i; j < trackNames.length; j++){
				if(low == -1 || trackNames[j].toLowerCase().compareTo(trackNames[low].toLowerCase()) < 0) low = j;
			}
			String temp = trackNames[i];
			trackNames[i] = trackNames[low];
			trackNames[low] = temp;
		}
	}
	
}
