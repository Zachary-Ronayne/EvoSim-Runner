package menu.types;

import java.awt.Graphics;

import menu.Main;
import menu.input.SettingsTypingString;

public class MenuChangeSettings extends Menu{
	
	private SettingsTypingString settingsTyping;
	
	public MenuChangeSettings(Main instance){
		super(instance);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		MenuRunStats.addTabButtons(this, instance);
		resetSettingsTyping();
		addControled(settingsTyping);
	}
	
	private void resetSettingsTyping(){
		if(settingsTyping != null){
			settingsTyping.removeFromMenu(this);
		}
		
		settingsTyping = new SettingsTypingString(instance.getMainSettings(), 700, 70){
			@Override
			public void enter(){
				super.enter();
				instance.getMainSim().nextGenReTest();
			}
		};
		settingsTyping.setOn(true);
		settingsTyping.addToMenu(this);
	}
	
	@Override
	public void render(Graphics g){
		super.render(g);
		settingsTyping.drawHelpInfo(g, 700, 60, instance.getMainSettings().getFontName());
	}
}
