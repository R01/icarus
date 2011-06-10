package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.ColorSensor.Color;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Timer;
import lejos.util.TimerListener;

abstract public class Eurobot {
	/** Measured constants you probably shouldn't change **/
	final int MATCH_LENGTH = 90000; // milliseconds
	final float WHEEL_DIAMETER = 8.0f;
	final float WHEEL_BASE = 30.5f;
	boolean competition = false;
	
	/** Trial and error constants you can play with **/
	final int AVOIDANCE_THRESHOLD = 13; // cm
	final int speed = 15; // cm/sec
	
	DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER, // cm
															WHEEL_BASE, // cm
															Motor.B, Motor.C, true);
	TouchSensor bump = new TouchSensor(SensorPort.S3);
	TouchSensor pawn = new TouchSensor(SensorPort.S4);
	UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
	ColorSensor light = new ColorSensor(SensorPort.S2);

	public void registerStopButtonInterrupt(){
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

	public void startSonicAvoidanceThread(){
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

	public Timer initMatchTimer(){
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
	
	public int opposite(int color) {
		if(color == Color.BLUE) return Color.RED;
		else if(color == Color.RED) return Color.BLUE; 
		else return -1;
	}
	
	abstract public void go(int startColor);
	abstract public void initialize();
	
	void moveFoot(int distance) {
		//Initialize the motor and tell it to rotate for ages
		Motor.A.setSpeed(400);
		Motor.A.setStallThreshold(20, 10);
		Motor.A.rotate(distance, true);

		// Wait for stall
		Motor.A.waitComplete();
		
		//Wind back a bit to relax motor
		Motor.A.rotate(-20); 
	}
	
	void footUp(){
		moveFoot(1000);
	}
	
	void footDown(){
		moveFoot(-1000);
	}

	String getColorString(int v){
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
