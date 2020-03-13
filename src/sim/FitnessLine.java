package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A line that also has a direction that determines if fitness should be granted or subtracted when a runner passes it
 * @author Owner
 *
 */
public class FitnessLine extends Line2D.Double{
	private static final long serialVersionUID = 1L;
	
	private boolean positive;
	
	/**
	 * the angle from p1 to p2
	 */
	private double angle;
	private Point2D.Double center;
	
	/**
	 * @param positive true if the angle from p1 to the test point (the center of the runner), if that angle should be between 0 and 180, false otherwise
	 */
	public FitnessLine(double x1, double y1, double x2, double y2, boolean positive){
		super(x1, y1, x2, y2);
		this.positive = positive;
		calculateInfo();
	}
	
	public boolean isPositive(){
		return positive;
	}
	
	public void setPositive(boolean p){
		positive = p;
	}
	
	/**
	 * Determines several variables based on the positions of the points of this line and if this line is positive, MUST be called after any of those variables are modified
	 */
	public void calculateInfo(){
		angle = Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
		if(angle < 0) angle += 360;
		center = new Point2D.Double((x1 + x2) / 2, (y1 + y2) / 2);
	}
	
	/**
	 * Draw this line with its direction shown on the given graphics object. The x and y are this lines position on the graphics
	 * @param g
	 * @param x
	 * @param y
	 */
	public void render(Graphics2D g, double x, double y){
		g.setColor(new Color(200, 0, 0));
		g.setStroke(new BasicStroke(8f));
		
		//main line
		g.drawLine((int)Math.round(x1 + x), (int)Math.round(y1 + y), (int)Math.round(x2 + x), (int)Math.round(y2 + y));
		
		double size = 50;
		if(positive) size *= -1;
		double offX = center.x + x + size * Math.cos(Math.toRadians(angle + 90));
		double offY = center.y + y + size * Math.sin(Math.toRadians(angle + 90));
		
		//line perpendicular to main line
		g.drawLine((int)Math.round(center.x + x), (int)Math.round(center.y + y), (int)Math.round(offX), (int)Math.round(offY));
		
		//arrow lines
		g.drawLine((int)Math.round(offX), (int)Math.round(offY),
				(int)Math.round(offX - .8 * size * Math.cos(Math.toRadians(angle + 45))),
				(int)Math.round(offY - .8 * size * Math.sin(Math.toRadians(angle + 45))));
		g.drawLine((int)Math.round(offX), (int)Math.round(offY),
				(int)Math.round(offX - .8 * size * Math.cos(Math.toRadians(angle + 135))),
				(int)Math.round(offY - .8 * size * Math.sin(Math.toRadians(angle + 135))));
	}
	
	/**
	 * Returns 0 if the given positions represent a runner that has not entered or left this line, 
	 * 1 if the positions represent a runner that has passed over the line in the correct direction, 
	 * -1 if the positions represent a runner that has passed over the line in the wrong direction
	 * @param oX old x position of the runner
	 * @param oY old y position of the runner
	 * @param nX new x position of the runner
	 * @param nY new y position of the runner
	 * @param r the radius of the runner
	 * @param allowAdd true to let this method return 1, -1 or 0, false to return 0 when it would normally return 1
	 * @return
	 */
	public int crossedLine(double oX, double oY, double nX, double nY, double r, boolean allowAdd){
		//find out which of the circles of (oX, oY) and (nX, nY) intersect this line
		boolean oldIn = intersectsCircle(oX, oY, r);
		boolean newIn = intersectsCircle(nX, nY, r);
		
		//if you are not moving onto or off the line, then no change happens
		if(newIn && oldIn || !newIn && !oldIn) return 0;
		
		//if going off the line
		if(oldIn && !newIn){
			//find angle from p1 to the new pos
			double a = Math.toDegrees(Math.PI / 2 + Math.atan2(y1 - nY, x1 - nX));
			a -= angle;
			while(a < 0) a += 360;
			//true if the angle is in the correct region
			boolean inPos = a < 90;
			//if correct direction, return 0
			if(positive && !inPos || !positive & inPos) return 0;
			//otherwise (wrong direction), return -1
			else return -1;
		}
		//if going on the line
		else{
			//if the fitness lines should not add fitness, return 0 now
			if(!allowAdd) return 0;
			
			//find angle from p1 to the old pos
			double a = Math.toDegrees(Math.PI / 2 + Math.atan2(y1 - oY, x1 - oX));
			a -= angle;
			while(a < 0) a += 360;
			//true if the angle is in the correct region
			boolean inPos = a < 90;
			//if correct direction, return 1
			if(!positive && !inPos || positive && inPos) return 1;
			//otherwise (wrong direction), return 0
			else return 0;
		}
	}
	
	/**
	 *true if the given circle intersects this line
	 * @param x center of circle
	 * @param y center of circle
	 * @param r radius of circle
	 * @return
	 */
	public boolean intersectsCircle(double x, double y, double r){
		//check to see if the end points of the line intersect the circle
		Point2D.Double p = new Point2D.Double(x, y);
		double d = p.distance(x1, y1);
		if(d <= r) return true;
		d = p.distance(x2, y2);
		if(d <= r) return true;

		
		//if the line is horizontal
		if((y1 == y2)){
			double minx = Math.min(x1, x2);
			double maxx = Math.max(x1, x2);
			return x > minx && x < maxx && Math.abs(y - y1) < r;
		}
		//if the line is vertical
		else if(x1 == x2){
			double miny = Math.min(y1, y2);
			double maxy = Math.max(y1, y2);
			return y > miny && y < maxy && Math.abs(x - x1) < r;
		}
		//only do this if this line is not vertical or horizontal
		else{
			double slope = (y1 - y2) / (x1 - x2);
			double b = y1 - slope * x1;
			double perpSlope = -1 / slope;
			double perpB = y - perpSlope * x;
			
			double intersectX = (b - perpB) / (perpSlope - slope);
			double intersectY = perpSlope * intersectX + perpB;
			Point2D.Double ip = new Point2D.Double(intersectX, intersectY);
			double length = new Point2D.Double(x1, y1).distance(x2, y2);
			return p.distance(ip) <= r && ip.distance(x1, y1) <= length && ip.distance(x2, y2) <= length;
		}
	}
}