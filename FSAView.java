package fsa;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

public class FSAView extends JFrame {
	
	/**
	 *@author Eoin Murphy
	 */
	private final static int SIM_AREA=700;
	private final static int WINDOW_SIZE=800;
	
	private FSA fsa;
	private ArrayList<Shape> states;
	private BufferedImage backBuffer;
	private	Graphics2D g2d;
	public FSAView(FSA f){
		super("Configuez votre automate");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(WINDOW_SIZE,WINDOW_SIZE);
		setExtendedState(MAXIMIZED_BOTH);
		fsa=f;
		backBuffer=new BufferedImage(SIM_AREA,SIM_AREA,BufferedImage.TYPE_INT_RGB);
		g2d=backBuffer.createGraphics();
		states=new ArrayList<Shape>();
		initialise();
		setVisible(true);
		
	}
	
	
	
	private void initialise() {
		int i=0;
		
		update();
	}
	
	public void paint(Graphics g){
		g.drawImage(backBuffer,0,0,this);
	}
	
	public void update(Graphics g){
		for(Shape state:states){
			g2d.setColor(Color.WHITE);
			g2d.draw(state);
		}	
		repaint();
	}

	public void update(){
		update(g2d);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

}
