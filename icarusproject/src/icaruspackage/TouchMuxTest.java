package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.addon.TouchMUX;


public class TouchMuxTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LCD.drawString("TouchMuxTest:", 0, 0);
		TouchMUX tmux = new TouchMUX(SensorPort.S4);
		while(true){
			if(tmux.T1.isPressed()) {
				LCD.drawString("T1 Pressed", 0, 1);
			} else {LCD.drawString("          ", 0, 1);}
			if(tmux.T2.isPressed()) {
				LCD.drawString("T2 Pressed", 0, 2);
			} else {LCD.drawString("          ", 0, 2);}
			if(tmux.T3.isPressed()) {
				LCD.drawString("T3 Pressed", 0, 3);
			} else {LCD.drawString("          ", 0, 3);}
		}
	}
}
