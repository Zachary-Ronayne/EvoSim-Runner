package menu.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import menu.Main;
import menu.component.graph.LineGraph;
import menu.component.graph.LineGraphDetail;

public class MenuGraphs extends Menu{
	
	private LineGraph fitnessGraph;
	private LineGraph mutabilityGraph;
	
	public MenuGraphs(Main instance){
		super(instance);
	}

	@Override
	public void resetMenu(){
		super.resetMenu();
		
		MenuRunStats.addTabButtons(this, instance);
		
		fitnessGraph = new LineGraph(instance.getMainSettings(), 10, 100, 1500, 400, 40, 10,
				new LineGraphDetail[]{
					new LineGraphDetail(2f, Color.BLACK),
					new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK),
					new LineGraphDetail(2f, Color.RED),
					new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK),
					new LineGraphDetail(2f, Color.BLACK),
				});
		addComponent(fitnessGraph);
		addControled(fitnessGraph);
		
		mutabilityGraph = new LineGraph(instance.getMainSettings(), 10, 550, 1500, 400, 40, 10,
				new LineGraphDetail[]{
					new LineGraphDetail(2f, Color.BLACK),
					new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK),
					new LineGraphDetail(2f, Color.RED),
					new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK), new LineGraphDetail(1f, Color.BLACK),
					new LineGraphDetail(2f, Color.BLACK),
				});
		addComponent(mutabilityGraph);
		addControled(mutabilityGraph);
	}
	
	@Override
	public void tick(){
		super.tick();
		if(instance.getMainSim().getCurrentGen() + 1 != fitnessGraph.getCurrentSize()) fitnessGraph.updateGraphImage(instance.getMainSim().getFitnessData());
		if(instance.getMainSim().getCurrentGen() + 1 != mutabilityGraph.getCurrentSize()) mutabilityGraph.updateGraphImage(instance.getMainSim().getMutabilityData());
	}
	
	@Override
	public void render(Graphics g){
		super.render(g);
		g.setColor(Color.BLACK);
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 30));
		g.drawString("Fitness graph", fitnessGraph.getX(), fitnessGraph.getY() - 10);
		g.drawString("Mutability graph", mutabilityGraph.getX(), mutabilityGraph.getY() - 10);
		
		//instructions on how to use key controls for the graphs
		g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 16));
		g.drawString("Right click and hold to pan camera", 1530, 150);
		g.drawString("Scroll wheel to zoom in and out", 1530, 170);
		g.drawString("Hold shift to zoom only on the x axis", 1530, 190);
		g.drawString("Hold ctrl to zoom only on the y axis", 1530, 210);
		g.drawString("Left click to reset graph", 1530, 230);
	}
	
}
