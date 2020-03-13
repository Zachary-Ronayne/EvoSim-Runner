package menu.types;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import data.SavesLoader;
import data.Settings;
import menu.Main;
import menu.component.HorizontalScroller;
import menu.component.MenuButton;
import menu.input.InputControl;
import menu.input.TypingStringInput;
import sim.NeuralNet;
import sim.Runner;

public class MenuRunStats extends Menu{
	
	public static final int GRID_X = 10;
	public static final int GRID_Y = 90;
	
	private boolean loopingGens;
	
	/**
	 * The index in the runner array that is selected to view, this number is -1 if no runner is selected. 
	 * selectedRunner >= 0 : a runner from this gen is selected. 
	 * selectedRunner == -1 : no runner is selected. 
	 * selectedRunner == -2 : the worst runner of the selected generation from the history is selected. 
	 * selectedRunner == -3 : the median runner of the selected generation from the history is selected. 
	 * selectedRunner == -4 : the best runner of the selected generation from the history is selected. 
	 */
	private int selectedRunner;
	
	/**
	 * The selected generation to display the best, median, and worst runners
	 */
	private int selectedHistoryGen;
	
	private HorizontalScroller historySelector;
	
	public MenuRunStats(Main instance){
		super(instance);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		
		selectedHistoryGen = 0;
		
		loopingGens = false;
		
		addTabButtons(this, instance);
		
		MenuButton viewRunnerB = new MenuButton(10, Settings.DEFAULT_SCREEN_HEIGHT - 100, 150, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("View Runner", getX() + 10, getY() + 22);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(selectedRunner >= -4 && selectedRunner != -1){
					//ensure that no other runners have mouse controls the runner menu
					for(int i = 0; i < instance.getMainSettings().getNumRunners(); i++){
						NeuralNet n = instance.getMainSim().getRunner(i).getBrain();
						n.eraseNetLines();
					}
					for(int i = 0; i < instance.getMainSim().getRunnerHistory().size(); i++){
						for(int j = 0; j < 3; j++){
							NeuralNet n = instance.getMainSim().getRunnerHistory().get(i)[j].getBrain();
							n.eraseNetLines();
						}
					}
					
					instance.getRunnnerMenu().setTrack(instance.getMainSim().getTrack());
					Runner r;
					if(selectedRunner >= 0) r = instance.getMainSim().getRunner(selectedRunner);
					else r = instance.getMainSim().getRunnerHistory().get(selectedHistoryGen)[-selectedRunner - 2];
					r.getBrain().drawNetLines();
					instance.watchRunner(r);
					loopingGens = false;
				}
			}
		};
		addComponent(viewRunnerB);
		addControled(viewRunnerB);
		
		//click this button to view the entire generation
		MenuButton viewAllRunnersB = new MenuButton(165, Settings.DEFAULT_SCREEN_HEIGHT - 100, 150, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 18));
				g.drawString("View Entire Gen", getX() + 10, getY() + 22);
			}
			
			@Override
			public void press(MouseEvent e){
			super.press(e);
				//ensure that no other runners have mouse controls the runner menu
				for(int i = 0; i < instance.getMainSettings().getNumRunners(); i++){
					NeuralNet n = instance.getMainSim().getRunner(i).getBrain();
					n.eraseNetLines();
				}
				for(int i = 0; i < instance.getMainSim().getRunnerHistory().size(); i++){
					for(int j = 0; j < 3; j++){
						NeuralNet n = instance.getMainSim().getRunnerHistory().get(i)[j].getBrain();
						n.eraseNetLines();
					}
				}
				
				selectedRunner = 0;
				instance.getRunnnerMenu().setTrack(instance.getMainSim().getTrack());
				instance.watchRunners(instance.getMainSim().getRunners());
				
				loopingGens = false;
			}
		};
		addComponent(viewAllRunnersB);
		addControled(viewAllRunnersB);
		
		MenuButton nextGenerationB = new MenuButton(10, Settings.DEFAULT_SCREEN_HEIGHT - 70, 150, 30){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Next Gen", getX() + 10, getY() + 22);
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				loopingGens = false;
				instance.getMainSim().nextGeneration();
				selectedHistoryGen = instance.getMainSim().getCurrentGen();
				historySelector.setScrollPerc(1);
			}
		};
		
		addComponent(nextGenerationB);
		addControled(nextGenerationB);
		
		selectedRunner = -1;
		
		MenuButton[] runnerSelectButtons = new MenuButton[instance.getMainSettings().getNumRunners()];
		for(int i = 0; i < runnerSelectButtons.length; i++){
			final int ii = i;
			runnerSelectButtons[i] = new MenuButton(GRID_X + i % 25 * 71, GRID_Y + i / 25 * 40, 71, 40){
				@Override
				public void render(Graphics g){
					super.render(g);
					try{
						boolean bornNow = instance.getMainSim().getRunner(ii).getBirthGen() == instance.getMainSim().getCurrentGen();
						boolean dieNext = false;
						int[] die = instance.getMainSim().getRunnersToDie();
						for(int i = 0; i < die.length && !dieNext; i++) if(die[i] == ii) dieNext = true;
						if(bornNow){
							g.setColor(new Color(0, 255, 0, 50));
							g.fillRect(getX(), getY(), getWidth(), getHeight());
						}
						if(dieNext){
							g.setColor(new Color(255, 0, 0, 50));
							g.fillRect(getX(), getY(), getWidth(), getHeight());
						}
						
						if(selectedRunner == ii){
							g.setColor(new Color(0, 0, 255, 80));
							g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
							
							instance.getMainSim().getRunner(ii).renderAdvancedInfo(g, 350, Settings.DEFAULT_SCREEN_HEIGHT - 108);
						}
						g.setColor(Color.BLACK);
						g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 10));
						DecimalFormat df = new DecimalFormat("#");
						df.setMaximumFractionDigits(15);
						g.drawString("Id: " + instance.getMainSim().getRunner(ii).getRunnerID(), getX() + 4, getY() + 13);
						g.drawString("Fit: " + df.format(instance.getMainSim().getRunner(ii).getStoredFitness()), getX() + 4, getY() + 24);
						g.drawString("Mut: " + df.format(instance.getMainSim().getRunner(ii).getMutability()), getX() + 4, getY() + 35);
					}catch(Exception e){}
				}
				@Override
				public void press(MouseEvent e){
					super.press(e);
					if(selectedRunner != ii) selectedRunner = ii;
					else selectedRunner = -1;
				}
			};
			addComponent(runnerSelectButtons[i]);
			addControled(runnerSelectButtons[i]);
		}
		
		//buttons for displaying and selecting the best, median, and worst runners from the history
		MenuButton[] runnerHistoryButtons = new MenuButton[3];
		for(int i = 0; i < runnerHistoryButtons.length; i++){
			final int ii = i;
			runnerHistoryButtons[i] = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 220 + i * 71, Settings.DEFAULT_SCREEN_HEIGHT - 46, 71, 40){
				@Override
				public void render(Graphics g){
					super.render(g);
					if(selectedRunner == -2 - ii){
						g.setColor(new Color(0, 0, 255, 80));
						g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
						
						instance.getMainSim().getRunnerHistory().get(selectedHistoryGen)[ii].renderAdvancedInfo(g, 350, Settings.DEFAULT_SCREEN_HEIGHT - 108);
					}
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 10));
					DecimalFormat df = new DecimalFormat("#");
					df.setMaximumFractionDigits(15);
					g.drawString("Id: " + instance.getMainSim().getRunnerHistory().get(selectedHistoryGen)[ii].getRunnerID(), getX() + 4, getY() + 13);
					g.drawString("Fit: " + df.format(instance.getMainSim().getRunnerHistory().get(selectedHistoryGen)[ii].getStoredFitness()), getX() + 4, getY() + 24);
					g.drawString("Mut: " + df.format(instance.getMainSim().getRunnerHistory().get(selectedHistoryGen)[ii].getMutability()), getX() + 4, getY() + 35);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 15));
					if(ii == 0) g.drawString("Worst", getX(), getY() - 2);
					else if(ii == 1) g.drawString("Median", getX(), getY() - 2);
					else if(ii == 2) g.drawString("Best", getX(), getY() - 2);
				}
				@Override
				public void press(MouseEvent e){
					super.press(e);
					if(selectedRunner != -2 - ii) selectedRunner = -2 - ii;
					else selectedRunner = -1;
				}
			};
			addComponent(runnerHistoryButtons[i]);
			addControled(runnerHistoryButtons[i]);
		}
		
		//scroller used to select the slected gen for the history
		selectedHistoryGen = 0;
		historySelector = new HorizontalScroller(Settings.DEFAULT_SCREEN_WIDTH - 530, Settings.DEFAULT_SCREEN_HEIGHT - 70, 300, 30, 40){
			@Override
			public void pressLeftButton(){
				selectedHistoryGen = Math.max(0, selectedHistoryGen - 1);
				setPerc((double)selectedHistoryGen / instance.getMainSim().getCurrentGen(), false);
			}
			@Override
			public void pressRightButton(){
				selectedHistoryGen = Math.min(selectedHistoryGen + 1, instance.getMainSim().getCurrentGen());
				setPerc((double)selectedHistoryGen / instance.getMainSim().getCurrentGen(), false);
			}
			@Override
			public void setScrollPerc(double perc){
				setPerc(perc, true);
			}
			private void setPerc(double perc, boolean update){
				super.setScrollPerc(perc);
				if(update) selectedHistoryGen = (int)Math.round(instance.getMainSim().getCurrentGen() * scrollPerc);
			}
		};
		addComponent(historySelector);
		addControled(historySelector);
		
		//click this button to loop gens
		MenuButton loopGensButton = new MenuButton(165, Settings.DEFAULT_SCREEN_HEIGHT - 70, 150, 60){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				if(!loopingGens) g.drawString("Loops Gens", getX() + 10, getY() + 22);
				else{
					g.drawString("Right click to", getX() + 10, getY() + 22);
					g.drawString("stop looping", getX() + 10, getY() + 44);
				}
			}
			
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(e.getButton() == MouseEvent.BUTTON1){
					loopingGens = true;
				}
			}
		};
		
		addComponent(loopGensButton);
		addControled(loopGensButton);
		
		//detects when a right click happens so that looping gens stops
		InputControl rightClickDetect = new InputControl(){
			private MouseAdapter mouseInput;
			@Override
			public void unlink(Component c){
				c.removeMouseListener(mouseInput);
				c.removeMouseMotionListener(mouseInput);
				c.removeMouseWheelListener(mouseInput);
			}
			
			@Override
			public void link(Component c){
				createControl();
				c.addMouseListener(mouseInput);
				c.addMouseMotionListener(mouseInput);
				c.addMouseWheelListener(mouseInput);
			}
			
			@Override
			public void createControl(){
				mouseInput = new MouseAdapter(){
					@Override
					public void mousePressed(MouseEvent e){
						super.mousePressed(e);
						if(e.getButton() == MouseEvent.BUTTON3) loopingGens = false;
					}
				};
			}
		};
		addControled(rightClickDetect);
		
		//used for keyboard input for typing in the name
	}
	
	@Override
	public void tick(){
		super.tick();
		if(loopingGens){
			instance.getMainSim().nextGeneration();
			selectedHistoryGen = instance.getMainSim().getCurrentGen();
			historySelector.setScrollPerc(1);
		}
	}
	
	@Override
	public void render(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, Settings.DEFAULT_SCREEN_WIDTH, Settings.DEFAULT_SCREEN_HEIGHT);
		super.render(g);
		g.setColor(Color.BLACK);
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
		g.drawString("Current Gen: " + instance.getMainSim().getCurrentGen(), 10, Settings.DEFAULT_SCREEN_HEIGHT - 10);
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 15));
		g.drawString("History for gen " + selectedHistoryGen, Settings.DEFAULT_SCREEN_WIDTH - 220, Settings.DEFAULT_SCREEN_HEIGHT - 80);
		g.drawString("Click a runner, then the \"View Runner\" button to  watch the runner", 600, Settings.DEFAULT_SCREEN_HEIGHT - 80);
	}
	
	/**
	 * Add the buttons for the tab for the given menu
	 * @param m
	 * @param instance
	 */
	public static void addTabButtons(Menu m, Main instance){
		
		TypingStringInput saveSimFileInput = new TypingStringInput(){
			@Override
			public void enter(){
				super.enter();
				if(getString() != "") instance.save(getString());
			}
		};
		m.addControled(saveSimFileInput);
		
		MenuButton stat = new MenuButton(10, 40, 100, 24){
			@Override
			public void render(Graphics g){
				super.render(g);
				if(instance.getSelectedMenu().equals(instance.getRunStatsMenu())){
					g.setColor(new Color(0, 0, 255, 80));
					g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
				}
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Stats", getX() + 10, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!instance.getSelectedMenu().equals(instance.getRunStatsMenu())) instance.setSelectedMenu(instance.getRunStatsMenu());
			}
		};
		m.addComponent(stat);
		m.addControled(stat);

		MenuButton graph = new MenuButton(112, 40, 100, 24){
			@Override
			public void render(Graphics g){
				super.render(g);
				if(instance.getSelectedMenu().equals(instance.getGraphsMenu())){
					g.setColor(new Color(0, 0, 255, 80));
					g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
				}
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Graphs", getX() + 10, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!instance.getSelectedMenu().equals(instance.getGraphsMenu())) instance.setSelectedMenu(instance.getGraphsMenu());
			}
		};
		m.addComponent(graph);
		m.addControled(graph);

		MenuButton track = new MenuButton(214, 40, 100, 24){
			@Override
			public void render(Graphics g){
				super.render(g);
				if(instance.getSelectedMenu().equals(instance.getTrackSelectMenu())){
					g.setColor(new Color(0, 0, 255, 80));
					g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
				}
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Tracks", getX() + 10, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!instance.getSelectedMenu().equals(instance.getTrackSelectMenu())){
					instance.getTrackSelectMenu().resetMenu();
					instance.setSelectedMenu(instance.getTrackSelectMenu());
				}
			}
		};
		m.addComponent(track);
		m.addControled(track);
		
		MenuButton settings = new MenuButton(316, 40, 100, 24){
			@Override
			public void render(Graphics g){
				super.render(g);
				if(instance.getSelectedMenu().equals(instance.getChangeSettingsMenu())){
					g.setColor(new Color(0, 0, 255, 80));
					g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
				}
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Settings", getX() + 10, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(!instance.getSelectedMenu().equals(instance.getChangeSettingsMenu())){
					instance.getTrackSelectMenu().resetMenu();
					instance.setSelectedMenu(instance.getChangeSettingsMenu());
				}
			}
		};
		m.addComponent(settings);
		m.addControled(settings);

		MenuButton save = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 510, 40, 500, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				if(saveSimFileInput.on()){
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 12));
					g.drawString("Press escape to cancel save, or press enter to save as: ", getX() + 10, getY() + 13);
					g.drawString(saveSimFileInput.getString(), getX() + 10, getY() + 27);
				}
				else{
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
					g.drawString("Save", getX() + 10, getY() + 20);
				}
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				saveSimFileInput.setOn(true);
			}
		};
		m.addComponent(save);
		m.addControled(save);

		MenuButton goToMainMenu = new MenuButton(Settings.DEFAULT_SCREEN_WIDTH - 620, 40, 110, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 20));
				g.drawString("Main Menu", getX() + 5, getY() + 20);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				SavesLoader.scanFiles();
				instance.getMainMenu().resetMenu();
				instance.setSelectedMenu(instance.getMainMenu());
			}
		};
		m.addComponent(goToMainMenu);
		m.addControled(goToMainMenu);
		
		
	}
}
