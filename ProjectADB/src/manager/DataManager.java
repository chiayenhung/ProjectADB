package manager;

import java.util.*;

import component.Xclass;

/**
 * Data Manager object class
 * @author Chia-Yen Hung
 * 
 */
public class DataManager{
	ArrayList<String> a = new ArrayList<String>();
	
	/**
	 * this method ask LM to set read lock on Xclass X
	 */
	public void Ask_LM_setRead_Lock(LockManager _LM, Xclass _X, String _tid)
	{
		_LM.setRead(_X, _tid);
	}
	
	/**
	 * this method ask LM to set write lock on Xclass X
	 */
	public void Ask_LM_setWrite_Lock(LockManager _LM, Xclass _X, String _tid)
	{
		_LM.setWrite(_X, _tid);
	}
	
	/**
	 *this method ask LM to release lock on Xclass X 
	 */
	public void Ask_LM_release_Lock(LockManager _LM, Xclass _X)
	{
		_LM.release(_X);
	}
	
	/**
	 * this method used to return the data of variable X
	 */
	public int ReadData(Xclass _X)
	{
		return _X.getValue();
	}
	
	/**
	 * this method used to return the data of varialbe X
	 */
	public int ReadPreData(Xclass _X)
	{
		return _X.getPreviousValue();
	}
	
	/**
	 * this method used to write the new value to the variable X
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
	 * when a site fail, so delete all data and release all locks of the site
	 */
	public void Fail(LockManager _LM, ArrayList<Xclass> _X_q)
	{
		for(int i  = 0; i < _X_q.size(); i++)
		{
			if(_X_q.get(i).IsLock())
			{
				this.Ask_LM_release_Lock(_LM, _X_q.get(i));
				_X_q.get(i).setValue(_X_q.get(i).getPreviousValue());
			}
		}
	}
	
	/**
	 * this method used to copy data to backup site
	 */
	public void Backup(ArrayList<Xclass> _X_q, ArrayList<Xclass> b_X_q)
	{
		a = new ArrayList<String>();
		for(int i  = 0; i < _X_q.size(); i++)
		{
			boolean match = false;
			if(_X_q.get(i).IsLock() && !_X_q.get(i).IsCopy()){
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
	 * this method used to write back the data by copying it from other sites
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
	 * this method commit the values of all Xclass X
	 */
	public void Dump(LockManager _LM, ArrayList<Xclass> _X_q)
	{
		for(int i  = 0; i < _X_q.size(); i++)
		{
			if(!_X_q.get(i).IsCopy())
			{
				_X_q.get(i).setPreviousValue(_X_q.get(i).getValue());
				this.Ask_LM_release_Lock(_LM, _X_q.get(i));
			}
		}
	}
	
	/**
	 * this method can commit the values of Xclass Xj
	 */
	public void Dump(LockManager _LM, Xclass Xj)
	{
		Xj.setPreviousValue(Xj.getValue());
		this.Ask_LM_release_Lock(_LM, Xj);
	}
	
	/**
	 * this method delete the value of Xclass X
	 */
	public void Abort(LockManager _LM, Xclass X)
	{
		X.setValue(X.getPreviousValue());
		this.Ask_LM_release_Lock(_LM, X);
	}

}