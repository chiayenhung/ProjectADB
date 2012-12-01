package manager;

import java.util.*;
import java.io.*;

import component.Operation;
import component.Site;
import component.Transaction;
import component.Operation.action_type;
import component.Transaction.Attribute;


/**
 * 
 * Transaction Manager object class 
 *	@author Chia-Yen Hung 
 */
public class TransactionManager 
{
	//class variable
	private Map<String, Transaction> transactions; //list of transaction
	private Map<Integer, Site> sites;//list of site
	private String outFile = "";
	private String current_state="";
	private int intCurrentTimeStamp =0;
	private Map<String, ArrayList<Operation>> WaitingList;//a map structure to hold all kind of waiting operation from all transactions
	private Map<Integer, Integer> SiteRecords;//a map structure to hold all the down/recovery record
	
	/**
	 * Transaction Manager object class constructor
	 */
	public TransactionManager(String output_Filename)
	{
		//instantiate lists
		this.sites = new HashMap<Integer, Site>();
		this.transactions = new HashMap<String, Transaction>();
		this.WaitingList = new HashMap<String, ArrayList<Operation>>();
		this.outFile = output_Filename;
		this.SiteRecords = new HashMap<Integer, Integer>();
		for(int i = 1 ; i < 11; i++)
			this.sites.put(i, new Site(i));
		
	}
	/**
	 * This method can add site for  
	 * @param site
	 */
//	public void addSite(Site site)
//	{
//		sites.put(site.getID(), site);
//		//create new record for new site
//		SiteRecords.put(site.getID(), intCurrentTimeStamp); //fail timestamp record
//		WriteOutput("Added site " + site.getID());
//	}

	/**
	 * Do method execute all kinds of input instructions
	 */
	public void Do(String action)
	{
		//determine which action to perform
		action = action.trim();
		String[] actions = action.split(";");
		for(int i = 0;i < actions.length; i++)
		{
			//make it lower case
			String temp = actions[i].toLowerCase();
			temp = temp.trim();
			
			if(temp.contains("begin("))
			{	
				//begin a new transaction
				CreateNewTransaction(temp,intCurrentTimeStamp,Transaction.Attribute.ReadWrite);
			}
			else if(temp.contains("beginro("))
			{
				//this transaction is read-only
				//begin a new transaction
				CreateNewTransaction(temp,intCurrentTimeStamp,Transaction.Attribute.ReadOnly);
			}
			else if(temp.contains("w("))
			{
				//write x variable at site
				//need return result??
				Write(temp);
			}
			else if(temp.contains("end("))
			{
				//extract transaction id ;譬如 end(T1)，要把T1拿出來
				int index = temp.indexOf("(");
				String id = temp.substring(index+1, temp.length()-1);
				
				//commit and end a particular transaction
				//test whether this Transaction can commit or not
				if(CanCommit(temp))
				{
					
					Commit("commit(" + id +")");
				}
				else
				{
					
				}
			}
			else if(temp.contains("abort("))
			{
				//abort a transaction
				Abort(temp);
			}
			
			else if(temp.substring(0, 2).compareTo("r(")==0)
			{
				//read a value
				Read(temp);
			}
			
			else if(temp.contains("fail("))
			{
				//purposely fail a site
				Fail(temp);
				
			}
			else if(temp.contains("dump("))
			{
				//commit everyhting on all sites/a site
				Dump(temp);
			}
			
			else if(temp.contains("recover"))
			{
				//recover a down site
				Recovery(temp);
			}
			
			else
			{
				WriteOutput("Unrecognized command " + temp);	
			}
		
		}
		
		current_state = action;
		//increase the timestamp value
		intCurrentTimeStamp++;
	}
	
	/**
	 * This method can be used to report current state
	 */
	public String ToString()
	{
		//returning what operation is being performed
		return current_state;
	}
	 
	/**
	 * WriteOutput method can write the output result to a file 
	 */
	private void WriteOutput(String info)
	{
		boolean blnExist=false;
		try 
		{
	        //check for existing file
			File f = new File(outFile);
			blnExist = f.exists();
			if(!blnExist)
			{
				//create a new file if doesn't exist
				blnExist = f.createNewFile();
				f=null;
			}
			if(blnExist)
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(outFile,true));
				Date date = new Date();
				out.write(date.toString() + ": " + info);
				out.newLine();
				out.flush();
				out.close();
			}
			else
			{
				System.out.println("Failed to create output file");
			}
	    } 
		catch (IOException e) 
	    {
			System.out.println("Error in WriteOutput-"+ e.getMessage());
	    }


	}
	
	/**
	 * This method can write the value to the variable X 
	 */
	private void Write(String info)
	{
		
		try
		{
			WriteOutput("Processing " + info);
			boolean blnInsertOp = false; //flag to insert the current operation into the transaction object
			
			Transaction t  = null;
			//filter some redundant character
			int index = info.indexOf("(");
			info = info.substring(index+1, info.length()-1);
			
			
			String[] elements = info.split(",");//split the content by "," separator
			String t_id = elements[0];//transaction id
			t_id = t_id.trim(); //會像是T1 T2 T3
			
			String x_id = elements[1];//x's id
			x_id = x_id.substring(x_id.indexOf("x")+1);
			x_id = x_id.trim();  //取出只有X的號碼，EX: X的 1,2,4,5
			
			String x_value = elements[2];//x's value  要寫在X上的值
			x_value = x_value.substring(0, x_value.length());
			x_value = x_value.trim();
			//convert both string into int
			int intValue = Integer.parseInt(x_value);  //x_value是string ， intValue是int
			int intX = Integer.parseInt(x_id);		   //x_id是string，intX是int
			
			//check for valid transaction 
			if(transactions.containsKey(t_id))
			{
				//check transaction attribute
				//doesn't allow to write if is read-only
				t = (Transaction)transactions.get(t_id);
				if(t.getAttribute() == Transaction.Attribute.ReadOnly)
				{
					WriteOutput("Transaction " + t_id + " doesn't allow to write.");
					return;
				}
			}
			else
			{
				WriteOutput("Transaction " + t_id + " not found.");
				return;
			}
			
			ArrayList<Site> evenSite = new ArrayList<Site>();
			
			if( (intX%2) == 1)
			{
				int answer = (intX % 10) +1;
				Site s = (Site)sites.get(answer);
				if(!s.isDown())
				{
					blnInsertOp=WriteToSingle(intX,intValue,t_id,answer);
				}
				else
				{
					//insert the stuck operation into the waiting list
					//time stamp = 0 because hasn't written to site yet
					Operation op = new Operation(Operation.action_type.write,intValue,intX,0);
					InsertIntoWaitingList(op,t_id);
				}
			}
			else
			{
				int temp = (intX/2)*3;
				int answer = 0;
				for (int i = (temp-2); i <= temp; i++){
					if((i%10) == 0)
					{
						answer = 10;
					}
					else
					{
						int answerSite = (i%10);
						answer = answerSite;
					}
					Site s1 = (Site)sites.get(answer);
					evenSite.add(s1);		
				}
				
				Iterator<Site> iteratorSite = evenSite.iterator();
				
				boolean writeFlag = false;
				while(iteratorSite.hasNext()){
					Site s = (Site)iteratorSite.next();
			        if(!s.isDown())
			        {
			        	//ReadSingle(s.getID(),intX,t_id);
			        	blnInsertOp=WriteToSingle(intX,intValue,t_id,s.getID());
			        	//break;
			        	writeFlag = true;
			        }
			        if( (!iteratorSite.hasNext()) && (writeFlag == false) )
			        {
			        	//insert the stuck operation into the waiting list
						//time stamp = 0 because hasn't written to site yet
						Operation op = new Operation(Operation.action_type.write,intValue,intX,0);
						InsertIntoWaitingList(op,t_id);
			        }   
				}
			}
			//insert the new operation into the record
			if(blnInsertOp)
			{
				//increase the time stamp counter
				//so that don't hold a same time stamp will regular operation
				//maybe this is one from waiting list
				intCurrentTimeStamp++;
				Operation op = new Operation(Operation.action_type.write, intValue,intX,intCurrentTimeStamp);
				t.Insert_Operation(op);
				WriteOutput("Operation inserted into " + t_id);
			}
			
		}
		catch(Exception e)
		{
			WriteOutput("Error in Write-"+ e.getMessage());
			
		}
		
	}
	
	/**
	 * 
	 */
	private boolean WriteToAll(int intX,int intValue,String t_id)
	{
		//success if we make it to One site and not all sites down
		boolean blnWriteOnce= false;
		
		ArrayList<Site> kk = new ArrayList<Site>();
		int xEvenTargetSite = 0;
		if(intX%2 == 0){
			int temp = (intX/2)*3;
			for (int i = (temp-2); i <= temp; i++){
				if((i%10) == 0)
				{
					xEvenTargetSite = 10;
					
				}
				else
				{
					int answerSite = (i%10);
					xEvenTargetSite = answerSite;
				}
				Site s1 = sites.get(xEvenTargetSite);
				kk.add(s1);
			}
		}
		
		Iterator<Site> iteratorSite = kk.iterator();
		
		//get iterator
		Iterator<Site> k = sites.values().iterator();
		
		while(iteratorSite.hasNext())
		{
			Site _site = (Site)iteratorSite.next();
			
			//only if is not down
			if(!_site.isDown())
			{
				_site.WriteData(intX, intValue, t_id);
				
				//check lock message
				if(_site.getLockMsg().compareToIgnoreCase(t_id)==0)
				{
					//same id
					//write again
					_site.WriteData(intX, intValue, t_id);
					blnWriteOnce = true;//set flag, indicating at least write once
				}
				else if(_site.getLockMsg() != "NULL")
				{
					
					WriteOutput("Write X" + intX
							//+ " from site" + _site.getID() 
							+ " failed, because it is locked by transaction " 
							+ _site.getLockMsg());				
					//make decision on which transaction to be aborted
					if(MakeDecision(_site.getLockMsg(),t_id))
					{
						//request-trasanction still alive	
						//insert the stuck operation into the waiting list
						//time stamp = 0 because hasn't written to site yet
						Operation op = new Operation(Operation.action_type.write,intValue,intX,0);
						InsertIntoWaitingList(op,t_id);
					}
					else
					{
						//current transaction aborted
					}
					//reset lock message
					_site.resetLockMsg();
					
					//break the loop
					break;
				}
				else
				{
					blnWriteOnce = true;//set flag, indicating at least write once
				}
			}//end if site is down
		}

		return blnWriteOnce;
	}
	
	/**
	 * write the value to variable that is on target site and back up site(id+1)
	 */
	private boolean WriteToSingle(int intX,int intValue,String t_id,int targetSite)
	{
		//success if we make it to One site or backup
		boolean blnWriteOnce= false;
		//target + backup
		Site s_target = (Site)sites.get(targetSite);
		int backup=0;
		if(targetSite==1)			
		{
			backup = 10;
		}
		else						
		{
			backup = targetSite-1;
		}
		
		Site s_backup = (Site)sites.get(backup);
		
		s_target.WriteData(intX, intValue, t_id);
		
		//self locked
		if(s_target.getLockMsg().compareToIgnoreCase(t_id)==0)
		{
			s_target.WriteData(intX, intValue, t_id);
			blnWriteOnce  = true;
		}
		else if(s_target.getLockMsg() != "NULL")
		{
			WriteOutput("Write X" + intX
					//+ " from site" + s_target.getID() 
					+ " failed, because it is locked by transaction " 
					+ s_target.getLockMsg());
			
			//abort my self?
			if(MakeDecision(s_target.getLockMsg(),t_id))
			{
				
				//save this operation in waiting list
				//insert the stuck operation into the waiting list
				//time stamp = 0 because hasn't written to site yet
				Operation op = new Operation(Operation.action_type.write,intValue,intX,0);
				InsertIntoWaitingList(op,t_id);
				
			}
			else
			{
				//this transaction no longer exist
				
			}
			//reset lock message
			s_target.resetLockMsg();
			blnWriteOnce = false;
		}
		else
		{
			blnWriteOnce  = true;
		}
	
		//update backup site also
		if(!s_backup.isDown())
		{
			if(blnWriteOnce)
			{
				//do backup
				s_target.Backup(s_backup);
				/*
				//set lock
				s_backup.WriteData(intX, intValue, t_id);
				//try to write again if is self lock
				if(s_backup.getLockMsg().compareToIgnoreCase(t_id)==0)
				{
					s_backup.WriteData(intX, intValue, t_id);
				}
				*/
			}
			
		
		}//back up
		
	
		return blnWriteOnce;
		
	}
	
	/**
	 * Commit method can commit each operation under the transaction
	 */
	private void Commit(String info)
	{
		try
		{
			WriteOutput("Processing " + info);
			
			//extract transaction id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			if(transactions.containsKey(id))
			{
				//commit each operation under this transaction
				Transaction t = (Transaction)transactions.get(id); 
				ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
				
				for(int i = 0 ;i < ops.size(); i++)
				{
					Operation op = ops.get(i);
					if(op.getOperationType() == Operation.action_type.write)
					{
						//get site id where this x belongs to
						//calculate the site id
						int answer = (op.getTarget() % 10) + 1;
						
						//determine whether is at all sites or just one single + backup
						if(op.getTarget() % 2 == 0)
						{
							//all sites
							
							//get iterator if not site is down
							Iterator<Site> k = sites.values().iterator();
						    while (k.hasNext()) 
						    {
						    	Site _site = (Site)k.next();
								_site.Dump(op.getTarget()); //op.getTarget()是得到 X1'2'3 ..... etc
								
						    }
						}
						else
						{
							//single site + backup
							//check for site down
							Site _site = (Site)sites.get(answer);
							_site.Dump(op.getTarget());
							//_site = (Site)sites.get(answer+1);
							//_site.Dump(op.getTarget());
							
						}
					}
				}
				WriteOutput("Transaction " + id + " commited.");
				EndTransaction("end("+ id + ")");
			}
			else
			{
				WriteOutput("Cannot commit transaction " + id + " , not found.");
			}
			
			
		}
		catch(Exception e)
		{
			WriteOutput("Error in Commit-"+ e.getMessage());
		}
	}
	
	
	/**
	 * This method can end a transaction by the transaction ID 
	 */
	private void EndTransaction(String _id)
	{
		try
		{
			
			//extract transaction id
			int index = _id.indexOf("(");
			String id = _id.substring(index+1,_id.length()-1);
			if(transactions.containsKey(id))
			{
				//remove the transaction from the map
				transactions.remove(id);
				WriteOutput("Transaction " + id + " removed from the list.");
				
				//pending operation will be removed as well
				//impossible to have pending operation if the caller is commit()
				//and is ok to remove if the caller is abort()
				WaitingList.remove(id);
				//process waiting list
				ProcessWaitingList();
			}
			else
			{
				WriteOutput("Cannot end transaction " + id + " , not found.");
			}
		}
		catch(Exception e)
		{
			//WriteOutput(e.getMessage());
			System.out.println("Error in EndTransaction-"+ e.getMessage());
		}
	}
	
	/**
	 * This method can create a new transaction
	 */
	private void CreateNewTransaction(String _id, int timestamp, Transaction.Attribute attri)
	{
		
		try
		{
			//extract transaction id
			int index = _id.indexOf("(");
			String id = _id.substring(index+1, _id.length()-1);
			
			//check for existing transaction ID before create new one
			if(transactions.containsKey(id))
			{
				String msg = "Transaction " + id + " already existed in the collection.";
				WriteOutput(msg);
				//System.out.println(msg);
			}
			else
			{
				//create a new transaction object
				Transaction t = new Transaction(id, timestamp, attri);
				transactions.put(id, t);//insert into map
				WriteOutput("Transaction " + id + " has been created.");
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in CreateNewTransaction-"+ e.getMessage());
		}
	}
	
	/**
	 * This method can be used to abort a transaction and roll back
	 */
	private  void Abort(String info)
	{
		try
		{
			WriteOutput("Processing " + info);
			//extract transaction id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			//abort the operation if the transaction not found
			if(!transactions.containsKey(id))
			{
				WriteOutput("Transaction " + id + " not found.");
				return;
			}
			
			Transaction t = (Transaction)transactions.get(id);
			//get a list of operation belongs to this transaction id
			ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
			//loop through every operation
			for(int i = 0 ; i<ops.size();i++)
			{
				Operation op = ops.get(i);
				
				
				if((op.getOperationType() == Operation.action_type.write) ||
						(op.getOperationType() == Operation.action_type.read))
				{
					//roll back if is write operation only
					int answer = (op.getTarget() % 10) + 1;
					if(op.getTarget() % 2 == 0)
					{
						//x is at all sites
						Iterator<Site> it = sites.values().iterator();
						while(it.hasNext())
						{ 
							Site s = (Site)it.next();
							
							s.AbortT(id);
							//s.Abort(op.getTarget());
						}
						
						//can stop, because aborted from all sites
						//exit for loop
						break;
					}
					else
					{
						//x is at one particular site + backup site
						Site s = (Site)sites.get(answer);
						//s.Abort(op.getTarget());
						s.AbortT(id);
					}
				}
			}
			
			//end the transaction
			EndTransaction("end("+ id +")");
		}
		catch(Exception e)
		{
			System.out.println("Error in Abort-"+ e.getMessage());
		}
		
	}
	
	/**
	 * This method can be used to compare their time stamp so that:
	 * to aborted the request transaction or keep in the waiting list
	 */
	private boolean MakeDecision(String LockByID,String RequestID)
	{
		if(transactions.containsKey(LockByID) && transactions.containsKey(RequestID))
		{
			//if both transaction existed
			Transaction t_locked = (Transaction)transactions.get(LockByID);
			Transaction t_request = (Transaction)transactions.get(RequestID);
			if(t_locked.getTimeStamp()<=t_request.getTimeStamp())
			{
				//t_request is younger or equal than the t_locked
				//keep t_locked 
				//abort request id
				Abort("abort(" + RequestID + ")");
				
			}
			else
			{
				//t_locked is younger than t_request
				//keep in waiting list
				return true;//alive
			}
		}
		else
		{
			WriteOutput("Cannot make decision because invalid transaction's ID");
		}
		//signal the caller cannot proceed
		return false;//request transaction is gone
	}
	
	/**
	 * This method can be used to insert the stuck operation into the waiting list
	 * , Update and commit/end operations
	 */
	private void InsertIntoWaitingList(Operation op,String t_id)
	{
		ArrayList<Operation> ops;
		
		//check if there is an existing record current transaction
		if(WaitingList.containsKey(t_id))
		{
			ops = (ArrayList<Operation>)WaitingList.get(t_id);
			ops.add(op);
		}
		else
		{
			//create one
			ops = new ArrayList<Operation>();
			ops.add(op);
			WaitingList.put(t_id, ops);
		}
		WriteOutput("Operation inserted into waiting list.");
	}
	//to tell a site to recover
	private void Recovery(String info)
	{
		
		try
		{
			Site s_backup = null;
			//boolean blnOnce = false;//to know whether there is a source site up and running to recover
			
			WriteOutput("Processing " + info);
			
			//get site id
			//extract site id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			int intIndex = Integer.parseInt(id);
			
			//check for valid site id
			if(!sites.containsKey(intIndex))
			{
				WriteOutput("Site " + id + " not found.");
				return;
			}
			
			//call a site to recovery
			//get failed site
			Site s_down = (Site)sites.get(intIndex);
			//look for backup site
			if(intIndex==1)
			{
				//available at all 
				//but this site is a backup for another odd index site
				//so need to do back up from that site
				//one back up only
				s_backup = (Site)sites.get(10);
				
			}
			else
			{
				//one back up only
				s_backup = (Site)sites.get(intIndex-1);
				
				
			}
			//check whether backup is down also
			
			if(s_backup.isDown())
			{
				WriteOutput("Operation failed because backup site is down currently");
			}
			else
			{
				
				s_down.Recovery(s_backup);
				/*
				//update timestamp
				String record = (String)SiteRecords.get(intIndex);
				
				String[] recs = record.split(";");
				record = recs[0] + ";" + Integer.toString(intCurrentTimeStamp);
				SiteRecords.put(intIndex, record);
				*/
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Error in Recovery-"+ e.getMessage());
		}
		
	}
	
	/**
	 * this method can be used to purposely fail a site
	 */
	private void Fail(String info)
	{
		try
		{
			WriteOutput("Processing " + info);
			Site s_backup = null;
			//get site id
			//extract site id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			int intIndex = Integer.parseInt(id);
			//check for valid site id
			if(!sites.containsKey(intIndex))
			{
				WriteOutput("Site " + id + " not found.");
				return;
			}
			
			//call backup before fail
			if(intIndex==1)
			{
				s_backup = (Site)sites.get(10);
			}
			else
			{
				s_backup = (Site)sites.get(intIndex-1);
			}
			//call a site to fail
			Site s = (Site)sites.get(intIndex);
			s.Backup(s_backup);
			s.Fail();
			//String record = (String)SiteRecords.get(s.getID()); 
			//record = Integer.toString(intCurrentTimeStamp) + ";0";//reset recovery time
			
			SiteRecords.put(s.getID(),intCurrentTimeStamp);
		}
		catch(Exception e)
		{
			System.out.println("Error in Fail-"+ e.getMessage());
		}
	}

	/**
	 * This method can commit a site/variable/ALL
	 */
	private void Dump(String info)
	{
		WriteOutput("Processing " + info);
		try
		{
			//to determine whether is a site/a variable /or all
			if(info.compareToIgnoreCase("dump()")==0)
			{
				//commit everything
				CommitALL();
			}
			else if(info.contains("dump(x"))
			{
				//commit only a particular variable
				//check for if target site is down
				//get variable id
				//extract variable id
				int index = info.indexOf("(x");
				String id = info.substring(index+2, info.length()-1);
				
				int intID = Integer.parseInt(id);
				
				intID = (intID % 10) + 1;
				
				Site s = (Site)sites.get(intID);
				if(!s.isDown())
				{
					CommitVariable(info);
					WriteOutput("Site" + id + " " +s.ToString());
				}
				else
				{
					WriteOutput("Site" + id + " is down");
				}
			}
			else
			{
				//commit a site
				//get variable id
				//extract variable id
				int index = info.indexOf("(");
				String id = info.substring(index+1, info.length()-1);
				
				int intID = Integer.parseInt(id);
				Site s = (Site)sites.get(intID);
				if(!s.isDown())
				{
					CommitSite(info);
					WriteOutput("Site" + id + " " +s.ToString());
				}
				else
				{
					WriteOutput("Site" + id + " is down");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in Dump-"+ e.getMessage());
		}
	}
	
	/**
	 * this method can be used to commit a particular variable at all sites
	 */
	private void CommitVariable(String info)
	{
		try
		{
			//get variable id
			//extract variable id
			int index = info.indexOf("(x");
			String id = info.substring(index+2, info.length()-1);
			
			int intX = Integer.parseInt(id);
			//calculate the site id
			int answer = (intX % 10) +1;
			
			//determine whether is at all sites or just one single + backup
			if(intX % 2 == 0)
			{
				//at all sites
				Iterator<Site> it = sites.values().iterator();
				while(it.hasNext())
				{
					Site s = (Site)it.next();
					s.Dump(intX);
				}
			}
			else
			{
				//at particular site + backup
				//check for valid site id
				if(!sites.containsKey(answer))
				{
					WriteOutput("Site " + answer + " not found.");
					return;
				}
				Site s = (Site)sites.get(answer);
				s.Dump(intX);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in CommitVariable-"+ e.getMessage());
		}
		
	}

	/**
	 * this method can be used to commit a site
	 */
	private void CommitSite(String info)
	{
		try
		{
			//get site id
			//extract site id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			int intID = Integer.parseInt(id);
			//check if the site id valid
			if(!sites.containsKey(intID))
			{
				WriteOutput("Site " + id +" not found.");
				return;
			}
			
			//every site has even index Xs
			//such as x:2,4,6,8,10,12,14,16,18,20
			for(int i = 1;i<=5;i++)
			{
				int evenIndex = i*2;
				
				CommitVariable("dump(x" + evenIndex + ")");
				
			}
		
			//find that particular odd indexed x in this site
			//odd index = (site-1)
			//if odd index = 0 then is at site 10
			int oddVarIndex = intID -1;
			if(oddVarIndex<1)
			{
				oddVarIndex = 10;
			}
			
			if(oddVarIndex%2>0)
			{
				CommitVariable("dump(x" + oddVarIndex + ")");
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Error in CommitSite-"+ e.getMessage());
		}
	}

	/**
	 * this method can be used to commit every element on every site
	 */
	private void CommitALL()
	{
		Iterator<Site> it = sites.values().iterator();
		while(it.hasNext())
		{
			Site s = (Site)it.next();
			if(s.isDown())
			{
				//do nothing
			}
			else
			{
				s.Dump();
			}
			WriteOutput("Site" + s.getID() + " " + s.ToString());
		}
	}

	/**
	 * This method can be used to read a variable
	 */
	private void Read(String info)
	{
		WriteOutput("Processing " + info);
		try
		{
			//int value = 0;
			//Site  s = null;
			//filter some redundant character
			int index = info.indexOf("(");
			info = info.substring(index+1, info.length()-1);
			
			String[] elements = info.split(",");
			
			String t_id = elements[0];
			
			String x_id = elements[1].trim().substring(1);
			
			//check for valid transaction id
			if(!transactions.containsKey(t_id))
			{
				WriteOutput("Transaction " + t_id + " not found.");
				return;
			}
			
			
			
			//convert x id into int
			int intX = Integer.parseInt(x_id);
			//System.out.println(t_id);
			//get transaction object
			
			if(!transactions.containsKey(t_id))
			{
				WriteOutput("Transaction " + t_id + " not found.");
				return;
			}
			Transaction t = (Transaction)transactions.get(t_id);
			
			if(t.getAttribute()==Transaction.Attribute.ReadOnly)
			{
				//read from multiversion
				ReadOnly(t_id,intX);
			}
			else
			{
				//set read lock
				ReadWithLock(intX,t_id);
			}
			
		
		}
		catch(Exception e)
		{
			System.out.println("Error in Read-"+ e.getMessage());
		}
	}
	
	/**
	 * this method can call the site to use multiversion method to read
	 */
	private void ReadOnly(String t_id,int intX)
	{
		Site s=null;
		int value=0;
		//get target site
		int answer = (intX % 10 ) + 1;
		if(intX%2==0)
		{
			
			//at all site
			//simply pick one, must be up and running
			Iterator<Site> k = sites.values().iterator();
		    while (k.hasNext()) 
		    {
		    	s = (Site)k.next();
				if( s.isDown()==false)
				{
					break;//break the loop immediately after you have one site is up
				}
				
		    }
		    
			if(s!=null)
			{
				//read the value
				value = s.ReadOnly(intX, t_id);
				WriteOutput("x" + intX + " is " + value);
			}
			else
			{
				//abort this transaction since none of the site is up
				WriteOutput("Cannot read x" + intX + " all sites are down");
				Abort("abort(" +  t_id +")");
			}
		
			
		}
		else
		{
			//at one particular site
			if(!sites.containsKey(answer))
			{
				WriteOutput("Site " + answer + " doesn't contained variable x" + intX + ".");
				//impossible will be at backup site also
				return;
			}
			//target site
			s = (Site)sites.get(answer);
			if(!s.isDown())
			{
				value = s.ReadOnly(intX, t_id);
				WriteOutput("x" + intX + " is " + value);
			}
			else
			{
				//get from backup site
				int backup = 0;
				if(answer%2==0)
				{
					backup = answer-1;
				}
				else
				{
					backup = answer+1;
				}
				s = (Site)sites.get(backup);
				if(!s.isDown())
				{
					value = s.ReadOnly(intX,t_id);
					WriteOutput("x" + intX + " is " + value);
				}
				else
				{
					//abort this transaction since none of the site is up
					WriteOutput("Cannot read x" + intX + " all sites are down");
					Abort("abort(" +  t_id +")");
				}
			}
			
		}
	}
	/**
	 * this method can read the variable is at target + backup
	 */
	private void ReadSingle(int TargetSite,int intX,String t_id)
	{
		int value = 0;
		boolean blnSuccess = false;
	
		//at one particular site
		if(!sites.containsKey(TargetSite))
		{
			WriteOutput("Site " + TargetSite + " doesn't contained variable x" + intX + ".");
			//impossible will be at backup site, so won't check
			//abort the transaction
			Abort("abort(" + t_id + ")");
			return;
		}
		//target site
		Site s = (Site)sites.get(TargetSite);
		WriteOutput("target site " + TargetSite);
		//if(!s.isDown())
		//{
			value = s.ReadData(intX, t_id);
			
			//check lock
			if(s.getLockMsg()=="NULL")
			{
				blnSuccess = true;
				WriteOutput("x" + intX + " is " + value);
				/*
				//set lock on backup site also
				int intBackup =0;
				if(TargetSite%2==0)
				{
					intBackup = TargetSite-1;
				}
				else
				{
					intBackup = TargetSite+1;
				}
				Site s_backup = (Site)sites.get(intBackup);
				if(!s_backup.isDown())
				{
					s_backup.ReadData(intX, t_id);
				}
				*/
				//write to transaction record
				//increase time stamp so that won't conflict with regular transaction
				//maybe this operation is invoke from waiting list
				intCurrentTimeStamp++;
				Transaction t = (Transaction)transactions.get(t_id);
				Operation op = new Operation(Operation.action_type.read,0,intX,intCurrentTimeStamp);
				t.Insert_Operation(op);
				WriteOutput("Operation input into " + t_id);
			}
			else if(s.getLockMsg().compareToIgnoreCase(t_id)==0)
			{
				//self locked
				//try again
				value = s.ReadData(intX, t_id);
				blnSuccess = true;
				/*
				//set lock on backup site also
				Site s_backup = (Site)sites.get(TargetSite+1);
				if(!s_backup.isDown())
				{
					s_backup.ReadData(intX, t_id);
					s_backup.ReadData(intX, t_id);//read again to get the value if is the same transaction id
				}
				*/
			}
			else
			{
				//abort current transaction?
				if(MakeDecision(s.getLockMsg(),t_id))
				{
					//still alive
					blnSuccess = true;
					WriteOutput("x" + intX + " is lock by " + s.getLockMsg());
					
					//reset message
					s.resetLockMsg();
				}
				else
				{
					//current transaction is gone
					//reset message
					s.resetLockMsg();
					return;//exit method
				}
				
			}
		//}//end of if the target site is up
		
		if(!blnSuccess)
		{
			//save the operation to waiting list
			//time stamp = 0, because hasn't written to site yet
			Operation op = new Operation(Operation.action_type.read,0,intX,0);
	    	InsertIntoWaitingList(op,t_id);
		}
	}
	/**
	 * this method can read a variable which is at all sites
	 */
	private void ReadAll(int TargetSite,int intX,String t_id)
	{
		int value = 0;
		boolean blnSuccess = false;
		Site s = null;
		//at one particular site
		if(!sites.containsKey(TargetSite))
		{
			WriteOutput("Site " + TargetSite + " doesn't contained variable x" + intX + ".");
			//impossible will be at backup site, so won't check
			//abort the transaction
			Abort("abort(" + t_id + ")");
			return;
		}		
		//check target site is up and running
		//s = (Site)sites.get(TargetSite);
		//if(!s.isDown())
		//{
		//read the value, and set lock at all sites
		
		ArrayList<Site> kk = new ArrayList<Site>();
		
		//int xOddTargetSite = (intX % 10) +1;
		int xEvenTargetSite = 0;
		if(intX%2 == 0){
			int temp = (intX/2)*3;
			for (int i = (temp-2); i <= temp; i++){
				if((i%10) == 0)
				{
					xEvenTargetSite = 10;
					
				}
				else
				{
					int answerSite = (i%10);
					xEvenTargetSite = answerSite;
				}
				Site s1 = sites.get(xEvenTargetSite);
				kk.add(s1);		
			}			
		}			
		//Iterator<Site> k = sites.values().iterator();
		
		Iterator<Site> iteratorSite = kk.iterator();
		
		while(iteratorSite.hasNext())
		{
			s = (Site)iteratorSite.next();
			if(!s.isDown())
	    	{
	    		//read + set lock
		    	value = s.ReadData(intX, t_id);
		    	if(s.getLockMsg().compareToIgnoreCase(t_id)==0)
		    	{
		    		//self locked
		    		blnSuccess = true;
		    		value = s.ReadData(intX, t_id);
		    		this.WriteOutput("the target site is site"+ s.getID());
		    		//return;
		    	}
		    	else if(s.getLockMsg()!="NULL")
		    	{
		    		WriteOutput("x" + intX + " is locked by " + s.getLockMsg());
		    		
		    		
		    		//to abort current transaction?
		    		if(MakeDecision(s.getLockMsg(),t_id))
		    		{
		    			//reset the lock msg
			    		s.resetLockMsg();
		    			//current transaction still alive
		    			//write into waiting list at the end of method
		    			break;
		    		
		    		}
		    		else
		    		{
		    			//current transaction aborted
		    			//reset the lock msg
			    		s.resetLockMsg();
		    			return;
		    		}
		    		
		    		
		    	}
		    	else
		    	{
		    		//no lock on it
		    		blnSuccess = true;
		    	}
	    	}//if site is up only
			
		}
		if(!blnSuccess)
		{
			//save the operation to waiting list
			//time stamp = 0, because hasn't written to site yet
			Operation op = new Operation(Operation.action_type.read,0,intX,0);
	    	InsertIntoWaitingList(op,t_id);
		}
		else
		{
			WriteOutput("x" + intX + " is " + value);
			//write to transaction record
			//increase time stamp so that won't conflict with regular transaction
			//maybe this operation is invoke from waiting list
			intCurrentTimeStamp++;
			Transaction t = (Transaction)transactions.get(t_id);
			Operation op = new Operation(Operation.action_type.read,0,intX,intCurrentTimeStamp);
			t.Insert_Operation(op);
			WriteOutput("Operation input into " + t_id);
		}
	}
	//read will set a read-lock
	private void ReadWithLock(int intX,String t_id)
	{
		
		//get target site
		int answer = 0;
		ArrayList<Site> evenSite = new ArrayList<Site>();
		
		if( (intX%2) == 1)
		{
			answer = (intX % 10) +1;
			Site s = (Site)sites.get(answer);
			if(!s.isDown())
			{
				ReadSingle(answer,intX,t_id);
			}
			else
			{
				//insert the current operation into the waiting list
				//insert the stuck operation into the waiting list
				//time stamp = 0, because hasn't written into site yet
				Operation op = new Operation(Operation.action_type.read,0,intX,0);
				InsertIntoWaitingList(op,t_id);
			}
		}
		else
		{
			int temp = (intX/2)*3;
			for (int i = (temp-2); i <= temp; i++){
				if((i%10) == 0)
				{
					answer = 10;
				}
				else
				{
					int answerSite = (i%10);
					answer = answerSite;
				}
				Site s1 = (Site)sites.get(answer);
				evenSite.add(s1);		
			}
			
			Iterator<Site> iteratorSite = evenSite.iterator();
			
			
			while(iteratorSite.hasNext()){
				Site s = (Site)iteratorSite.next();
		        if(!s.isDown())
		        {
		        	ReadSingle(s.getID(),intX,t_id);
		        	break;
		        }
		        if(!(iteratorSite.hasNext()))
		        {
		        	//insert the current operation into the waiting list
					//insert the stuck operation into the waiting list
					//time stamp = 0, because hasn't written into site yet
					Operation op = new Operation(Operation.action_type.read,0,intX,0);
					InsertIntoWaitingList(op,t_id);
		        }
		        
			}
			
			
			
		}
		//check whether the target down is down currently
		//Site s = (Site)sites.get(answer);
		/*
		if(!s.isDown())
		{
			if(intX%2==0)
			{
				ReadAll(answer,intX,t_id);
				//ReadSingle(xEvenTargetSite, intX, t_id);
			}
			else
			{  
				ReadSingle(answer,intX,t_id);
			}
		}
		else
		{
			//insert the current operation into the waiting list
			//insert the stuck operation into the waiting list
			//time stamp = 0, because hasn't written into site yet
			Operation op = new Operation(Operation.action_type.read,0,intX,0);
			InsertIntoWaitingList(op,t_id);
			
		}*/
	}
	
	/**
	 * to determine whether a site was down before
	 * will lost all the lock info if so
	 */
	private boolean TargetSiteWasDown(ArrayList<Operation>ops)
	{
		boolean blnFlag = false;
		//get all site down times
		for(int i = 0; i<ops.size();i++)
		{
			Operation op = ops.get(i);
			int SiteIndex = (op.getTarget()%10)+1;
			//need to check all sites for even index variables
			
			if(op.getTarget()%2==0)
			{   
				for(int j = 1;j<=10;j++)
				{
					int downTime = (Integer)SiteRecords.get(j);
					
					if(op.getTimeStamp()<downTime)
					{
						//the site has down before
						return true;
					}
					
				}
			}
			else
			{
				int downTime = (Integer)SiteRecords.get(SiteIndex);
				if(op.getTimeStamp()<downTime)
				{
					//the site has down before
					return true;
				}
			}
			/*
			int downTime = (Integer)SiteRecords.get(SiteIndex);
			if(op.getTimeStamp()<downTime)
			{
				//the site has down before
				return true;
			}
			*/
		}
		
		return blnFlag;
	}
	
	/**
	 * this method is to make sure that is at least a site up and running to perform commit operation
	 */
	private boolean CanCommit(String info)
	{
		
		try
		{
			
			boolean blnSiteUp = true;//target site is up and running
			//extract transaction id
			int index = info.indexOf("(");
			String id = info.substring(index+1, info.length()-1);
			
			WriteOutput("Test commit for transaction " + id);
			
			if(transactions.containsKey(id))
			{
				//commit each operation under this transaction
				Transaction t = (Transaction)transactions.get(id); 
				ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
				
				//no operation to be checked
				if(ops.size()==0)
				{
					blnSiteUp = true;
				}
				
				
				//abort if the target site just recovered
				if(TargetSiteWasDown(ops))
				{
					WriteOutput("target site was down before, lost local lock info");
					Abort("abort(" + id + ")");
					return false;
				}
				
				//check target site is down
				for(int i = 0 ;i < ops.size(); i++)
				{
					Operation op = ops.get(i);
					if((op.getOperationType() == Operation.action_type.write)||
							(op.getOperationType() == Operation.action_type.read))
					{
						//get site id where this x belongs to
						//calculate the site id
						int answer = (op.getTarget() % 10) + 1;
						
						//abort if target site is down
						//or down previously
						Site s = (Site)sites.get(answer);
						if(s.isDown())
						{
							WriteOutput("target site is down");
							Abort("abort(" + id + ")");
							return false;
						}
					}
					
					
					
				}//for loop
						
					//last check any pending operation under this transaction id
					if(WaitingList.containsKey(id))
					{
						WriteOutput("There is still operation pending for this transaction " + id);
						Abort("abort(" + id + ")");
						return false;
						/*
						WriteOutput("Cannot commit yet, pending operation found under this transaction id");
						blnSiteUp = false;
						//insert this operation into waitinglist also
						Operation op  = new Operation(Operation.action_type.commit,0,0,0);
						InsertIntoWaitingList(op,id);
						*/
						
					}
				
			}
			else
			{
				WriteOutput("Transaction " + id + " , not found.");
				blnSiteUp = false;
			}
			
			
			return blnSiteUp;
			
		}
		catch(Exception e)
		{
			System.out.println("Error in CanCommit-"+ e.getMessage());
			return false;
		}
		
	}//end method

	/**
	 * this method is used to try to process operations in waiting list
	 */
	private void ProcessWaitingList()
	{
		ArrayList<String> trans = new ArrayList<String>();
		Transaction t;
		int index =0;
		//process older transaction 1st
		//move all the transaction id into array list
		Iterator<String> it =WaitingList.keySet().iterator();
		while(it.hasNext())
		{
			trans.add((String)it.next());
		}
		
		//get the oldest transaction 1st
		//do for all transactions
		while(trans.size()>0)
		{
			if(trans.size()==1)
			{
				index = 0;
				t = (Transaction)transactions.get(trans.get(index));
				
			}
			else
			{
				index= getNext(trans);
				t = (Transaction)transactions.get(trans.get(index));
			}
			
			//process the transaction
			processWaitingOperations(t.getID());
			
			//remove it from the list after checked
			trans.remove(index);
			
		}
	}

	/**
	 * process all the pending operation under this transaction
	 */
	private void processWaitingOperations(String t_id)
	{
		ArrayList<Operation> ops = (ArrayList<Operation>)WaitingList.get(t_id);
		ArrayList<Operation> tempOps = new ArrayList<Operation>();
		for(int i = 0;i<ops.size();i++)
		{
			tempOps.add(ops.get(i));
		}
		
		//need to copy it to another temporary list else the size will grow
		//each time when it fails to process again and adds to waiting list = infinite loop
		//java is using actual reference over here
		for(int i=0;i<tempOps.size();i++)
		{
			//remove the 1st element in the list before processing
			//it the operation fails it will add it back to the waitinglist(last element)
			ops.remove(0);
			//if there isn't anymore pending operation
			//remove this transaction id from the map
			if(ops.size()==0)
			{
				WaitingList.remove(t_id);
			}
			
			Operation op = tempOps.get(i);
			if(op.getOperationType()==Operation.action_type.commit)
			{
				//commit
				Do("end(" + t_id + ")");
			}
			else if(op.getOperationType()==Operation.action_type.write)
			{
				//write operation
				Do("w(" + t_id + ",x" + op.getTarget()+"," +op.getValue()+ ")");
			}
			else
			{
				//read
				Do("r(" + t_id + ",x" + op.getTarget() +")");
			}
		}
	}

	/**
	 * this method return the oldest transaction index in array list 
	 */
	private int getNext(ArrayList<String> trans)
	{
		int index = 0;
		Transaction t_keep =null;
		Transaction t_temp=null;
		String t_id="";
		for(int i = 0;i<trans.size();i++)
		{
			if(i==0)
			{
				//get the 1st transaction
				t_id = trans.get(0);
				t_keep = (Transaction)transactions.get(t_id);
			}
			else
			{
				t_id = trans.get(i);
				t_temp = (Transaction)transactions.get(t_id);
				//compare both time stamp
				if(t_keep.getTimeStamp()>t_temp.getTimeStamp())
				{
					//get the transaction location in array list
					index = i;
					//swap if the holding transaction is younger with current
					t_keep = t_temp;
				}
			}
				
		}
		return index;
	}
	
}
