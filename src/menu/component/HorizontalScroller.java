package menu.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import menu.input.InputControl;

public class HorizontalScroller extends MenuComponent implements InputControl{
	
	private int scrollSize;
	/**
	 * The percentage of the way this scroller is along the entire path
	 */
	protected double scrollPerc;
	
	/**
	 * The last position the mouse was on the x coordinate
	 */
	private int mx;
	/**
	 * The last position the mouse was on the y coordinate
	 */
	private int my;
	
	/**
	 * true if the mouse is pressed or not
	 */
	private boolean movingSlider;
	
	private MouseAdapter mouseInput;
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param scrollSize the length of the scroller length is measured in the same direction as the vertical direction
	 */
	public HorizontalScroller(int x, int y, int width, int height, int scrollSize){
		super(x, y, width, height);
		
		this.scrollSize = scrollSize;
		movingSlider = false;
		
		createControl();
	}
	
	/**
	 * Set the current percentage of the way through the scroller is
	 */
	public void setScrollPerc(double perc){
		scrollPerc = Math.max(0, Math.min(1, perc));
	}
	
	@Override
	public void tick(){}
	
	@Override
	public void render(Graphics g){
		//draw background
		g.setColor(Color.BLACK);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2);
		
		//draw buttons at left end
		g.setColor(Color.BLACK);
		g.fillRect(getX(), getY(), getHeight(), getHeight());
		
		if(onLeftButton()){
			if(movingSlider) g.setColor(new Color(150, 150, 255));
			else g.setColor(new Color(200, 200, 255));
		}
		else g.setColor(Color.WHITE);
		g.fillRect(getX() + 1, getY() + 1, getHeight() - 2, getHeight() - 2);
		
		int px = getX() + getHeight() / 2;
		int py = getY() + getHeight() / 2;
		//draw arrow for slider
		g.setColor(Color.BLACK);
		g.fillPolygon(new Polygon(
					new int[]{(int)Math.round(px + getHeight() * .25), (int)Math.round(px + getHeight() * .25), (int)Math.round(px - getHeight() * .25)},
					new int[]{(int)Math.round(py - getHeight() * .25), (int)Math.round(py + getHeight() * .25), py},
				3));
		px = getX() + getWidth() - getHeight() / 2;
		
		//draw buttons at right end
		g.setColor(Color.BLACK);
		g.fillRect(getX() + getWidth() - getHeight(), getY(), getHeight(), getHeight());
		
		if(onRightButton()){
			if(movingSlider) g.setColor(new Color(150, 150, 255));
			else g.setColor(new Color(200, 200, 255));
		}
		else g.setColor(Color.WHITE);
		g.fillRect(getX() + getWidth() - getHeight() + 1, getY() + 1, getHeight() - 2, getHeight() - 2);
		
		//draw arrow for slider
		g.setColor(Color.BLACK);
		g.fillPolygon(new Polygon(
				new int[]{(int)Math.round(px - getHeight() * .25), (int)Math.round(px - getHeight() * .25), (int)Math.round(px + getHeight() * .25)},
				new int[]{(int)Math.round(py - getHeight() * .25), (int)Math.round(py + getHeight() * .25), py},
			3));
		
		//draw scroller
		double sx = getScrollX();
		g.setColor(Color.BLACK);
		g.fillRect((int)Math.round(sx), getY() - 2, scrollSize, getHeight() + 4);
		if(on() && mouseOnScroll()){
			if(movingSlider) g.setColor(new Color(150, 150, 255));
			else g.setColor(new Color(200, 200, 255));
		}
		else g.setColor(new Color(230, 230, 230));
		g.fillRect((int)Math.round(sx + 1), getY() - 1, scrollSize - 2, getHeight() + 2);
	}
	
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
				super.mouseMoved(e);
				mx = e.getX();
				my = e.getY();
			}
			@Override
			public void mouseDragged(MouseEvent e){
				super.mouseDragged(e);
				mx = e.getX();
				my = e.getY();

				if(movingSlider) setScrollPerc((mx - scrollSize * .5 - getX() - getHeight()) / (getWidth() - getHeight() * 2 - scrollSize));
			}
			@Override
			public void mousePressed(MouseEvent e){
				super.mousePressed(e);
				if(on() && mouseOnScroll()) movingSlider = true;
				if(onLeftButton()) pressLeftButton();
				else if(onRightButton()) pressRightButton();
			}
			@Override
			public void mouseReleased(MouseEvent e){
				super.mouseReleased(e);
				movingSlider = false;
			}
		};
	}
	
	/**
	 * move the slider slightly to the left, called when the left arrow is pressed. Override completely to use a custom value
	 */
	public void pressLeftButton(){
		setScrollPerc(scrollPerc - .01);
	}
	/**
	 * move the slider slightly to the right, called when the right arrow is pressed. Override completely to use a custom value
	 */
	public void pressRightButton(){
		setScrollPerc(scrollPerc + .01);
	}
	
	public boolean on(){
		return new Rectangle(getX(), getY(), getWidth(), getHeight()).contains(mx, my);
	}
	
	public boolean mouseOnScroll(){
		return mx >= getScrollX() && mx <= getScrollX() + scrollSize;
	}
	
	public double getScrollX(){
		return getX() + getHeight() + (getWidth() - getHeight() * 2 - scrollSize) * scrollPerc;
	}

	public boolean onLeftButton(){
		return on() && mx < getX() + getHeight();
	}
	public boolean onRightButton(){
		return on() && mx > getX() + getWidth() - getHeight();
	}
	
}
