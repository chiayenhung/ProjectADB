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
		//add even X variable into all sites
		//for(int i = 1; i <= 10; i++)
		//{
		//	Xclass X = new Xclass(2*i); 
		//	Insert_Xclass(X);
		//}
		//add odd X variable into target sites
		for (int i = 1; i <= 10; i++) {
			Xclass X = new Xclass(i * 2);
			this.X_list.add(X);
//			strbuilder.append("X" + i*2 + ", ");
		}
//		for (int i = 1; i < 20; i += 2) {
//			int temp = ((1 + i) % 10 == 0) ? 10 : (1 + i) % 10;
//			if (temp == this.site_ID+1) {
//				Xclass X = new Xclass(i);
//				this.X_list.add(X);
////				strbuilder.append("X" + i + ", ");
//			}
//		}
		
//		Xclass X2 = new Xclass(2);
//		Xclass X4 = new Xclass(4);
//		Xclass X6 = new Xclass(6);
//		Xclass X8 = new Xclass(8);
//		Xclass X10 = new Xclass(10);
//		Xclass X12 = new Xclass(12);
//		Xclass X14 = new Xclass(14);
//		Xclass X16 = new Xclass(16);
//		Xclass X18 = new Xclass(18);
//		Xclass X20 = new Xclass(20);
//		
		switch(site_ID)
		{
			//switch site id
//			case 1:
//				Insert_Xclass(X2);
//				Insert_Xclass(X8);
//				Insert_Xclass(X14);
//				break;
			case 2:
				Xclass X1 = new Xclass(1); 
				Insert_Xclass(X1);
				Xclass X11 = new Xclass(11); 
				Insert_Xclass(X11);
//				Insert_Xclass(X2);
//				Insert_Xclass(X8);
//				Insert_Xclass(X16);
				break;
//			case 3:
//				Insert_Xclass(X2);
//				Insert_Xclass(X10);
//				Insert_Xclass(X16);
//				break;
			case 4:
				Xclass X3 = new Xclass(3); 
				Insert_Xclass(X3);
				Xclass X13 = new Xclass(13); 
				Insert_Xclass(X13);
//				Insert_Xclass(X4);
//				Insert_Xclass(X10);
//				Insert_Xclass(X16);
				break;
//			case 5:
//				Insert_Xclass(X4);
//				Insert_Xclass(X10);
//				Insert_Xclass(X18);
//				break;
			case 6:
				Xclass X5 = new Xclass(5); 
				Insert_Xclass(X5);
				Xclass X15 = new Xclass(15); 
				Insert_Xclass(X15);
//				Insert_Xclass(X4);
//				Insert_Xclass(X12);
//				Insert_Xclass(X18);
				break;
//			case 7:
//				Insert_Xclass(X6);
//				Insert_Xclass(X12);
//				Insert_Xclass(X18);
//				break;
			case 8:
				Xclass X7 = new Xclass(7); 
				Insert_Xclass(X7);
				Xclass X17 = new Xclass(17); 
				Insert_Xclass(X17);
//				Insert_Xclass(X6);
//				Insert_Xclass(X12);
//				Insert_Xclass(X20);
				break;
//			case 9:
//				Insert_Xclass(X6);
//				Insert_Xclass(X14);
//				Insert_Xclass(X20);
//				break;
			case 10:
				Xclass X9 = new Xclass(9); 
				Insert_Xclass(X9);
				Xclass X19 = new Xclass(19); 
				Insert_Xclass(X19);
//				Insert_Xclass(X8);
//				Insert_Xclass(X14);
//				Insert_Xclass(X20);
				break;
		}
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
	public ArrayList<Xclass> getX_q()
	{
		return X_list;
	}
	
	/**
	 * insert a new X
	 */
	public void Insert_Xclass(Xclass _X)
	{
		X_list.add(_X);
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
				if(!X_list.get(i).IsLock())
				{
					
					DM.Ask_LM_setRead_Lock(LM, X_list.get(i), tid);
					this.resetLockMsg();
					return DM.ReadPreData(X_list.get(i));
				}
				//x is locked by itself
				else if(X_list.get(i).getLockID().contains(tid))
				{
					this.resetLockMsg();
//					return DM.ReadData(X_q.get(i));
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
				if(!X_list.get(i).IsLock())
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
			DM.Backup(X_list, backup.getX_q());
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
		fail = false;
		
		if(backupID == target.getID() && !target.isDown())
		{
			return DM.Recovery(X_list, target.getX_q());
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
	
	public Xclass dump(int xClassNum){
		return this.X_list.get(xClassNum - 1);
	}
	
}
