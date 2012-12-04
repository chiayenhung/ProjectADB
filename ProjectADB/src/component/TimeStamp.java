/**
 * 
 */
package component;

/**
 * @author Chia-Yen Hung Ming-Chien Kao
 *
 */
public class TimeStamp {
	private int timeStamp;
	private int value;
	
	public TimeStamp(int value, int timeStamp){
		this.value = value;
		this.timeStamp = timeStamp;
	}
	
	public int getTimeStamp(){
		return this.timeStamp;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public void setTimeStamp(int timeStamp){
		this.timeStamp = timeStamp;
	}

}
