package icaruspackage;

import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;
import lejos.nxt.addon.TouchMUX;
import lejos.robotics.Touch;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Stopwatch;
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
	final float WHEEL_DIAMETER = 8.0f;// cm
	final float WHEEL_BASE = 30.5f;// cm
	boolean competition = false;

	/** Trial and error constants you can play with **/
	final int AVOIDANCE_THRESHOLD = 13; // cm
	final int FAST = 25; // cm/sec (15 is slow but accurate, 30 is nearly too fast)
	final int SLOW = 8;
	final int rotateSpeed = 30;
	final int MAX_ACCELERATION = 6000;
	final int MIN_ACCELERATION = 1000;


	/** Robot Hardware **/
	TouchMUX tmux = new TouchMUX(SensorPort.S4);// handle to the touch multiplexer
	Touch stopButton = tmux.T3;// stop button is in multiplexer T3 
	Touch pawnButton = tmux.T2;// pawn button is in multiplexer port T2
	UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);// in the Splitter...
	ColorSensor light = new ColorSensor(SensorPort.S3);// analogue sensor - port S3
	DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER, // cm
			WHEEL_BASE, // cm
			Motor.B, Motor.C, true);
	CompassSensor compass = new CompassSensor(SensorPort.S2);// I2C sensor (address 2)
	Stopwatch stopwatch = new Stopwatch();// a timer for debugging
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

		SensorPort.S4.addSensorPortListener(new SensorPortListener() {
			public void stateChanged(SensorPort port, int oldValue, int newValue){
				LCD.drawString("B"+tmux.readSensors()+" "+stopwatch.elapsed(),0,5);
				if (stopButton.isPressed()) {
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
				//LCD.drawString(" "+speed+"   ",6,1);//track speed
				//LCD.drawString("    ",0,2);//clear old colour value
				//LCD.drawInt(light.getColorID(),0,2);
				if(sonic.getDistance() < AVOIDANCE_THRESHOLD){
					// obstacle detected - stop the robot
					//LCD.drawString("STOP!",0,3);
					// warning beep:
					int v=Sound.getVolume();// get current volume
					Sound.setVolume(100);// change volume to max
					Sound.systemSound(false,1);
					Sound.setVolume(v);// reset master volume
					pilot.setTravelSpeed(0);
					pilot.setRotateSpeed(0);

				} else if(sonic.getDistance() >= AVOIDANCE_THRESHOLD) {
					// no obstacle - carry on
					//LCD.drawString("GO!  ",0,3);
					pilot.setTravelSpeed(FAST);
					pilot.setRotateSpeed(FAST*Math.PI*WHEEL_BASE/180.0f);
				}
			}   
		};
		// set this timer event to fire every 500ms
		Timer timer = new Timer(500,tl);	   
		timer.start();
	}

	/**
	 * Prepares a timer which turns the robot off when the match's 
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

	void waitForPawn(){
		pilot.setAcceleration(MAX_ACCELERATION);// allow quick stop
		while (pilot.isMoving()) {
			if (pawnButton.isPressed()) pilot.stop(); //Found a pawn!
		}
		pilot.setAcceleration(MIN_ACCELERATION);// reset acceleration
	}


	/**
	 * Place the robot's foot in the up position.
	 */
	void footUp(){
		moveFoot(2000);
	}

	/**
	 * Place the robot's foot in the down position.
	 */
	void footDown(){
		moveFoot(-2000);
	}

	/**
	 * The foot of the robot can be moved to sit on a pawn
	 * or to stop pawns entering the robot.
	 * @param distance angle to turn the foot driving motor
	 */
	void moveFoot(int distance) {
		//Initialize the motor and tell it to rotate for ages
		Motor.A.setSpeed(400);
		Motor.A.setStallThreshold(30,30);//(20, 10);
		Motor.A.rotate(distance, true);

		// Wait for stall
		Motor.A.waitComplete();

		//Wind back a bit to relax motor
		Motor.A.rotate(-21); 
		//... and forward again a tiny bit to prevent coasting
		Motor.A.rotate(1); 
	}
	
	// convert to radians
	double radians(double d) {
		return d*Math.PI/180.0;
	}

	// convert to radians
	 double degrees(double r) {
		return r*180.0/Math.PI;
	}

}
