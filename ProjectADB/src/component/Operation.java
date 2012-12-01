package component;

/**
 * operation object class
 * @author Chia-Yen Hung
 *
 */
public class Operation 
{
	public enum action_type
	{
		read,
		write,
		commit
	}
	//class variable
	private action_type op_type = action_type.read;
	private int value = -1;
	private int target = -1;
	private int timeStamp = 0;
	
	/**
	 * class constructor
	 * @param type
	 * @param _value
	 * @param location
	 * @param ts
	 */
	public Operation(action_type type,int _value,int location,int ts)
	{
		op_type = type;
		value = _value;
		target = location;
		timeStamp = ts;
	}
	/**
	 * set the operation type
	 * @param new_type
	 */
	public void setOperationType(action_type new_type)
	{
		op_type = new_type;
	}
	/**
	 * return the this object operation read/write
	 * @return
	 */
	public action_type getOperationType()
	{
		return op_type;
	}
	/**
	 * set the new value for this object
	 * @param new_value
	 */
	public void setValue(int new_value)
	{
		value = new_value;
	}
	/**
	 * return the value for this object to write
	 * @return
	 */
	public int getValue()
	{
		return value;
	}
	/**
	 * set the new target location for this object
	 * @param new_target
	 */
	public void setTarget(int new_target)
	{
		target = new_target;
	}
	/**
	 * return the target location for this operation to be performed
	 * @return
	 */
	public int getTarget()
	{
		return target;
	}
	/**
	 * set the new target location for this object
	 * @param ts
	 */
	public void setTimeStamp(int ts)
	{
		timeStamp = ts;
	}
	/**
	 * return the target location for this operation to be performed
	 * @return
	 */
	public int getTimeStamp()
	{
		return timeStamp;
	}
}
