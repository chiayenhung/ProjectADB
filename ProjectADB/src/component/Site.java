package component;

import java.util.*;

import component.Xclass;

import manager.DataManager;
import manager.LockManager;
/**
 * 
 *  Site Class Object
 *  @author Chia-Yen Hung Ming-Chien Kao
 */
public class Site{
	//class variable
	private int site_ID;
	private ArrayList<Xclass> X_list;
	private LockManager LM;
	private DataManager DM;
	private boolean fail;
	private String lockmsg;
	private int backupID;
	
	/**
	 * class constructor
	 * @param _ID
	 */
	public Site(int _ID)
	{
		site_ID = _ID;//assign site id
		X_list = new ArrayList<Xclass>();
		LM = new LockManager();
		DM = new DataManager();
		fail = false;
		Initial_Xclass();
		lockmsg = "NULL";
		backupID = -1;
	}
	
	/**
	 * get fail message
	 * @return
	 */
	public boolean isDown()
	{
		return fail;
	}
	/**
	 * Initialize Xclass
	 */
	public void Initial_Xclass()
	{
		//add odd X variable into target sites
		for (int i = 1; i <= 10; i++) {
			Xclass X = new Xclass(i * 2);
			this.X_list.add(X);
		}

		switch(site_ID)
		{
			//switch site id
			case 2:
				this.X_list.add(new Xclass(1));
				this.X_list.add(new Xclass(11));
				break;
			case 4:
				this.X_list.add(new Xclass(3));
				this.X_list.add(new Xclass(13));
				break;
			case 6:
				this.X_list.add(new Xclass(5));
				this.X_list.add(new Xclass(15));
				break;
			case 8:
				this.X_list.add(new Xclass(7));
				this.X_list.add(new Xclass(17));
				break;
			case 10:
				this.X_list.add(new Xclass(9));
				this.X_list.add(new Xclass(19));
				break;
		}
	}
	
	/**
	 * @return the previous value in this site
	 */
	public String dump(){
		StringBuilder sb =new StringBuilder();
		if(this.fail)
		{
			sb.append("This site is down.\n");
		}
		else
		{
			sb.append("This site is up.\n");
		}
		for(Xclass x: this.X_list)
			sb.append("X" + x.getID() + ": " + x.getPreviousValue() + " ");
		return sb.toString();
	}
	
	/**
	 * @param xIndex
	 * @return the indexed x previous value in this site
	 */
	public String dump(int xIndex){
		StringBuilder sb =new StringBuilder();
		if(this.fail)
		{
			sb.append("This site is down.\n");
		}
		else
		{
			sb.append("This site is up.\n");
		}
		String tmp = "X" + this.X_list.get(xIndex - 1).getID() + ": " + this.X_list.get(xIndex - 1).getPreviousValue() + " ";
		return sb.append(tmp).toString();
	}
	
	/**
	 * get site id
	 * @return
	 */
	public int getID()
	{
		return site_ID;
	}
	
	/**
	 * return a array list of x
	 * @return
	 */
	public ArrayList<Xclass> getX_list()
	{
		return X_list;
	}
	
	/**
	 * remove item
	 * @param location
	 */
	public void Remove_Xclass(int location)
	{
		//exit if the size is larger than the list length
		if(location > X_list.size()){return;}
		X_list.remove(location);
	}
	
	/**
	 * get lock message
	 * @return
	 */
	public String getLockMsg()
	{
		return lockmsg;
	}
	
	/**
	 * set lock message
	 * @param _tid
	 */
	public void setLockMsg(String _tid)
	{
		lockmsg = _tid;
	}
	
	/**
	 * reset lock message
	 */
	public void resetLockMsg()
	{
		lockmsg = "NULL";
	}
	
	/**
	 * read data for read only transaction
	 * @param _XID
	 * @param tid
	 * @return
	 */
	public int ReadOnly(int _XID, String tid, int transactionTimeStamp)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			//find x
			if(X_list.get(i).getID() == _XID)
			{
//					return DM.ReadPreData(X_list.get(i));
				System.out.println("error in site");
					return DM.readOnlyData(X_list.get(i), transactionTimeStamp);
			}
		}
		//cannot find x
		System.out.println("Read X" + _XID
				+ " from site" + site_ID + " fails, because it doesn't exist.");
		return -1;
	}
	
	/**
	 * read data
	 * @param _XID
	 * @param tid
	 * @return
	 */
	public int ReadData(int _XID, String tid)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).IsCopy())
			{
				continue;
			}
			//find x
			if(X_list.get(i).getID() == _XID)
			{
				//x is not locked
				if(!X_list.get(i).isLock())
				{
					
					DM.Ask_LM_setRead_Lock(LM, X_list.get(i), tid);
					this.resetLockMsg();
					return DM.ReadPreData(X_list.get(i));
				}
				//x is locked by itself
				else if(X_list.get(i).getLockID().contains(tid))
				{
					this.resetLockMsg();
					return DM.ReadPreData(this.X_list.get(i));
				}
				//x is locked by others
				else
				{
					this.setLockMsg(X_list.get(i).getLockID());
					return -1;
				}
			}
		}
		//cannot find x
		System.out.println("Read X" + _XID
				+ " from site" + site_ID + " fails, because it doesn't exist.");
		return -1;
	}
	
	/**
	 * write data
	 * @param _XID
	 * @param _Value
	 * @param tid
	 * @return
	 */
	public int WriteData(int _XID, int _Value, String tid)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).IsCopy())
			{
				System.out.println("test " + X_list.get(i).getID());
				continue;
			}
			//find x
			if(X_list.get(i).getID() == _XID)
			{
				//x is not locked
				if(!X_list.get(i).isLock())
				{
					DM.Ask_LM_setWrite_Lock(LM, X_list.get(i), tid);
					DM.WriteData(X_list.get(i), _Value);
					this.resetLockMsg();
					return _Value;
				}
				//x is locked by itself
				else if(X_list.get(i).getLockID().contains(tid))
				{
					DM.WriteData(X_list.get(i), _Value);
					this.resetLockMsg();
					return _Value;
				}
				//x is locked by others
				else
				{
					this.setLockMsg(X_list.get(i).getLockID());
					return -1;
				}
			}
		}
		//cannot find x
		System.out.println("Write X" + _XID
				+ " into site" + site_ID + " fails, because it doesn't exist.");
		return -1;
	}
	
	/**
	 * site fail
	 */
	public void Fail()
	{
		fail = true;
		DM.Fail(LM, X_list);
	}
	
	/**
	 * return backup site id
	 * @return
	 */
	public int getBackupID()
	{
		return backupID;
	}
	
	/**
	 * site backup
	 * @param backup
	 */
	public void Backup(Site backup)
	{
		if(!backup.isDown())
		{
			backupID = backup.getID();
			DM.Backup(X_list, backup.getX_list());
		}
	}
	
	/**
	 * site recovery
	 * @param target
	 * @return
	 */
	public ArrayList<String> Recovery(Site target)
	{
		ArrayList<String> a = new ArrayList<String>();
		this.fail = false;
		
		
		if(backupID == target.getID() && !target.isDown())
		{
			return DM.Recovery(X_list, target.getX_list());
		}
		else if(target.isDown())
		{
			System.out.println("target site" + target.getID() + " is down");
			return a;
		}
		else
		{
			System.out.println("wrong target site" + target.getID());
			return a;
		}
	}
	
	/**
	 * site dump
	 * @param intCurrentTimeStamp 
	 */
	public void Dump(int time)
	{
		DM.Dump(LM, X_list, time);
	}
	
	/**
	 * dump Xclass Xj
	 * @param _XID
	 */
	public void Dump(int _XID, int time)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).getID() == _XID && !X_list.get(i).IsCopy())
			{
				DM.Dump(LM, X_list.get(i), time);
			}
		}
	}
	
	/**
	 * abort Xclass X
	 * @param _XID
	 */
	public void Abort(int _XID)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).getID() == _XID)
			{
				DM.Abort(LM, X_list.get(i));
			}
		}	
	}
	
	/**
	 * abort transaction
	 * @param _tid
	 */
	public void AbortT(String _tid)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).getLockID().contains(_tid))
			{
				DM.Abort(LM, X_list.get(i));
			}
		}	
	}
	
	/**
	 * check which transaction lock x 
	 * @param _XID
	 * @return
	 */
	public String ByLock(int _XID)
	{
		for(int i = 0; i < X_list.size(); i++)
		{
			if(X_list.get(i).getID() == _XID)
			{
				return "X" + _XID + " is " + X_list.get(i).getLockType() 
					+ " locked by "  + X_list.get(i).getLockID();
			}
		}
		return "X" + _XID + " doesn't exist in this site."; 
	}
	
	/**
	 * print the state of Site
	 * @return
	 */
	public String ToString()
	{
		String s;
		if(fail)
		{
			s = "This site is down.\n";
		}
		else
		{
			s = "This site is up.\n";
		}
		for(int i = 0; i < X_list.size(); i++)
		{
			if(!X_list.get(i).IsCopy()){
				s += "X" + X_list.get(i).getID() + " = " 
					+ X_list.get(i).getValue() + " ";
			}
			else
			{
				s += "copy X" + X_list.get(i).getID() + " = " 
				+ X_list.get(i).getValue() + " ";
			}
		}
		return s;
	}
	
}
