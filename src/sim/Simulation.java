package sim;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import data.Settings;

public class Simulation{
	
	private Settings settings;
	
	private Runner[] runners;
	
	/**
	 * an array of all the indexes of runners that will die next generation
	 */
	private int[] runnersToDie;
	
	/**
	 * The track that the runners are moving in
	 */
	private Track track;
	
	/**
	 * The generation that this simulation is currently up to
	 */
	private int currentGen;
	
	/**
	 * The data from the fitness of each generation
	 */
	private ArrayList<double[]> fitnessData;
	/**
	 * The data from the mutability of each generation
	 */
	private ArrayList<double[]> mutabilityData;
	
	/**
	 * The best, median, and worst runners of each generation. 
	 * Each array in the ArrayList is a set of the best, median, and worst. 
	 * arr[0] is the worst runner, arr[1] is the median runner, arr[2] is the best runner
	 */
	private ArrayList<Runner[]> runnerHistory;
	
	/**
	 * true if every runner should be tested next generation, is automatically set to false after a generation
	 */
	private boolean updateAllRunners;
	
	/**
	 * @param trackName the name of the track to load in
	 */
	public Simulation(Settings settings, String trackName){
		this.settings = settings;
		
		runnersToDie = new int[settings.getNumRunners() / 2];
		for(int i = 0; i < runnersToDie.length; i++) runnersToDie[i] = -1;
		
		runners = new Runner[settings.getNumRunners()];
		
		updateAllRunners = false;
		
		if(trackName == null){
			track = new Track(settings, new Point2D.Double(0, 0));
			track.setDefault();
		}
		else{
			track = new Track(settings, new Point2D.Double(0, 0));
			track.load(trackName);
		}
		
		currentGen = 0;
		
		fitnessData = new ArrayList<double[]>();
		mutabilityData = new ArrayList<double[]>();
		
		runnerHistory = new ArrayList<Runner[]>();
	}
	
	public ArrayList<double[]> getFitnessData(){
		return fitnessData;
	}
	public ArrayList<double[]> getMutabilityData(){
		return mutabilityData;
	}
	public ArrayList<Runner[]> getRunnerHistory(){
		return runnerHistory;
	}
	
	public Track getTrack(){
		return track;
	}
	
	/**
	 * Get the runner at the specified index
	 * @param index
	 * @return
	 */
	public Runner getRunner(int index){
		return runners[index];
	}
	public Runner[] getRunners(){
		return runners;
	}
	
	public int[] getRunnersToDie(){
		return runnersToDie;
	}
	
	public int getCurrentGen(){
		return currentGen;
	}
	
	/**
	 * Creates a new simulation and tests and sorts generation 0
	 */
	public void newSimulation(){
		Runner.resetRunnerIDs();
		runners = new Runner[settings.getNumRunners()];
		for(int i = 0; i < runners.length; i++){
			//create the runner
			runners[i] = new Runner(settings, track);
			//test the runner
			runners[i].testRunner();
		}
		sortRunners();
		findRunnersToDie();
		updateData();
	}
	
	/**
	 * Add a new spot to the data, make sure runners are sorted before this is called
	 */
	public void updateData(){
		//add fitness data
		fitnessData.add(new double[]{
			//best runner
			runners[0].getStoredFitness(),
			//10-40 percentile
			runners[runners.length / 10].getStoredFitness(), runners[runners.length / 5].getStoredFitness(),
			runners[runners.length / 10 * 3].getStoredFitness(), runners[runners.length / 5 * 2].getStoredFitness(),
			//50 percentile (median)
			runners[runners.length / 2].getStoredFitness(),
			//60-90 percentile
			runners[runners.length / 5 * 3].getStoredFitness(), runners[runners.length / 10 * 7].getStoredFitness(),
			runners[runners.length / 5 * 4].getStoredFitness(), runners[runners.length / 10 * 9].getStoredFitness(),
			//worst runner
			runners[runners.length - 1].getStoredFitness(),
		});
		
		//make and sort a list of the mutability
		double[] m = new double[runners.length];
		for(int i = 0; i < m.length; i++) m[i] = runners[i].getMutability();
		for(int i = 0; i < m.length; i++){
			int high = -1;
			for(int j = i; j < m.length; j++){
				if(high == -1 || m[high] < m[j]) high = j;
			}
			double temp = m[i];
			m[i] = m[high];
			m[high] = temp;
		}
		
		//add mutability data
		mutabilityData.add(new double[]{
				//highest mutability
				m[0],
				//10-40 percentile
				m[m.length / 10], m[m.length / 5],
				m[m.length / 10 * 3], m[m.length / 5 * 2],
				//50 percentile (median)
				m[m.length / 2],
				//60-90 percentile
				m[m.length / 5 * 3], m[m.length / 10 * 7],
				m[m.length / 5 * 4], m[m.length / 10 * 9],
				//lowest mutability
				m[m.length - 1],
			});
		
		//add runner history data
		runnerHistory.add(new Runner[]{runners[runners.length - 1], runners[runners.length / 2], runners[0]});
	}
	
	/**
	 * Selects the next set of runners to die, with a higher change for the worse runners to die
	 */
	public void findRunnersToDie(){
		runnersToDie = new int[settings.getNumRunners() / 2];
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(int i = 0; i < runners.length; i++) indexes.add(i);
		
		for(int i = 0; i < runners.length - runnersToDie.length; i++) indexes.remove((int)(Math.pow(Math.random(), 3) * indexes.size()));

		for(int i = 0; i < runnersToDie.length; i++) runnersToDie[i] = indexes.get(i);
	}
	
	/**
	 * Kills half of the runners, with a higher chance to kill worse runners, creates new offspring based on the survivors, 
	 * tests all of the new offspring, then sorts them
	 */
	public void nextGeneration(){
		//kill runners to die
		for(int i = 0; i < runnersToDie.length; i++) runners[runnersToDie[i]] = null;
		
		//replace the dead runners with mutated offspring of the survivors
		int[] alive = new int[runners.length - runnersToDie.length];
		int cnt = 0;
		for(int i = 0; i < runners.length; i++){
			if(runners[i] != null){
				alive[cnt] = i;
				cnt++;
			}
		}
		
		int birthIndex = 0;
		for(int i = 0; i < runners.length; i++){
			if(runners[i] == null){
				runners[runnersToDie[birthIndex]] = runners[alive[birthIndex]].getOffspring(currentGen + 1);
				birthIndex++;
			}
		}
		
		//test all runners
		for(int i = 0; i < runners.length; i++){
			boolean found = false;
			if(!updateAllRunners){
				for(int j = 0; j < runnersToDie.length && !found; j++){
					if(runnersToDie[j] == i) found = true;
				}
			}
				
			if(updateAllRunners || found) runners[i].testRunner();
		}
		
		updateAllRunners = false;
		
		//sort this generation
		sortRunners();
		
		updateData();
		
		findRunnersToDie();
		
		//increase gen counter
		currentGen++;
	}
	
	/**
	 * Call this to make every runner get tested again in the next generation, geenrally used for changes to the track
	 */
	public void nextGenReTest(){
		updateAllRunners = true;
	}
	
	/**
	 * Sort the runners, with index 0 being the highest fitness
	 */
	public void sortRunners(){
		for(int i = 0; i < runners.length; i++){
			int high = -1;
			for(int j = i; j < runners.length; j++){
				if(high == -1 || runners[high].getStoredFitness() < runners[j].getStoredFitness()) high = j;
			}
			Runner temp = runners[i];
			runners[i] = runners[high];
			runners[high] = temp;
		}
	}
	
	/**
	 * Save all the data of this simulation with the given PrintWriter
	 * @param write
	 */
	public void save(PrintWriter write){
		//save the current settings of this sim
		settings.save(write);
		
		//save misc variables for this sim
		write.println(currentGen);
		write.println(Runner.getCurrentRunnerID());
		
		//save track of this sim
		track.save(write);
		
		//save fitness data
		write.println(fitnessData.size());
		for(double[] d : fitnessData){
			write.println(d.length);
			for(double dd : d) write.print(dd + " ");
			write.println();
		}
		
		//save mutability data
		write.println(mutabilityData.size());
		for(double[] d : mutabilityData){
			write.println(d.length);
			for(double dd : d) write.print(dd + " ");
			write.println();
		}
		
		//save runner history
		write.println(runnerHistory.size());
		for(Runner[] r : runnerHistory){
			write.println(r.length);
			for(Runner rr : r) rr.save(write);
			write.println();
		}
		
		//save the main set of runners for this sim
		write.println(runners.length);
		for(Runner r : runners) r.save(write);
		
		//save the runners to die
		for(Integer i : runnersToDie) write.print(i + " ");
	}
	
	/**
	 * Load in this simulation from the given Scanner
	 * @param scan
	 */
	public void load(Scanner scan){
		//load settings
		settings.load(scan);
		
		//load misc values
		currentGen = scan.nextInt();
		Runner.setCurrentRunnerID(scan.nextInt());
		
		//load track
		track = new Track(settings, new Point2D.Double(0, 0), new Line2D.Double[0]);
		track.load(scan);
		
		//load fitness data
		fitnessData = new ArrayList<double[]>();
		int size = scan.nextInt();
		for(int i = 0; i < size; i++){
			fitnessData.add(new double[scan.nextInt()]);
			for(int j = 0; j < fitnessData.get(i).length; j++) fitnessData.get(i)[j] = scan.nextDouble();
		};
		
		//load mutability data
		mutabilityData = new ArrayList<double[]>();
		size = scan.nextInt();
		for(int i = 0; i < size; i++){
			mutabilityData.add(new double[scan.nextInt()]);
			for(int j = 0; j < mutabilityData.get(i).length; j++) mutabilityData.get(i)[j] = scan.nextDouble();
		}
		
		//load runner history
		runnerHistory = new ArrayList<Runner[]>();
		size = scan.nextInt();
		for(int i = 0; i < size; i++){
			runnerHistory.add(new Runner[scan.nextInt()]);
			for(int j = 0; j < runnerHistory.get(i).length; j++){
				runnerHistory.get(i)[j] = new Runner(settings, track);
				runnerHistory.get(i)[j].load(scan);
			}
		}
		
		runners = new Runner[scan.nextInt()];
		for(int i = 0; i < runners.length; i++){
			runners[i] = new Runner(settings, track);
			runners[i].load(scan);
		}
		
		//load runners to die
		runnersToDie = new int[settings.getNumRunners() / 2];
		for(int i = 0; i < runnersToDie.length; i++ ) runnersToDie[i] = scan.nextInt();
		
		//calculate everything from the loaded generation
		for(Runner r : runners) r.testRunner();
		sortRunners();
	}
	
}
