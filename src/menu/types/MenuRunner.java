package menu.types;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import data.Settings;
import menu.Main;
import menu.component.MenuButton;
import menu.input.InputControl;
import sim.NeuralNet;
import sim.Runner;
import sim.Track;

public class MenuRunner extends Menu{

	/**
	 * The runner to display and update in this menu
	 */
	private Runner currentRunner;
	/**
	 * The runners to display and update in this menu
	 */
	private Runner[] currentRunnerSet;
	/**
	 * True if one runner should be used, false if the entire set should be used
	 */
	private boolean useOneRunner;
	
	
	private Track loadedTrack;
	/**
	 * The position of the camera
	 */
	private Point2D.Double cameraPos;
	/**
	 * The coordinates on the screen that should be zoomed into
	 */
	private Point2D.Double zoomPos;
	/**
	 * True if the camera should center on the runner at all times
	 */
	private boolean focusOnRunner;
	/**
	 * The number that is put into a function that is multiplied by the width and height of the track size, -10 is fully zoomed in, 10 is fully zoome din
	 */
	private double cameraScale;
	
	/**
	 * The number of ticks the runner has been in the track for
	 */
	private int timer;
	/**
	 * The current tick speed, used as powers of 2
	 */
	private int tickSpeed;
	/**
	 * The number of ticks that have passed without an update happening
	 */
	private double tickCounter;
	/**
	 * True if the sim should not update
	 */
	private boolean paused;
	/**
	 * The longest time the runner has lived, only increases when the runner is not dead
	 */
	private int deadTime;
	
	/**
	 * true if the help text should be shown
	 */
	private boolean showHelp;
	
	/**
	 * An object that the neural net of the runneer uses to keep track of which lines should be displayed
	 */
	private InputControl neuralNetDetection;
	
	public MenuRunner(Main instance, Runner r){
		super(instance);

		currentRunner = r;

		setCurrentRunner(currentRunner);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		
		useOneRunner = true;
		
		focusOnRunner = true;
		showHelp = false;
		timer = 0;
		deadTime = 0;
		
		tickSpeed = 0;
		tickCounter = 0;
		paused = false;
		
		if(currentRunner != null){
			cameraPos = currentRunner.getPos();
			alignCamera();
		}
		else cameraPos = new Point2D.Double(0, 0);
		cameraScale = 0;
		
		zoomPos = new Point2D.Double(Settings.DEFAULT_SCREEN_WIDTH / 2, Settings.DEFAULT_SCREEN_HEIGHT / 2);
		
		//exit back to stats button
		MenuButton exitButton = new MenuButton(10, 30, 100, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Exit", getX() + 10, getY() + 22);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				instance.setSelectedMenu(instance.getRunStatsMenu());
			}
		};
		addComponent(exitButton);
		addControled(exitButton);
		
		//toggle focus on runner button
		MenuButton focusRunner = new MenuButton(120, 30, 200, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				if(focusOnRunner) g.drawString("Runner Centered", getX() + 10, getY() + 22);
				else g.drawString("User Controled", getX() + 10, getY() + 22);
				
				//draw zoom factor based on this buttons position
				String s = "Zoom: " + getScale();
				g.setColor(new Color(255, 255, 255, 127));
				g.fillRect(getX() + getWidth() + 5, getY(), g.getFontMetrics().stringWidth(s) + 5, 30);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString(s, getX() + getWidth() + 7, getY() + 20);
				
				//draw timer based on this buttons position
				s = "Timer: " + timer / 100.0;
				g.setColor(new Color(255, 255, 255, 127));
				g.fillRect(getX() + getWidth() + 5, getY() + 35, g.getFontMetrics().stringWidth(s) + 5, 30);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString(s, getX() + getWidth() + 7, getY() + 55);
				
				if(!useOneRunner) return;
				
				//draw dead time based on this buttons position
				if(currentRunner != null){
					if(currentRunner.dead()) s = "Died at: " + deadTime / 100.0;
					else s = "Still alive at: " + deadTime / 100.0;
					g.setColor(new Color(255, 255, 255, 127));
					g.fillRect(getX() + getWidth() + 5, getY() + 70, g.getFontMetrics().stringWidth(s) + 5, 30);
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
					g.drawString(s, getX() + getWidth() + 7, getY() + 95);
					
					//draw the fitness of the runner based on this buttons position
					s = "Fitness: " + currentRunner.getFitness();
					g.setColor(new Color(255, 255, 255, 127));
					g.fillRect(getX() + getWidth() + 5, getY() + 105, g.getFontMetrics().stringWidth(s) + 5, 30);
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
					g.drawString(s, getX() + getWidth() + 7, getY() + 130);
				}
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				focusOnRunner = !focusOnRunner;
			}
		};
		addComponent(focusRunner);
		addControled(focusRunner);
		
		//button for changing tick speed
		MenuButton tickSpeedButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 260, 30, 250, 30){
			private static final int LEFT_BOUND = 30;
			private static final int RIGHT_BOUND = 220;
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("-", getX() + 8, getY() + 20);
				g.drawString("+", getX() + RIGHT_BOUND + 8, getY() + 20);
				g.drawString("Speed: x" + new Double(Math.pow(2, tickSpeed)).toString(), getX() + LEFT_BOUND + 4, getY() + 20);
				
				g.fillRect(getX() + LEFT_BOUND, getY(), 2, getHeight());
				g.fillRect(getX() + RIGHT_BOUND, getY(), 2, getHeight());
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(e.getX() <= getX() + LEFT_BOUND) tickSpeed--;
				if(e.getX() >= getX() + RIGHT_BOUND) tickSpeed++;
				tickSpeed = Math.max(tickSpeed, instance.getMainSettings().getMinTickSpeed());
				tickSpeed = Math.min(tickSpeed, instance.getMainSettings().getMaxTickSpeed());
			}
		};
		addComponent(tickSpeedButton);
		addControled(tickSpeedButton);
		
		//pause button
		MenuButton pauseButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 260, 65, 250, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				if(paused) g.drawString("Click to unpause", getX() + 8, getY() + 20);
				else g.drawString("Click to pause", getX() + 8, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				paused = !paused;
			}
		};
		addComponent(pauseButton);
		addControled(pauseButton);
		
		//show help button
		MenuButton helpButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 260, 100, 250, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				if(showHelp) g.drawString("Hide help", getX() + 8, getY() + 20);
				else g.drawString("Show help", getX() + 8, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				showHelp = !showHelp;
			}
		};
		addComponent(helpButton);
		addControled(helpButton);
		
		//object to detect scroll where input and pan camera
		InputControl scrollWheelDetect = new InputControl(){
			private MouseAdapter mouseInput;
			@Override
			public void unlink(Component c){
				c.removeMouseWheelListener(mouseInput);
				c.removeMouseMotionListener(mouseInput);
				c.removeMouseListener(mouseInput);
			}
			@Override
			public void link(Component c){
				createControl();
				c.addMouseWheelListener(mouseInput);
				c.addMouseMotionListener(mouseInput);
				c.addMouseListener(mouseInput);
			}
			@Override
			public void createControl(){
				mouseInput = new MouseAdapter(){
					private boolean anchored = false;
					private Point2D.Double anchor = new Point2D.Double(0, 0);
					
					@Override
					public void mousePressed(MouseEvent e){
						super.mousePressed(e);
						if(e.getButton() == MouseEvent.BUTTON3 && !focusOnRunner){
							anchored = true;
							anchor = new Point2D.Double(e.getX() - cameraPos.x, e.getY() - cameraPos.y);
						}
					}
					@Override
					public void mouseReleased(MouseEvent e){
						super.mouseReleased(e);
						anchored = false;
					}
					@Override
					public void mouseDragged(MouseEvent e){
						super.mouseDragged(e);
						if(anchored){
							cameraPos = new Point2D.Double(e.getX() - anchor.x, e.getY() - anchor.y);
							keepCameraInBounds();
						}
						zoomPos = new Point2D.Double(e.getX(), e.getY());
					}
					
					@Override
					public void mouseMoved(MouseEvent e){
						super.mouseMoved(e);
						zoomPos = new Point2D.Double(e.getX(), e.getY());
					}
					
					@Override
					public void mouseWheelMoved(MouseWheelEvent e){
						super.mouseWheelMoved(e);
						double percX = (zoomPos.x - cameraPos.x) / (getScale());
						double percY = (zoomPos.y - cameraPos.y) / (getScale());
						
						if(e.getWheelRotation() > 0) cameraScale--;
						if(e.getWheelRotation() < 0) cameraScale++;
						cameraScale = Math.min(cameraScale, instance.getMainSettings().getSimMaxZoom());
						cameraScale = Math.max(cameraScale, instance.getMainSettings().getSimMinZoom());
						//recenter the camera based on zoomPos
						if(!focusOnRunner) cameraPos = new Point2D.Double(
								zoomPos.x - percX * getScale(),
								zoomPos.y - percY * getScale());
						else alignCamera();
						keepCameraInBounds();
					}
				};
			}
		};
		addControled(scrollWheelDetect);
	}
	
	public void setCurrentRunner(Runner r){
		if(r == null) return;
		
		useOneRunner = true;
		
		currentRunner = r;
		
		resetMenu();
		neuralNetDetection = new InputControl(){
			private MouseAdapter mouseInput;
			
			@Override
			public void unlink(Component c){
				c.removeMouseListener(mouseInput);
				c.removeMouseMotionListener(mouseInput);
				c.removeMouseWheelListener(mouseInput);
			}
			@Override
			public void link(Component c){
				c.addMouseListener(mouseInput);
				c.addMouseMotionListener(mouseInput);
				c.addMouseWheelListener(mouseInput);
			}
			@Override
			public void createControl(){
				mouseInput = new MouseAdapter(){
					@Override
					public void mouseClicked(MouseEvent e){
						super.mouseClicked(e);
						if(on(e)){
							currentRunner.getBrain().setDisplayAllLines(!currentRunner.getBrain().getDisplayAllLines());
							currentRunner.getBrain().drawNetLines(e.getX(), e.getY()); 
						}
					}
					@Override
					public void mouseMoved(MouseEvent e){
						super.mouseMoved(e);
						if(on(e)) currentRunner.getBrain().drawNetLines(e.getX(), e.getY()); 
					}
					@Override
					public void mouseDragged(MouseEvent e){
						super.mouseDragged(e);
						if(on(e)) currentRunner.getBrain().drawNetLines(e.getX(), e.getY()); 
					}
				};
			}
			public boolean on(MouseEvent e){
				return new Rectangle(
						currentRunner.getBrain().getDisplayDrawPoint().x,
						currentRunner.getBrain().getDisplayDrawPoint().y, currentRunner.getBrain().getNetLines().getWidth(),
						currentRunner.getBrain().getNetLines().getHeight()).contains(e.getX(), e.getY());
			}
		};
		neuralNetDetection.createControl();
		addControled(neuralNetDetection);
		currentRunner.getBrain().drawNetLines();
		
		zoomPos = new Point2D.Double(Settings.DEFAULT_SCREEN_WIDTH / 2, Settings.DEFAULT_SCREEN_HEIGHT / 2);
		cameraScale = 0;
		focusOnRunner = true;
		
		timer = 0;
		deadTime = 0;
		tickSpeed = 0;
		tickCounter = 0;
		paused = false;
		
		loadedTrack.drawImage();
		loadedTrack.enterRunner(currentRunner);
		currentRunner.getBrain().setDisplayAllLines(true);
		
		cameraPos = currentRunner.getPos();
		alignCamera();
	}
	
	/**
	 * Set the array of runners to the given array
	 * @param r
	 */
	public void setRunners(Runner[] r){
		resetMenu();

		cameraScale = 0;
		timer = 0;
		deadTime = 0;
		tickSpeed = 0;
		tickCounter = 0;
		
		setCurrentRunner(r[0]);
		currentRunnerSet = r;
		
		focusOnRunner = false;

		for(Runner rr : r){
			loadedTrack.enterRunner(rr);
			rr.getBrain().eraseNetLines();
		}
		
		useOneRunner = false;
	}
	
	public Track getTrack(){
		return loadedTrack;
	}
	
	public void setTrack(Track t){
		loadedTrack = t;
	}
	
	@Override
	public void tick(){
		super.tick();
		if(paused) return;
		double t = Math.pow(2, tickSpeed);
		//if the sim should be updated several times, update it the correct number of times
		if(t >= 1){
			for(int i = 0; i < t; i++){
				update();
			}
		}
		//otherwise, wait until the tick counter has gotten to 1
		else{
			//update it if it is above 1 and reset the counter
			if(tickCounter >= 1){
				tickCounter = 0;
				update();
			}
			//otherwise increase the counter by the sped
			else tickCounter += t;
		}
	}
	
	/**
	 * This is the code that ticks the simulation outside of the menu ticks
	 */
	private void update(){
		timer++;
		if(currentRunner != null){
			
			if(!currentRunner.dead()) deadTime = timer;
			
			if(useOneRunner){
				currentRunner.tick();
				loadedTrack.collideWithRunner(currentRunner);
			}
			else for(Runner r : currentRunnerSet){
				loadedTrack.collideWithRunner(r);
				r.tick();
			}
			
			if(focusOnRunner){
				zoomPos = new Point2D.Double(Settings.DEFAULT_SCREEN_WIDTH / 2, Settings.DEFAULT_SCREEN_HEIGHT / 2);
				alignCamera();
			}
		}
	}
	
	@Override
	public void render(Graphics g){
		double scale = getScale();
		
		BufferedImage trackImg;

		if(useOneRunner) trackImg = loadedTrack.getTrackImageWithRunner(currentRunner);
		else trackImg = loadedTrack.getTrackImageWithRunners(currentRunnerSet);
		
		int imgW = trackImg.getWidth();
		int imgH = trackImg.getHeight();
		
		g.drawImage(trackImg,
				(int)Math.round(cameraPos.x),
				(int)Math.round(cameraPos.y),
				(int)Math.round(imgW * scale),
				(int)Math.round(imgH * scale), null);
		
		if(currentRunner != null && useOneRunner){
			BufferedImage img = currentRunner.getBrain().getDisplay();
			g.setColor(Color.WHITE);
			int x = instance.getMainSettings().getNodeDispBaseX();
			int y = instance.getMainSettings().getNodeDispBaseY();
			currentRunner.getBrain().setDisplayDrawPoint(x, y);
			g.fillRect(x, y, img.getWidth(), img.getHeight());
			g.drawImage(img, x, y, null);
			
			//labels for net
			//inputs
			g.setColor(Color.BLACK);
			g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
			int i;
			double height = (img.getHeight() - NeuralNet.NET_LOC + instance.getMainSettings().getNodeDispRadius() * 2) / 11.0;
			for(i = 0; i < 8; i++) g.drawString("Vision: " + (i + 1), x + 2, y + 10 + (int)Math.round(i * height));
			g.drawString("Speed", x + 2, y + 10 + (int)Math.round(i * height));
			g.drawString("Angle", x + 2, y + 10 + (int)Math.round((i + 1) * height));
			
			//outputs
			g.drawString("Goal Speed", x + img.getWidth() - 20, y + 30);
			g.drawString("Goal Angle", x + img.getWidth() - 20, y + img.getHeight() - 30);
		}
		
		super.render(g);
		
		//draw help info
		if(showHelp){
			//general help
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Click the \"Runner Centered\" button to manually control camera", 950, 30);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Scroll mouse wheel to zoom in and out", 950, 54);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Right click to pan camera", 950, 78);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Left click the neural net on the left to toggle node display,", 950, 102);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "    All nodes connections are shown by default, the other option only", 950, 126);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "    shows nodes connections to nodes the mouse is hovoring over", 950, 150);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Change speed to simulate that many times faster or slower", 950, 174);
			
			//details in uppper left
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Current zoom level", 620, 30);
			if(useOneRunner){
				MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "# of seconds the simulation has ran", 620, 65);
				MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "# of seconds the runner has lived for", 620, 100);
				MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "current fitness of the runner", 620, 135);
			}
		}
	}
	
	/**
	 * The scaler used for resizing
	 * @return
	 */
	private double getScale(){
		return Math.pow(Math.E, cameraScale * .15);
	}
	
	/**
	 * Aligns the camera to be centered on the runner
	 */
	public void alignCamera(){
		cameraPos = new Point2D.Double(
				Settings.DEFAULT_SCREEN_WIDTH / 2 - (loadedTrack.getXOffset() + currentRunner.getX()) * getScale(),
				Settings.DEFAULT_SCREEN_HEIGHT / 2 - (loadedTrack.getYOffset() + currentRunner.getY()) * getScale());
	}
	
	/**
	 * Ensures that the camera does not go too far from view of the track
	 */
	private void keepCameraInBounds(){
		double w = loadedTrack.getTrackImage().getWidth() * getScale();
		double h = loadedTrack.getTrackImage().getHeight() * getScale();
		cameraPos.x = Math.max(-w + Settings.DEFAULT_SCREEN_WIDTH * .1, cameraPos.x);
		cameraPos.x = Math.min(Settings.DEFAULT_SCREEN_WIDTH * .9, cameraPos.x);
		cameraPos.y = Math.max(-h + Settings.DEFAULT_SCREEN_HEIGHT * .1, cameraPos.y);
		cameraPos.y = Math.min(Settings.DEFAULT_SCREEN_HEIGHT * .9, cameraPos.y);
	}
	
}
