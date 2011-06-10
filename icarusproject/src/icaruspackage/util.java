package icaruspackage;

import lejos.nxt.ColorSensor.Color;

public class util {
	public static String getColorString(int v){
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
	
	public int oppositeColor(int color) {
		if(color == Color.BLUE) return Color.RED;
		else if(color == Color.RED) return Color.BLUE; 
		else return -1;
	}
}
