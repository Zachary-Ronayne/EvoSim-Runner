package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Scanner;

import data.Settings;

public class NeuralNet{
	
	/**
	 * The amount of x and y that is added from the top of a net image before the first node
	 */
	public static final int NET_LOC = 30;
	
	private Settings settings;
	
	/**
	 * A buffered image that represents the lines drawn for this neural net, the lines represent the weights
	 */
	private BufferedImage netLines;
	
	/**
	 * The current values of each layer connected by weights. 
	 * The following describes what each part of the 2D array represents: 
	 * layers[layer index][node index] = value of the node in that layer at that node index
	 * there will always be at least 2 layers. 
	 * All but the last layer, the output layer, will have a constant node that will only connect to the next layer, not the previous
	 */
	private double[][] layers;
	
	/**
	 * The weights of every connection this neural net has. 
	 * The following describes what each part of the 3D array represents: 
	 * weights[layer index][index of the node closest to inputs][index of the node closest to outputs] = the weight between those 2 nodes at that layer index. 
	 * The layer index, in this case, describes which spot in between 2 layers this weight is at, ie layer index between 0 and 1 for layers is 0 for weigths, 
	 * and the max layer index of weights will always be exactly one less than the max layer index of layers. 
	 * Each weight is always between -1 and 1
	 */
	private double[][][] weights;

	/**
	 * The rate at which a node weight can change, plus or minus half of the variable
	 */
	private double mutability;
	/**
	 * the position that the neural net is being drawn to, is not relevant if this net is not being drawn to the screen
	 */
	private Point drawnLoc;
	/**
	 * true if all lines of the neural net should be drawn, false if only lines from one node that the mouse is hovoring over should be displayed
	 */
	private boolean displayAllLines;
	
	/**
	 * Create an empty neural net, where every weight is 0, and every node value is 0. 
	 * For sizes: sizes[number of nodes in that layer, excluding the hidden node]
	 * @param sizes
	 */
	public NeuralNet(Settings settings, int[] sizes){
		this.settings = settings;
		
		//create the layers object, adding in one node, the constant node, to each of the layers except for the output layer,
		//and setting all values to 0, except for the constant node, which is set to 1
		layers = new double[sizes.length][0];
		for(int i = 0; i < layers.length; i++){
			if(i != layers.length - 1) layers[i] = new double[sizes[i] + 1];
			else layers[i] = new double[sizes[i]];
			for(int j = 0; j < layers[i].length; j++){
				if(j == layers[i].length - 1 && i != layers.length - 1) layers[i][j] = 1;
				else layers[i][j] = 0;
			}
		}
		
		//Create the sizes of the weights array for each of the corresponding layer lengths, based on the specified lengths in sizes, and set all weights to 0
		weights = new double[layers.length - 1][0][0];
		for(int i = 0; i < weights.length; i++){
			weights[i] = new double[layers[i].length][layers[i + 1].length];
			for(int j = 0; j < weights[i].length; j++){
				for(int h = 0; h < weights[i][j].length; h++){
					weights[i][j][h] = 0;
				}
			}
		}
		mutability = settings.getMinMutability() + Math.random() * (settings.getMaxMutability() - settings.getMinMutability());
		
		drawnLoc = new Point(0, 0);
		displayAllLines = true;
	}
	
	/**
	 * Assigns this NeuralNet a new mutability
	 */
	public double newMutability(){
		double m = mutability + (Math.random() - .5) * settings.getMutabilityChange();
		m = Math.min(m, settings.getMaxMutability());
		m = Math.max(m, settings.getMinMutability());
		return m;
	}
	
	/**
	 * Assign this neural net with new random weights for the weights object, weights will always be between -1 and 1
	 */
	public void randomWeights(){
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < weights[i].length; j++){
				for(int h = 0; h < weights[i][j].length; h++){
					weights[i][j][h] = (Math.random() - .5) * 2;
				}
			}
		}
	}
	
	/**
	 * Set all the values of all the inputs, inputs must be the same size as the neural nets input node layer, excluding the hiden node
	 * @param inputs
	 */
	public void setInputs(double... inputs){
		for(int i = 0; i < inputs.length; i++){
			layers[0][i] = inputs[i];
		}
	}
	
	/**
	 * Determines the values of each node in each layer, to find the proper outputs based on the inputs
	 */
	public void calculateOutputs(){
		//ensure the constant nodes are at 1
		for(int i = 0; i < layers.length - 1; i++) layers[i][layers[i].length - 1] = 1;
			
		//do the calculations
		for(int i = 1; i < layers.length; i++){
			
			double[] totals = new double[layers[i].length];
			for(int j = 0; j < totals.length; j++) totals[j] = 0;
			
			for(int j = 0; j < weights[i - 1].length; j++){
				for(int h = 0; h < weights[i - 1][j].length; h++) 
					totals[h] += weights[i - 1][j][h] * layers[i - 1][j];
			}
			for(int j = 0; j < totals.length; j++){
				layers[i][j] = sigmoid(totals[j]);
			}
		}
		
		//ensure the constant nodes are at 1 again
		for(int i = 0; i < layers.length - 1; i++) layers[i][layers[i].length - 1] = 1;
	}
	
	/**
	 * Get the value of the node in layer i at node j
	 * @param i
	 * @param j
	 * @return
	 */
	public double getNodeValue(int i, int j){
		return layers[i][j];
	}
	
	/**
	 * The number of layers, including the in and output layers
	 * @return
	 */
	public int getNumLayers(){
		return layers.length;
	}
	
	/**
	 * Get the mutability of this NeuralNet which determines how much this net can mutater
	 * @return
	 */
	public double getMutability(){
		return mutability;
	}
	
	/**
	 * Get a slightly mutated version of this NeuralNet
	 * @return
	 */
	public NeuralNet getMutatedBrain(){
		//make a new net of the same size as this net
		NeuralNet mutatedNet = new NeuralNet(settings, getSizes());
		
		//set all the layer values to be the same as this net
		for(int i = 0; i < layers.length; i++){
			for(int j = 0; j < layers[i].length; j++){
				mutatedNet.layers[i][j] = layers[i][j];
			}
		}
		
		mutatedNet.mutability = newMutability();
		
		//set all the weights values to be the same as this net, but mutated slightly
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < weights[i].length; j++){
				for(int h = 0; h < weights[i][j].length; h++){
					//calculate a new weight
					double w =  weights[i][j][h] + (Math.random() - .5) * mutatedNet.mutability;
					
					//ensure that the new weight is between -1 and 1 inclusive
					w = Math.min(w, 1);
					w = Math.max(w, -1);
					
					//set the new weight
					mutatedNet.weights[i][j][h] = w;
				}
			}
		}
		
		return mutatedNet;
	}
	
	/**
	 * Get the number of nodes in each layer, excluding hidden nodes, formatted in the same way as the parameter for the creation of a new NeuralNet
	 * @return
	 */
	public int[] getSizes(){
		int[] sizes = new int[layers.length];
		for(int i = 0; i < layers.length; i++){
			if(i < layers.length - 1) sizes[i] = layers[i].length - 1;
			else sizes[i] = layers[i].length;
		}
		return sizes;
	}
	
	/**
	 * Get the net lines of this neural net
	 * @return
	 */
	public BufferedImage getNetLines(){
		return netLines;
	}
	
	/**
	 * Returns a buffered image that has a display of the current state of the neural net
	 * @return
	 */
	public BufferedImage getDisplay(){
		BufferedImage img = new BufferedImage(netLines.getWidth(), netLines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = img.getGraphics();
		g.drawImage(netLines, 0, 0, null);
		
		for(int i = 0; i < layers.length; i++){
			for(int j = 0; j < layers[i].length; j++){
				
				int spaceNodes = layers[i].length - 1;
				if(spaceNodes < 1) spaceNodes = 1;
				
				int nodeSpacing = (img.getHeight() - NET_LOC * 2) / spaceNodes;
				
				g.setColor(Color.BLACK);
				g.fillOval(NET_LOC - settings.getNodeDispRadius() + i * settings.getNodeDispSpacing(),
						   NET_LOC - settings.getNodeDispRadius() + j * nodeSpacing,
						   settings.getNodeDispRadius() * 2, settings.getNodeDispRadius() * 2);
				double node = layers[i][j];
				if(node < 0){
					int fade = (int)(255 * (1 - node / -1.0));
					fade = Math.min(fade, 255);
					fade = Math.max(fade, 0);
					g.setColor(new Color(255, fade, fade));
				}
				else{
					int fade = (int)(255 * (1 - node / 1.0));
					fade = Math.min(fade, 255);
					fade = Math.max(fade, 0);
					g.setColor(new Color(fade, fade, 255));
				}
				g.fillOval(NET_LOC - settings.getNodeDispRadius() + 2 + i * settings.getNodeDispSpacing(),
						   NET_LOC - settings.getNodeDispRadius() + 2 + j * nodeSpacing,
						   settings.getNodeDispRadius() * 2 - 4, settings.getNodeDispRadius() * 2 - 4);
				g.setColor(Color.BLACK);
				g.setFont(new Font(settings.getFontName(), Font.BOLD, 15));
				g.drawString("" + (int)Math.round(node * 1000), NET_LOC + i * settings.getNodeDispSpacing() - 15, NET_LOC + j * nodeSpacing + 5);
			}
		}
		
		return img;
	}
	
	/**
	 * erases the netLines image to save space
	 */
	public void eraseNetLines(){
		netLines = null;
	}
	
	/**
	 * Draws the net lines representing the weights of this neural net to the corresponding image
	 */
	public void drawNetLines(){
		drawNetLines(0, 0);
	}
	/**
	 * Draws the net lines representing the weights of this neural net to the corresponding image, 
	 * the x and y represent the location of the mouse for to decide if only certain lines should display
	 * @param x
	 * @param y
	 */
	public void drawNetLines(int x, int y){
		resetNetLineImage();
		Graphics2D g = (Graphics2D)netLines.getGraphics();
		g.setStroke(new BasicStroke(2f));
		g.setColor(Color.BLACK);
		int ww = netLines.getWidth();
		int hh = netLines.getHeight();
		g.drawLine(0, 0, ww, 0);
		g.drawLine(ww, 0, ww, hh);
		g.drawLine(ww, hh, 0, hh);
		g.drawLine(0, hh, 0, 0);
		
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < weights[i].length; j++){
				for(int h = 0; h < weights[i][j].length; h++){
					int spaceNodes = layers[i + 1].length - 1;
					if(spaceNodes < 1) spaceNodes = 1;
					int nodeSpacing1 = (hh - NET_LOC * 2) / spaceNodes;

					spaceNodes = layers[i].length - 1;
					if(spaceNodes < 1) spaceNodes = 1;
					int nodeSpacing2 = (hh - NET_LOC * 2) / spaceNodes;
					
					int x1 = NET_LOC + (i + 1) * settings.getNodeDispSpacing();
					int y1 = NET_LOC + h * nodeSpacing1;
					int x2 = NET_LOC+ i * settings.getNodeDispSpacing();
					int y2 = NET_LOC + j * nodeSpacing2;
					
					//only draw a line if all lines should be drawn, or the mouse is on the node that should have lines coming from it
					if(displayAllLines ||
							settings.getNodeDispRadius() > new Point(x - drawnLoc.x, y - drawnLoc.y).distance(x1, y1) ||
							settings.getNodeDispRadius() > new Point(x - drawnLoc.x, y - drawnLoc.y).distance(x2, y2)){
						double weight = weights[i][j][h];
						if(weight < 0) g.setColor(new Color(255, 0, 0, (int)(255 * (weight / -1.0))));
						else g.setColor(new Color(0, 0, 255, (int)(255 * (weight / 1.0))));
						g.drawLine(x1, y1, x2, y2);
					}
				}
			}
		}
	}
	
	/**
	 * resets the net line buffered image object to a default state
	 */
	public void resetNetLineImage(){
		int bigIndex = -1;
		for(int i = 0; i < layers.length; i++){
			if(bigIndex == -1 || layers[i].length > layers[bigIndex].length) bigIndex = i;
		}
		
		int extraHeight = layers[bigIndex].length * 100;
		if(extraHeight > settings.getNodeDispMaxHeight()) extraHeight = settings.getNodeDispMaxHeight();
		
		netLines = new BufferedImage(NET_LOC * 2 + (layers.length - 1) * settings.getNodeDispSpacing(), 100 + extraHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	/**
	 * Set the position that the neural net is being drawn at
	 * @param x
	 * @param y
	 */
	public void setDisplayDrawPoint(int x, int y){
		drawnLoc = new Point(x, y);
	}
	public Point getDisplayDrawPoint(){
		return drawnLoc;
	}
	public void setDisplayAllLines(boolean b){
		displayAllLines = b;
	}
	public boolean getDisplayAllLines(){
		return displayAllLines;
	}
	
	public void save(PrintWriter write){
		//save misc data
		write.println(mutability);
		
		//save weight values
		write.println(weights.length);
		for(double[][] d : weights){
			write.println(d.length);
			for(double[] dd : d){
				write.println(dd.length);
				for(double ddd : dd) write.print(ddd + " ");
				write.println();
			}
		}
	}
	
	/**
	 * Load in this NeuralNet from the given Scanner
	 */
	public void load(Scanner scan){
		//load misc data
		mutability = scan.nextDouble();
		
		//load weights
		weights = new double[scan.nextInt()][0][0];
		for(int i = 0; i < weights.length; i++){
			weights[i] = new double[scan.nextInt()][0];
			for(int j = 0; j < weights[i].length; j++){
				weights[i][j] = new double[scan.nextInt()];
				for(int h = 0; h < weights[i][j].length; h++) weights[i][j][h] = scan.nextDouble();
			}
		}
	}
	
	/**
	 * Return a number between -1 and 1
	 * @param x
	 * @return
	 */
	public static double sigmoid(double x){
		return (1.0 / (1.0 + Math.pow(Math.E, -x)) - .5) * 2;
	}
	
}
