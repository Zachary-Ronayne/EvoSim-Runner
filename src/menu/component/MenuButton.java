package menu.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import menu.input.InputControl;

/**
 * A black and white button that takes mouse input
 */
public class MenuButton extends MenuComponent implements InputControl{
	
	/**
	 * True if the mouse is on this object, false otherwise
	 */
	protected boolean mouseOn;
	
	/**
	 * True if the mouse is held down, false otherwise
	 */
	protected boolean mousePressed;
	
	protected MouseAdapter mouseInput;
	
	public MenuButton(int x, int y, int width, int height){
		super(x, y, width, height);
		mouseOn = false;
		mousePressed = false;
		createControl();
	}
	
	/**
	 * Draw this button to the given graphics object, this method may be overridden to add additional detail, such as text
	 */
	@Override
	public void render(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		if(!mouseOn) g.setColor(Color.WHITE);
		else if(!mousePressed) g.setColor(new Color(210, 210, 210));
		else g.setColor(new Color(170, 170, 170));
		g.fillRect(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
	}

	@Override
	public void tick(){}
	
	/**
	 * Update the variables to see if the button is still on the mouse
	 */
	public void update(MouseEvent e){
		mouseOn = getBounds().contains(e.getX(), e.getY());
	}
	
	/**
	 * This method is called when a button is pressed, override this method to do something when the button is pressed
	 */
	public void press(MouseEvent e){}
	
	@Override
	public void link(Component c){
		c.addMouseListener(mouseInput);
		c.addMouseMotionListener(mouseInput);
		c.addMouseWheelListener(mouseInput);
	}

	@Override
	public void unlink(Component c){
		c.removeMouseListener(mouseInput);
		c.removeMouseMotionListener(mouseInput);
		c.removeMouseWheelListener(mouseInput);
	}

	@Override
	public void createControl(){
		mouseInput = new MouseAdapter(){
			@Override
			public void mouseMoved(MouseEvent e){
				mouseOn = getBounds().contains(e.getPoint());
			}
			@Override
			public void mouseDragged(MouseEvent e){
				mouseOn = getBounds().contains(e.getPoint());
			}
			@Override
			public void mousePressed(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1) mousePressed = true;
			}
			@Override
			public void mouseReleased(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					if(mouseOn) press(e);
					mousePressed = false;
				}
			}
		};
	}

}
