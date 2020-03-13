package menu.input;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * An object that keeps track of what information is typed in as part of a string, but the string needs to be apart of a valid file name
 */
public class TypingStringInput implements InputControl{
	
	private KeyAdapter keyInput;
	
	/**
	 * true if this object should detect key input, false if it should not
	 */
	private boolean on;
	
	/**
	 * The string that keeps track of what has been typed
	 */
	protected String string;
	
	public TypingStringInput(){
		
		on = false;
		string = "";
		
		createControl();
	}
	
	public boolean on(){
		return on;
	}
	public void setOn(boolean on){
		this.on = on;
	}
	
	public String getString(){
		return string;
	}
	
	/**
	 * Override this to do something when the escape key is pressed and this object is on. By default it just turns the object off
	 */
	public void escape(){
		on = false;
	}
	
	
	/**
	 * Override this to do something when the enter key is pressed and this object is on. By default it just turns the object off
	 */
	public void enter(){
		on = false;
	}
	
	/**
	 * Override this method to do something when a character is attempted to be added to the string. By default only allows characters that are valid file names
	 * @param e
	 */
	public void typeCharacter(KeyEvent e){
		char c = e.getKeyChar();
		if((int)c <= 122 && (int)c >= 32 &&
			//make sure that the character that is going to be added is not an invalid character for a file name
			c !=  '/' && c != '\\' && c != '.' && c != ',' && c != '<' && c != '>' && c != '?' && c != ':' && c != '*' && c != '"' && c != '\"' && c != '.') appendString(c);
	}
	
	/**
	 * Add the given character to the given string, should be used by typeCharacter
	 * @param c
	 */
	public void appendString(char c){
		string += c;
	}
	
	@Override
	public void link(Component c){
		c.addKeyListener(keyInput);
	}
	
	@Override
	public void unlink(Component c){
		c.removeKeyListener(keyInput);
	}
	
	@Override
	public void createControl(){
		keyInput = new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				super.keyPressed(e);
				if(on){
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
						escape();
					}
					else if(e.getKeyCode() == KeyEvent.VK_ENTER){
						enter();
					}
					else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && string.length() != 0){
						string = string.substring(0, string.length() - 1);
					}
					else typeCharacter(e);
				}
			}
		
		};
	}
	
}
