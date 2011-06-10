package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.ColorSensor.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Eurobot {
	/** Measured constants you probably shouldn't change **/
	static final int MATCH_LENGTH = 90000; // milliseconds
	static final float WHEEL_DIAMETER = 8.0f;
	static final float WHEEL_BASE = 30.5f;
	static boolean competition = false;
	
	/** Trial and error constants you can play with **/
	static final int AVOIDANCE_THRESHOLD = 13; // cm
	static final int speed = 15; // cm/sec
	
	static DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER, // cm
															WHEEL_BASE, // cm
															Motor.B, Motor.C, true);
	static TouchSensor bump = new TouchSensor(SensorPort.S3);
	static TouchSensor pawn = new TouchSensor(SensorPort.S4);
	static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
	static ColorSensor light = new ColorSensor(SensorPort.S2);

	public static void main(String[] args) {		
		/** More initialization ***********************/
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
		go(color);
	}

	public static void registerStopButtonInterrupt(){
		SensorPort.S3.addSensorPortListener(new SensorPortListener() {
			public void stateChanged(SensorPort port, int oldValue, int newValue){
				if (bump.isPressed()) {
					// stop all motors
					Motor.A.stop();
					Motor.B.stop();
					Motor.C.stop();
					NXT.shutDown();
				}}});	

	}

	public static void startSonicAvoidanceThread(){
		// Make sonar continuously measure distances
		sonic.continuous();
		
		// Poll the sensor for the latest reading and stop if obstacle
		TimerListener tl = new TimerListener()
		{		   
			public void timedOut(){
				LCD.clear(1);
				LCD.drawInt(light.getColorID(),0,1);//NEW LINE
				if(sonic.getDistance() < AVOIDANCE_THRESHOLD){
					pilot.setTravelSpeed(0);
				} else if(sonic.getDistance() >= AVOIDANCE_THRESHOLD) {
					pilot.setTravelSpeed(speed);
				}
			}   
		};
		// set the timer event to fire every 500ms
		Timer timer = new Timer(500,tl);	   
		timer.start();
	}

	public static Timer initMatchTimer(){
		TimerListener tl = new TimerListener()
		{		   
			public void timedOut(){
				// Match over: switch off...
				NXT.shutDown();
			}   
		};
		// set the timer event to fire after MATCH_LENGTH
		Timer timer = new Timer(MATCH_LENGTH,tl);	
		return timer;
	}
	
	public static int opposite(int color) {
		if(color == Color.BLUE) return Color.RED;
		else if(color == Color.RED) return Color.BLUE; 
		else return -1;
	}
	
	public void go2(String startColor){
		// *** NEW STRATEGY FOR THE FINALS ***
		
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
	
	
	public static void go(int startColor) {
		int turnFactor = (startColor == Color.BLUE)?1:-1;
		
		// Move out of the starting box
		pilot.travel(100, true);
		while (pilot.isMoving()) {
			if (light.getColorID() != startColor) pilot.stop();
		}
	
		pilot.reset();
	
		// Turn onto the first line
		pilot.arc(turnFactor*20.0f,turnFactor*90.0f);
		
		pilot.reset();
		
		// Drive forwards until you find a pawn
		pilot.travel(200, true);
		while (pilot.isMoving()) {
			if (pawn.isPressed()) pilot.stop(); //Found a pawn!
		}
		// Remember how far you drove
		float travel2 = pilot.getMovement().getDistanceTraveled();

		// Turn round and go home
		pilot.rotate(180); 
		pilot.travel(travel2+22.0f, false);
		pilot.rotate(-90*turnFactor);
		pilot.travel(60, true);

		// Go past black
		int n = 0;
		do {
			if(light.getColorID() == Color.BLACK) ++n;
			else n = 0;
			lejos.util.Delay.msDelay(100);
		} while(n < 3);
			
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
	}

	static void moveFoot(int distance) {
		//Initialize the motor and tell it to rotate for ages
		Motor.A.setSpeed(400);
		Motor.A.setStallThreshold(20, 10);
		Motor.A.rotate(distance, true);

		// Wait for stall
		Motor.A.waitComplete();
		
		//Wind back a bit to relax motor
		Motor.A.rotate(-20); 
	}
	
	static void footUp(){
		moveFoot(1000);
	}
	
	static void footDown(){
		moveFoot(-1000);
	}


	static String getColorString(int v){
		switch(v){
		case Color.BLACK:
			return "BLACK";
		case Color.BLUE:
			return "BLUE";
		case Color.GREEN:
			return "GREEN";
		case Color.RED:
			return "RED";
		default:
			return "UNKNOWN COLOR";
		}
	}
}
