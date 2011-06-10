package icaruspackage;

import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Timer;
import lejos.util.TimerListener;



public class Eurobot {
	static int MIN_DISTANCE = 13;
	static int MATCH_LENGTH = 90000; // 90 seconds
	static int speed = 400;
	static boolean competition = false;
	static boolean obstacle = false;
	static DifferentialPilot pilot;
	static TouchSensor bump = new TouchSensor(SensorPort.S3);
	static TouchSensor pawn = new TouchSensor(SensorPort.S4);
	static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
	static ColorSensor light = new ColorSensor(SensorPort.S2,ColorSensor.TYPE_COLORFULL);

	public static void main(String[] args) {
		Eurobot eurobot = new Eurobot();
		pilot = new DifferentialPilot(8.0f, 30.5f, Motor.B, Motor.C, true);
		pilot.setTravelSpeed(speed);
		Timer matchTimer = initMatchTimer();
		sonic.continuous();
		Timer avoidance = initSonicAvoidance();
		avoidance.start();
//		TODO: init 90 sec counter
		initStopButton();
		
		// wait for start signal:
		while(getColorString(light.getLightValue()).equals("BLACK")){competition = true;}
		// GO! start the 90 sec match timer
		matchTimer.start();
		// wait 300ms to make sure the starting paper is clear of the colour sensor.
		lejos.util.Delay.msDelay(300);
		// get the start colour, pass it as an argument to the main go() method
		eurobot.go(getColorString(light.getLightValue()));
		// clean up
		avoidance.stop();
		/*		for(;;){
			int lastVal = light.getLightValue();
			while (light.getLightValue() == lastVal){}
			LCD.clearDisplay();
			LCD.drawString(getColorString(light.getLightValue()), 0, 0,true);
		}	
		 */
	}

	public static void initStopButton(){
		SensorPort.S3.addSensorPortListener(new SensorPortListener() {
			public void stateChanged(SensorPort port, int oldValue, int newValue){
				if (bump.isPressed()) {
					// stop all motors
					Motor.A.stop();
					Motor.B.stop();
					Motor.C.stop();
					// turn off power
					NXT.shutDown();
				}}});	

	}

	public static Timer initSonicAvoidance(){
		TimerListener tl = new TimerListener()
		{		   
			public void timedOut(){
				LCD.drawInt(light.getLightValue(),6,0,0);//NEW LINE
				if(!obstacle && sonic.getDistance()<MIN_DISTANCE){
					obstacle = true;
					pilot.setTravelSpeed(0);
				} else if(obstacle && sonic.getDistance()>=MIN_DISTANCE) {
					obstacle = false;
					pilot.setTravelSpeed(speed);
				}
			}   
		};
		// set the timer event to fire every 500ms
		Timer timer = new Timer(500,tl);	   
		return timer;
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

	public String otherColor(String thisColor){
		if(thisColor.equals("RED")) return "BLUE";
		if(thisColor.equals("BLUE")) return "RED";
		LCD.drawString("ERROR", 0, 0,true);
		lejos.util.Delay.msDelay(10000);
		return"";
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
	
	
	public void go(String startColor) {
		int turnFactor = startColor.equals("BLUE")?1:-1;
		pilot.travel(100, true);
		while (pilot.isMoving()) {
			if (getColorString(light.getLightValue()).equals(otherColor(startColor))) pilot.stop();
		}
		
		//pilot.rotate(90);
		pilot.arc(turnFactor*20.0f,90.0f);
		pilot.reset();
		pilot.travel(200, true);
		while (pilot.isMoving()) {
			if (pawn.isPressed()) pilot.stop();//Found a pawn!
		}
		float travel2 = pilot.getMovement().getDistanceTraveled();
		
		pilot.rotate(180); // turn round and go home
		
		speed = 200;
		pilot.setTravelSpeed(speed);
		pilot.travel(travel2+22.0f, false);
		pilot.rotate(-90*turnFactor);
		pilot.travel(60, true);
		while (pilot.isMoving()) {
			if (getColorString(light.getLightValue()).equals("BLACK")) {
				lejos.util.Delay.msDelay(100);
				if (getColorString(light.getLightValue()).equals("BLACK")) {
					lejos.util.Delay.msDelay(100);
					if (getColorString(light.getLightValue()).equals("BLACK")) {
						pilot.stop();
					}
				}
			}
		}
		speed = 100;
		if(competition) {
			pilot.travel(12, false);
		} else {
			pilot.travel(5, false);
		}
		lift(500);
		// REMOVE THIS FOR THE COMPETITION:
		if(!competition) {
			lejos.util.Delay.msDelay(4000);
			lift(-500);
			pilot.setTravelSpeed(400);
			pilot.rotate(180);
		} else {
			NXT.shutDown();
		}
	}

	void lift(int distance){
		Motor.A.resetTachoCount();
		if(distance>0){
			Motor.A.backward();
		} else {
			Motor.A.forward();
		}    int count = 0;
		while( java.lang.Math.abs(count) < java.lang.Math.abs(distance)) count = Motor.A.getTachoCount();         
		Motor.A.stop();
	}

	static String getColorString(int v){
		switch(v){
		case 1:
			return "BLACK";
		case 2:
			return "BLUE";
		case 3:
			return "GREEN";
		case 5:
			return "RED";
		default:
			return "UNKNOWN COLOR";
		}
	}
}
