package icaruspackage;

import lejos.nxt.NXT;
import lejos.nxt.ColorSensor.Color;
import lejos.util.Timer;

public class TwoPawnBot extends Eurobot {
	public static void main(String[] args) {		
		Eurobot bot = new TwoPawnBot();
		bot.initialize();
		bot.go();
		NXT.shutDown();
	}
	
	@Override
	public void initialize() {
		pilot.setTravelSpeed(speed);
		pilot.setRotateSpeed(speed*Math.PI*WHEEL_BASE/180.0f); // degrees/sec#
		
		registerStopButtonInterrupt();
		Timer matchTimer = initMatchTimer();
		startSonicAvoidanceThread();
		footUp(); // Just in case!
		
		// wait for start signal:
		while(light.getColorID() == Color.BLACK){competition = true;}
		matchTimer.start();
		
		// wait 300ms to make sure the starting paper is clear of the colour sensor.
		lejos.util.Delay.msDelay(300);
		
		// get the start colour, pass it as an argument to the main go() method
		int color;
		do {
			 color = light.getColorID();
		}while(color != Color.RED && color != Color.BLUE);
	}
	
	@Override
	public void go() {
		// get the start colour
		int startColor;
		do {
			 startColor = light.getColorID();
		}while(startColor != Color.RED && startColor != Color.BLUE);
		int dir = (startColor == Color.BLUE)?1:-1;
		
		int distanceDownBoard = 0;
		
		// Move out of the starting box
		pilot.travel(100, true);
		while (pilot.isMoving()) {
			if (light.getColorID() != startColor) pilot.stop();
		}
	
		// Turn onto the first line
		pilot.arc(dir*20.0f,dir*90.0f);
		
		// Drive forwards until you find a pawn (max 3 squares)
		pilot.reset();
		pilot.travel(105, true);
		while (pilot.isMoving()) {
			if (pawn.isPressed()) pilot.stop(); //Found a pawn!
		}
		distanceDownBoard += pilot.getMovement().getDistanceTraveled();
		//TODO Handle pawns on 1st junction
		int travelDistance = 35;
		if (distanceDownBoard < 15){// a pawn on 1st junction found
			lejos.nxt.Sound.beep();
			travelDistance = 15;
			distanceDownBoard = distanceDownBoard + 20;
		}	
		// Move back 1 square
		pilot.rotate(180); 
		pilot.travel(travelDistance);
		pilot.travel(-35);
		pilot.rotate(-180);
		
		// Find next pawn
		pilot.reset();
		pilot.travel(105-distanceDownBoard, true);
		while (pilot.isMoving()) {
			if (pawn.isPressed()) pilot.stop(); //Found a pawn!
		}
		distanceDownBoard += pilot.getMovement().getDistanceTraveled();
		
		// Place pawn in protected area
		pilot.rotate(dir*90);
		pilot.travel(15);
		pilot.rotate(dir*-90);
		pilot.travel(153+10-distanceDownBoard);
		
		// Return to line
		pilot.travel(distanceDownBoard-150);
		pilot.rotate(dir*90);
		pilot.travel(-15);
		pilot.rotate(dir*90);
		
		pilot.reset();
		
		// Find next pawn
		pilot.reset();
		pilot.travel(105, true);
		while (pilot.isMoving()) {
			if (pawn.isPressed()) pilot.stop(); //Found a pawn!
		}
		
		distanceDownBoard -= pilot.getMovement().getDistanceTraveled();
		
		pilot.travel(distanceDownBoard + 22.0f);	
		pilot.rotate(-90*dir);
		pilot.travel(60, true);

		// Go past black
		int n = 0;
		do {
			if(light.getColorID() == Color.BLACK) ++n;
			else n = 0;
			lejos.util.Delay.msDelay(100);
		} while(n < 2 && pilot.isMoving());
		
		pilot.stop();
			
		// Go a little bit further
		if(competition) {
			pilot.travel(12);
		} else {
			pilot.travel(5);
		}
		
		footDown();
		
		if(!competition) {
			lejos.util.Delay.msDelay(4000);
			footUp();
			pilot.setTravelSpeed(speed);
			pilot.rotate(180);
		} else {
			NXT.shutDown();
		}
		
		// move forward to 1st position
		// arc +90 R150 (on one wheel)
		// if pawn detected {
		// 		rotate +180
		//		move forward 150mm (with pawn)
		//		rotate -90
		//		move forward 350mm (with pawn)
		//		move backward 350mm
		//		rotate -90
		// } else {
		//		move forward X to first pawn (max 3 squares)
		//		rotate +180
		//		move forward 350mm (with pawn)
		//		move backward 350mm
		//		rotate +180
		// }
		// move forward to 2nd pawn (max ... squares)
		// rotate +90
		// move forward 150mm (with pawn)
		// rotate -90
		// move forward to safe square (with pawn) (2nd pawn is now in place)
		// move backward 450mm
		// rotate -90
		// move forward 500mm
		// if no pawn detected {
		//		rotate -90
		//		move forward X until we hit a pawn (max 3 squares)
		//		rotate +180
		//		move forward X+50 (with pawn)
		//		rotate -90
		// } else {
		//		rotate 90
		//		move forward 50mm
		//		rotate -90
		// }
		// arc +135 R300 (with pawn) (3rd pawn is now in place)
		// arc -135 R300 in reverse
		// move 350mm backward
		// rotate -90
		// move forward 1200mm
		// rotate -90
		// move forward 600mm (1st pawn is now in place
		// lift up!
	}

}
