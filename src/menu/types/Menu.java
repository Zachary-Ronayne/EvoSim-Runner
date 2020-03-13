package menu.types;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

import menu.Main;
import menu.component.MenuComponent;
import menu.input.InputControl;;

public abstract class Menu{
	
	protected Main instance;
	
	/**
	 * A list of every component in this menu
	 */
	private ArrayList<MenuComponent> components;
	/**
	 * A list of every mouse controlled object in this menu
	 */
	private ArrayList<InputControl> inputControlled;
	
	public Menu(Main instance){
		this.instance = instance;
		resetMenu();
	}
	
	/**
	 * Resets this menu to a default state, put all component and InputControl object initialization here. Override this method and call super
	 */
	public void resetMenu(){
		removeAllComponents();
	};
	
	public void addComponent(MenuComponent m){
		components.add(m);
	}
	public void removeComponent(MenuComponent m){
		components.remove(m);
	}

	public void addControled(InputControl m){
		inputControlled.add(m);
	}
	public void removeControled(InputControl m){
		inputControlled.remove(m);
	}
	
	/**
	 * Updates this menu
	 * @param g
	 */
	public void tick(){
		for(MenuComponent m : components) m.tick();
	}
	
	/**
	 * Draw this menu to the given graphics object
	 * @param g
	 */
	public void render(Graphics g){
		for(MenuComponent m : components) m.render(g);
	}
	
	/**
	 * Link this menu to a component to accept mouse and or keyboard input
	 */
	public void linkToComponent(Component c){
		for(InputControl m : inputControlled) m.link(c);
	}

	/**
	 * Unlink this menu from a component to no longer accept mouse and or keyboard input
	 */
	public void unlinkFromComponent(Component c){
		for(int i = 0; i < inputControlled.size(); i++){
			if(inputControlled.get(i) == null){
				inputControlled.remove(i);
				i--;
			}
		}
		for(InputControl m : inputControlled){
			m.unlink(c);
		}
	}
	
	/**
	 * Removes all of the components and mouseControled things
	 */
	public void removeAllComponents(){
		components = new ArrayList<MenuComponent>();
		inputControlled = new ArrayList<InputControl>();
	}
	
}
