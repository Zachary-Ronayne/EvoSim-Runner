package menu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;

import data.SavesLoader;
import data.Settings;
import menu.component.Screen;
import menu.types.Menu;
import menu.types.MenuChangeSettings;
import menu.types.MenuGraphs;
import menu.types.MenuMain;
import menu.types.MenuRunner;
import menu.types.MenuTrackSelect;
import menu.types.TrackEditor;
import menu.types.MenuRunStats;
import sim.Runner;
import sim.Simulation;

public class Main implements Runnable{
	
	private static Main instance;
	
	/**
	 * True if the program is currently running, false otherwise
	 */
	private boolean running;
	/**
	 * The screen for the main menu of the simulation
	 */
	private Screen mainScreen;
	/**
	 * The main frame for this simulation
	 */
	private JFrame frame;
	/**
	 * The main thread that runs the simulation
	 */
	private Thread mainThread;
	
	/**
	 * List of all menus
	 */
	private ArrayList<Menu> menus;
	/**
	 * selected menu to update and render
	 */
	private Menu selectedMenu;
	//all of the individual menu objects
	private MenuMain mainMenu;
	private MenuRunStats runStastMenu;
	private MenuGraphs graphsMenu;
	private MenuTrackSelect trackSelectMenu;
	private MenuChangeSettings changeSettingsMenu;
	private MenuRunner runnerMenu;
	private TrackEditor trackEditor;
	
	/**
	 * The main simulation that is being run with this program
	 */
	private Simulation mainSim;
	
	/**
	 * The main settings of the current simulation
	 */
	private Settings mainSettings;
	
	/**
	 * Constructor for main class
	 */
	public Main(){
		mainSettings = new Settings(){
			@Override
			public void setScreenWidth(int screenWidth){
				int old = getScreenWidth();
				super.setScreenWidth(screenWidth);
				if(old != getScreenWidth()) updateFrame();
			}
			@Override
			public void setScreenHeight(int screenHeight){
				int old = getScreenHeight();
				super.setScreenHeight(screenHeight);
				if(old != getScreenHeight()) updateFrame();
			}
		};
	}
	
	/**
	 * Updates the frame to the new size based on the settings
	 */
	private void updateFrame(){
		if(frame != null){
			frame.setSize(mainSettings.getScreenWidth(), mainSettings.getScreenHeight());
			for(Menu m : menus) m.resetMenu();
			frame.setLocationRelativeTo(null);
		}
	}
	
	public static void main(String[] args){
		instance = new Main();
		
		SavesLoader.scanFiles();
		try{
			Scanner scan = new Scanner(new File("./data/settings.txt"));
			instance.mainSettings.load(scan);
			scan.close();
		}catch(FileNotFoundException e){
			instance.mainSettings.loadDefaults();
		}
		
		instance.mainThread = new Thread(instance);
		instance.mainThread.start();
	}
	
	/**
	 * Save the current settings and simulation in a new file with the given name. Do not include .txt, it is added automatically
	 * @param name
	 */
	public void save(String name){
		SavesLoader.scanFiles();
		try{
			PrintWriter write = new PrintWriter(new File("./data/saves/" + name + ".txt"));
			mainSim.save(write);
			write.close();
		}catch(FileNotFoundException e){}
	}
	
	/**
	 * Load the simulation from give save file name and track name. Use null for trackName to use the default track
	 * @param fileName
	 * @param trackName
	 */
	public void load(String fileName, String trackName){
		mainSim = new Simulation(mainSettings, trackName);
		runStastMenu.resetMenu();
		
		try{
			Scanner scan = new Scanner(new File("./data/saves/" + fileName));
			mainSim.load(scan);
			scan.close();
			changeSettingsMenu.resetMenu();
			
		}catch(FileNotFoundException e){
			System.err.println("Failed to load sim, creating new simulation instead");
			e.printStackTrace();
			newSimulation(trackName);
		}
	}

	/**
	 * Start a new simulation, creating and testing an initial generation 0
	 */
	public void newSimulation(String trackName){
		mainSim = new Simulation(mainSettings, trackName);
		mainSim.newSimulation();
		runStastMenu.resetMenu();
	}
	
	public MenuMain getMainMenu(){
		return mainMenu;
	}
	public MenuGraphs getGraphsMenu(){
		return graphsMenu;
	}
	public MenuRunStats getRunStatsMenu(){
		return runStastMenu;
	}
	public MenuTrackSelect getTrackSelectMenu(){
		return trackSelectMenu;
	}
	public MenuChangeSettings getChangeSettingsMenu(){
		return changeSettingsMenu;
	}
	public MenuRunner getRunnnerMenu(){
		return runnerMenu;
	}
	public TrackEditor getTrackEditor(){
		return trackEditor;
	}
	
	/**
	 * Set the selected menu to the given menu, unlinks all other menus
	 * @param m
	 */
	public void setSelectedMenu(Menu m){
		for(Menu mm : menus) mm.unlinkFromComponent(frame);
		for(KeyListener l : frame.getKeyListeners()) frame.removeKeyListener(l);
		for(MouseListener l : frame.getMouseListeners()) frame.removeMouseListener(l);
		for(MouseMotionListener l : frame.getMouseMotionListeners()) frame.removeMouseMotionListener(l);
		for(MouseWheelListener l : frame.getMouseWheelListeners()) frame.removeMouseWheelListener(l);
		
		m.linkToComponent(frame);
		selectedMenu = m;
	}
	public Menu getSelectedMenu(){
		return selectedMenu;
	}
	
	/**
	 * Watch this runner go around the current track
	 * @param r
	 */
	public void watchRunner(Runner r){
		runnerMenu.setCurrentRunner(r);
		setSelectedMenu(runnerMenu);
	}
	/**
	 * Watch this array of runnesr go around the current track
	 * @param r
	 */
	public void watchRunners(Runner[] r){
		runnerMenu.setRunners(r);
		setSelectedMenu(runnerMenu);
	}
	
	public Simulation getMainSim(){
		return mainSim;
	}
	public Settings getMainSettings(){
		return mainSettings;
	}
	
	/**
	 * Initialize the main program to its default state
	 */
	private void init(){
		running = true;
		
		/**
		 * Set the mainScreen to a new object and define how it will be rendered
		 */
		mainScreen = new Screen(0, 0, Settings.DEFAULT_SCREEN_WIDTH, Settings.DEFAULT_SCREEN_WIDTH){
			@Override
			public void drawScreen(Graphics g){
				super.drawScreen(g);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, Settings.DEFAULT_SCREEN_WIDTH, Settings.DEFAULT_SCREEN_WIDTH);
				if(selectedMenu != null) selectedMenu.render(g);
			}
		};
		
		frame = new JFrame("Runner Evolution Simulation"){
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g){
				mainScreen.updateScreen();
				mainScreen.render(g);
			}
		};
		frame.setVisible(false);
		frame.setUndecorated(false);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		frame.setSize(mainSettings.getScreenWidth(), mainSettings.getScreenHeight());
		frame.setLocationRelativeTo(null);
		
		//menu creation
		menus = new ArrayList<Menu>();
		
		//main menu
		mainMenu = new MenuMain(this);
		menus.add(mainMenu);
		
		//stat menu
		runStastMenu = new MenuRunStats(this);
		menus.add(runStastMenu);
		
		//graphs menu
		graphsMenu = new MenuGraphs(this);
		menus.add(graphsMenu);
		
		//track select menu
		trackSelectMenu = new MenuTrackSelect(this);
		menus.add(trackSelectMenu);
		
		changeSettingsMenu = new MenuChangeSettings(this);
		menus.add(changeSettingsMenu);
		
		//runner menu
		runnerMenu = new MenuRunner(this, null);
		menus.add(runnerMenu);
		
		//track editor menu
		trackEditor = new TrackEditor(this);
		menus.add(trackEditor);
		
		//the default menu should be the main menu
		setSelectedMenu(mainMenu);
		
		//load in the default track
		SavesLoader.saveDefaultTrack(mainSettings);
	}
	
	/**
	 * Update the main program
	 */
	public void tick(){
		if(selectedMenu != null) selectedMenu.tick();
	}
	
	/**
	 * Render the main program on the screen. 
	 * To modify the rendering, go to the initialization of mainScreen, located in the init() method
	 */
	private void render(){
		frame.repaint();
	}
	
	/**
	 * Responsible for rendering and updating the main program
	 */
	@Override
	public void run(){
		init();
		long lastTime = System.nanoTime();
		final double numTicks = 100;
		final int nanoSecond = 1000000000;
		final double nanoTicks = nanoSecond / numTicks;
		double nanoTime = 0;
		int frames = 0;
		int ticks = 0;
		long timer = System.currentTimeMillis();

		long currentTime;
		
		long lastRenderTime = 0;
		boolean shouldRender = true;
		
		while(running){
			//general update
			currentTime = System.nanoTime();
			nanoTime += (currentTime - lastTime) / nanoTicks;
			lastTime = currentTime;
			
			//tick statements
			if(nanoTime >= 1){
				tick();
				ticks++;
				nanoTime--;
			}
			
			if(ticks > 101){
				nanoTime = 0;
			}
			
			//render statements
			lastRenderTime = lastTime;
			if(shouldRender){
				render();
				frames++;
			}
			lastRenderTime = System.nanoTime() - lastRenderTime;
			shouldRender = lastRenderTime < nanoTicks;
			
			//console output
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
				System.out.println(ticks + " Ticks\tFPS: " + frames);
				ticks = 0;
				frames = 0;
				shouldRender = true;
			}
		}
		stop();
	}
	

	public synchronized void stop(){
		if(!running) return;
		running = false;
		terminate();
	}
	
	public static void terminate(){
		System.out.println("Exit successful");
		System.exit(1);
		return;
	}
	
}
