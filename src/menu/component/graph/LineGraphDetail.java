package menu.component.graph;

import java.awt.Color;

/**
 * an object that keeps track of the color and width of a line in a line graph
 */
public class LineGraphDetail{
	
	private float thickness;
	private Color color;
	
	public LineGraphDetail(float thickness, Color color){
		this.thickness = thickness;
		this.color = color;
	}
	
	public Color getColor(){
		return color;
	}
	public void setColor(Color color){
		this.color = color;
	}
	public float getThickness(){
		return thickness;
	}
	public void setThickness(float thickness){
		this.thickness = thickness;
	}
}
