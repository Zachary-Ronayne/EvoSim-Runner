package menu.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

import data.SavesLoader;
import data.Settings;
import menu.Main;
import menu.component.MenuButton;
import menu.component.ScrollingList;
import sim.Track;

/**
 * Menu used to select a track for the current siumulation
 */
public class MenuTrackSelect extends Menu{
	
	/**
	 * The index of the selected track in the list
	 */
	private int selectedTrackIndex;
	
	private Track selectedTrack;
	
	public MenuTrackSelect(Main instance){
		super(instance);
	}
	
	@Override
	public void resetMenu(){
		super.resetMenu();
		selectedTrackIndex = -1;
		
		MenuRunStats.addTabButtons(this, instance);
		
		try{
			selectedTrack = instance.getMainSim().getTrack();
			selectedTrack.drawImageStartPoint();
		}catch(Exception e){}
		
		//button to select a track
		MenuButton selectButton = new MenuButton(550, 60, 200, 40){
			@Override
			public void render(Graphics g){
				super.render(g);
				g.setColor(Color.BLACK);
				g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 24));
				g.drawString("Select Track", getX() + 20, getY() + getHeight() - 10);
			}
			@Override
			public void press(MouseEvent e){
				super.press(e);
				if(selectedTrackIndex >= 0){
					try{
						Scanner scan = new Scanner(new File("./data/tracks/" + SavesLoader.getTrackNames()[selectedTrackIndex]));
						selectedTrack.load(scan);
						selectedTrack.drawImageStartPoint();
						scan.close();
						instance.getMainSim().nextGenReTest();
					}catch(Exception e1){}
				}
			}
		};
		addComponent(selectButton);
		addControled(selectButton);
		
		//selector for tracks
		ScrollingList trackSelector = new ScrollingList(10, 830, Settings.DEFAULT_SCREEN_WIDTH - 34, 40, SavesLoader.getTrackNames().length, 4){
			@Override
			public void renderButton(Graphics g, int index){
				try{
					//draw button
					super.renderButton(g, index);
					
					//draw text
					g.setColor(Color.BLACK);
					g.setFont(new Font(instance.getMainSettings().getFontName(), Font.PLAIN, 25));
					g.drawString("Track file: " + SavesLoader.getTrackNames()[index], getButton(index).getX() + 4, getButton(index).getY() + getButton(index).getHeight() - 10);
					
					//draw selected highlight
					if(selectedTrackIndex == index){
						g.setColor(new Color(150, 150, 255, 100));
						MenuButton b = getButton(index);
						g.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
					}
				}catch(Exception e) {}
			}
			@Override
			public void pressButton(int index){
				super.pressButton(index);
				if(selectedTrackIndex != index) selectedTrackIndex = index;
				else selectedTrackIndex = -1;
			}
		};
		addComponent(trackSelector);
		addControled(trackSelector);
	}
	
	@Override
	public void render(Graphics g){
		super.render(g);
		
		//draw preview of track
		//label
		g.setColor(Color.BLACK);
		g.setFont(new Font(Settings.DEFAULT_FONT_NAME, Font.PLAIN, 30));
		g.drawString("Selected Preview", 785, 90);
		
		//warning label
		g.setFont(new Font(Settings.DEFAULT_FONT_NAME, Font.PLAIN, 15));
		g.drawString("Note: selecting a track here will make the selected track be used", 10, 80);
		g.drawString("for tesing in future generations unless the track is changed again", 10, 97);
		
		//get the image
		BufferedImage img = instance.getMainSim().getTrack().getTrackImage();
		
		//variables for the width and height of the image
		double outlineStroke = 8;
		double drawX = 20;
		double drawY = 120;
		double maxW = Settings.DEFAULT_SCREEN_WIDTH - outlineStroke * 2 - 18;
		double maxH = 700;
		
		//background square for the track
		g.setColor(Color.BLACK);
		g.fillRect((int)Math.round(drawX - outlineStroke), (int)Math.round(drawY - outlineStroke),
				   (int)Math.round(maxW + outlineStroke * 2), (int)Math.round(maxH + outlineStroke * 2));
		
		//find the ratio of the width and height to draw the preview image at, then the new height and width
		double iRatio = (double)img.getWidth() / img.getHeight();
		
		double imgW;
		double imgH;
		
		//if width is scaled to fit perfectly
		if(maxW / iRatio <= maxH){
			imgW = maxW;
			imgH = maxW / iRatio;
		}
		//otherwise use height
		else{
			imgW = maxH * iRatio;
			imgH = maxH;
		}
		
		//draw the image
		g.drawImage(img, (int)Math.round(drawX + Math.abs(maxW - imgW) / 2), (int)Math.round(drawY + Math.abs(maxH - imgH) / 2),
						 (int)Math.round(imgW), (int)Math.round(imgH), null);
	}
}
