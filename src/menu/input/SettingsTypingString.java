package menu.input;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import data.Settings;
import menu.component.MenuButton;
import menu.types.Menu;
import menu.types.MenuMain;

public class SettingsTypingString extends TypingStringInput{
	/**
	 * the index of the settings to use, this may be temporary
	 */
	private int settingsIndex;
	
	private int outputState;
	private int outputStateTimer;
	
	private String[] settingsStrings;
	
	private MenuButton[] buttons;
	
	private Settings settings;

	private final String[] LABEL_NAMES = new String[]{
			"Window Width: ",
			"Window Height: ",
			"Font: ",
			"", 
			"Max zoom in sim: ",
			"Min zoom in sim: ",
			"Max tick speed: ",
			"Min tick speed: ",
			"Num runners: ",
			"Track tile size: ",
			"Fitness Lines give: ",
			"",
			"Runner speed change: ",
			"Runner angle change: ",
			"Runner min speed: ",
			"Runner max speed: ",
			"Runner radius: ",
			"Kill runner: ",
			"Runner test time: ",
			"Runner view distance: ",
			"Fitness gained per fitness line: ",
			"Fitness gained per tick: ",
			"Use speed for fitness: ",
			"Use vision distance for fitness: ",
			"Runner vision line weights: ",
			"Runner of vision line angles: ",
			"Runner hidden nodes: ",
			"",
			"Max mutability: ",
			"Min mutability: ",
			"Mutability change: ",
			"Node loc X: ",
			"Node loc Y: ",
			"Node disp radius: ",
			"Node disp height: ",
			"Node disp space X: ",
			"",
			"Graph max zoom: ",
			"Graph min zoom: "
	};
	private final String[] LABEL_DESCRIPTIONS = new String[]{
			"Width of the main window, must restart to take full effect (int)",
			"Height of the main window, must restart to take full effect (int)",
			"Font used by the simulation, not all fonts look good (String)",
			"", 
			"The max number of times the simulation can zoom in (int)",
			"The min number of times the simulation can zoom in (int)",
			"The fastest the viewed simulation can run, 2^TickSpeed = time multiplier (int)",
			"The slowest the viewed simulation can run, 2^TickSpeed = time multiplier (int)",
			"The number of runners in a generation, max 500 (int)",
			"The number of units a tile in a track is, keep this number small (int)",
			"True if fitness lines give and take fitness, false to only take fitness (true/false)",
			"",
			"The max amount a runner can change their speed every 1/100 of a second (double)",
			"The max amount a runner can change their angle every 1/100 of a second (double)",
			"The slowest a runner can move every 1/100 of a second, negative speed moves backwards (double)",
			"The fastest a runner can move every 1/100 of a second, negative speed goes backwards (double)",
			"The radius of the runner (double)",
			"If true, the runner will stop moving and gaaining fitness when it hits a wall, if false it will collide with it (true/false)",
			"The maximum amount of time a runner has to gain fitness when it is tested in a generation (int)",
			"The maximum distance a runner can see in each direction of it's vision angles (double)",
			"The fitness gained or lost when corssing a fitness line in the correct or incorrect direction respectivly (double)",
			"The fitness gained every 1/100 of a second, effected by below factors (double)",
			"True if moving faster should grant more fitness per 1/100 of a second, false for speed to not effect it (true/false)",
			"True if being further from walls grants more fitness per 1/100 of a second, false for it to not effect it (true/false)",
			"8 ints that say how important each vision angle is for the above stat (ints)",
			"8 doubles that are the angles, in degrees, that the runner can see (doubles)",
			"A number of hidden node layers, followed by the number of nodes in each layer, higher numbers run slower (ints)",
			"",
			"Max value where half of this value is added or subtracted to a node connection weight during mutation (double)",
			"Min value where half of this value is added or subtracted to a node connection weight during mutation (double)",
			"Half of this value can be added or subtracted from mutability during a mutation (double)",
			"The x screen location to display the neural net when viewing a runner (int)",
			"The y screen location to display the neural net when viewing a runner (int)",
			"The radius of the circles which represent the nodes of the neural net when viewing a runner (int)",
			"The height of the displayed neural net when viewing a runner (int)",
			"The space between each node layer of a displayed neural net when viewing a runner (int)",
			"",
			"The max number of times a graph can zoom in (int)",
			"The min number of times a graph can zoom in (int)"
	};
	
	/**
	 * Create the object at the specified coordinates
	 * @param x
	 * @param y
	 */
	public SettingsTypingString(Settings settings, int x, int y){
		this.settings = settings;
		
		updateStrings();
		
		outputState = 0;
		outputStateTimer = 0;
		
		settingsIndex = -1;
		
		buttons = new MenuButton[LABEL_NAMES.length];
		for(int i = 0; i < buttons.length; i++){
			final int ii = i;
			buttons[i] = new MenuButton(x, y + 15 * i, 400, 15){
				
				@Override
				public void render(Graphics g){
					g.setFont(new Font(settings.getFontName(), Font.PLAIN, 13));
					if(mouseOn || settingsIndex == ii){
						if(settingsIndex == ii) g.setColor(new Color(127, 127, 127, 63));
						else g.setColor(new Color(127, 127, 127, 127));
						setWidth(g.getFontMetrics().stringWidth(getLabel(ii)));
						g.fillRect(getX(), getY() - 1, getWidth(), getHeight() + 2);
					}
					
					g.setColor(Color.BLACK);
					g.drawString(getLabel(ii), getX(), getY() + getHeight() - 2);

					if(mouseOn) MenuMain.renderHelpString(settings.getFontName(), 13, g, LABEL_DESCRIPTIONS[ii], getX(), getY());
				}
				public String getLabel(int index){
					if(index != settingsIndex) return LABEL_NAMES[index] + settingsStrings[index];
					else return LABEL_NAMES[index] + string;
				}
				@Override
				public void press(MouseEvent e){
					super.press(e);
					if(settingsIndex != ii){
						if(settingsIndex >= 0) settingsStrings[settingsIndex] = string;
						settingsIndex = ii;
						string = settingsStrings[ii];
					}
					else{
						settingsStrings[settingsIndex] = string;
						settingsIndex = -1;
					}
				}
				@Override
				public void tick(){
					super.tick();
					if(ii == 0){
						if(outputState != 0){
							if(outputStateTimer > 0){
								outputStateTimer--;
								if(outputStateTimer <= 0) outputState = 0;
							}
						}
					}
				}
			};
		}
	}
	
	/**
	 * updates the string labels
	 */
	public void updateStrings(){
		settingsStrings = getStrings();
	}
	
	/**
	 * Add the buttons in the class to the given menu
	 * @param m
	 */
	public void addToMenu(Menu m){
		for(MenuButton b : buttons){
			m.addComponent(b);
			m.addControled(b);
		}
	}

	/**
	 * remove the buttons in the class from the given menu
	 * @param m
	 */
	public void removeFromMenu(Menu m){
		for(MenuButton b : buttons){
			m.removeComponent(b);
			m.removeControled(b);
		}
	}
	
	private String[] getStrings(){
		String weights = "";
		for(int i = 0; i < 8; i++) weights += settings.getRunnerVisionWeights()[i] + " ";
		String angles = "";
		for(int i = 0; i < 8; i++) angles += settings.getRunnerVisionAngles()[i] + " ";
		String nodes = settings.getRunnerHiddenNodes().length + " ";
		for(int i = 0; i < settings.getRunnerHiddenNodes().length; i++) nodes += settings.getRunnerHiddenNodes()[i] + " ";
		
		String[] s = new String[]{
				"" + settings.getScreenWidth(), "" + settings.getScreenHeight(), "" + settings.getFontName(),
				"", 
				
				"" + settings.getSimMaxZoom(), "" + settings.getSimMinZoom(), "" + settings.getMaxTickSpeed(),
				"" + settings.getMinTickSpeed(), "" + settings.getNumRunners(), "" + settings.getTrackTileSize(), "" + Settings.boolToInt(settings.getFitnessLinesGive()),
				"", 
				
				"" + settings.getRunnerSpeedChange(), "" + settings.getRunnerAngleChange(), "" + settings.getRunnerMinSpeed(),
				"" + settings.getRunnerMaxSpeed(), "" + settings.getRunnerRadius(), "" +  Settings.boolToInt(settings.getKillRunner()),
				"" + settings.getRunnerTestTime(), "" + settings.getRunnerMaxViewDistance(), "" + settings.getRunnerFitnessGain(),
				"" + settings.getRunnerFitnessBaseGain(), "" + Settings.boolToInt(settings.getRunnerFitnessSpeed()),
				"" + Settings.boolToInt(settings.getRunnerFitnessWall()),
				weights, angles, nodes,
				"", 
				
				"" + settings.getMaxMutability(), "" + settings.getMinMutability(), "" + settings.getMutabilityChange(),
				"" + settings.getNodeDispBaseX(), "" + settings.getNodeDispBaseY(), "" + settings.getNodeDispRadius(),
				"" + settings.getNodeDispMaxHeight(), "" + settings.getNodeDispSpacing(), 
				"", 
				
				"" + settings.getGraphMaxZoom(), "" + settings.getGraphMinZoom()
			};
		return s;
	}
	
	@Override
	public void typeCharacter(KeyEvent e){
		if(settingsIndex == 2) super.typeCharacter(e);
		else{
			char c = e.getKeyChar();
			if(c == '.' || c == ' ' || c == '-' || c >= '0' && c <= '9') appendString(c);
		}
	}
	
	/**
	 * Get the current state of if the settings were set successfully or not. 
	 * 0 = display nothing, 
	 * 1 = settings set, 
	 * 2 = failed to set settings
	 * @return
	 */
	public int getOutputState(){
		return outputState;
	}
	
	@Override
	public void enter(){
		outputStateTimer = 200;
		try{
			if(settingsIndex >= 0) settingsStrings[settingsIndex] = string;
			
			settings.setScreenWidth(Integer.parseInt(settingsStrings[0]));
			settings.setScreenHeight(Integer.parseInt(settingsStrings[1]));
			settings.setFontName(settingsStrings[2]);
			
			settings.setSimMaxZoom(Integer.parseInt(settingsStrings[4]));
			settings.setSimMinZoom(Integer.parseInt(settingsStrings[5]));
			settings.setMaxTickSpeed(Integer.parseInt(settingsStrings[6]));
			settings.setMinTickSpeed(Integer.parseInt(settingsStrings[7]));
			settings.setNumRunners(Integer.parseInt(settingsStrings[8]));
			settings.setTrackTileSize(Integer.parseInt(settingsStrings[9]));
			settings.setFitnessLinesGive(settingsStrings[10].equals("1"));
			
			settings.setRunnerSpeedChange(Double.parseDouble(settingsStrings[12]));
			settings.setRunnerAngleChange(Double.parseDouble(settingsStrings[13]));
			settings.setRunnerMinSpeed(Double.parseDouble(settingsStrings[14]));
			settings.setRunnerMaxSpeed(Double.parseDouble(settingsStrings[15]));
			settings.setRunnerRadius(Double.parseDouble(settingsStrings[16]));
			settings.setKillRunner(settingsStrings[17].equals("1"));
			settings.setRunnerTestTime(Integer.parseInt(settingsStrings[18]));
			settings.setRunnerMaxViewDistance(Double.parseDouble(settingsStrings[19]));
			settings.setRunnerFitnessGain(Double.parseDouble(settingsStrings[20]));
			settings.setRunnerFitnessBaseGain(Double.parseDouble(settingsStrings[21]));
			settings.setRunnerFitnessSpeed(settingsStrings[22].equals("1"));
			settings.setRunnerFitnessWall(settingsStrings[23].equals("1"));
			settings.setRunnerVisionWeights(new int[8]);
			String str = settingsStrings[24] + " ";
			for(int i = 0; i < 8; i++){
				String s = str.substring(0, str.indexOf(' '));
				str = str.substring(str.indexOf(' ') + 1);
				settings.getRunnerVisionWeights()[i] = Integer.parseInt(s);
			}
			settings.setRunnerVisionAngles(new double[8]);
			str = settingsStrings[25] + " ";
			for(int i = 0; i < 8; i++){
				String s = str.substring(0, str.indexOf(' '));
				str = str.substring(str.indexOf(' ') + 1);
				settings.getRunnerVisionAngles()[i] = Double.parseDouble(s);
			}
			str = settingsStrings[26] + " ";
			settings.setRunnerHiddenNodes(new int[Integer.parseInt(str.substring(0, str.indexOf(' ')))]);
			str = str.substring(str.indexOf(' ') + 1);
			for(int i = 0; i < settings.getRunnerHiddenNodes().length; i++){
				String s = str.substring(0, str.indexOf(' '));
				str = str.substring(str.indexOf(' ') + 1);
				settings.getRunnerHiddenNodes()[i] = Integer.parseInt(s);
			}

			settings.setMaxMutability(Double.parseDouble(settingsStrings[28]));
			settings.setMinMutability(Double.parseDouble(settingsStrings[29]));
			settings.setMutabilityChange(Double.parseDouble(settingsStrings[30]));
			settings.setNodeDispBaseX(Integer.parseInt(settingsStrings[31]));
			settings.setNodeDispBaseY(Integer.parseInt(settingsStrings[32]));
			settings.setNodeDispRadius(Integer.parseInt(settingsStrings[33]));
			settings.setNodeDispMaxHeight(Integer.parseInt(settingsStrings[34]));
			settings.setNodeDispSpacing(Integer.parseInt(settingsStrings[35]));

			settings.setGraphMaxZoom(Integer.parseInt(settingsStrings[37]));
			settings.setGraphMinZoom(Integer.parseInt(settingsStrings[38]));
			
			try{
				PrintWriter write = new PrintWriter(new File("./data/settings.txt"));
				settings.save(write);
				write.close();
			}catch(FileNotFoundException e){}

			try{
				Scanner scan = new Scanner(new File("./data/settings.txt"));
				settings.load(scan);
				scan.close();
			}catch(FileNotFoundException e){}
			
			settingsStrings = getStrings();
			
		}catch(Exception e){
			outputState = 2;
			return;
		}
		outputState = 1;
		settingsIndex = -1;
	}
	
	@Override
	public void escape(){}
	
	public void drawHelpInfo(Graphics g, int x, int y, String fontName){
		g.setColor(Color.BLACK);
		g.setFont(new Font(fontName, Font.BOLD, 20));
		g.drawString("Settings:", x, y);
		g.setFont(new Font(fontName, Font.BOLD, 16));
		g.drawString("Click a setting to type and change it.", x, 755);
		g.drawString("Press enter to apply changes.", x, 775);
		g.drawString("If all settings are valid, they will be changed and the text", x, 795);
		g.drawString("\"Settings changed successfully\" will be displayed.", x, 815);
		g.drawString("If any setting is invalid, then the text", x, 835);
		g.drawString("\"Invalid settings\" will be displayed.", x, 855);
		g.drawString("For true or false values, type a 1 for true, and a 0 for false.", x, 875);
		g.drawString("Hover over a setting to get a more detailed description of it", x, 895);
		
		g.setFont(new Font(fontName, Font.BOLD, 30));
		int outputState = getOutputState();
		if(outputState == 1) g.drawString("Settings changed successfully", x, 685);
		else if(outputState == 2) g.drawString("Invalid settings", x, 685);
	}
	
}
