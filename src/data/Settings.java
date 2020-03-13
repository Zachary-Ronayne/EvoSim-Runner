package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Settings{
	
	//general constants
	/**
	 * The width of the main window of the simulation, should not be less than 1800
	 */
	private int screenWidth;
	public static final int DEFAULT_SCREEN_WIDTH = 1800;
	/**
	 * The height of the main window of the simulation, should not be less than 1000
	 */
	private int screenHeight;
	public static final int DEFAULT_SCREEN_HEIGHT = 1000;
	/**
	 * The the font used by the simulation
	 */
	private String fontName;
	public static final String DEFAULT_FONT_NAME = "Arial";
	
	//simulation constants
	/**
	 * The most a viewed simulation can be zoomed in, 0 is normal scale, positive is zooming in, negative is zooming out
	 */
	private int simMaxZoom;
	public static final int DEFAULT_SIM_MAX_ZOOM = 10;
	/**
	 * The least a viewed simulation can be zoomed in, 0 is normal scale, positive is zooming in, negative is zooming out
	 */
	private int simMinZoom;
	public static final int DEFAULT_SIM_MIN_ZOOM = -10;
	/**
	 * Constant used for slowing down and speeding up the rate at which a runner moves when viewed by the user. 
	 * The maximum tickSpeed can be, 2 raised to the power of tick speed is the number of ticks to happen every 100th of a second
	 */
	private int maxTickSpeed;
	public static final int DEFAULT_MAX_TICK_SPEED = 12;
	/** 
	 * Constant used for slowing down and speeding up the rate at which a runner moves when viewed by the user. 
	 * The minimum tickSpeed can be, 2 raised to the power of tick speed is the number of ticks to happen every 100th of a second
	 */
	private int minTickSpeed;
	public static final int DEFAULT_MIN_TICK_SPEED = -5;
	/**
	 * The total number of runners in the simulation for each generation, not recommended to go over 500
	 */
	private int numRunners;
	public static final int DEFAULT_NUM_RUNNERS = 500;
	/**
	 * The size of a tile for the checkerboard pattern background of a track, used to help see how fast runners are moving
	 */
	private int trackTileSize;
	public static final int DEFAULT_TRACK_TILE_SIZE = 100;
	/**
	 * True if fitness lines should give fitness and remove fitness, false if they should only remove fitness
	 */
	private boolean fitnessLinesGive;
	public static final boolean DEFAULT_FITNESS_LINES_GIVE = true;
	
	//runner constants
	/**
	 * The maximum amount a runners speed can change in a single tick
	 */
	private double runnerSpeedChange;
	public static final double DEFAULT_RUNNER_SPEED_CHANGE = .01;
	/**
	 * The maximum amount a runners angle can change in a single tick
	 */
	private double runnerAngleChange;
	public static final double DEFAULT_RUNNER_ANGLE_CHANGE = 10;
	/**
	 * The minimum value that a runners speed can be, positive values go forward, negative values go in reverse
	 */
	private double runnerMinSpeed;
	public static final double DEFAULT_RUNNER_MIN_SPEED = -10;
	/**
	 * The maximum value that a runners speed can be, positive values go forward, negative values go in reverse
	 */
	private double runnerMaxSpeed;
	public static final double DEFAULT_RUNNER_MAX_SPEED = 20;
	/**
	 * The radius of the runner
	 */
	private double runnerRadius;
	public static final double DEFAULT_RUNNER_RADIUS = 20;
	/**
	 * True if a runner should die when it hits a wall, false if it should collide
	 */
	private boolean killRunner;
	public static final boolean DEFAULT_KILL_RUNNER = true;
	/**
	 * The max number of ticks the runners are tested for, 1 tick = 1/100 of a second
	 */
	private int runnerTestTime;
	public static final int DEFAULT_RUNNER_TEST_TIME = 2000;
	/**
	 * The maximum distance, from the edge of the runner, that they can see to a wall in a given direction
	 */
	private double runnerMaxViewDistance;
	public static final double DEFAULT_RUNNER_MAX_VIEW_DISTANCE = 600;
	/**
	 * The amount of fitness gained when moving over a fitness line in the correct direction, or the amount subtracted when moving over a fitness line in the wrong direction
	 */
	private double runnerFitnessGain;
	public static final double DEFAULT_RUNNER_FITNESS_GAIN = 1;
	/**
	 * The base amount of fitness that is added each tick of the simulation, 1 tick = 1/100th of a second. This value is multiplied by the absolute value of the percent of the maximum speed
	 * that the runner is currently moving (so a value between 0 and 1 inclusive) and the distance it is from walls based on the weights of the vision lines,
	 * both of these modifiers can be turned off with other variables
	 */
	private double runnerFitnessBaseGain;
	public static final double DEFAULT_RUNNER_FITNESS_BASE_GAIN = .01;
	/**
	 * True if the absolute value of the percent of the maximum speed that the runner is currently moving should be multiplied onto the fitness gained each tick. 
	 * If this value is true, then moving faster means gaining more fitness, and moving slowly means gaining substantially less fitness.
	 * This value is also set the the power of e before it is multiplied, meaning lower speeds reduce fitness gains very substantially, and moving at near top speed means
	 * almost the full fitness amount is gained
	 */
	private boolean runnerFitnessSpeed;
	public static final boolean DEFAULT_RUNNER_FITNESS_SPEED = true;
	/**
	 * True if the distance the runner detects on its vision lines should effect fitness gained, false otherwise. 
	 * If this value is true, then being further from walls means more fitness will be gained every tick. The importance of the distance is based on the vision weights, higher weights 
	 * mean that that vision line is more important, and will effect the multiplied number far more substantially, so if a vision line with a high weight is far from a wall, a much 
	 * high multiplier will be used. The end multiplier is always 0-1 inclusive
	 */
	private boolean runnerFitnessWall;
	public static final boolean DEFAULT_RUNNER_FITNESS_WALL = true;
	/**
	 * The weights that determine how important each vision line is for gaining fitness. The more weight a line is, the more fitness will be gained the further that line is from a wall. 
	 * There are always exactly 8 of these, and the angles are associated with these weights are defined by the RUNNER_VISION_ANGLES values. The index match up, so index 0 of 
	 * RUNNER_VISION_WEIGHTS is the weight used by index 0 of RUNNER_VISION_ANGLES, and so on for the remaining 7 indexes
	 */
	private int[] runnerVisionWeights;
	public static final int[] DEFAULT_RUNNER_VISION_WEIGHTS = new int[]{4, 3, 2, 1, 2, 1, 2, 3};
	/**
	 * The angles, in degrees used for calculating the distance the runner is from the wall. 0 degrees is directly in front of the runner, 180 is directly behind the runner, angles in between 
	 * go that many degrees away, treating the direction the runner is facing as angle 0
	 */
	private double[] runnerVisionAngles;
	public static final double[] DEFAULT_RUNNER_VISION_ANGLES = new double[]{0, 45, 90, 135, 180, 225, 270, 315};
	/**
	 * The amount of nodes in each hidden layer of the runners brain, excluding the constant node. This array can be empty, or can have as many nodes as desired. 
	 * More nodes and more layers means longer computation times and longer time to evolve, however it increases the potential for complex behavior. 
	 * There is very little reason to use more than 1 hidden layer, maybe 2 hidden layers.
	 */
	private int[] runnerHiddenNodes;
	public static final int[] DEFAULT_RUNNER_HIDDEN_NODES = new int[]{6};
	
	//neural net settings
	/**
	 * The maximum rate at which mutability can change, mutability is a range of how much a node value can change, so up to half of mutability can be added or subtracted during a mutation
	 */
	private double maxMutability;
	public static final double DEFAULT_MAX_MUTABILITY = 4;
	/**
	 * The minimum rate at which mutability can change, mutability is a range of how much a node value can change, so up to half of mutability can be added or subtracted during a mutation
	 */
	private double minMutability;
	public static final double DEFAULT_MIN_MUTABILITY = -4;
	/**
	 * The rate at which mutability can change. Mutability change is a range of how much mutability can change, so up to half the mutability change can be added or subtracted to the mutability
	 * when a mutation occurs. Use large values to allow mutability to change quickly and make large leaps, and use small values to make mutability change slowly ovr time
	 */
	private double mutabilityChange;
	public static final double DEFAULT_MUTABILITY_CHANGE = 1;
	
	/**
	 * The x position where the nodes are initially drawn on the display
	 */
	private int nodeDispBaseX;
	public static final int DEFAULT_NODE_DISP_BASE_X = 10;
	/**
	 * The y position where the nodes are initially drawn on the display
	 */
	private int nodeDispBaseY;
	public static final int DEFAULT_NODE_DISP_BASE_Y = 80;
	/**
	 * The radius of the circles that represent the nodes on the display
	 */
	private int nodeDispRadius;
	public static final int DEFAULT_NODE_DISP_RADIUS = 20;
	/**
	 * The maximum height for the box the nodes are drawn on the display
	 */
	private int nodeDispMaxHeight;
	public static final int DEFAULT_NODE_DISP_MAX_HEIGHT = 800;
	/**
	 * The distance on the x axis between node layers in the display
	 */
	private int nodeDispSpacing;
	public static final int DEFAULT_NODE_DISP_SPACING = 100;
	
	//graph settings
	/**
	 * The most a graph can be zoomed in, 0 is normal scale, positive is zooming in, negative is zooming out
	 */
	private int graphMaxZoom;
	public static final int DEFAULT_GRAPH_MAX_ZOOM = 20;
	/**
	 * The least a graph can be zoomed in, 0 is normal scale, positive is zooming in, negative is zooming out
	 */
	private int graphMinZoom;
	public static final int DEFAULT_GRAPH_MIN_ZOOM = -10;
	
	public Settings(){}
	
	/**
	 * Load all the default settings, and save those settings
	 */
	public void loadDefaults(){
		setScreenWidth(DEFAULT_SCREEN_WIDTH);
		setScreenHeight(DEFAULT_SCREEN_HEIGHT);
		setFontName(DEFAULT_FONT_NAME);
		
		setSimMaxZoom(DEFAULT_SIM_MAX_ZOOM);
		setSimMinZoom(DEFAULT_SIM_MIN_ZOOM);
		setMaxTickSpeed(DEFAULT_MAX_TICK_SPEED);
		setMinTickSpeed(DEFAULT_MIN_TICK_SPEED);
		setNumRunners(DEFAULT_NUM_RUNNERS);
		setTrackTileSize(DEFAULT_TRACK_TILE_SIZE);
		setFitnessLinesGive(DEFAULT_FITNESS_LINES_GIVE);
		
		setRunnerSpeedChange(DEFAULT_RUNNER_SPEED_CHANGE);
		setRunnerAngleChange(DEFAULT_RUNNER_ANGLE_CHANGE);
		setRunnerMinSpeed(DEFAULT_RUNNER_MIN_SPEED);
		setRunnerMaxSpeed(DEFAULT_RUNNER_MAX_SPEED);
		setRunnerRadius(DEFAULT_RUNNER_RADIUS);
		setKillRunner(DEFAULT_KILL_RUNNER);
		setRunnerTestTime(DEFAULT_RUNNER_TEST_TIME);
		setRunnerMaxViewDistance(DEFAULT_RUNNER_MAX_VIEW_DISTANCE);
		setRunnerFitnessGain(DEFAULT_RUNNER_FITNESS_GAIN);
		setRunnerFitnessBaseGain(DEFAULT_RUNNER_FITNESS_BASE_GAIN);
		setRunnerFitnessSpeed(DEFAULT_RUNNER_FITNESS_SPEED);
		setRunnerFitnessWall(DEFAULT_RUNNER_FITNESS_WALL);
		setRunnerVisionWeights(DEFAULT_RUNNER_VISION_WEIGHTS);
		setRunnerVisionAngles(DEFAULT_RUNNER_VISION_ANGLES);
		setRunnerHiddenNodes(DEFAULT_RUNNER_HIDDEN_NODES);
		
		setMaxMutability(DEFAULT_MAX_MUTABILITY);
		setMinMutability(DEFAULT_MIN_MUTABILITY);
		setMutabilityChange(DEFAULT_MUTABILITY_CHANGE);
		setNodeDispBaseX(DEFAULT_NODE_DISP_BASE_X);
		setNodeDispBaseY(DEFAULT_NODE_DISP_BASE_Y);
		setNodeDispRadius(DEFAULT_NODE_DISP_RADIUS);
		setNodeDispMaxHeight(DEFAULT_NODE_DISP_MAX_HEIGHT);
		setNodeDispSpacing(DEFAULT_NODE_DISP_SPACING);
		
		setGraphMaxZoom(DEFAULT_GRAPH_MAX_ZOOM);
		setGraphMinZoom(DEFAULT_GRAPH_MIN_ZOOM);
		
		try{
			PrintWriter write = new PrintWriter(new File("./data/settings.txt"));
			save(write);
			write.close();
		}catch(FileNotFoundException e){}
	}
	
	/**
	 * Save the settings of the current simulation to the given print writer
	 * @param write
	 */
	public void save(PrintWriter write){
		write.println("WindowWidth: " + getScreenWidth());
		write.println("WindowHeight: " + getScreenHeight());
		write.println("Font: " + getFontName());
		write.println();
		write.println("MaxZoomSim: " + getSimMaxZoom());
		write.println("MinZoomSim: " + getSimMinZoom());
		write.println("MaxTickSpeed: " + getMaxTickSpeed());
		write.println("MintickSpeed: " + getMinTickSpeed());
		write.println("NumRunners: " + getNumRunners());
		write.println("TrackTileSize: " + getTrackTileSize());
		write.println("FitnessLinesGive: " + boolToInt(getFitnessLinesGive()));
		write.println();
		write.println("RunnerSpeedChange: " + getRunnerSpeedChange());
		write.println("RunnerAngleChange: " + getRunnerAngleChange());
		write.println("RunnerMinSpeed: " + getRunnerMinSpeed());
		write.println("RunnerMaxSpeed: " + getRunnerMaxSpeed());
		write.println("RunnerRadius: " + getRunnerRadius());
		write.println("KillRunner: " + boolToInt(getKillRunner()));
		write.println("RunnerTestTime: " + getRunnerTestTime());
		write.println("RunnerViewDistance: " + getRunnerMaxViewDistance());
		write.println("FitnessPerLine: " + getRunnerFitnessGain());
		write.println("FitnessPerTick: " + getRunnerFitnessBaseGain());
		write.println("SpeedMultFitness: " + boolToInt(getRunnerFitnessSpeed()));
		write.println("WallMultFitness: " + boolToInt(getRunnerFitnessWall()));
		write.print("RunnerVisionWeights: ");
		for(int i = 0; i < 8; i++) write.print(getRunnerVisionWeights()[i] + " ");
		write.println();
		write.print("RunnerVisionAngles: ");
		for(int i = 0; i < 8; i++) write.print(getRunnerVisionAngles()[i] + " ");
		write.println();
		write.print("RunnerNumHiddenNodes: " + getRunnerHiddenNodes().length + " ");
		for(int i = 0; i < getRunnerHiddenNodes().length; i++) write.print(getRunnerHiddenNodes()[i] + " ");
		write.println();
		write.println();
		write.println("MaxMutability: " + getMaxMutability());
		write.println("MinMutability: " + getMinMutability());
		write.println("MutabilityChange: " + getMutabilityChange());
		write.println("NodeLocX: " + getNodeDispBaseX());
		write.println("NodeLocY: " + getNodeDispBaseY());
		write.println("NodeDispRadius: " + getNodeDispRadius());
		write.println("NodeDispHeight: " + getNodeDispMaxHeight());
		write.println("NodeDispSpcaeX: " + getNodeDispSpacing());
		write.println();
		write.println("GraphMaxZoom: " + getGraphMaxZoom());
		write.println("GraphMinZoom: " + getGraphMinZoom());
	}
	
	/**
	 * Load in the settings from the given scanner
	 * @param scan
	 */
	public void load(Scanner scan){
		try{
			scan.next(); setScreenWidth(scan.nextInt());
			scan.next(); setScreenHeight(scan.nextInt());
			scan.next(); setFontName(scan.nextLine().substring(1));
			
			scan.next(); setSimMaxZoom(scan.nextInt());
			scan.next(); setSimMinZoom(scan.nextInt());
			scan.next(); setMaxTickSpeed(scan.nextInt());
			scan.next(); setMinTickSpeed(scan.nextInt());
			scan.next(); setNumRunners(scan.nextInt());
			scan.next(); setTrackTileSize(scan.nextInt());
			scan.next(); setFitnessLinesGive(scan.nextInt() == 1);
			
			scan.next(); setRunnerSpeedChange(scan.nextDouble());
			scan.next(); setRunnerAngleChange(scan.nextDouble());
			scan.next(); setRunnerMinSpeed(scan.nextDouble());
			scan.next(); setRunnerMaxSpeed(scan.nextDouble());
			scan.next(); setRunnerRadius(scan.nextDouble());
			scan.next(); setKillRunner(scan.nextInt() == 1);
			scan.next(); setRunnerTestTime(scan.nextInt());
			scan.next(); setRunnerMaxViewDistance(scan.nextDouble());
			scan.next(); setRunnerFitnessGain(scan.nextDouble());
			scan.next(); setRunnerFitnessBaseGain(scan.nextDouble());
			scan.next(); setRunnerFitnessSpeed(scan.nextInt() == 1);
			scan.next(); setRunnerFitnessWall(scan.nextInt() == 1);
			scan.next(); setRunnerVisionWeights(new int[8]);
			for(int i = 0; i < 8; i++) getRunnerVisionWeights()[i] = scan.nextInt();
			scan.next(); setRunnerVisionAngles(new double[8]);
			for(int i = 0; i < 8; i++) getRunnerVisionAngles()[i] = scan.nextDouble();
			scan.next(); setRunnerHiddenNodes(new int[scan.nextInt()]);
			for(int i = 0; i < getRunnerHiddenNodes().length; i++) getRunnerHiddenNodes()[i] = scan.nextInt();
			
			scan.next(); setMaxMutability(scan.nextDouble());
			scan.next(); setMinMutability(scan.nextDouble());
			scan.next(); setMutabilityChange(scan.nextDouble());
			scan.next(); setNodeDispBaseX(scan.nextInt());
			scan.next(); setNodeDispBaseY(scan.nextInt());
			scan.next(); setNodeDispRadius(scan.nextInt());
			scan.next(); setNodeDispMaxHeight(scan.nextInt());
			scan.next(); setNodeDispSpacing(scan.nextInt());
			
			scan.next(); setGraphMaxZoom(scan.nextInt());
			scan.next(); setGraphMinZoom(scan.nextInt());
		}catch(Exception e){
			loadDefaults();
		}
	}
	
	public int getScreenWidth(){
		return screenWidth;
	}
	public int getScreenHeight(){
		return screenHeight;
	}
	public String getFontName(){
		return fontName;
	}
	public int getSimMaxZoom(){
		return simMaxZoom;
	}
	public int getSimMinZoom(){
		return simMinZoom;
	}
	public int getMaxTickSpeed(){
		return maxTickSpeed;
	}
	public int getMinTickSpeed(){
		return minTickSpeed;
	}
	public int getNumRunners(){
		return numRunners;
	}
	public int getTrackTileSize(){
		return trackTileSize;
	}
	public boolean getFitnessLinesGive(){
		return fitnessLinesGive;
	}
	public double getRunnerSpeedChange(){
		return runnerSpeedChange;
	}
	public double getRunnerAngleChange(){
		return runnerAngleChange;
	}
	public double getRunnerMinSpeed(){
		return runnerMinSpeed;
	}
	public double getRunnerMaxSpeed(){
		return runnerMaxSpeed;
	}
	public double getRunnerRadius(){
		return runnerRadius;
	}
	public boolean getKillRunner(){
		return killRunner;
	}
	public int getRunnerTestTime(){
		return runnerTestTime;
	}
	public double getRunnerMaxViewDistance(){
		return runnerMaxViewDistance;
	}
	public double getRunnerFitnessGain(){
		return runnerFitnessGain;
	}
	public double getRunnerFitnessBaseGain(){
		return runnerFitnessBaseGain;
	}
	public boolean getRunnerFitnessSpeed(){
		return runnerFitnessSpeed;
	}
	public boolean getRunnerFitnessWall(){
		return runnerFitnessWall;
	}
	public int[] getRunnerVisionWeights(){
		return runnerVisionWeights;
	}
	public double[] getRunnerVisionAngles(){
		return runnerVisionAngles;
	}
	public int[] getRunnerHiddenNodes(){
		return runnerHiddenNodes;
	}
	public double getMaxMutability(){
		return maxMutability;
	}
	public double getMinMutability(){
		return minMutability;
	}
	public double getMutabilityChange(){
		return mutabilityChange;
	}
	public int getNodeDispBaseX(){
		return nodeDispBaseX;
	}
	public int getNodeDispBaseY(){
		return nodeDispBaseY;
	}
	public int getNodeDispRadius(){
		return nodeDispRadius;
	}
	public int getNodeDispMaxHeight(){
		return nodeDispMaxHeight;
	}
	public int getNodeDispSpacing(){
		return nodeDispSpacing;
	}
	public int getGraphMaxZoom(){
		return graphMaxZoom;
	}
	public int getGraphMinZoom(){
		return graphMinZoom;
	}
	
	public void setScreenWidth(int screenWidth){
		this.screenWidth = screenWidth;
	}
	public void setScreenHeight(int screenHeight){
		this.screenHeight = screenHeight;
	}
	public void setFontName(String fontName){
		this.fontName = fontName;
	}
	public void setSimMaxZoom(int simMaxZoom){
		this.simMaxZoom = simMaxZoom;
	}
	public void setSimMinZoom(int simMinZoom){
		this.simMinZoom = simMinZoom;
	}
	public void setMaxTickSpeed(int maxTickSpeed){
		this.maxTickSpeed = maxTickSpeed;
	}
	public void setMinTickSpeed(int minTickSpeed){
		this.minTickSpeed = minTickSpeed;
	}
	public void setNumRunners(int numRunners){
		this.numRunners = numRunners;
	}
	public void setTrackTileSize(int trackTileSize){
		this.trackTileSize = trackTileSize;
	}
	public void setFitnessLinesGive(boolean fitnessLinesGive){
		this.fitnessLinesGive = fitnessLinesGive;
	}
	public void setRunnerSpeedChange(double runnerSpeedChange){
		this.runnerSpeedChange = runnerSpeedChange;
	}
	public void setRunnerAngleChange(double runnerAngleChange){
		this.runnerAngleChange = runnerAngleChange;
	}
	public void setRunnerMinSpeed(double runnerMinSpeed){
		this.runnerMinSpeed = runnerMinSpeed;
	}
	public void setRunnerMaxSpeed(double runnerMaxSpeed){
		this.runnerMaxSpeed = runnerMaxSpeed;
	}
	public void setRunnerRadius(double runnerRadius){
		this.runnerRadius = runnerRadius;
	}
	public void setKillRunner(boolean killRunner){
		this.killRunner = killRunner;
	}
	public void setRunnerTestTime(int runnerTestTime){
		this.runnerTestTime = runnerTestTime;
	}
	public void setRunnerMaxViewDistance(double runnerMaxViewDistance){
		this.runnerMaxViewDistance = runnerMaxViewDistance;
	}
	public void setRunnerFitnessGain(double runnerFitnessGain){
		this.runnerFitnessGain = runnerFitnessGain;
	}
	public void setRunnerFitnessBaseGain(double runnerFitnessBaseGain){
		this.runnerFitnessBaseGain = runnerFitnessBaseGain;
	}
	public void setRunnerFitnessSpeed(boolean runnerFitnessSpeed){
		this.runnerFitnessSpeed = runnerFitnessSpeed;
	}
	public void setRunnerFitnessWall(boolean runnerFitnessWall){
		this.runnerFitnessWall = runnerFitnessWall;
	}
	public void setRunnerVisionWeights(int[] runnerVisionWeights){
		this.runnerVisionWeights = runnerVisionWeights;
	}
	public void setRunnerVisionAngles(double[] runnerVisionAngles){
		this.runnerVisionAngles = runnerVisionAngles;
	}
	public void setRunnerHiddenNodes(int[] runnerHiddenNodes){
		this.runnerHiddenNodes = runnerHiddenNodes;
	}
	public void setMaxMutability(double maxMutability){
		this.maxMutability = maxMutability;
	}
	public void setMinMutability(double minMutability){
		this.minMutability = minMutability;
	}
	public void setMutabilityChange(double mutabilityChange){
		this.mutabilityChange = mutabilityChange;
	}
	public void setNodeDispBaseX(int nodeDispBaseX){
		this.nodeDispBaseX = nodeDispBaseX;
	}
	public void setNodeDispBaseY(int nodeDispBaseY){
		this.nodeDispBaseY = nodeDispBaseY;
	}
	public void setNodeDispRadius(int nodeDispRadius){
		this.nodeDispRadius = nodeDispRadius;
	}
	public void setNodeDispMaxHeight(int nodeDispMaxHeight){
		this.nodeDispMaxHeight = nodeDispMaxHeight;
	}
	public void setNodeDispSpacing(int nodeDispSpacing){
		this.nodeDispSpacing = nodeDispSpacing;
	}
	public void setGraphMaxZoom(int graphMaxZoom){
		this.graphMaxZoom = graphMaxZoom;
	}
	public void setGraphMinZoom(int graphMinZoom){
		this.graphMinZoom = graphMinZoom;
	}
	
	/**
	 * if b is true, return 1, if b is false, return 0
	 * @param b
	 * @return
	 */
	public static int boolToInt(boolean b){
		if(b) return 1;
		else return 0;
	}
}
