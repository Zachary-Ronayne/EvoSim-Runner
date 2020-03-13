package menu.component;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class MenuComponent{
	
	private int x;
	private int y;
	private int width;
	private int height;
	
	public MenuComponent(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Draws this component on the specified graphics object
	 * @param g
	 */
	public abstract void render(Graphics g);
	
	/**
	 * Updates this object each game tick
	 */
	public abstract void tick();
	
	public Rectangle getBounds(){
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}
	
	public int getX(){
		return x;
	}
	public void setX(int x){
		this.x = x;
	}
	
	public int getY(){
		return y;
	}
	public void setY(int y){
		this.y = y;
	}
	
	public int getWidth(){
		return width;
	}
	public void setWidth(int width){
		this.width = width;
	}
	
	public int getHeight(){
		return height;
	}
	public void setHeight(int height){
		this.height = height;
	}
	
}
