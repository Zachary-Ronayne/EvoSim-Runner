package menu.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import data.SavesLoader;
import data.Settings;
import menu.Main;
import menu.component.MenuButton;
import menu.component.ScrollingList;
import menu.input.SettingsTypingString;

public class MenuMain extends Menu{
	
	/**
	 * scrolling list for selecting which file to load
	 */
	private ScrollingList saveSelector;
	/**
	 * The index of the saveSelector buttons that should be used to load a save when loading. -1 for nothing being selected
	 */
	private int selectedFileIndex;
	/**
	 * scrolling list for selecting which track to load
	 */
	private ScrollingList trackSelector;
	/**
	 * The index of the trackSelector buttons that should be used to load a track when loading. -1 for nothing being selected
	 */
	private int selectedTrackIndex;
	
	/**
	 * true if the main menu should display information along with all the other buttons
	 */
	private boolean showingHelp;
	
	/**
	 * the settings display object
	 */
	private SettingsTypingString settingsTyping;
	
	public MenuMain(Main instance){
		super(instance);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		
		showingHelp = false;
		
		//button to create a new simulation
		MenuButton newSimButton = new MenuButton(100, 40, 400, 100){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 50));
				g.drawString("New Simulation", getX() + 10, getY() + 60);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(selectedTrackIndex == -1) instance.newSimulation(null);
				else instance.newSimulation(SavesLoader.getTrackNames()[selectedTrackIndex]);
				instance.getChangeSettingsMenu().resetMenu();
				instance.setSelectedMenu(instance.getRunStatsMenu());
			}
		};
		addComponent(newSimButton);
		addControled(newSimButton);
		
		//button to load a saved simulation
		MenuButton loadSimButton = new MenuButton(100, 150, 400, 100){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 50));
				g.drawString("Load Simulation", getX() + 10, getY() + 60);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(selectedFileIndex != -1){
					if(selectedTrackIndex == -1) instance.load(SavesLoader.getSaveNames()[selectedFileIndex], null);
					else instance.load(SavesLoader.getSaveNames()[selectedFileIndex], SavesLoader.getTrackNames()[selectedTrackIndex]);
					instance.setSelectedMenu(instance.getRunStatsMenu());
				}
			}
		};
		addComponent(loadSimButton);
		addControled(loadSimButton);
		
		//button to reload settings
		MenuButton reloadSettingsButton = new MenuButton(600, 40, 500, 50){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 35));
				g.drawString("Reload settings", getX() + 10, getY() + 35);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				try{
					Scanner scan = new Scanner(new File("./data/settings.txt"));
					instance.getMainSettings().load(scan);
					scan.close();
				}catch(FileNotFoundException e1){}
				resetMenu();
				instance.setSelectedMenu(instance.getMainMenu());
			}
		};
		addComponent(reloadSettingsButton);
		addControled(reloadSettingsButton);
		
		//button to load default settings
		MenuButton loadDefaultsettings = new MenuButton(600, 100, 500, 50){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 35));
				g.drawString("Load default settings", getX() + 10, getY() + 35);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				instance.getMainSettings().loadDefaults();
				resetMenu();
				instance.setSelectedMenu(instance.getMainMenu());
			}
		};
		addComponent(loadDefaultsettings);
		addControled(loadDefaultsettings);
		
		//button to scan for new saves and tracks
		MenuButton scanSavesButton = new MenuButton(600, 160, 500, 50){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 35));
				g.drawString("Scan for files", getX() + 10, getY() + 35);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				SavesLoader.scanFiles();
				resetMenu();
				instance.setSelectedMenu(instance.getMainMenu());
			}
		};
		addComponent(scanSavesButton);
		addControled(scanSavesButton);
		
		//button to open track editor
		MenuButton openTrackEditorButton = new MenuButton(600, 220, 500, 50){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 35));
				g.drawString("Open track editor", getX() + 10, getY() + 35);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				instance.getTrackEditor().resetMenu();
				if(selectedTrackIndex != -1) instance.getTrackEditor().setTrack(SavesLoader.getTrackNames()[selectedTrackIndex]);
				instance.setSelectedMenu(instance.getTrackEditor());
			}
		};
		addComponent(openTrackEditorButton);
		addControled(openTrackEditorButton);
		
		selectedFileIndex = -1;
		selectedTrackIndex = -1;
		
		//selector for saves
		saveSelector = new ScrollingList(100, 300, 1000, 40, SavesLoader.getSaveNames().length, 8){
			@Override
			public void renderButton(Graphics g, int index){
				try{
					//draw button
					super.renderButton(g, index);
					
					//draw text
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 25));
					g.drawString("Save file: " + SavesLoader.getSaveNames()[index], getButton(index).getX() + 4, getButton(index).getY() + getButton(index).getHeight() - 10);
					
					//draw selected highlight
					if(selectedFileIndex == index){
						g.setColor(new Color(150, 150, 255, 100));
						MenuButton b = getButton(index);
						g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
					}
				}catch(Exception e){}
			}
			@Override
			public void pressButton(int index){
				super.pressButton(index);
				if(selectedFileIndex == index) selectedFileIndex = -1;
				else selectedFileIndex = index;
			}
		};
		addComponent(saveSelector);
		addControled(saveSelector);
		
		//selector for tracks
		trackSelector = new ScrollingList(100, 650, 1000, 40, SavesLoader.getTrackNames().length, 8){
			@Override
			public void renderButton(Graphics g, int index){
				try{
					//draw button
					super.renderButton(g, index);
					
					//draw text
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 25));
					g.drawString("Track file: " + SavesLoader.getTrackNames()[index], getButton(index).getX() + 4, getButton(index).getY() + getButton(index).getHeight() - 10);
					
					//draw selected highlight
					if(selectedTrackIndex == index){
						g.setColor(new Color(150, 150, 255, 100));
						MenuButton b = getButton(index);
						g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
					}
				}catch(Exception e){}
			}
			@Override
			public void pressButton(int index){
				super.pressButton(index);
				if(selectedTrackIndex == index) selectedTrackIndex = -1;
				else selectedTrackIndex = index;
			}
		};
		addComponent(trackSelector);
		addControled(trackSelector);
		
		//help button
		MenuButton helpButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 110, 40, 100, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				if(showingHelp) g.drawString("Hide help", getX() + 4, getY() + 20);
				else g.drawString("Show Help", getX() + 4, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				showingHelp = !showingHelp;
			}
		};
		addComponent(helpButton);
		addControled(helpButton);
		
		//key input for typing settings
		resetSettingsTyping();
		addControled(settingsTyping);
	}
	
	private void resetSettingsTyping(){
		if(settingsTyping != null){
			settingsTyping.removeFromMenu(this);
		}
		
		settingsTyping = new SettingsTypingString(instance.getMainSettings(), 1120, 50);
		settingsTyping.setOn(true);
		settingsTyping.addToMenu(this);
	}
	
	@Override
	public void tick(){
		super.tick();
	}
	
	@Override
	public void render(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, Settings.DEFAULT_SCREEN_WIDTH, Settings.DEFAULT_SCREEN_HEIGHT);
		super.render(g);

		//draw text for scroll list labels
		g.setColor(Color.BLACK);
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.BOLD, 20));
		g.drawString("Saved sims:", saveSelector.getX(), saveSelector.getY() - 4);
		g.drawString("Saved tracks: ", trackSelector.getX(), trackSelector.getY() - 4);
		
		//draw the text for the settings
		settingsTyping.drawHelpInfo(g, 1120, 45, instance.getMainSettings().getFontName());
		
		//draw the overlay and info for the help menu
		if(showingHelp){
			//new sim help
			renderHelpString(instance.getMainSettings().getFontName(), g, "Click a track to use it in the new sim", 110, 105);
			renderHelpString(instance.getMainSettings().getFontName(), g, "Select no track to use the default track", 110, 130);
			
			//load sim help
			renderHelpString(instance.getMainSettings().getFontName(), g, "Click a saved sim file, then this button to load it", 110, 225);

			//reload settings help
			renderHelpString(instance.getMainSettings().getFontName(), g, "Reload settings from settings.txt", 600, 75);

			//load default settings help
			renderHelpString(instance.getMainSettings().getFontName(), g, "Set the settings to their defaults", 600, 135);

			//scan files help
			renderHelpString(instance.getMainSettings().getFontName(), g, "See if any files were removed or added", 600, 195);
			
			//track editor help
			renderHelpString(instance.getMainSettings().getFontName(), g, "Enter the track editor", 600, 255);
			renderHelpString(instance.getMainSettings().getFontName(), g, "Select a track to load it in the editor", 600, 280);
		}
	}
	
	/**
	 * draw a help string at the given location, it is just a string with a transparent background to make the text more visible
	 * @param s
	 * @param x
	 * @param y
	 * @param g
	 */
	public static void renderHelpString(String fontName, int fontSize, Graphics g, String s, int x, int y){
		g.setColor(new Color(0, 0, 0, 200));
		g.setFont(new Font(fontName, Font.PLAIN, fontSize));
		int w = g.getFontMetrics().stringWidth(s);
		g.fillRect(x - 2, y - 2, w + 4, fontSize + 4);
		g.setColor(Color.WHITE);
		g.drawString(s, x, y + fontSize - 2);
	}

	public static void renderHelpString(String fontName, Graphics g, String s, int x, int y){
		renderHelpString(fontName, 20, g, s, x, y);
	}
	
}
