package icaruspackage;

import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class CalibrateCompass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CompassSensor compass = new CompassSensor(SensorPort.S2);
		DifferentialPilot pilot = new DifferentialPilot(8.0f, // cm
				30.5f, // cm
				Motor.B, Motor.C, true);
		
		pilot.setRotateSpeed(360/20);
		
		compass.startCalibration();
		pilot.rotate(720);
		compass.stopCalibration();
		
	}

}
