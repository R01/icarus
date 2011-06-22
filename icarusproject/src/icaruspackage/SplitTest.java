package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;

public class SplitTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LCD.drawString("SplitTest:", 0, 0);

		CompassSensor compass = new CompassSensor(SensorPort.S2);
		LCD.drawString("Compass:"+compass.getAddress(), 0, 1);

		UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
		LCD.drawString("Sonic:"+sonic.getAddress(), 0, 2);

		while(true){
			LCD.drawString("="+compass.getDegrees()+"   ", 9, 1);
			LCD.drawString("="+sonic.getDistance()+"   ", 9, 2);
			lejos.util.Delay.msDelay(200);	
		}
	}
}
