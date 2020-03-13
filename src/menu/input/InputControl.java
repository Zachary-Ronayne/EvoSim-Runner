package menu.input;

import java.awt.Component;

/**
 * An object that can be linked to a component to detect mouse or key input
 */
public interface InputControl{
	
	/**
	 * Link this object to the given component so that mouse or key input is used by the given objects. 
	 * If the object is already linked to the given component, nothing should happen
	 * @param c
	 */
	public void link(Component c);
	
	/**
	 * Unlink this object from the given component so that mouse or key input is not used by the given objects. 
	 * If the object is not linked to the given component, nothing should happen
	 * @param c
	 */
	public void unlink(Component c);
	
	/**
	 * This should create a MousAdapter or KeyAdapter object for this object to use to detect mouse input
	 */
	public void createControl();
}
