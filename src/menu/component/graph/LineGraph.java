package menu.component.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import data.Settings;
import menu.component.MenuComponent;
import menu.input.InputControl;

/**
 * An object that keeps track of an image that represents any given set of data
 */
public class LineGraph extends MenuComponent implements InputControl{
	
	private Settings settings;
	
	/**
	 * The image that keeps track of the data drawn to this graph
	 */
	private BufferedImage graph;
	
	/**
	 * The amount of data points currently displayed on the graph
	 */
	private int currentSize;
	
	/**
	 * the space to the left and right of the lines of the graph that has no lines
	 */
	private int wSpace;
	/**
	 * the space above and below the lines of the graph that has no lines
	 */
	private int hSpace;
	
	private LineGraphDetail[] lineDetails;
	
	/**
	 * The data that was last put into the graph
	 */
	private ArrayList<double[]> lastData;
	
	/**
	 * Object that keeps track of this graphs mouse input
	 */
	private MouseAdapter mouseInput;
	
	/**
	 * The point at which the graph is currently drawn
	 */
	private Point2D.Double cameraPos;
	/**
	 * The x axis scale of the displayed graph
	 */
	private int scaleX;
	/**
	 * The y axis scale of the displayed graph
	 */
	private int scaleY;
	
	/**
	 * 
	 * @param x x position of the graph
	 * @param y y position of the graph
	 * @param width total width the graph takes up
	 * @param height total height the graph takes up
	 * @param wSpace the space to the left and right of the lines of the graph that has no lines
	 * @param hSpace the space above and below the lines of the graph that has no lines
	 * @param lineDetails the objects that represent the color and thickness of each grpah line
	 */
	public LineGraph(Settings settings, int x, int y, int width, int height, int wSpace, int hSpace, LineGraphDetail[] lineDetails){
		super(x, y, width, height);
		
		this.settings = settings;
		
		this.wSpace = wSpace;
		this.hSpace = hSpace;
		
		this.lineDetails = lineDetails;

		cameraPos = new Point2D.Double(0, 0);
		scaleX = 0;
		scaleY = 0;
		
		graph = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		
		updateGraphImage(new ArrayList<double[]>());
		
		createControl();
	}
	
	public int getCurrentSize(){
		return currentSize;
	}
	
	public void updateGraphImage(ArrayList<double[]> data){
		lastData = data;
		
		//update current size
		currentSize = data.size();
		
		//create graphics object
		Graphics2D g = (Graphics2D)graph.getGraphics();
		
		//draw background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
		
		//only continue if data is not empty
		if(data.size() <= 0) return;
		
		//find biggest and lowest values in graph
		double low = data.get(0)[0];
		double big = low;
		for(int i = 0; i < data.size(); i++){
			for(int j = 0; j < data.get(i).length; j++){
				low = Math.min(low, data.get(i)[j]);
				big = Math.max(big, data.get(i)[j]);
			}
		}
		
		//find the appropriate x and y positions, and width and height of the new graph
		double xx = wSpace * getScale(scaleX) + cameraPos.x;
		double yy = hSpace * getScale(scaleY) + cameraPos.y;
		double ww = getWidth() * getScale(scaleX) - wSpace * 2 * getScale(scaleX);
		double hh = getHeight() * getScale(scaleY) - hSpace * 2 * getScale(scaleY);
		
		//determine the number of lines that should be drawn for this graph, based on the height
		int numLines = (int)Math.round(hh / 40);

		//this is the ratio between the total height that the graph lines can take up and the range of the graph values
		double yScale = Math.abs(big - low) / (hh);
		
		/* determine the y position that the x axis would be drawn at, does not necessarily mean the x axis line is draw, 
		   but the x axis is the base point where all other scale lines are drawn and where all graph lines are drawn relative to.
		   this y position is based on the big and low values*/
		double xAxisPos = big / yScale;
		
		//determine the distance between lines on the screen
		double perc = (Math.abs(big) / Math.abs(big - low));
		double lineDist = Math.abs(big) / (int)(numLines * perc);
		
		//decimal formatter for printing numbers
		DecimalFormat df = new DecimalFormat("#");
		df.setMinimumFractionDigits(3);
		df.setMinimumIntegerDigits(1);
		
		//draw the scale lines
		for(double i = big + lineDist; i > low - lineDist * 2; i -= lineDist){
			g.setColor(new Color(100, 100, 100));
			
			//determine if this line is the x axis, if it is then this line should be thicker
			boolean zero = Math.abs(i) < lineDist * .5;
			if(zero) g.setStroke(new BasicStroke(2f));
			else g.setStroke(new BasicStroke(1f));
			
			//the y position of the line
			double outY = yy + (xAxisPos - i / yScale);
			//draw the line
			g.drawLine((int)Math.round(xx), (int)Math.round(outY), (int)Math.round(xx + ww), (int)Math.round(outY));
			//draw the text label for the line
			g.setColor(Color.BLACK);
			g.setFont(new Font(settings.getFontName(), Font.PLAIN, 14));
			String s;
			if(zero) s = df.format(0);
			else s = df.format(i);
			g.drawString(s, (int)Math.round(Math.max(xx - 50, 2)), (int)Math.round(outY));
		}
		
		//the x position of the y axis on the graph
		double yAxisPos = xx;
		
		//the distance between each axis line on the graph
		double lineLength = ww / data.size();

		//the amount of data points between each of the x axis label lines, where the first line is the y axis
		int skipAxis = Math.max(1, (int)Math.round(40 / lineLength));
		
		//draw x axis labels and lines
		for(int i = 0; i <= data.size(); i += skipAxis){
			g.setColor(new Color(100, 100, 100));
			
			//set the thickness
			if(i == 0) g.setStroke(new BasicStroke(2f));
			else g.setStroke(new BasicStroke(1f));
			
			//draw the line
			g.drawLine((int)Math.round(yAxisPos + i * lineLength), (int)Math.round(yy - lineDist / yScale * 2),
					   (int)Math.round(yAxisPos + i * lineLength), (int)Math.round(yy + hh + lineDist / yScale * 2));
			
			//draw the text label for the line
			g.setColor(Color.BLACK);
			g.setFont(new Font(settings.getFontName(), Font.PLAIN, 14));
			g.drawString(i + "", (int)Math.round(yAxisPos + i * lineLength + 2), (int)Math.round(Math.min(yy + hh, getHeight() - hSpace)));
		}
		
		//determine distance between each of the x coordinates for each column of data
		double xDist = ww / data.size();
		
		//now draw the lines that represent that actual data
		for(int i = 0; i < data.get(0).length; i++){
			//the current x coordinate to draw the data
			double currentX = xx;
			//the y coordinate of the last drawn point
			double lastY = yy + xAxisPos;
			for(int j = 0; j < data.size(); j++){
				//set line thickness and color
				g.setColor(lineDetails[i].getColor());
				g.setStroke(new BasicStroke(lineDetails[i].getThickness()));
				
				//find y position
				double drawY = yy + (xAxisPos - data.get(j)[i] / yScale);
				
				//draw line
				g.drawLine((int)Math.round(currentX), (int)Math.round(lastY),
						   (int)Math.round(currentX + xDist), (int)Math.round(drawY));
				
				//update positions for next line
				lastY = drawY;
				currentX += xDist;
			}
		}
	}
	
	/**
	 * Draw this graph again with the same data it used last time
	 */
	public void redrawGraph(){
		updateGraphImage(lastData);
	}
	
	@Override
	public void tick(){}
	
	@Override
	public void render(Graphics g){
		g.drawImage(graph, getX(), getY(), getWidth(), getHeight(), null);
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
			private Point2D.Double anchor = new Point2D.Double(0, 0);
			private boolean anchored = false;
			
			@Override
			public void mousePressed(MouseEvent e){
				super.mousePressed(e);
				if(on(e) && e.getButton() == MouseEvent.BUTTON3){
					anchored = true;
					anchor = new Point2D.Double(e.getX() - cameraPos.x, e.getY() - cameraPos.y);
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e){
				super.mouseReleased(e);
				if(e.getButton() == MouseEvent.BUTTON3) anchored = false;
			}
			
			@Override
			public void mouseClicked(MouseEvent e){
				super.mouseClicked(e);
				if(on(e) && e.getButton() == MouseEvent.BUTTON1){
					cameraPos = new Point2D.Double(0, 0);
					scaleX = 0;
					scaleY = 0;
					redrawGraph();
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e){
				super.mouseDragged(e);
				if(anchored){
					cameraPos = new Point2D.Double(e.getX() - anchor.x, e.getY() - anchor.y);
					keepInRange();
					redrawGraph();
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e){
				super.mouseWheelMoved(e);
				if(on(e)){
					double percX = (e.getX() - getX() - cameraPos.x) / (getScale(scaleX));
					double percY = (e.getY() - getY() - cameraPos.y) / (getScale(scaleY));
					
					if(e.getWheelRotation() > 0){
						if(e.isControlDown()) scaleY--;
						else if(e.isShiftDown()) scaleX--;
						else{
							scaleX--;
							scaleY--;
						}
					}
					else if(e.getWheelRotation() < 0){
						if(e.isControlDown()) scaleY++;
						else if(e.isShiftDown()) scaleX++;
						else{
							scaleX++;
							scaleY++;
						}
					}
					scaleX = Math.max(settings.getGraphMinZoom(), scaleX);
					scaleX = Math.min(settings.getGraphMaxZoom(), scaleX);
					scaleY = Math.max(settings.getGraphMinZoom(), scaleY);
					scaleY = Math.min(settings.getGraphMaxZoom(), scaleY);
					
					cameraPos = new Point2D.Double(e.getX() - getX() - percX * getScale(scaleX), e.getY() - getY() - percY * getScale(scaleY));
					keepInRange();
					redrawGraph();
				}
			}
			
			public boolean on(MouseEvent e){
				return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight()).contains(e.getX(), e.getY());
			}
			
			public void keepInRange(){
				double w = (getWidth() - wSpace * 2) * getScale(scaleX);
				double h = (getHeight() - hSpace * 2) * getScale(scaleY);
				cameraPos.x = Math.max(-w + getWidth() * .1, cameraPos.x);
				cameraPos.x = Math.min(getWidth() * .9, cameraPos.x);
				cameraPos.y = Math.max(-h + getHeight() * .1, cameraPos.y);
				cameraPos.y = Math.min(getHeight() * .9, cameraPos.y);
			}
		};
	}
	
	/**
	 * The scaler used for resizing based on the given number
	 * @param n use 0 for normal scale, negative for zooming in, positive for zooming out
	 * @return
	 */
	private double getScale(int n){
		return Math.pow(Math.E, n * .15);
	}
	
}
