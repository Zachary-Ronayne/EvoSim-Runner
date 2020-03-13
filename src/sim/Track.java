package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import data.Settings;

public class Track{
	
	private Settings settings;
	
	/**
	 * The point where the origin is, relative to trackImage. So if the upperleft corner of the image is -100, -100, then that is this point
	 */
	private Point2D.Double trackImagePos;
	/**
	 * The current image of the track without the runner
	 */
	private BufferedImage trackImage;
	/**
	 * A list of all the lines in this track. Each line represents a wall that the runner should avoid hitting
	 */
	private Line2D.Double[] trackLines;
	private FitnessLine[] fitnessLines;
	/**
	 * The position a runner should be placed when they enter the track
	 */
	private Point2D.Double startingPoint;

	/**
	 * The angle that the runner should be facing at the start of the track
	 */
	private double startingAngle;
	
	public Track(Settings settings, Point2D.Double startingPoint, Line2D.Double... trackLines){
		this.settings = settings;
		
		this.startingPoint = startingPoint;
		this.trackLines = trackLines;
		fitnessLines = new FitnessLine[0];
		
		drawImage();
	}
	
	/**
	 * Set this track to the default track
	 */
	public void setDefault(){
		startingPoint = new Point2D.Double(100, 0);
		trackLines = new Line2D.Double[] {
			new Line2D.Double(-100, -100, 2100, -100),
			new Line2D.Double(2100, -100, 2100, 2100),
			new Line2D.Double(2100, 2100, -100, 2100),
			new Line2D.Double(-100, 2100, -100, -100),
			new Line2D.Double(300, 300, 1700, 300),
			new Line2D.Double(1700, 300, 1700, 1700),
			new Line2D.Double(1700, 1700, 300, 1700),
			new Line2D.Double(300, 1700, 300, 300)
		};
		setFitnessLines(
			new FitnessLine(400, 300, 400, -100, true),
			new FitnessLine(1000, 300, 1000, -100, true),
			new FitnessLine(1600, 300, 1600, -100, true),
			
			new FitnessLine(1700, 400, 2100, 400, true),
			new FitnessLine(1700, 1000, 2100, 1000, true),
			new FitnessLine(1700, 1600, 2100, 1600, true),
			
			new FitnessLine(400, 1700, 400, 2100, true),
			new FitnessLine(1000, 1700, 1000, 2100, true),
			new FitnessLine(1600, 1700, 1600, 2100, true),
			
			new FitnessLine(300, 400, -100, 400, true),
			new FitnessLine(300, 1000, -100, 1000, true),
			new FitnessLine(300, 1600, -100, 1600, true)
		);
	}
	
	public void setFitnessLines(FitnessLine... lines){
		fitnessLines = lines;
	}
	
	/**
	 * Draws the track with a generic runner circle at the starting point
	 */
	public void drawImageStartPoint(){
		drawImage();

		Graphics2D g = (Graphics2D)trackImage.getGraphics();

		g.setColor(Color.BLACK);
		g.fillOval((int)Math.round(startingPoint.x + getXOffset() - settings.getRunnerRadius()),
				   (int)Math.round(startingPoint.y + getYOffset()- settings.getRunnerRadius()),
				   (int)Math.round(settings.getRunnerRadius() * 2), (int)Math.round(settings.getRunnerRadius() * 2));
		g.setColor(new Color(100, 100, 255));
		g.fillOval((int)Math.round(startingPoint.x + getXOffset() + 2 - settings.getRunnerRadius()),
				   (int)Math.round(startingPoint.y + getYOffset() + 2 - settings.getRunnerRadius()),
				   (int)Math.round(settings.getRunnerRadius() * 2 - 4), (int)Math.round(settings.getRunnerRadius() * 2 - 4));
	}
	
	/**
	 * Draws the given runner to the track image
	 * @param r
	 * @return
	 */
	public void drawImage(){
		double lowX;
		double highX;
		double lowY;
		double highY;

		//find the lowest and highest coordinates of the track, based on track lines
		if(trackLines.length > 0){
			lowX = trackLines[0].x1;
			highX = lowX;
			lowY = trackLines[0].y1;
			highY = lowY;
		}
		//if no track lines, find the lowest and highest coordinates of the track, based on fitness lines
		else if(fitnessLines.length > 0){
			lowX = fitnessLines[0].x1;
			highX = lowX;
			lowY = fitnessLines[0].y1;
			highY = lowY;
		}
		//if there are no lines, use this default size
		else{
			lowX = -200;
			highX = 200;
			lowY = -200;
			highY = 200;
		}

		for(int i = 0; i < trackLines.length; i++){
			lowX = Math.min(lowX, trackLines[i].x1);
			lowX = Math.min(lowX, trackLines[i].x2);
			lowY = Math.min(lowY, trackLines[i].y1);
			lowY = Math.min(lowY, trackLines[i].y2);
			
			highX = Math.max(highX, trackLines[i].x1);
			highX = Math.max(highX, trackLines[i].x2);
			highY = Math.max(highY, trackLines[i].y1);
			highY = Math.max(highY, trackLines[i].y2);
		}
		for(int i = 0; i < fitnessLines.length; i++){
			lowX = Math.min(lowX, fitnessLines[i].x1);
			lowX = Math.min(lowX, fitnessLines[i].x2);
			lowY = Math.min(lowY, fitnessLines[i].y1);
			lowY = Math.min(lowY, fitnessLines[i].y2);
			
			highX = Math.max(highX, fitnessLines[i].x1);
			highX = Math.max(highX, fitnessLines[i].x2);
			highY = Math.max(highY, fitnessLines[i].y1);
			highY = Math.max(highY, fitnessLines[i].y2);
		}
		
		//the width and height that should be used for the image, the tileSize * 2 is added on as an extra bit around the main track
		double w = Math.abs(lowX - highX) + settings.getTrackTileSize() * 2;
		double h = Math.abs(lowY - highY) + settings.getTrackTileSize() * 2;
		
		trackImagePos = new Point2D.Double(lowX, lowY);
		
		//create image object
		trackImage = new BufferedImage((int)Math.round(w), (int)Math.round(h), BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g = (Graphics2D)trackImage.getGraphics();

		//draw the tiled background
		for(int i = 0; i < 1 + w / settings.getTrackTileSize(); i++){
			for(int j = 0; j < 1 + h / settings.getTrackTileSize(); j++){
				if(i % 2 == 0 && j % 2 == 0 || i % 2 == 1 && j % 2 == 1) g.setColor(new Color(150, 150, 150));
				else g.setColor(new Color(200, 200, 200));
				g.fillRect(settings.getTrackTileSize() * i, settings.getTrackTileSize() * j, settings.getTrackTileSize(), settings.getTrackTileSize());
			}
		}
		
		//draw the lines of the track
		g.setStroke(new BasicStroke(8f));
		g.setColor(Color.BLACK);
		for(Line2D.Double l : trackLines) g.drawLine((int)Math.round(l.x1 + getXOffset()), (int)Math.round(l.y1 + getYOffset()),
													 (int)Math.round(l.x2 + getXOffset()), (int)Math.round(l.y2 + getYOffset()));
		//draw the goal lines
		for(FitnessLine f : fitnessLines) f.render(g, getXOffset(), getYOffset());
	}
	
	public BufferedImage getTrackImage(){
		return trackImage;
	}

	/**
	 * Get the buffered image that contains the given runner. Must call drawImage before calling this method
	 * @param r
	 * @return
	 */
	public BufferedImage getTrackImageWithRunner(Runner r){
		if(r == null) return trackImage;
		
		BufferedImage img = new BufferedImage(trackImage.getWidth(), trackImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g = (Graphics2D)img.getGraphics();
		g.drawImage(trackImage, 0, 0, null);
		
		//draw the runner
		 r.render(g, new Point2D.Double(getXOffset(), getYOffset()), true);
		
		return img;
	}

	/**
	 * Get the buffered image that contains the given runners
	 * @param r
	 * @return
	 */
	public BufferedImage getTrackImageWithRunners(Runner[] run){
		if(run == null) return trackImage;
		
		BufferedImage img = new BufferedImage(trackImage.getWidth(), trackImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g = (Graphics2D)img.getGraphics();
		g.drawImage(trackImage, 0, 0, null);
		
		//draw the runners
		for(Runner r : run) r.render(g, new Point2D.Double(getXOffset(), getYOffset()), false);
		
		return img;
	}
	
	
	/**
	 * get the amount of x this track is offset when rendered, needs to be subtracted from the x of the image that is returned from the drawImage method
	 * @return
	 */
	public double getXOffset(){
		return settings.getTrackTileSize() - trackImagePos.x;
	}
	
	/**
	 * get the amount of y this track is offset when rendered, needs to be subtracted from the y of the image that is returned from the drawImage method
	 * @return
	 */
	public double getYOffset(){
		return settings.getTrackTileSize() - trackImagePos.y;
	}
	
	public Point2D.Double getStartingPoint(){
		return startingPoint;
	}
	public void setStartingPoint(Point2D.Double p){
		startingPoint = p;
	}
	public double getStartingAngle(){
		return startingAngle;
	}
	public void setStartingAngle(double angle){
		startingAngle = angle;
	}
	
	/**
	 * Sets up the given runner to begin running on the track
	 * @param r
	 */
	public void enterRunner(Runner r){
		r.reset();
		r.setCurrentAngle(startingAngle);
		r.setX(startingPoint.x);
		r.setY(startingPoint.y);
	}
	
	/**
	 * Get the array of lines that represent the track walls
	 * @return
	 */
	public Line2D.Double[] getLines(){
		return trackLines;
	}
	
	/**
	 * Add on to the list of track lines in this list
	 * @param l
	 */
	public void addTrackLine(Line2D.Double l){
		Line2D.Double[] lines = new Line2D.Double[trackLines.length + 1];
		for(int i = 0; i < trackLines.length; i++) lines[i] = trackLines[i];
		lines[lines.length - 1] = l;
		trackLines = lines;
	}
	
	/**
	 * Add on to the list of fitness lines in this list
	 * @param l
	 */
	public void addFitnessLine(Line2D.Double l, boolean pos){
		FitnessLine[] lines = new FitnessLine[fitnessLines.length + 1];
		for(int i = 0; i < fitnessLines.length; i++) lines[i] = fitnessLines[i];
		lines[lines.length - 1] = new FitnessLine(l.x1, l.y1, l.x2, l.y2, pos);
		fitnessLines = lines;
	}
	
	/**
	 * Causes the given runner to collide with this Track. If the runner hits the track it will stop moving, 
	 * or it will simply collide with the wall depending on settings. 
	 * Also checks for the number of FitnessLines crossed
	 * @param r
	 */
	public void collideWithRunner(Runner r){
		//keeps track of the distance between the runner and a wall
		double moveDistance = 0;
		
		boolean hitWall = false;
		for(Line2D.Double line : trackLines){
			//check to see if the runner touches the wall if they havn't already hit a wall
			if(!hitWall){
				double tanX1 = 0;
				double tanY1 = 0;
				double tanX2 = 0;
				double tanY2 = 0;

				//find slope and intercept of the wall line
				double lineSlope = (line.y1 - line.y2) / (line.x1 - line.x2);
				double lineB = line.y1 - lineSlope * line.x1;
				
				boolean wallVert = line.x1 == line.x2;
				boolean wallHor = line.y1 == line.y2;
				
				//if the line is vertical
				if(wallVert){
					double miny = Math.min(line.y1, line.y2);
					double maxy = Math.max(line.y1, line.y2);
					moveDistance = Math.abs(r.getX() - line.x1);
					hitWall = hitWall || r.getY() >= miny && r.getY() <= maxy && moveDistance < settings.getRunnerRadius();
					if(hitWall){
						tanX1 = r.getX() + settings.getRunnerRadius();
						tanY1 = r.getY();
						tanX2 = r.getX() - settings.getRunnerRadius();
						tanY2 = r.getY();
					}
				}
				//if the line is horizontal
				else if(wallHor){
					double minx = Math.min(line.x1, line.x2);
					double maxx = Math.max(line.x1, line.x2);
					moveDistance = Math.abs(r.getY() - line.y1);
					hitWall = hitWall || r.getX() >= minx && r.getX() <= maxx && moveDistance < settings.getRunnerRadius();
					if(hitWall){
						tanX1 = r.getX();
						tanY1 = r.getY() + settings.getRunnerRadius();
						tanX2 = r.getX();
						tanY2 = r.getY() - settings.getRunnerRadius();
					}
				}
				//otherwise use this to test the distance to a horizontal line
				else{
					//find the slope and intercept of a line perpendicular to the wall line that intersects the center of the runner
					double perpSlope = -1 / lineSlope;
					double perpB = r.getY() - perpSlope * r.getX();
					
					//find the intersection point of those 2 lines
					double intersectX = (lineB - perpB) / (perpSlope - lineSlope);
					double intersectY = lineSlope * intersectX + lineB;
					Point2D.Double intersect = new Point2D.Double(intersectX, intersectY);
					
					//find the distance that the runner is from the wall
					double wallDist = r.getPos().distance(intersect);
					
					//find the length of the main line, used for determining if the intersection point of the wall line is actually on the line segment
					double length = new Point2D.Double(line.x1, line.y1).distance(line.x2, line.y2);
					
					//if the runner is touching a line, then set hitWall to true,
					//comparing the intersection distance to the line points is to ensure that the intersection point is on the line segment
					hitWall = hitWall ||
							  intersect.distance(line.x1, line.y1) < length &&
							  intersect.distance(line.x2, line.y2) < length &&
							  wallDist < settings.getRunnerRadius();
					if(hitWall){
						//find point that should end up tangent to the wall
						//find the perpendicular line to the wall
						
						//find where that perpendicular line intersects the edge of the runner

						//this math was used to figure out the tanRoot variable
						//basically found the 2 intersection points of the circle of the runner and the perpendicular line to the wall line
						//I can guarantee that the line circle intersects the wall as I have already tested for that
						//the math for tanRoot, tanX1, and tanX2 comes from the result from WolframAlpha at this url:
						//https://www.wolframalpha.com/input/?i=solve+for+x:+(x-h)%5E2+%2B+(mx%2Bb-k)%5E2%3Dr%5E2
						//I tried to solve for x myself, but I got stuck and couldn't be bothered to try to figure it out myself, so I just let WolframAlpha do it for me
						double tanRoot = Math.sqrt(
								-Math.pow(perpB, 2) -
								2 * perpB * r.getX() * perpSlope +
								2 * perpB * r.getY() - 
								Math.pow(r.getX(), 2) * Math.pow(perpSlope, 2) +
								2 * r.getX() * r.getY() * perpSlope -
								Math.pow(r.getY(), 2) + 
								Math.pow(perpSlope, 2) * Math.pow(settings.getRunnerRadius(), 2) + 
								Math.pow(settings.getRunnerRadius(), 2));
						tanX1 = (tanRoot - perpSlope * perpB + r.getX() + r.getY() * perpSlope) / (Math.pow(perpSlope, 2) + 1);
						tanY1 = perpSlope * tanX1 + perpB;
						tanX2 = (-tanRoot - perpSlope * perpB + r.getX() + r.getY() * perpSlope) / (Math.pow(perpSlope, 2) + 1);
						tanY2 = perpSlope * tanX2 + perpB;
					}
				}
				if(hitWall){
					
					/*
					 * determine which of those 2 points is the one that you need to find the distance to.
					 * Of the 2 calculated points, the one closer to any distance moved in the same direction as the way the runner is facing will be the correct point
					 */
					Point2D.Double run = new Point2D.Double(
							r.getX() + Math.cos(Math.toRadians(r.getCurrentAngle())) * r.getSpeed(),
							r.getY() + Math.sin(Math.toRadians(r.getCurrentAngle())) * r.getSpeed());
					boolean useTan1 = run.distance(tanX1, tanY1) < run.distance(tanX2, tanY2);
					Point2D.Double usePoint;
					if(useTan1) usePoint = new Point2D.Double(tanX1, tanY1);
					else usePoint = new Point2D.Double(tanX2, tanY2);
					
//					System.out.println(tanX1 + " " + tanY1 + " " + tanX2 + " " + tanY2 + " " + usePoint.x);
					
					//find the distance from the tangent point to: the line that intersects the wall line and is parallel to the line at the angle the runner is facing
					//that is the distance the runner has to move

					//if the slope of the runner's direction angle is vertical
					if(r.getCurrentAngle() % 180 == 90){
						//if the wall line is vertical, should not happen because of the way the paralell lines would act
						if(wallVert) moveDistance = 0;
						//if the wall is horizontal
						else if(wallHor) moveDistance = Math.abs(usePoint.y - line.y1);
						//otherwise do the general case
						else moveDistance = Math.abs(usePoint.y - (lineSlope * usePoint.x + lineB));
					}
					//if the slope of the runner's direction angle is horizontal
					else if(r.getCurrentAngle() % 180 == 0){
						//if the wall line is vertical
						if(wallVert) moveDistance = Math.abs(usePoint.x - line.x1);
						//if the wall is horizontal, should not happen because of the way the paralell lines would act
						else if(wallHor) moveDistance = 0;
						//otherwise do the general case
						else moveDistance = Math.abs(usePoint.y - (lineSlope * usePoint.x + lineB));
					}
					//otherwise do the general case
					else{
						double parallelSlope = Math.sin(Math.toRadians(r.getCurrentAngle())) / Math.cos(Math.toRadians(r.getCurrentAngle()));
						double parallelB = usePoint.y - usePoint.x * parallelSlope;
						
						//if the wall line is vertical
						if(wallVert) moveDistance = usePoint.distance(line.x1, parallelSlope * line.x1 + parallelB);
						//if the wall is horizontal
						else if(wallHor) moveDistance = usePoint.distance((line.y1 - parallelB) / parallelSlope, line.y1);
						//otherwise do the general case
						else{
							double parIntX = (parallelB - lineB) / (lineSlope - parallelSlope);
							double parIntY = parIntX * parallelSlope + parallelB;
							moveDistance = usePoint.distance(parIntX, parIntY);
						}
					}
				}
			}
			
			//if a line was hit, then quit looping
			if(hitWall) break;
		}
		
		//test for hitting fitness lines
		for(FitnessLine l : fitnessLines) r.addFitness(settings.getRunnerFitnessGain() * l.crossedLine(r.getLastX(), r.getLastY(), r.getX(), r.getY(), settings.getRunnerRadius(), settings.getFitnessLinesGive()));
		
		if(settings.getKillRunner()){
			//if a line was hit, kill the runner
			if(hitWall) r.kill();
		}
		else{
			//collide the runner with the wall
			if(hitWall){
				double move = moveDistance;
				if(move > settings.getRunnerMaxSpeed()) move = settings.getRunnerMaxSpeed();
				if(r.getSpeed() > 0) move *= -1;
				r.addX(Math.cos(Math.toRadians(r.getCurrentAngle())) * move);
				r.addY(Math.sin(Math.toRadians(r.getCurrentAngle())) * move);
				
				//reduce the speed of the runner by half, only if the speed of the runner is above a threshold
				if(Math.abs(r.getSpeed()) > Math.abs(settings.getRunnerMaxSpeed() * .1)) r.setSpeed(r.getSpeed() * .5);
			}
		}
	}
	
	/**
	 * Save this track to the given PrintWriter
	 * @param write
	 */
	public void save(PrintWriter write){
		//save the starting point
		write.println(startingPoint.x + " " + startingPoint.y + " " + startingAngle);
		
		//save the track lines
		write.println(trackLines.length);
		for(Line2D.Double l : trackLines) write.println(l.x1 + " " + l.y1 + " " + l.x2 + " " + l.y2);
		
		//save the fitness lines
		write.println(fitnessLines.length);
		for(FitnessLine l : fitnessLines) write.println(l.x1 + " " + l.y1 + " " + l.x2 + " " + l.y2 + " " + Settings.boolToInt(l.isPositive()));
		
		write.println();
	}
	
	/**
	 * Load in the data for the next track object from the given scanner
	 * @param scan
	 */
	public void load(Scanner scan){
		//load starting point
		startingPoint = new Point2D.Double(scan.nextDouble(), scan.nextDouble());
		
		//load starting angle
		startingAngle = scan.nextDouble();
		
		//load track lines
		trackLines = new Line2D.Double[scan.nextInt()];
		for(int i = 0; i < trackLines.length; i++) trackLines[i] = new Line2D.Double(scan.nextDouble(), scan.nextDouble(), scan.nextDouble(), scan.nextDouble());
		
		//load fitness lines
		fitnessLines = new FitnessLine[scan.nextInt()];
		for(int i = 0; i < fitnessLines.length; i++) fitnessLines[i] = new FitnessLine(scan.nextDouble(), scan.nextDouble(), scan.nextDouble(), scan.nextDouble(), scan.nextInt() == 1);
		
		//calculate things
		drawImage();
	}
	
	/**
	 * Load this track with the given file name, file should already include .txt
	 * @param trackName
	 */
	public void load(String trackName){
		try{
			Scanner scan = new Scanner(new File("./data/tracks/" + trackName));
			load(scan);
			scan.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Removes the closest track line or fitness line within the range of the given coordinates. If no line is in range, nothing is removed
	 * @param x
	 * @param y
	 * @param range
	 */
	public void removeLine(double x, double y, double range){
		if(trackLines.length == 0 && fitnessLines.length == 0) return;
		
		//true if a track line is the closest, false if a fitness line is the closest
		boolean trackNear = true;
		double dist = -1;
		int index = -1;
		
		//search trackLines
		for(int i = 0; i < trackLines.length; i++){
			double d = trackLines[i].ptSegDist(x, y);
			if(dist == -1 || d < dist){
				dist = d;
				index = i;
			}
		}
		//search fitnessLines
		for(int i = 0; i < fitnessLines.length; i++){
			double d = fitnessLines[i].ptSegDist(x, y);
			if(dist == -1 || d < dist){
				trackNear = false;
				dist = d;
				index = i;
			}
		}
		
		//if nothing is in range, do nothing
		if(dist > range || index == -1) return;
		
		//if the to remove is a trackLine
		if(trackNear) removeTrackLine(index);
		//if the to remove is a fitnessLine
		else removeFitnessLine(index);
	}
	
	/**
	 * Removes the track line of the given index
	 * @param index
	 */
	public void removeTrackLine(int index){
		Line2D.Double[] lines = new Line2D.Double[trackLines.length - 1];
		int cnt = 0;
		for(int i = 0; i < trackLines.length; i++){
			if(i != index){
				lines[cnt] = trackLines[i];
				cnt++;
			}
		}
		trackLines = lines;
	}

	/**
	 * Removes the fitness line of the given index
	 * @param index
	 */
	public void removeFitnessLine(int index){
		FitnessLine[] lines = new FitnessLine[fitnessLines.length - 1];
		int cnt = 0;
		for(int i = 0; i < fitnessLines.length; i++){
			if(i != index){
				lines[cnt] = fitnessLines[i];
				cnt++;
			}
		}
		fitnessLines = lines;
	}
	
	/**
	 * Swaps the boolean value of the closest fitness line within the given range. If no line is found within the range nothing happens
	 * @param x
	 * @param y
	 * @param range
	 */
	public void swapFitnessLine(double x, double y, double range){
		
		int index = -1;
		
		//find the closest line
		for(int i = 0; i < fitnessLines.length; i++){
			if(index == -1 || fitnessLines[index].ptSegDist(x, y) > fitnessLines[i].ptSegDist(x, y)) index = i;
		}
		
		//if no line is in the range, do nothing
		if(index == -1 || fitnessLines[index].ptSegDist(x, y) > range) return;
		
		//otherwise, swap the fitnessLine
		fitnessLines[index].setPositive(!fitnessLines[index].isPositive());
		fitnessLines[index].calculateInfo();
	}
	
	/**
	 * Get the trackLine at the given index
	 * @param index
	 * @return
	 */
	public Line2D.Double getTrackLine(int index){
		return trackLines[index];
	}
	/**
	 * Get the fitnessLine at the given index
	 * @param index
	 * @return
	 */
	public Line2D.Double getFitnessLine(int index){
		return fitnessLines[index];
	}
	
	/**
	 * Add the given x and y to the values of the line at the given index and type
	 * @param x the amount to add on the x axis of both points of the line
	 * @param y the amount to add on the y axis of both points of the line
	 * @param index the index of the list
	 * @param track	true if the index is from the trackLines, false if it is from the fitnessLines
	 */
	public void addLinePos(double x, double y, int index, boolean track){
		if(track){
			trackLines[index].x1 += x;
			trackLines[index].y1 += y;
			trackLines[index].x2 += x;
			trackLines[index].y2 += y;
		}
		else{
			fitnessLines[index].x1 += x;
			fitnessLines[index].y1 += y;
			fitnessLines[index].x2 += x;
			fitnessLines[index].y2 += y;
			fitnessLines[index].calculateInfo();
		}
	}
	
	/**
	 * Returns the index of the closest track line or fitness line within the given range of the given coordinates. 
	 * If the closest line is a trackLine, returns a positive number that is 1 higher than the actual index. 
	 * If the closest line is a fitnessLine, returns a negative number, whose absolute value is 1 higher than the actual index. 
	 * Returns 0 if no line is found
	 * @param x
	 * @param y
	 * @param range
	 * @return
	 */
	public int getClosestLineIndex(double x, double y, double range){
		if(trackLines.length == 0 && fitnessLines.length == 0) return 0;
		
		//true if a track line is the closest, false if a fitness line is the closest
		boolean trackNear = true;
		double dist = -1;
		int index = -1;
		
		//search trackLines
		for(int i = 0; i < trackLines.length; i++){
			double d = trackLines[i].ptSegDist(x, y);
			if(dist == -1 || d < dist){
				dist = d;
				index = i;
			}
		}
		//search fitnessLines
		for(int i = 0; i < fitnessLines.length; i++){
			double d = fitnessLines[i].ptSegDist(x, y);
			if(dist == -1 || d < dist){
				trackNear = false;
				dist = d;
				index = i;
			}
		}
		
		if(index == -1 || dist > range) return 0;
		
		if(trackNear) return index + 1;
		else return -(index + 1);
	}
	
	/**
	 * Find the closest end point of a trackLine or fitnessLine that is within the range of the given coordinates. 
	 * Returns null if no valid point is within range
	 * @param x
	 * @param y
	 * @param range
	 * @return
	 */
	public Point2D.Double getClosestEndPoint(double x, double y, double range){
		boolean trackNear = true;
		boolean p1 = true;
		int index = -1;
		double dist = 0;
		
		//search trackLines
		for(int i = 0; i < trackLines.length; i++){
			double d = trackLines[i].getP1().distance(x, y);
			if(index == -1 || d <= dist){
				dist = d;
				index = i;
				p1 = true;
			}
			d = trackLines[i].getP2().distance(x, y);
			if(index == -1 || d <= dist){
				dist = d;
				index = i;
				p1 = false;
			}
		}
		//search fitnessLines
		for(int i = 0; i < fitnessLines.length; i++){
			double d = fitnessLines[i].getP1().distance(x, y);
			if(index == -1 || d <= dist){
				dist = d;
				index = i;
				p1 = true;
				trackNear = false;
			}
			d = fitnessLines[i].getP2().distance(x, y);
			if(index == -1 || d <= dist){
				dist = d;
				index = i;
				p1 = false;
				trackNear = false;
			}
		}
		
		//if no point was found, return null
		if(index == -1 || dist > range) return null;
		
		//otherwise, return the corresponding point
		
		Line2D.Double l;
		if(trackNear) l = trackLines[index];
		else l = fitnessLines[index];
		
		if(p1) return (java.awt.geom.Point2D.Double)l.getP1();
		else return (java.awt.geom.Point2D.Double)l.getP2();
	}
	
	/**
	 * Find the closest end point of a trackLine or fitnessLine within the range of the given coordinates (x, y), 
	 * and set the coordinates of that point to the given coordinates (mx, my). 
	 * Does nothing if no point is found within the given range
	 * @param x
	 * @param y
	 * @param mx
	 * @param my
	 * @param range
	 */
	public void setClosestLineLoc(double x, double y, double mx, double my, double range){
		boolean trackNear = true;
		boolean p1 = true;
		int index = -1;
		double dist = 0;
		
		//search trackLines
		for(int i = 0; i < trackLines.length; i++){
			double d = new Point2D.Double(trackLines[i].x1, trackLines[i].y1).distance(x, y);
			if(index == -1 || d < dist){
				dist = d;
				index = i;
				p1 = true;
			}
			d = new Point2D.Double(trackLines[i].x2, trackLines[i].y2).distance(x, y);
			if(index == -1 || d < dist){
				dist = d;
				index = i;
				p1 = false;
			}
		}
		//search fitnessLines
		for(int i = 0; i < fitnessLines.length; i++){
			double d = new Point2D.Double(fitnessLines[i].x1, fitnessLines[i].y1).distance(x, y);
			if(index == -1 || d < dist){
				dist = d;
				index = i;
				p1 = true;
				trackNear = false;
			}
			d = new Point2D.Double(fitnessLines[i].x2, fitnessLines[i].y2).distance(x, y);
			if(index == -1 || d < dist){
				dist = d;
				index = i;
				p1 = false;
				trackNear = false;
			}
		}
		
		//return if no point is found
		if(index == -1 || dist > range) return;
		
		if(trackNear){
			if(p1){
				trackLines[index].x1 = mx;
				trackLines[index].y1 = my;
			}
			else{
				trackLines[index].x2 = mx;
				trackLines[index].y2 = my;
			}
		}
		else{
			if(p1){
				fitnessLines[index].x1 = mx;
				fitnessLines[index].y1 = my;
			}
			else{
				fitnessLines[index].x2 = mx;
				fitnessLines[index].y2 = my;
			}
			fitnessLines[index].calculateInfo();
		}
	}
	
}
