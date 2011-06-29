package icaruspackage;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.CompassSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.NavPathController;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.WayPoint;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class AdvancedBot {
	/** Measured constants you probably shouldn't change **/
	final int MATCH_LENGTH = 90000; // milliseconds
	final float WHEEL_DIAMETER = 8.0f;
	final float WHEEL_BASE = 30.5f;
	final Pose blueStartPose = new Pose(24, 20, 0);
	final Pose redStartPose = new Pose(300-24, 20, 180);
	
	/** Trial and error constants you can play with **/
	final int AVOIDANCE_THRESHOLD = 13; // cm
	final float speed = 15.0f; // cm/sec
	
	/** Robot Hardware **/
	TouchSensor bump = new TouchSensor(SensorPort.S3);
	TouchSensor pawn = new TouchSensor(SensorPort.S4);
	UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
	ColorSensor light = new ColorSensor(SensorPort.S2);
	DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER, // cm
			WHEEL_BASE, // cm
			Motor.B, Motor.C, true);
	
	/** Navigation Objects **/
	OdometryPoseProvider pProvider = new OdometryPoseProvider(pilot);
	NavPathController npController = new NavPathController(pilot, pProvider);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AdvancedBot bot = new AdvancedBot();
		bot.initialize();
		bot.startLCDLoop();
		
		boolean redStart = true; //(bot.light.getColorID() == Color.RED);
		
		if(redStart) {
			bot.pProvider.setPose(bot.redStartPose);
			Sound.beep();
		} else {
			bot.pProvider.setPose(bot.blueStartPose);
		}
		
		lejos.util.Delay.msDelay(5000);
		
		/*while(true) {
			LCD.clearDisplay();
			LCD.drawInt((int)bot.pProvider.getPose().getHeading(), 0, 0);
			LCD.drawInt((int)bot.compass.getDegreesCartesian(), 0, 1);
			lejos.util.Delay.msDelay(200);
		}
		*/
		bot.npController.goTo(new WayPoint(240, 20, 90));
		
		lejos.util.Delay.msDelay(4000);
		
		bot.npController.goTo(new WayPoint(220, 20, 90));
		
		Sound.beep();
		
		lejos.util.Delay.msDelay(30000);
		
	}
	
	private void startLCDLoop() {
		// Poll the sensor for the latest reading and stop if obstacle
		TimerListener tl = new TimerListener()
		{		   
			public void timedOut(){
				LCD.clear(1); LCD.clear(2);
				LCD.drawInt((int)pProvider.getPose().getHeading(), 0, 2);
			}   
		};
		// set the timer event to fire every 500ms
		Timer timer = new Timer(200,tl);	   
		timer.start();
	}
	
	public AdvancedBot() {
		// Nothing
	}
	
	public void initialize() {
		pilot.setTravelSpeed(speed);
		pilot.setRotateSpeed(speed/(WHEEL_BASE*Math.PI)*360.0f); // degrees/sec#
	}

}
