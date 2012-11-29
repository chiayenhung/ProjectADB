package adb;

import java.util.*;
/**
 * 
 *  Site Class Object
 *  @author Chia-Ming Lin, Li-Yen Hung
 */
public class Site{
	//class variable
	private int ID;
	private ArrayList<Xclass> X_q;
	private LockManager LM;
	private DataManager DM;
	private boolean Failed;
	private String lockmsg;
	private int backupID;
	
	/**
	 * class constructor
	 * @param _ID
	 */
	public Site(int _ID)
	{
		ID = _ID;//assign site id
		X_q = new ArrayList<Xclass>();
		LM = new LockManager();
		DM = new DataManager();
		Failed = false;
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
		return Failed;
	}
	/**
	 * Initialize Xclass
	 */
	public void Initial_Xclass()
	{
		//add even X variable into all sites
		//for(int i = 1; i <= 10; i++)
		//{
		//	Xclass X = new Xclass(2*i); 
		//	Insert_Xclass(X);
		//}
		//add odd X variable into target sites
		
		Xclass X2 = new Xclass(2);
		Xclass X4 = new Xclass(4);
		Xclass X6 = new Xclass(6);
		Xclass X8 = new Xclass(8);
		Xclass X10 = new Xclass(10);
		Xclass X12 = new Xclass(12);
		Xclass X14 = new Xclass(14);
		Xclass X16 = new Xclass(16);
		Xclass X18 = new Xclass(18);
		Xclass X20 = new Xclass(20);
		
		switch(ID)
		{
			//switch site id
			case 1:
				Insert_Xclass(X2);
				Insert_Xclass(X8);
				Insert_Xclass(X14);
				break;
			case 2:
				Xclass X1 = new Xclass(1); 
				Insert_Xclass(X1);
				Xclass X11 = new Xclass(11); 
				Insert_Xclass(X11);
				Insert_Xclass(X2);
				Insert_Xclass(X8);
				Insert_Xclass(X16);
				break;
			case 3:
				Insert_Xclass(X2);
				Insert_Xclass(X10);
				Insert_Xclass(X16);
				break;
			case 4:
				Xclass X3 = new Xclass(3); 
				Insert_Xclass(X3);
				Xclass X13 = new Xclass(13); 
				Insert_Xclass(X13);
				Insert_Xclass(X4);
				Insert_Xclass(X10);
				Insert_Xclass(X16);
				break;
			case 5:
				Insert_Xclass(X4);
				Insert_Xclass(X10);
				Insert_Xclass(X18);
				break;
			case 6:
				Xclass X5 = new Xclass(5); 
				Insert_Xclass(X5);
				Xclass X15 = new Xclass(15); 
				Insert_Xclass(X15);
				Insert_Xclass(X4);
				Insert_Xclass(X12);
				Insert_Xclass(X18);
				break;
			case 7:
				Insert_Xclass(X6);
				Insert_Xclass(X12);
				Insert_Xclass(X18);
				break;
			case 8:
				Xclass X7 = new Xclass(7); 
				Insert_Xclass(X7);
				Xclass X17 = new Xclass(17); 
				Insert_Xclass(X17);
				Insert_Xclass(X6);
				Insert_Xclass(X12);
				Insert_Xclass(X20);
				break;
			case 9:
				Insert_Xclass(X6);
				Insert_Xclass(X14);
				Insert_Xclass(X20);
				break;
			case 10:
				Xclass X9 = new Xclass(9); 
				Insert_Xclass(X9);
				Xclass X19 = new Xclass(19); 
				Insert_Xclass(X19);
				Insert_Xclass(X8);
				Insert_Xclass(X14);
				Insert_Xclass(X20);
				break;
		}
	}
	
	/**
	 * get site id
	 * @return
	 */
	public int getID()
	{
		return ID;
	}
	
	/**
	 * return a array list of x
	 * @return
	 */
	public ArrayList<Xclass> getX_q()
	{
		return X_q;
	}
	
	/**
	 * insert a new X
	 */
	public void Insert_Xclass(Xclass _X)
	{
		X_q.add(_X);
	}
	
	/**
	 * remove item
	 * @param location
	 */
	public void Remove_Xclass(int location)
	{
		//exit if the size is larger than the list length
		if(location > X_q.size()){return;}
		X_q.remove(location);
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
	public int ReadOnly(int _XID, String tid)
	{
		for(int i = 0; i < X_q.size(); i++)
		{
			//find x
			if(X_q.get(i).getID() == _XID)
			{
					return DM.ReadPreData(X_q.get(i));
			}
		}
		//not find x
		System.out.println("Read X" + _XID
				+ " from site" + ID + " fails, because it doesn't exist.");
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
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).IsCopy())
			{
				continue;
			}
			//find x
			if(X_q.get(i).getID() == _XID)
			{
				//x is not locked
				if(!X_q.get(i).IsLock())
				{
					
					DM.Ask_LM_setRead_Lock(LM, X_q.get(i), tid);
					this.resetLockMsg();
					return DM.ReadPreData(X_q.get(i));
				}
				//x is locked by itself
				else if(X_q.get(i).getLockID().contains(tid))
				{
					this.resetLockMsg();
					return DM.ReadData(X_q.get(i));
				}
				//x is locked by others
				else
				{
					this.setLockMsg(X_q.get(i).getLockID());
					return -1;
				}
			}
		}
		//not find x
		System.out.println("Read X" + _XID
				+ " from site" + ID + " fails, because it doesn't exist.");
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
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).IsCopy())
			{
				System.out.println("test " + X_q.get(i).getID());
				continue;
			}
			//find x
			if(X_q.get(i).getID() == _XID)
			{
				//x is not locked
				if(!X_q.get(i).IsLock())
				{
					DM.Ask_LM_setWrite_Lock(LM, X_q.get(i), tid);
					DM.WriteData(X_q.get(i), _Value);
					this.resetLockMsg();
					return _Value;
				}
				//x is locked by itself
				else if(X_q.get(i).getLockID().contains(tid))
				{
					DM.WriteData(X_q.get(i), _Value);
					this.resetLockMsg();
					return _Value;
				}
				//x is locked by others
				else
				{
					this.setLockMsg(X_q.get(i).getLockID());
					return -1;
				}
			}
		}
		//not find x
		System.out.println("Write X" + _XID
				+ " into site" + ID + " fails, because it doesn't exist.");
		return -1;
	}
	
	/**
	 * site fail
	 */
	public void Fail()
	{
		Failed = true;
		DM.Fail(LM, X_q);
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
			DM.Backup(X_q, backup.getX_q());
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
		Failed = false;
		
		if(backupID == target.getID() && !target.isDown())
		{
			return DM.Recovery(X_q, target.getX_q());
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
	 */
	public void Dump()
	{
		DM.Dump(LM, X_q);
	}
	
	/**
	 * dump Xclass Xj
	 * @param _XID
	 */
	public void Dump(int _XID)
	{
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).getID() == _XID && !X_q.get(i).IsCopy())
			{
				DM.Dump(LM, X_q.get(i));
			}
		}
	}
	
	/**
	 * abort Xclass X
	 * @param _XID
	 */
	public void Abort(int _XID)
	{
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).getID() == _XID)
			{
				DM.Abort(LM, X_q.get(i));
			}
		}	
	}
	
	/**
	 * abort transaction
	 * @param _tid
	 */
	public void AbortT(String _tid)
	{
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).getLockID().contains(_tid))
			{
				DM.Abort(LM, X_q.get(i));
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
		for(int i = 0; i < X_q.size(); i++)
		{
			if(X_q.get(i).getID() == _XID)
			{
				return "X" + _XID + " is " + X_q.get(i).getLockType() 
					+ " locked by "  + X_q.get(i).getLockID();
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
		if(Failed)
		{
			s = "This site is down.\n";
		}
		else
		{
			s = "This site is up.\n";
		}
		for(int i = 0; i < X_q.size(); i++)
		{
			if(!X_q.get(i).IsCopy()){
				s += "Current X" + X_q.get(i).getID() + " = " 
					+ X_q.get(i).getValue() + "\n";
			}
			else
			{
				s += "Current copy X" + X_q.get(i).getID() + " = " 
				+ X_q.get(i).getValue() + "\n";
			}
		}
		return s;
	}
}