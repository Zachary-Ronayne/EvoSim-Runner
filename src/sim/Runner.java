package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;

import data.Settings;

public class Runner{
	
	private Settings settings;
	
	/**
	 * The track this runner is in
	 */
	private Track track;
	
	/**
	 * The x coordinate of the center of this runner
	 */
	private double x;
	/**
	 * The y coordinate of the center of this runner
	 */
	private double y;
	/**
	 * The x from the previous tick
	 */
	private double lastX;
	/**
	 * The y from the previous tick
	 */
	private double lastY;
	/**
	 * The current speed of this runner
	 */
	private double speed;
	/**
	 * The speed this runner is trying to get to
	 */
	private double goalSpeed;
	/**
	 * The current angle, in degrees, that this runner is facing
	 */
	private double angle;
	/**
	 * The angle this runner is trying to get to
	 */
	private double goalAngle;
	/**
	 * True if this runner has hit a wall and should no longer move, false otherwise
	 */
	private boolean dead;
	
	/**
	 * The ID this runner was given at birth
	 */
	private int runnerID;
	/**
	 * The ID that the parent of this runner had, -1 if this runner is created initially and has no parent
	 */
	private int parentID;
	/**
	 * the generation that this runner was born in
	 */
	private int birthGen;
	
	/**
	 * The neural net that controls this runner
	 */
	private NeuralNet brain;
	/**
	 * The fitness of this runner in their current moving state. Default value is -1, meaning the runner has not been tested yet
	 */
	private double currentFitness;
	/**
	 * The fitness of this runner after it has been tested
	 */
	private double storedFitness;
	
	/**
	 * The next id for a runner, increments every time a runner object is created
	 */
	private static int currentRunnerID = 0;
	
	public Runner(Settings settings, Track track){
		this.settings = settings;
		
		this.track = track;
		
		reset();
		storedFitness = 0;
		
		runnerID = currentRunnerID++;
		parentID = -1;
		birthGen = 0;
		
		int[] nodes = new int[2 + settings.getRunnerHiddenNodes().length];
		nodes[0] = 10;
		for(int i = 1; i < nodes.length - 1; i++) nodes[i] = settings.getRunnerHiddenNodes()[i - 1];
		nodes[nodes.length - 1] = 2;
		brain = new NeuralNet(settings, nodes);
		
		brain.randomWeights();
	}
	
	/**
	 * Get the fitness of this runner
	 * @return
	 */
	public double getFitness(){
		return currentFitness;
	}
	public void addFitness(double f){
		currentFitness += f;
	}
	public double getStoredFitness(){
		return storedFitness;
	}
	
	/**
	 * Reset the runner to a default state of position
	 */
	public void reset(){
		x = 500;
		y = 500;
		lastX = x;
		lastY = y;
		speed = 0;
		goalSpeed = 0;
		angle = 0;
		goalAngle = 0;
		dead = false;
		currentFitness = 0;
	}
	
	/**
	 * Get a runner that is like this runner, but slightly mutated
	 * @param gen
	 * @return
	 */
	public Runner getOffspring(int gen){
		NeuralNet offspringBrain = brain.getMutatedBrain();
		Runner offspring = new Runner(settings, track);
		offspring.parentID = runnerID;
		offspring.birthGen = gen;
		offspring.brain = offspringBrain;
		return offspring;
	}
	
	/**
	 * Has this runner run around the track and determine its fitness
	 */
	public void testRunner(){
		track.enterRunner(this);
		storedFitness = -1;
		int timer = 0;
		while(timer < settings.getRunnerTestTime() && !dead){
			timer++;
			track.collideWithRunner(this);
			tick();
		}
		storedFitness = currentFitness;
	}
	
	public double getMutability(){
		return brain.getMutability();
	}
	
	public NeuralNet getBrain(){
		return brain;
	}
	
	public int getRunnerID(){
		return runnerID;
	}
	public int getParentID(){
		return parentID;
	}
	public int getBirthGen(){
		return birthGen;
	}
	
	public Point2D.Double getPos(){
		return new Point2D.Double(x, y);
	}
	
	public double getX(){
		return x;
	}
	public void setX(double x){
		this.x = x;
	}
	public void addX(double x){
		this.x += x;
	}
	public double getLastX(){
		return lastX;
	}
	
	public double getY(){
		return y;
	}
	public void setY(double y){
		this.y = y;
	}
	public void addY(double y){
		this.y += y;
	}
	public double getLastY(){
		return lastY;
	}
	public double getCurrentAngle(){
		return angle;
	}
	public void setCurrentAngle(double a){
		angle = a;
	}
	
	
	public void setSpeed(double s){
		s = Math.min(s, settings.getRunnerMaxSpeed());
		s = Math.max(s, settings.getRunnerMinSpeed());
		speed = s;
	}
	public double getSpeed(){
		return speed;
	}
	/**
	 * Kill this runner and make them stop moving
	 */
	public void kill(){
		dead = true;
	}
	public boolean dead(){
		return dead;
	}
	
	/**
	 * Update this runner in real time
	 */
	public void tick(){
		//store last x and y
		lastX = x;
		lastY = y;
		
		//calculate inputs
		//distance inputs
		double[] inputs = new double[10];
		
		Line2D.Double[] lines = track.getLines();
		for(int a = 0; a < 8; a++){
			double distance = settings.getRunnerMaxViewDistance();
			boolean distFound = false;
			for(int i = 0; i < lines.length; i++){
				Line2D.Double l = lines[i];
				
				//find slope and intercept of the track line
				double tSlope = (l.y1 - l.y2) / (l.x1 - l.x2);
				double tInt = l.y1 - l.x1 * tSlope;
				
				//find slope and intercept of vision line
				double vSlope = Math.sin(Math.toRadians(angle + settings.getRunnerVisionAngles()[a])) / Math.cos(Math.toRadians(angle + settings.getRunnerVisionAngles()[a]));
				double vInt = y - x * vSlope;
				
				boolean vVertical = Math.cos(Math.toRadians(angle + settings.getRunnerVisionAngles()[a])) == 0;
				boolean tVertical = l.x1 == l.x2;
				
				//if the lines are not parallel, continue
				if(vSlope != tSlope && !(tVertical && vVertical)){
					//find intersection point
					double intX = (tInt - vInt) / (vSlope - tSlope);
					double intY = intX * vSlope + vInt;
					
					//if either slope is vertical, then the intersection must be calculated differently
					if(vVertical){
						intX = x;
						intY = intX * tSlope + tInt;
					}
					else if(tVertical){
						intX = l.x1;
						intY = intX * vSlope + vInt;
					}
					
					//check to see if the vision line intersects the track line
					//if it does, find its distance
					//also needs to account for if the vision line, which is a ray, actually hits the track line segment
					double lDist = new Point2D.Double(l.x1, l.y1).distance(l.x2, l.y2);
					
					//find the angle of the vision line
					double testAngle = settings.getRunnerVisionAngles()[a] + angle;
					while(testAngle > 360) testAngle -= 360;
					while(testAngle < 0) testAngle += 360;
					
					//find the angle from the the center of the runner to the intersection point
					double toAngle = Math.toDegrees(Math.PI + Math.atan2(y - intY, x - intX));
					while(toAngle > 360) toAngle -= 360;
					while(toAngle < 0) toAngle += 360;
					
					//compare the difference between the two previously calculated angles to see if th
					
					if(new Point2D.Double(l.x1, l.y1).distance(intX, intY) <= lDist &&
					   new Point2D.Double(l.x2, l.y2).distance(intX, intY) <= lDist &&
					   (Math.abs(testAngle - toAngle) < 90) || Math.abs(testAngle - toAngle) > 270){
						//find new distance
						double newDist = getPos().distance(intX, intY) - settings.getRunnerRadius();
						
						if(!distFound || distance > newDist){
							distance = newDist;
							distFound = true;
						}
					}
				}
			}
			
			distance = Math.min(distance, settings.getRunnerMaxViewDistance());
			inputs[a] = distance / settings.getRunnerMaxViewDistance();
		}
		
		//angle and speed inputs
		inputs[8] = speed / Math.max(Math.abs(settings.getRunnerMaxSpeed()), Math.abs(settings.getRunnerMinSpeed()));
		double sendAngle = angle;
		while(sendAngle < 0) sendAngle += 360;
		while(sendAngle > 360) sendAngle -= 360;
		sendAngle = Math.sqrt(1 - Math.pow(sendAngle / 180 - 1, 2));
		sendAngle = sendAngle * 2 - 1;
		inputs[9] = sendAngle;
		
		//send inputs
		brain.setInputs(inputs);
		
		//calculate outputs
		brain.calculateOutputs();
		
		//use outputs
		goalSpeed = settings.getRunnerSpeedChange() * brain.getNodeValue(brain.getNumLayers() - 1, 0);
		goalAngle = settings.getRunnerAngleChange() * brain.getNodeValue(brain.getNumLayers() - 1, 1);
		
		//if the runner has hit a wall, then it should no longer move, so the method quits from here
		if(dead) return;
		
		speed += goalSpeed;
		
		//ensure that speed is a valid speed
		if(speed < settings.getRunnerMinSpeed()) speed = settings.getRunnerMinSpeed();
		else if(speed > settings.getRunnerMaxSpeed()) speed = settings.getRunnerMaxSpeed();
		
		//the following line is for chaning the angle
		angle += goalAngle;
		
		//ensure that the angle and goal angles are valid angles where 0 <= angle < 360
		while(angle < 0) angle += 360;
		while(angle >= 360) angle -= 360;
		
		//update the position of the runner based on their speed and angle
		x += Math.cos(Math.toRadians(angle)) * speed;
		y += Math.sin(Math.toRadians(angle)) * speed;
		
		//calculate the fitness to add based on the distances from each wall
		if(!dead){
			double valueTotal = 0;
			double weightTotal = 0;
			//some vision lines are more important than others
			for(int i = 0; i < 8; i++){
				valueTotal += brain.getNodeValue(0, i) * settings.getRunnerVisionWeights()[i];
				weightTotal += settings.getRunnerVisionWeights()[i];
			}
			//add fitness for this tick. The runner moving faster and being further from walls means higher fitness
			double speedFactor = (Math.abs(speed) / settings.getRunnerMaxSpeed());
			speedFactor = Math.pow(speedFactor, Math.E);
			double addFit = settings.getRunnerFitnessBaseGain();
			if(settings.getRunnerFitnessSpeed()) addFit *= speedFactor;
			if(settings.getRunnerFitnessWall()) addFit *= valueTotal / weightTotal;
			addFitness(addFit);
		}
	}
	
	/**
	 * Draw this runner to the given graphics object, based on the camera position and the scale to resizing
	 * @param g
	 * @param drawLines
	 * @param scale
	 */
	public void render(Graphics2D g, Point2D.Double cameraPos, boolean drawLines){
		Color blackC;
		Color bodyC;
		
		if(dead()){
			blackC = new Color(0, 0, 0, 100);
			bodyC = new Color(100, 100, 255, 100);
		}
		else{
			blackC = new Color(0, 0, 0);
			bodyC = new Color(100, 100, 255);
		}
		
		//camera variables, should be the center point of the runner when rendered on the graphics object
		double xx = x + cameraPos.x;
		double yy = y + cameraPos.y;
		
		//draw the vision lines
		if(drawLines){
			g.setColor(blackC);
			g.setStroke(new BasicStroke(2f));
			for(int a = 0; a < settings.getRunnerVisionAngles().length; a++){
				g.drawLine((int)Math.round(xx), (int)Math.round(yy),
						   (int)Math.round(xx + Math.cos(Math.toRadians(angle + settings.getRunnerVisionAngles()[a])) * (settings.getRunnerMaxViewDistance() + settings.getRunnerRadius())),
						   (int)Math.round(yy + Math.sin(Math.toRadians(angle + settings.getRunnerVisionAngles()[a])) * (settings.getRunnerMaxViewDistance() + settings.getRunnerRadius())));
			}
		}
		
		//draw body
		//create image
		BufferedImage body = new BufferedImage((int)Math.round(settings.getRunnerRadius() * 2), (int)Math.round(settings.getRunnerRadius() * 2), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g2 = body.getGraphics();
		
		//main circle
		g2.setColor(blackC);
		g2.fillOval(0, 0, (int)Math.round(settings.getRunnerRadius() * 2), (int)Math.round(settings.getRunnerRadius() * 2));
		g2.setColor(bodyC);
		g2.fillOval(2, 2, (int)Math.round(settings.getRunnerRadius() * 2 - 4), (int)Math.round(settings.getRunnerRadius() * 2 - 4));
		
		//draw eye
		g2.setColor(blackC);
		int r = 8;
		double d = 30 * (settings.getRunnerRadius() / 50);
		g2.fillOval((int)Math.round(d * Math.cos(Math.toRadians(angle)) - r + settings.getRunnerRadius()),
					(int)Math.round(d * Math.sin(Math.toRadians(angle)) - r + settings.getRunnerRadius()), r * 2, r * 2);
		
		//draw id
		g2.setFont(new Font(settings.getFontName(), Font.PLAIN, 10));
		g2.drawString("" + getRunnerID(), (int)(settings.getRunnerRadius() * .6), (int)(settings.getRunnerRadius() + 5));
		
		//draw body on screen
		g.drawImage(body, (int)Math.round(xx - settings.getRunnerRadius()), (int)Math.round(yy -settings.getRunnerRadius()),
						  (int)Math.round(body.getWidth()), (int)Math.round(body.getHeight()), null);
	}
	
	/**
	 * draw advanced info for this runner on the specified graphics object at the specified coordinates. Advanced info includes: 
	 * ID, parentID, birthGen, mutability, fitness
	 * @param g
	 * @param x
	 * @param y
	 */
	public void renderAdvancedInfo(Graphics g, int x, int y){
		g.setColor(Color.BLACK);
		g.fillRect(x, y, 250, 104);
		
		g.setColor(Color.WHITE);
		g.fillRect(x + 2, y + 2, 246, 100);
		
		g.setColor(new Color(0, 0, 255, 80));
		g.fillRect(x + 2, y + 2, 246, 100);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font(settings.getFontName(), Font.PLAIN, 12));
		
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(15);
		
		g.drawString("ID: " + getRunnerID(), x + 4, y + 12);
		g.drawString("Fitness: " + df.format(getStoredFitness()), x + 4, y + 24);
		if(getParentID() == -1) g.drawString("Parent ID: WILD", x + 4, y + 36);
		else g.drawString("Parent ID: " + getParentID(), x + 4, y + 36);
		g.drawString("Birth Gen: " + getBirthGen(), x + 4, y + 48);
		g.drawString("Mutability: " + df.format(getMutability()), x + 4, y + 60);
	}
	
	/**
	 * Save this runner to the given PrintWriter. Only saves data essential to this runner like its brain, ID, and so on
	 * @param write
	 */
	public void save(PrintWriter write){
		//save misc data
		write.println(runnerID);
		write.println(parentID);
		write.println(birthGen);
		write.println(storedFitness);
		
		//save brain
		brain.save(write);
	}
	
	/**
	 * Loads in this runner from the given scanner
	 * @param scan
	 */
	public void load(Scanner scan){
		///laod misc data
		runnerID = scan.nextInt();
		parentID = scan.nextInt();
		birthGen = scan.nextInt();
		storedFitness = scan.nextDouble();
		
		//load brain
		brain.load(scan);
	}
	
	/**
	 * Resets the starting runner ID to 0. WARNING, once this is called, all previously created runner objects will have invalid ids
	 */
	public static void resetRunnerIDs(){
		currentRunnerID = 0;
	}
	
	/**
	 * Sets the CurrentRunnerID
	 * WARNING should only be used when loading a simulation.
	 * @param id
	 */
	public static void setCurrentRunnerID(int id){
		currentRunnerID = id;
	}
	
	/**
	 * Get the current runner ID of the current simulation
	 * @return
	 */
	public static int getCurrentRunnerID(){
		return currentRunnerID;
	}
}
