package adb;

/**
 * Xclass object class
 * @author Chia-Ming Lin, Li-Yen Hung
 *
 */
public class Xclass{
	//class variable
	private int ID;
	private boolean Lock;
	private String LockType;
	private String LockID;
	private int Value;
	private int PreviousValue;
	private boolean Copy;
	
	/**
	 * class constructor
	 * @param _ID
	 */
	public Xclass(int _ID)
	{
		ID = _ID;//assign Xclass id
		Lock = false;
		LockType = "NULL";
		LockID = "NULL";
		Value = _ID * 10;
		PreviousValue = _ID * 10;
		Copy = false;
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
	public boolean IsLock()
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
		return Value;
	}
	
	/**
	 * get Xclass previous value
	 * @return
	 */
	public int getPreviousValue()
	{
		return PreviousValue;
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
		Value = _Value;
	}
	
	/**
	 * set Xclass previous value
	 * @param _PreviousValue
	 */
	public void setPreviousValue(int _PreviousValue)
	{
		PreviousValue = _PreviousValue;
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
		if(_x.IsLock())
			Lock = true;
		else
			Lock = false;
		LockType = _x.getLockType();
		LockID = _x.getLockID();
		Value = _x.getValue();
		PreviousValue = _x.getPreviousValue();
		Copy = true;
	}
}