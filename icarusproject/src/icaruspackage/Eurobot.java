package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * An abstract class containing wrapper methods hiding away a 
 * lot of boring and low-level code. Extend this class and
 * provide an initialize function along with a go function
 * which contains the robot's tactics.
 * 
 */
abstract public class Eurobot {
	
	/** Measured constants you probably shouldn't change **/
	final int MATCH_LENGTH = 90000; // milliseconds
	final float WHEEL_DIAMETER = 8.0f;
	final float WHEEL_BASE = 30.5f;
	boolean competition = false;
	
	/** Trial and error constants you can play with **/
	final int AVOIDANCE_THRESHOLD = 13; // cm
	final int speed = 15; // cm/sec
	
	/** Robot Hardware **/
	TouchSensor bump = new TouchSensor(SensorPort.S3);
	TouchSensor pawn = new TouchSensor(SensorPort.S4);
	UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
	ColorSensor light = new ColorSensor(SensorPort.S2);
	DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER, // cm
			WHEEL_BASE, // cm
			Motor.B, Motor.C, true);
	CompassSensor compass = new CompassSensor(SensorPort.S2);
	
	/**
	 * Contains the initialization logic of the robot
	 */
	abstract public void initialize();

	/**
	 * Contains the tactical logic of the robot.
	 */
	abstract public void go();

	/**
	 * Registers an interrupt which turns the robot off when the
	 * emergency stop button is pressed.
	 */
	public void registerStopButtonInterrupt(){
		SensorPort.S3.addSensorPortListener(new SensorPortListener() {
			public void stateChanged(SensorPort port, int oldValue, int newValue){
				if (bump.isPressed()) {
					Motor.A.stop();
					Motor.B.stop();
					Motor.C.stop();
					NXT.shutDown();
				}}});	
	}

	/**
	 * Starts a thread which monitors the sonar (by polling) and stops
	 * the robot if there are any potentially dangerous obstacles.
	 */
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

	/**
	 * Prepares a timer which turns the robot when the match's 
	 * time limit is over. 
	 * @return A timer which should be started at the beginning 
	 * of the match.
	 */
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
	
	/**
	 * Place the robot's foot in the up position.
	 */
	void footUp(){
		moveFoot(1000);
	}
	
	/**
	 * Place the robot's foot in the down position.
	 */
	void footDown(){
		moveFoot(-1000);
	}
	 
	/**
	 * The foot of the robot can be moved to sit on a pawn
	 * or to stop pawns enterring the robot.
	 * @param distance angle to turn the foot driving motor
	 */
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
}
