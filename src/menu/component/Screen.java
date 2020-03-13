package menu.component;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * An object that keeps track of the graphics that were drawn on it for resizing
 */
public class Screen extends MenuComponent{
	
	private BufferedImage img;
	
	public Screen(int x, int y, int width, int height){
		super(x, y, width, height);
		img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	/**
	 * Call this method redraw this screen
	 */
	public void updateScreen(){
		Graphics g = img.getGraphics();
		drawScreen(g);
		g.dispose();
	}
	
	/**
	 * Override this method to define what will be drawn on this object
	 * @param g
	 */
	public void drawScreen(Graphics g){}
	
	@Override
	public void render(Graphics g){
		g.drawImage(img, getX(), getY(), getWidth(), getHeight(), null);
	}
	
	@Override
	public void tick(){
		
	}
	
	@Override
	public void setWidth(int width){
		super.setWidth(width);
		img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	@Override
	public void setHeight(int height){
		super.setHeight(height);
		img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	}
	
}
