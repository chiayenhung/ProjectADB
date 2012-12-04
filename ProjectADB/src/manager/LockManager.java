package manager;

import component.Xclass;

//LockManager object class
/**
 * This Class implements a Lock Manager object
 * @author Chia-Yen Hung Ming-Chien Kao
 */
public class LockManager{
	
	/**
	 * set read lock on Xclass
	 * @param _X
	 * @param _tid
	 */
	public void setRead(Xclass _X, String _tid)
	{
		_X.setLock();
		_X.setLockType("Read");
		_X.setLockID(_tid);
	}
	
	/**
	 * set write lock on Xclass
	 * @param _X
	 * @param _tid
	 */
	public void setWrite(Xclass _X, String _tid)
	{
		_X.setLock();
		_X.setLockType("Write");
		_X.setLockID(_tid);
	}
	
	/**
	 * release lock on Xclass
	 * @param _X
	 */
	public void release(Xclass _X)
	{
		_X.unLock();
		_X.setLockType("NULL");
		_X.setLockID("NULL");
	}
}