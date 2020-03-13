package menu.types;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;

import data.SavesLoader;
import data.Settings;
import menu.Main;
import menu.component.MenuButton;
import menu.input.InputControl;
import menu.input.TypingStringInput;
import sim.Track;

public class TrackEditor extends Menu{
	
	private static final String[] TOOL_NAMES = new String[]{"1: Runner", "2: Wall", "3: Fitness Line", "4: Move", "5: Move Point", "6: Swap", "7: Delete"};
	
	private Track editedTrack;
	
	private Point2D.Double cameraPos;
	private int scale;
	/**
	 * represents the first place the user clicked for actions that require 2 clicks
	 */
	private Point2D.Double basePoint;
	
	/**
	 * The index of the line that is going to be moved
	 */
	private int moveLineIndex;
	/**
	 * The point that was initially clicked on to move a fitness line
	 */
	private Point2D.Double movePoint;
	
	/**
	 * The point, on the track, of a line that will be moved, will be null if no point is slected
	 */
	private Point2D.Double moveLinePoint;
	
	/**
	 * the last point that the mouse was at
	 */
	private Point2D.Double lastMousePoint;
	
	/**
	 * True to snap the coordinates you click on to the closest coordinate on the grid
	 */
	private boolean coordinateSnap;
	
	/**
	 * The current tool that the user has selected. 
	 * 0 = set runner position; 
	 * 1 = place wall, first click to place point 1, second click to place other point; 
	 * 2 = place fitness line, first click to place point 1, second click to place other point; 
	 * 3 = move, move a line close to where you clicked, works for walls and fitness lines; 
	 * 4 = move point, move a point on a line close to where you clicked works for walls and fitness lines; 
	 * 5 = swap, swaps the direction of a fitness lines; 
	 * 6 = delete, removes a wall or fitness line; 
	 */
	private int selectedTool;
	
	/**
	 * the object that tracks key input for typing in the name of the export name
	 */
	private TypingStringInput exportKeyInput;
	
	/**
	 * true if extra help information should be shown
	 */
	private boolean showHelp;
	
	public TrackEditor(Main instance){
		super(instance);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		
		coordinateSnap = true;
		
		scale = 0;
		
		editedTrack = new Track(instance.getMainSettings(), new Point2D.Double(300, 300));
		
		basePoint = null;
		
		lastMousePoint = new Point2D.Double(0, 0);
		
		showHelp = false;
		
		//help button
		MenuButton helpButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 145, Settings.DEFAULT_SCREEN_HEIGHT - 135, 140, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 25));
				if(showHelp) g.drawString("Hide Help", getX() + 6, getY() + getHeight() - 8);
				else g.drawString("Show Help", getX() + 6, getY() + getHeight() - 8);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				showHelp = !showHelp;
			}
		};
		addComponent(helpButton);
		addControled(helpButton);
		
		//exit button
		MenuButton exitButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 145, Settings.DEFAULT_SCREEN_HEIGHT - 45, 140, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 30));
				g.drawString("Exit", getX() + 6, getY() + getHeight() - 8);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				SavesLoader.scanFiles();
				instance.setSelectedMenu(instance.getMainMenu());
			}
		};
		addComponent(exitButton);
		addControled(exitButton);
		
		//button for exporting
		MenuButton exportButton = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 145, Settings.DEFAULT_SCREEN_HEIGHT - 90, 140, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 30));
				g.drawString("Export", getX() + 6, getY() + getHeight() - 8);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!exportKeyInput.on()){
					exportKeyInput.setOn(true);
					basePoint = null;
					movePoint = null;
					moveLinePoint = null;
					moveLineIndex = 0;
				}
			}
		};
		addComponent(exportButton);
		addControled(exportButton);
		
		//button for toggling coordinate snapping
		MenuButton snapButton = new MenuButton(4, 264, 100, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 14));
				if(coordinateSnap) g.drawString("Snapping", getX() + 6, getY() + getHeight() - 8);
				else g.drawString("No snap", getX() + 6, getY() + getHeight() - 8);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!exportKeyInput.on()) coordinateSnap = !coordinateSnap;
			}
		};
		addComponent(snapButton);
		addControled(snapButton);
		
		//buttons for selecting tools
		for(int i = 0; i < 7; i++){
			final int ii = i;
			MenuButton button = new MenuButton(4, 40 + i * 32, 100, 30){
				@Override
				public void render(Graphics g){
					super.render(g);
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 14));
					g.drawString(TOOL_NAMES[ii], getX() + 4, getY() + getHeight() - 8);
				}
				@Override
				public void press(MouseEvent e){
					super.press(e);
					if(!exportKeyInput.on()){
						selectedTool = ii;
						basePoint = null;
						movePoint = null;
						moveLinePoint = null;
						moveLineIndex = 0;
					}
				}
			};
			addComponent(button);
			addControled(button);
		}
		
		//object for key input of typing
		exportKeyInput = new TypingStringInput(){
			@Override
			public void enter(){
				if(exportKeyInput.getString().length() != 0){
					try{
						PrintWriter write = new PrintWriter(new File("./data/tracks/" + exportKeyInput.getString() + ".txt"));
						
						Point2D.Double p = editedTrack.getStartingPoint();
						editedTrack.setStartingPoint(new Point2D.Double(p.x - editedTrack.getXOffset(), p.y - editedTrack.getYOffset()));
						
						editedTrack.save(write);
						
						editedTrack.setStartingPoint(p);
						
						write.close();
					}catch(Exception e1){}
				}
				super.enter();
			}
		};
		addControled(exportKeyInput);
		
		//object for zooming, panning around, and tracking key input for setting which control is selected
		InputControl pannAndZoom = new InputControl(){
			
			private MouseAdapter mouseInput;
			private KeyAdapter keyInput;
			
			@Override
			public void unlink(Component c){
				c.removeMouseListener(mouseInput);
				c.removeMouseMotionListener(mouseInput);
				c.removeMouseWheelListener(mouseInput);
				c.removeKeyListener(keyInput);
			}
			
			@Override
			public void link(Component c){
				createControl();
				c.addMouseListener(mouseInput);
				c.addMouseMotionListener(mouseInput);
				c.addMouseWheelListener(mouseInput);
				c.addKeyListener(keyInput);
			}
			
			@Override
			public void createControl(){
				mouseInput = new MouseAdapter(){
					private Point2D.Double anchor = new Point2D.Double(0, 0);
					private boolean anchored = false;
					
					@Override
					public void mousePressed(MouseEvent e){
						super.mousePressed(e);
						if(!exportKeyInput.on()){
							//code for panning
							if(!anchored && e.getButton() == MouseEvent.BUTTON3){
								anchored = true;
								anchor = new Point2D.Double(e.getX() - cameraPos.x, e.getY() - cameraPos.y);
							}
						}
					}
					@Override
					public void mouseReleased(MouseEvent e){
						super.mouseReleased(e);
						if(!exportKeyInput.on()){
							//code for panning
							if(e.getButton() == MouseEvent.BUTTON3) anchored = false;
							
							//code for placing things
							if(e.getButton() == MouseEvent.BUTTON1 &&
							   !(e.getX() < 104 && e.getY() < 294) &&
							   !(e.getX() > Settings.DEFAULT_SCREEN_WIDTH - 145 &&
							   e.getY() > Settings.DEFAULT_SCREEN_HEIGHT - 135)){
								
								//get coordinates for placing things
								double placeX = (e.getX() - cameraPos.x - editedTrack.getXOffset()) / getScale();
								double placeY = (e.getY() - cameraPos.y - editedTrack.getYOffset()) / getScale();
								
								if(coordinateSnap && selectedTool != 6){
									double factor = (instance.getMainSettings().getTrackTileSize() / 4);
									placeX = Math.round(placeX / factor) * factor;
									placeY = Math.round(placeY / factor) * factor;
								}
								
								//place the runner tool
								if(selectedTool == 0){
									if(basePoint == null) basePoint = new Point2D.Double(placeX, placeY);
									else{
										double oX = editedTrack.getXOffset();
										double oY = editedTrack.getYOffset();
										editedTrack.setStartingPoint(basePoint);
										editedTrack.setStartingAngle(Math.toDegrees(Math.atan2((placeY - oY) - (basePoint.y - oY), (placeX - oX) - (basePoint.x - oX))));
										basePoint = null;
									}
								}
								
								//place a track line took
								else if(selectedTool == 1 || selectedTool == 2){
									if(basePoint == null) basePoint = new Point2D.Double(placeX, placeY);
									else{
										double oX = editedTrack.getXOffset();
										double oY = editedTrack.getYOffset();
										if(selectedTool == 1) editedTrack.addTrackLine(new Line2D.Double(placeX - oX, placeY - oY, basePoint.x - oX, basePoint.y - oY));
										else editedTrack.addFitnessLine(new Line2D.Double(placeX - oX, placeY - oY, basePoint.x - oX, basePoint.y - oY), true);
										
										editedTrack.drawImage();
										cameraPos.x -= (editedTrack.getXOffset() - oX) * (1 + getScale());
										cameraPos.y -= (editedTrack.getYOffset() - oY) * (1 + getScale());
										
										editedTrack.setStartingPoint(new Point2D.Double(
																	 editedTrack.getStartingPoint().x - (oX - editedTrack.getXOffset()),
																	 editedTrack.getStartingPoint().y - (oY - editedTrack.getYOffset())));
										keepInRange();
										basePoint = null;
									}
								}
								//move a line
								else if(selectedTool == 3){
									//save current offset
									double oX = editedTrack.getXOffset();
									double oY = editedTrack.getYOffset();
									
									//move the fitness or track line
									//if no line is selected to move, select a line
									if(moveLineIndex == 0){
										moveLineIndex = editedTrack.getClosestLineIndex(placeX - oX, placeY - oY, 10);
										movePoint = new Point2D.Double(placeX - oX, placeY - oY);
									}
									//if a line is selected, move the line to the position clicked
									else{
										if(moveLineIndex > 0){
											editedTrack.addLinePos(
												placeX - editedTrack.getXOffset() - movePoint.x,
												placeY - editedTrack.getYOffset() - movePoint.y,
												moveLineIndex - 1, true);
										}
										else{
											editedTrack.addLinePos(
												placeX - editedTrack.getXOffset() - movePoint.x,
												placeY - editedTrack.getYOffset() - movePoint.y,
												-moveLineIndex - 1, false);
										}
										moveLineIndex = 0;
									}
									
									//update the camera based on the new offset
									editedTrack.drawImage();
									cameraPos.x -= (editedTrack.getXOffset() - oX) * (1 + getScale());
									cameraPos.y -= (editedTrack.getYOffset() - oY) * (1 + getScale());
									
									editedTrack.setStartingPoint(new Point2D.Double(
																 editedTrack.getStartingPoint().x - (oX - editedTrack.getXOffset()),
																 editedTrack.getStartingPoint().y - (oY - editedTrack.getYOffset())));
									keepInRange();
								}
								//move a point on a line
								else if(selectedTool == 4){
									//save current offset
									double oX = editedTrack.getXOffset();
									double oY = editedTrack.getYOffset();
									
									//if no point is selected, select the point that will be moved
									if(moveLinePoint == null){
										Point2D.Double closestP = editedTrack.getClosestEndPoint(placeX - oX, placeY - oY, 10);
										if(closestP != null) moveLinePoint = closestP;
									}
									//otherwise move the selected point to where the mouse was clicked
									else{
										editedTrack.setClosestLineLoc(moveLinePoint.x, moveLinePoint.y, placeX - oX, placeY - oY, 10);
										moveLinePoint = null;
									}
									
									//update the camera based on the new offset
									editedTrack.drawImage();
									cameraPos.x -= (editedTrack.getXOffset() - oX) * (1 + getScale());
									cameraPos.y -= (editedTrack.getYOffset() - oY) * (1 + getScale());
									
									editedTrack.setStartingPoint(new Point2D.Double(
																 editedTrack.getStartingPoint().x - (oX - editedTrack.getXOffset()),
																 editedTrack.getStartingPoint().y - (oY - editedTrack.getYOffset())));
								}
								//swap a fitness line
								else if(selectedTool == 5){
									editedTrack.swapFitnessLine(placeX - editedTrack.getXOffset(), placeY - editedTrack.getYOffset(), 10);
									editedTrack.drawImage();
								}
								//remove a line
								else if(selectedTool == 6){
									editedTrack.removeLine(placeX - editedTrack.getXOffset(), placeY - editedTrack.getYOffset(), 10);
									double oX = editedTrack.getXOffset();
									double oY = editedTrack.getYOffset();
									
									editedTrack.drawImage();
									cameraPos.x -= (editedTrack.getXOffset() - oX) * (1 + getScale());
									cameraPos.y -= (editedTrack.getYOffset() - oY) * (1 + getScale());
									
									editedTrack.setStartingPoint(new Point2D.Double(
																 editedTrack.getStartingPoint().x - (oX - editedTrack.getXOffset()),
																 editedTrack.getStartingPoint().y - (oY - editedTrack.getYOffset())));
									keepInRange();
								}
							}
						}
					}
					
					@Override
					public void mouseDragged(MouseEvent e){
						super.mouseDragged(e);
						if(!exportKeyInput.on()){
							//code for panning
							if(anchored){
								cameraPos = new Point2D.Double(e.getX() - anchor.x, e.getY() - anchor.y);
								keepInRange();
								editedTrack.drawImage();
							}
							lastMousePoint = new Point2D.Double(e.getX(), e.getY());
						}
					}
					@Override
					public void mouseMoved(MouseEvent e){
						super.mouseMoved(e);
						if(!exportKeyInput.on()){
							lastMousePoint = new Point2D.Double(e.getX(), e.getY());
						}
					}
					
					@Override
					public void mouseWheelMoved(MouseWheelEvent e){
						super.mouseWheelMoved(e);
						
						if(!exportKeyInput.on()){
							//code for zooming
							double percX = (e.getX() - editedTrack.getXOffset() - cameraPos.x) / (getScale());
							double percY = (e.getY() - editedTrack.getYOffset() - cameraPos.y) / (getScale());
							
							if(e.getWheelRotation() > 0) scale--;
							else if(e.getWheelRotation() < 0) scale++;
							scale = Math.max(instance.getMainSettings().getSimMinZoom(), scale);
							scale = Math.min(instance.getMainSettings().getSimMaxZoom(), scale);
							
							cameraPos = new Point2D.Double(e.getX() - editedTrack.getXOffset() - percX * getScale(),
														   e.getY() - editedTrack.getYOffset() - percY * getScale());
							keepInRange();
							editedTrack.drawImage();
						}
					}
				};
				
				keyInput = new KeyAdapter(){
					@Override
					public void keyPressed(KeyEvent e){
						super.keyPressed(e);
						//used for keyboard shortcuts for the tools
						if(!exportKeyInput.on()){
							boolean change = false;
							if(e.getKeyCode() == KeyEvent.VK_1){
								selectedTool = 0;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_2){
								selectedTool = 1;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_3){
								selectedTool = 2;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_4){
								selectedTool = 3;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_5){
								selectedTool = 4;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_6){
								selectedTool = 5;
								change = true;
							}
							else if(e.getKeyCode() == KeyEvent.VK_7){
								selectedTool = 6;
								change = true;
							}
							if(change){
								basePoint = null;
								movePoint = null;
								moveLinePoint = null;
								moveLineIndex = 0;
							}
						}
					}
				};
			}
		};
		addControled(pannAndZoom);
		
		cameraPos = new Point2D.Double(Settings.DEFAULT_SCREEN_WIDTH / 2 - editedTrack.getXOffset() * 2,
				Settings.DEFAULT_SCREEN_HEIGHT / 2 - editedTrack.getYOffset() * 2);
	}

	/**
	 * Set the current track for this menu based on the given trackName that will be loaded in
	 * @param t
	 */
	public void setTrack(String trackName){
		editedTrack.load(trackName);
		Point2D.Double p = editedTrack.getStartingPoint();
		editedTrack.setStartingPoint(new Point2D.Double(p.x + editedTrack.getXOffset(), p.y + editedTrack.getYOffset()));
		editedTrack.drawImage();
		cameraPos = new Point2D.Double(
				-editedTrack.getTrackImage().getWidth() * .5,
				-editedTrack.getTrackImage().getHeight() * .5);
	}
	
	@Override
	public void render(Graphics g){
		double x = cameraPos.x + editedTrack.getXOffset();
		double y = cameraPos.y + editedTrack.getYOffset();
		
		//draw track
		BufferedImage img = editedTrack.getTrackImage();
		double s = getScale();
		g.drawImage(img, (int)Math.round(x),
						 (int)Math.round(y),
						 (int)Math.round(img.getWidth() * s),
						 (int)Math.round(img.getHeight() * s), null);
		
		//draw runner position
		g.setColor(Color.BLACK);
		double r = instance.getMainSettings().getRunnerRadius() * s;
		g.fillOval((int)Math.round(x + editedTrack.getStartingPoint().x * s - r),
				   (int)Math.round(y + editedTrack.getStartingPoint().y * s - r),
				   (int)Math.round(r * 2), (int)Math.round(r * 2));
		g.setColor(new Color(100, 100, 255));
		g.fillOval((int)Math.round(x + editedTrack.getStartingPoint().x * s - r + 2),
				   (int)Math.round(y + editedTrack.getStartingPoint().y * s - r + 2),
				   (int)Math.round(r * 2 - 4), (int)Math.round(r * 2 - 4));
		//draw the direction of the runner
		g.setColor(Color.BLACK);
		g.drawLine((int)Math.round(x + editedTrack.getStartingPoint().x * s),
				   (int)Math.round(y + editedTrack.getStartingPoint().y * s),
				   (int)Math.round(x + editedTrack.getStartingPoint().x * s + s * Math.cos(Math.toRadians(editedTrack.getStartingAngle())) * instance.getMainSettings().getRunnerRadius()),
				   (int)Math.round(y + editedTrack.getStartingPoint().y * s + s * Math.sin(Math.toRadians(editedTrack.getStartingAngle())) * instance.getMainSettings().getRunnerRadius()));
		
		//draw dot showing where a line will be drawn
		if(basePoint != null){
			r = 7 * getScale();
			g.setColor(new Color(0, 0, 100));
			g.fillOval((int)Math.round(x + basePoint.x * getScale() - r), (int)Math.round(y + basePoint.y * getScale() - r), (int)Math.round(r * 2), (int)Math.round(r * 2));
		}
		
		//draw line showing where a line will be moved to
		if(moveLineIndex != 0){
			Line2D.Double l;
			if(moveLineIndex > 0){
				g.setColor(new Color(100, 100, 100));
				l = editedTrack.getTrackLine(moveLineIndex - 1);
			}
			else{
				g.setColor(new Color(100, 0, 0));
				l = editedTrack.getFitnessLine(-moveLineIndex - 1);
			}
			Graphics2D g2 = (Graphics2D)g;
			g2.setStroke(new BasicStroke(3f));
			g2.drawLine((int)Math.round(lastMousePoint.x - (movePoint.x - l.x1) * getScale()), (int)Math.round(lastMousePoint.y - (movePoint.y - l.y1) * getScale()),
					   (int)Math.round(lastMousePoint.x - (movePoint.x - l.x2) * getScale()), (int)Math.round(lastMousePoint.y - (movePoint.y - l.y2) * getScale()));
		}
		
		//draw points showing where the moved points will go
		if(moveLinePoint != null){
			r = 7 * getScale();
			g.setColor(new Color(100, 0, 0));
			//draw point on line
			g.fillOval((int)Math.round(x + (moveLinePoint.x + editedTrack.getXOffset()) * getScale() - r),
					   (int)Math.round(y + (moveLinePoint.y + editedTrack.getYOffset()) * getScale() - r),
					   (int)Math.round(r * 2), (int)Math.round(r * 2));
			
			//draw new point where mouse is
			g.fillOval((int)Math.round(lastMousePoint.x - r), (int)Math.round(lastMousePoint.y - r), (int)Math.round(r * 2), (int)Math.round(r * 2));
		}
		
		//draw buttons
		super.render(g);
		
		//draw extra details
		g.setColor(Color.BLACK);
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 25));
		g.drawString("Selected Tool: " + TOOL_NAMES[selectedTool], 120, 70);
		g.drawString("Zoom: " + getScale(), 120, 90);
		
		//draw help stuff
		if(showHelp){
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Click the above buttons, or the corresponding # key to use that tool.", 10, 300);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Runner: place the center point of the runner, then the angle of the runner", 10, 324);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Wall: place a wall, click to place the first point of a line, then click again to place the second point", 10, 348);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Fitness line: same process as wall, but places a fitness line", 10, 372);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Move: click on a line to select it, then click again to place it", 10, 396);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Move Point: click the end point of a line to select it, then again to move it", 10, 420);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Swap: click on a fitness line to change the direction it faces", 10, 444);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Delete: remove a line from the track", 10, 468);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Snapping = clicks move to the nearest 4th of a tile, no snapping = clicks are the exact location", 10, 492);
			
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Change tools to cancel an action", 10, 540);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Right click and drag to pan camera", 10, 564);
			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Scroll mouse wheel to zoom in and out", 10, 588);

			MenuMain.renderHelpString(instance.getMainSettings().getFontName(), g, "Click export to save the track", 10, 626);
		}
		
		//draw stuff from exporting
		if(exportKeyInput.on()){
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Settings.DEFAULT_SCREEN_WIDTH, Settings.DEFAULT_SCREEN_HEIGHT);
			g.setColor(Color.BLACK);
			//instructions
			g.setFont(new Font(Settings.DEFAULT_FONT_NAME, Font.PLAIN, 50));
			g.drawString("Press ESC to cancel exporting", 450, 100);
			g.drawString("Press ENTER to export", 450, 160);
			
			//draw the file name
			g.setFont(new Font(Settings.DEFAULT_FONT_NAME, Font.PLAIN, 30));
			g.drawString("File name: " + exportKeyInput.getString(), 450, 200);
		}
	}
	
	/**
	 * keeps the camera in range of the screen
	 */
	public void keepInRange(){
		double w = editedTrack.getTrackImage().getWidth() * getScale();
		double h = editedTrack.getTrackImage().getHeight() * getScale();
		cameraPos.x = Math.max(-w * .9 - editedTrack.getXOffset(), cameraPos.x);
		cameraPos.x = Math.min(Settings.DEFAULT_SCREEN_WIDTH - w * .1 - editedTrack.getXOffset(), cameraPos.x);
		cameraPos.y = Math.max(-h * .9 - editedTrack.getYOffset(), cameraPos.y);
		cameraPos.y = Math.min(Settings.DEFAULT_SCREEN_HEIGHT - h * .1 - editedTrack.getYOffset(), cameraPos.y);
	}

	/**
	 * The scaler used for resizing based on the scale
	 * @return
	 */
	private double getScale(){
		return Math.pow(Math.E, scale * .15);
	}
}
