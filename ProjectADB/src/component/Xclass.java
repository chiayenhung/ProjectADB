package component;

import java.util.ArrayList;
import java.util.List;

/**
 * Xclass object class
 * @author Chia-Yen Hung Ming-Chien Kao
 *
 */
public class Xclass{
	//class variable
	private int ID;
	private boolean Lock;
	private String LockType;
	private String LockID;
	private List<TimeStamp> valueList;
	private boolean Copy;
	
	/**
	 * class constructor
	 * @param _ID
	 */
	public Xclass(int ID)
	{
		this.ID = ID;//assign Xclass id
		this.Lock = false;
		this.LockType = "NULL";
		this.LockID = "NULL";
		this.Copy = false;
		this.valueList = new ArrayList<TimeStamp>();
		this.valueList.add(new TimeStamp(ID * 10, 0));
		this.valueList.add(new TimeStamp(ID * 10, 0));
	}
	
	/**
	 * get Xclass id
	 * @return
	 */
	public int getID()
	{
		return ID;
	}
	
	/**
	 * check is Xclass locked
	 * @return
	 */
	public boolean isLock()
	{
		return Lock;
	}
	
	/**
	 * get Xclass lock type
	 * @return
	 */
	public String getLockType()
	{
		return LockType;
	}
	
	/**
	 * get Xclass lock id
	 * @return
	 */
	public String getLockID()
	{
		return LockID;
	}
	
	/**
	 * get Xclass value
	 * @return
	 */
	public int getValue()
	{
//		return Value;
		return this.valueList.get(this.valueList.size() - 1).getValue();
	}
	
	/**
	 * get Xclass previous value
	 * @return
	 */
	public int getPreviousValue()
	{
//		return PreviousValue;
		return this.valueList.get(this.valueList.size() - 2).getValue();
	}
	
	/**
	 * check if Xclass is a copy
	 * @return
	 */
	public boolean IsCopy()
	{
		return Copy;
	}
	
	/**
	 * set Xclass id
	 * @param _ID
	 */
	public void setID(int _ID)
	{
		ID = _ID;
	}
	
	/**
	 * set Xclass lock
	 */
	public void setLock()
	{
		Lock = true;
	}
	
	/**
	 * unlock Xclass
	 */
	public void unLock()
	{
		Lock = false;
	}
	
	/**
	 * set Xclass lock type
	 * @param _LockType
	 */
	public void setLockType(String _LockType)
	{
		LockType = _LockType;
	}
	
	/**
	 * get Xclass lock id
	 * @param _tid
	 */
	public void setLockID(String _tid)
	{
		LockID = _tid;
	}
	
	/**
	 * set Xclass value
	 * @param _Value
	 */
	public void setValue(int _Value)
	{
//		Value = _Value;
		this.valueList.get(this.valueList.size() - 1).setValue(_Value);
	}
	
	/**
	 * set Xclass previous value
	 * @param _PreviousValue
	 */
	public void setPreviousValue(int _PreviousValue)
	{
//		PreviousValue = _PreviousValue;
		this.valueList.get(this.valueList.size() - 2).setValue(_PreviousValue);
	}
	
	/**
	 * set Xclass a copy
	 */
	public void setCopy()
	{
		Copy = true;
	}
	
	/**
	 * set Xclass not a copy
	 */
	public void unCopy()
	{
		Copy = false;
	}
	
	/**
	 * copy x value from another x
	 */
	public void doCopy(Xclass _x){
		ID = _x.getID();
		if(_x.isLock())
			Lock = true;
		else
			Lock = false;
		LockType = _x.getLockType();
		LockID = _x.getLockID();
		this.valueList.get(this.valueList.size() - 1).setValue(_x.getValue());
		this.valueList.get(this.valueList.size() - 2).setValue(_x.getPreviousValue());
		Copy = true;
	}
	
	public List<TimeStamp> getValueList(){
		return this.valueList;
	}
}