package icaruspackage;

import lejos.nxt.LCD;
import lejos.nxt.NXT;

public class CompassTest extends Eurobot {
	public static void main(String[] args) {		
		Eurobot bot = new CompassTest();
		bot.initialize();
		bot.go();
		NXT.shutDown();
	}
	@Override
	public void initialize() {
		// Nothing
	}

	@Override
	public void go() {
		while(true)
		{
			LCD.drawInt((int)compass.getDegrees(), 0, 0);
			lejos.util.Delay.msDelay(200);
		}
	}

}
