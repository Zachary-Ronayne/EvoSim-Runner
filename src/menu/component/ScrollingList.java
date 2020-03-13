package menu.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import menu.input.InputControl;

/**
 * An object that has a list of menu buttons that can all be clicked to select a specific button and can be scrolled through using the scroll wheel, some buttons
 * going off screen if there are more buttons in the list than there are rendered positions
 */
public class ScrollingList extends MenuComponent implements InputControl{
	
	private MouseAdapter mouseInput;
	
	private MenuButton[] buttons;
	
	/**
	 * The index of the button that is currently at the top of the list
	 */
	private int topButton;
	private int numSlots;
	
	/**
	 * @param x
	 * @param y
	 * @param width the width of each button
	 * @param buttonHeight the height of one button
	 * @param numButtons the number of buttons that exist for this object
	 * @param numSlots the max number of buttons that can be displayed at once
	 */
	public ScrollingList(int x, int y, int width, int buttonHeight, int numButtons, int numSlots){
		super(x, y, width, buttonHeight * numSlots);
		this.numSlots = numSlots;
		
		topButton = 0;
		
		buttons = new MenuButton[numButtons];
		for(int i = 0; i < buttons.length; i++){
			final int ii = i;
			buttons[i] = new MenuButton(x, y, width, buttonHeight){
				@Override
				public void press(MouseEvent e){
					super.press(e);
					if(buttonOnScreen(ii)) pressButton(ii);
				}
				@Override
				public void render(Graphics g){
					super.render(g);
					renderButton(g, ii);
				}
			};
		}
		scroll(0);
		
		createControl();
	}
	
	/**
	 * Get the number of slots for displayed buttons
	 * @return
	 */
	public int getNumSlots(){
		return numSlots;
	}
	
	/**
	 * move the list this many units, positive for down, negative for up, zero to update the list
	 * @param num
	 */
	private void scroll(int num){
		topButton += num;
		if(topButton < 0) topButton = buttons.length - 1;
		else if(topButton > buttons.length - 1) topButton = 0;
		
		for(int i = 0; i < buttons.length; i++){
			buttons[i].setY(getY() + (i - topButton) * buttons[i].getHeight());
			if(!buttonOnScreen(i)) buttons[i].setY(getY() + (buttons.length - topButton + i) * buttons[i].getHeight());
		}
	}
	
	/**
	 * Get the button of the specificed index
	 * @param index
	 * @return
	 */
	public MenuButton getButton(int index){
		return buttons[index];
	}

	/**
	 * Called when the button with the corresponding index is pressed. 
	 * Override this to do something when the button is pressed
	 * @param index
	 */
	public void pressButton(int index){}
	/**
	 * Called when the button with the corresponding index is rendered. 
	 * Override this to do something when the button is rendered
	 * @param index
	 */
	public void renderButton(Graphics g, int index){}
	
	/**
	 * Returns true if the buttons with the corresponding index is currentyl being rendered
	 * @param index
	 * @return
	 */
	public boolean buttonOnScreen(int index){
		return buttons[index].getY() >= getY() && buttons[index].getY() + buttons[index].getHeight() <= getY() + getHeight();
	}
	
	@Override
	public void tick(){
		for(int i = 0; i < buttons.length; i++){
			if(buttonOnScreen(i)) buttons[i].tick();
		}
	}

	@Override
	public void render(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
		
		for(int i = 0; i < buttons.length; i++){
			if(buttonOnScreen(i)) buttons[i].render(g);
		}
	}

	@Override
	public void link(Component c){
		c.addMouseListener(mouseInput);
		c.addMouseMotionListener(mouseInput);
		c.addMouseWheelListener(mouseInput);
		for(MenuButton m : buttons) m.link(c);
	}

	@Override
	public void unlink(Component c){
		c.removeMouseListener(mouseInput);
		c.removeMouseMotionListener(mouseInput);
		c.removeMouseWheelListener(mouseInput);
		for(MenuButton m : buttons) m.unlink(c);
	}

	@Override
	public void createControl(){
		mouseInput = new MouseAdapter(){
			private boolean on = false;

			@Override
			public void mouseMoved(MouseEvent e){
				super.mouseMoved(e);
				on = getBounds().contains(e.getX(), e.getY());
			}
			@Override
			public void mouseDragged(MouseEvent e){
				super.mouseDragged(e);
				on = getBounds().contains(e.getX(), e.getY());
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e){
				super.mouseWheelMoved(e);
				if(on && numSlots < buttons.length){
					if(e.getWheelRotation() > 0) scroll(1);
					if(e.getWheelRotation() < 0) scroll(-1);
					for(MenuButton b : buttons) b.update(e);
				}
			}
		};
	}
	
}
