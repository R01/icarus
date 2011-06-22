package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;
import lejos.nxt.addon.TouchMUX;
import lejos.robotics.Touch;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Stopwatch;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class TestRig extends Eurobot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		test various operations...
		Eurobot bot = new TwoPawnBot();
		bot.footDown();
		Button.ENTER.waitForPressAndRelease();
		//lejos.util.Delay.msDelay(3000);
		bot.footUp();
	}

	
	public void initialize(){
		//
	}
	
	public void go() {
		//
	}
}
