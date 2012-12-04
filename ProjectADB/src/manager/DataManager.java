package manager;

import java.util.*;

import component.TimeStamp;
import component.Xclass;

/**
 * Data Manager object class
 * @author Chia-Yen Hung Ming-Chien Kao
 * 
 */
public class DataManager{
	ArrayList<String> a = new ArrayList<String>();
	
	/**
	 * @param _LM
	 * @param _X
	 * @param _tid
	 * @description ask the Lock manager to set read lock
	 */
	public void Ask_LM_setRead_Lock(LockManager _LM, Xclass _X, String _tid)
	{
		_LM.setRead(_X, _tid);
	}
	
	/**
	 * @param _LM
	 * @param _X
	 * @param _tid
	 * @decription ask the Lock manager to set write lock
	 */
	public void Ask_LM_setWrite_Lock(LockManager _LM, Xclass _X, String _tid)
	{
		_LM.setWrite(_X, _tid);
	}
	
	/**
	 * @param _LM
	 * @param _X
	 * @description ask lock manager to release the lock
	 */
	public void Ask_LM_release_Lock(LockManager _LM, Xclass _X)
	{
		_LM.release(_X);
	}
	
	/**
	 * @param _X
	 * @return
	 * @description read the value
	 */
	public int ReadData(Xclass _X)
	{
		return _X.getValue();
	}
	
	/**
	 * @param _X
	 * @return
	 * @description read the previous value
	 */
	public int ReadPreData(Xclass _X)
	{
		return _X.getPreviousValue();
	}
	
	/**
	 * @param Xclass
	 * @param timeStamp
	 * @return
	 * @description implement multiversion control
	 */
	public int readOnlyData(Xclass Xclass, int timeStamp) {
//		System.out.println("list size: "+Xclass.getValueList().size() + " timestamp: " + timeStamp);
		for(int i = Xclass.getValueList().size() -2 ; i >= 0 ; i--){
			System.out.println("timeStamp: "+Xclass.getValueList().get(i).getTimeStamp());
			if(Xclass.getValueList().get(i).getTimeStamp() == 0)
				return Xclass.getValueList().get(i).getValue();
			if(Xclass.getValueList().get(i).getTimeStamp() <= timeStamp)
				return Xclass.getValueList().get(i).getValue();
		}
		return Xclass.getValueList().get(0).getValue();	
	}
	
	/**
	 * @param _X
	 * @param _Value
	 * @description write the current value
	 */
	public void WriteData(Xclass _X, int _Value)
	{
		_X.setValue(_Value);
	}
	
	/**
	 * this method used to write the new value to the variable X
	 * @param _X
	 * @param _Value
	 */
	public void WritePreData(Xclass _X, int _Value)
	{
		_X.setPreviousValue(_Value);
	}
	
	/**
	 * @param _LM
	 * @param _X_q
	 * @description when a site fail, so delete all data and release all locks of the site
	 */
	public void Fail(LockManager _LM, ArrayList<Xclass> _X_q)
	{
		for(int i  = 0; i < _X_q.size(); i++)
		{
			if(_X_q.get(i).isLock())
			{
				this.Ask_LM_release_Lock(_LM, _X_q.get(i));
				_X_q.get(i).setValue(_X_q.get(i).getPreviousValue());
			}
		}
	}
	
	/**
	 * @param _X_q
	 * @param b_X_q
	 * @description this method used to copy data to backup site
	 */
	public void Backup(ArrayList<Xclass> _X_q, ArrayList<Xclass> b_X_q)
	{
		a = new ArrayList<String>();
		for(int i  = 0; i < _X_q.size(); i++)
		{
			boolean match = false;
			if(_X_q.get(i).isLock() && !_X_q.get(i).IsCopy()){
				a.add(_X_q.get(i).getLockID());
			}
			if(_X_q.get(i).IsCopy() || _X_q.get(i).getID() % 2 == 0)
			{
				continue;
			}
			for(int j  = 0; j < b_X_q.size(); j++)
			{
				if(_X_q.get(i).getID() == b_X_q.get(j).getID())
				{
					match = true;
					b_X_q.get(j).doCopy(_X_q.get(i));
				}
			}
			if(!match)
			{
				Xclass x = new Xclass(_X_q.get(i).getID());
				x.doCopy(_X_q.get(i));
				b_X_q.add(x);
			}
		}
	}
	
	/**
	 * @param _X_q
	 * @param t_X_q
	 * @return
	 * @description this method used to write back the data by copying it from other sites
	 */
	public ArrayList<String> Recovery(ArrayList<Xclass> _X_q, ArrayList<Xclass> t_X_q)
	{
		for(int i  = 0; i < _X_q.size(); i++)
		{
			if(_X_q.get(i).IsCopy())
			{
				continue;
			}
			for(int j  = 0; j < t_X_q.size(); j++)
			{
				if(_X_q.get(i).getID() == t_X_q.get(j).getID())
				{
					_X_q.get(i).doCopy(t_X_q.get(j));
					_X_q.get(i).unCopy();
					_X_q.get(i).unLock();
				}
			}
		}
		return a;
	}
	

	/**
	 * @param _LM
	 * @param _X_q
	 * @param time
	 * @description this method commit the values of all Xclass X
	 */
	public void Dump(LockManager _LM, ArrayList<Xclass> _X_q, int time)
	{
		for(int i  = 0; i < _X_q.size(); i++)
		{
			if(!_X_q.get(i).IsCopy())
			{
				_X_q.get(i).getValueList().get(_X_q.get(i).getValueList().size() - 1).setTimeStamp(time);
				_X_q.get(i).getValueList().add(new TimeStamp(_X_q.get(i).getValue(), time));
				this.Ask_LM_release_Lock(_LM, _X_q.get(i));
			}
		}
	}
	
	/**
	 * @param _LM
	 * @param Xj
	 * @param time
	 * @description this method can commit the values of Xclass Xj
	 */
	public void Dump(LockManager _LM, Xclass Xj, int time)
	{
		Xj.getValueList().get(Xj.getValueList().size() - 1).setTimeStamp(time);
		Xj.getValueList().add(new TimeStamp(Xj.getValue(), time));
		this.Ask_LM_release_Lock(_LM, Xj);
	}
	
	/**
	 * @param _LM
	 * @param X
	 * @description this method delete the value of Xclass X
	 */
	public void Abort(LockManager _LM, Xclass X)
	{
		X.setValue(X.getPreviousValue());
		this.Ask_LM_release_Lock(_LM, X);
	}

	

}
