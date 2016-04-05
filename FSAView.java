package fsa;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;

public class FSAView extends JFrame {
	
	/**
	 *@author Eoin Murphy
	 */
	private final static int SIM_AREA=900;
	private final static int Y_AXIS=450;
	private final static int WINDOW_SIZE=1000;
	private final static int RADIUS=50;
	private final static int SPACE=50;
	private FSA fsa;
	private Hashtable<String,Shape> states;
	private BufferedImage backBuffer;
	private	Graphics2D g2d;
	public FSAView(FSA f){
		super("Visualisation de votre automate");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(WINDOW_SIZE,WINDOW_SIZE);
		setExtendedState(MAXIMIZED_BOTH);
		fsa=f;
		backBuffer=new BufferedImage(SIM_AREA,SIM_AREA,BufferedImage.TYPE_INT_RGB);
		g2d=backBuffer.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,SIM_AREA,SIM_AREA);
		states=new Hashtable<String,Shape>();
		draw();
		setVisible(true);
		
	}
	
	
	
	private void draw() {
		fsa.setStateNames();
		int i=0;
		if(fsa instanceof DFA){
			DFA dfa=(DFA)fsa;
			for(DFA.State state:dfa){
				Shape circle=new Ellipse2D.Double((i*(RADIUS*2)+SPACE),Y_AXIS,RADIUS,RADIUS);
				g2d.setColor(Color.BLACK);
				g2d.draw(circle);
				g2d.drawString(state.getName(),i*(RADIUS*2)+SPACE,Y_AXIS);
				states.put(state.getName(),circle);
				i++;
			}
			Shape err=new Ellipse2D.Double((0*(RADIUS*2)+SPACE),Y_AXIS-(SPACE*2),RADIUS,RADIUS);
			g2d.setColor(Color.BLACK);
			g2d.draw(err);
			g2d.drawString("Err",0*(RADIUS*2)+SPACE,Y_AXIS-(SPACE*2));
			states.put("Err",err);

			for(DFA.State state:dfa){
				Ellipse2D circle=(Ellipse2D)states.get(state.getName());
				Rectangle2D bound=circle.getBounds();
				int centreX=(int)bound.getCenterX();
				int centreY=(int)bound.getCenterY();
				Ellipse2D next;
				for(String label:state.arcs().keySet()){
					next=(Ellipse2D)(states.get(state.transition(label).getName()));
					Rectangle2D nextBound=next.getBounds();
					boolean leftToRight=(nextBound.getCenterX()-centreX>0);
					int outerX=(int)(nextBound.getCenterX()+(leftToRight?-RADIUS:RADIUS));
					int outerY=(int)(nextBound.getCenterY());
					g2d.drawLine(centreX,centreY,outerX,outerY);
			
				}
			}
		}
		repaint();
	}
	
	public void paint(Graphics g){
		g.drawImage(backBuffer,0,0,this);
	}

	public void update(){
		update(g2d);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args){
		
	}

}
