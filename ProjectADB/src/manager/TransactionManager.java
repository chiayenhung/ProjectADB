package manager;

import java.util.*;
import java.io.*;

import type.OperationType;
import type.TransactionType;

//import type.CommandType;

import component.Operation;
import component.Site;
import component.Transaction;
//import component.Operation.action_type;
//import component.Transaction.Attribute;


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
//	private String outFile = "";
//	private CommandType current_state = CommandType.unkown;
	private int intCurrentTimeStamp =0;
	private Map<String, ArrayList<Operation>> WaitingList;//a map structure to hold all kind of waiting operation from all transactions
	private Map<Integer, Integer> SiteRecords;//a map structure to hold all the down/recovery record
	private List<List<String>> commandList = null;		//List of command from file
	private Logger logger;
	/**
	 * Transaction Manager object class constructor
	 * @param output file path and name 
	 */
	public TransactionManager(String filename)
	{
		//instantiate lists
		this.sites = new HashMap<Integer, Site>();
		this.transactions = new HashMap<String, Transaction>();
		this.WaitingList = new HashMap<String, ArrayList<Operation>>();
		this.logger = new Logger(filename);
		this.SiteRecords = new HashMap<Integer, Integer>();
		for(int i = 1 ; i < 11; i++){
			this.sites.put(i, new Site(i));
			this.SiteRecords.put(i, intCurrentTimeStamp);
			this.logger.log("Added site " + i);
		}
		
	}
	
	/**
	 * @param list 
	 * @Description set the command list from file
	 */
	public void setCommandList(List<List<String>> list){
		this.commandList = list;
		this.run();
	}
	
	/**
	 * run TransactionManager 
	 */
	private void run(){
		CommandParser cp;
		for(List<String> stringList: this.commandList){
			for(String s: stringList){
				cp = new CommandParser(s);
				this.execute(cp);
			}
//			intCurrentTimeStamp++;
		}
	}
	
	public void execute(CommandParser cp){
		switch (cp.getCommandType()){
		case begin:
			this.begin(cp.getTransactionNum());
			break;
		case beginReadOnly:
			this.beginOnly(cp.getTransactionNum());
			break;
		case fail:
			this.fail(cp.getSiteNum());
			break;
		case end:
			this.end(cp.getTransactionNum());
			break;
		case recover:
			this.recover(cp.getSiteNum());
			break;
		case read:
			this.read(cp.getTransactionNum(), cp.getXClassNum());
			break;
		case write:
			this.write(cp.getTransactionNum(), cp.getXClassNum(), cp.getValue());
			break;
		case dump:
			this.dump(cp.getSiteNum(), cp.getXClassNum());
			break;
		default : 
			break;
				
		}
//		current_state = cp.getCommandType();
		//increase the timestamp value
		intCurrentTimeStamp++;
	}

	/**
	 * @param xClassNum 
	 * @param siteNum 
	 * 
	 */
	private void dump(int siteNum, int xClassNum) {
		this.logger.log("Processing dump()");
		try
		{
			//to determine whether is a site/a variable /or all
			if(siteNum == -1 && xClassNum == -1)
			{
				//commit everything
				CommitALL();
			}
			else if(siteNum == -1)
			{
				//commit only a particular variable
				//check for if target site is down
				//get variable id
				//extract variable id
				
//				int intID = xClassNum;
				
				int intID = (xClassNum % 10) + 1;
				
				Site s = (Site)sites.get(intID);
				if(!s.isDown())
				{
					CommitVariable(xClassNum);
					this.logger.log("Site" + xClassNum + " " +s.ToString());
				}
				else
				{
					this.logger.log("Site" + xClassNum + " is down");
				}
			}
			else if(xClassNum == -1)
			{
				//commit a site
				//get variable id
				//extract variable id
				
//				int intID = siteNum;
				Site s = this.sites.get(siteNum);
				if(!s.isDown())
				{
					CommitSite(siteNum);
					this.logger.log("Site " + siteNum + " " +s.ToString());
				}
				else
				{
					this.logger.log("Site " + siteNum + " is down");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in Dump-"+ e.getMessage());
		}
		
	}

	/**
	 * @param transactionNum
	 * @param xClassNum
	 * @param value
	 */
	private void write(int transactionNum, int xClassNum, int value) {
		try
		{
			this.logger.log("Processing write(T" + transactionNum + ", X" + xClassNum + ", " + value + ")");
			boolean blnInsertOp = false; //flag to insert the current operation into the transaction object
			
			Transaction t  = null;
			String t_id = "" + transactionNum;//transaction id

			//check for valid transaction 
			if(transactions.containsKey(t_id))
			{
				//check transaction attribute
				//doesn't allow to write if is read-only
				t = (Transaction)transactions.get(t_id);
				if(t.getAttribute() == TransactionType.ReadOnly)
				{
					this.logger.log("Transaction " + t_id + " doesn't allow to write.");
					return;
				}
			}
			else
			{
				this.logger.log("Transaction " + t_id + " not found.");
				return;
			}
			
			ArrayList<Site> evenSite = new ArrayList<Site>();
			
			if( (xClassNum%2) == 1)
			{
				int answer = (xClassNum % 10) +1;
				Site s = (Site)sites.get(answer);
				if(!s.isDown())
				{
					blnInsertOp=WriteToSingle(xClassNum,value,t_id,answer);
				}
				else
				{
					//insert the stuck operation into the waiting list
					//time stamp = 0 because hasn't written to site yet
					Operation op = new Operation(OperationType.write,value,xClassNum,0);
					InsertIntoWaitingList(op,t_id);
				}
			}
			else
			{
				int temp = (xClassNum / 2) * 3;
				int answer = 0;
				for (int i = (temp-2); i <= temp; i++){
					if((i % 10) == 0)
					{
						answer = 10;
					}
					else
					{
						int answerSite = (i % 10);
						answer = answerSite;
					}
					Site s1 = this.sites.get(answer);
					evenSite.add(s1);		
				}
				
				Iterator<Site> iteratorSite = evenSite.iterator();
				
				boolean writeFlag = false;
				while(iteratorSite.hasNext()){
					Site s = (Site)iteratorSite.next();
			        if(!s.isDown())
			        {
			        	//ReadSingle(s.getID(),intX,t_id);
			        	blnInsertOp=WriteToSingle(xClassNum,value,t_id,s.getID());
			        	//break;
			        	writeFlag = true;
			        }
			        if( (!iteratorSite.hasNext()) && (writeFlag == false) )
			        {
			        	//insert the stuck operation into the waiting list
						//time stamp = 0 because hasn't written to site yet
						Operation op = new Operation(OperationType.write,value,xClassNum,0);
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
				Operation op = new Operation(OperationType.write, value,xClassNum,intCurrentTimeStamp);
				t.Insert_Operation(op);
				this.logger.log("Operation inserted into " + t_id);
			}
			
		}
		catch(Exception e)
		{
			this.logger.log("Error in Write-"+ e.getMessage());
			
		}
		
	}

	/**
	 * @param transactionNum
	 * @param xClassNum
	 */
	private void read(int transactionNum, int xClassNum) {
		this.logger.log("Processing read(T" + transactionNum + ", X" + xClassNum + ")");
		try
		{

			String t_id = "" + transactionNum;
			
			//check for valid transaction id
			if(!transactions.containsKey(t_id))
			{
				this.logger.log("Transaction " + t_id + " not found.");
				return;
			}
			//get transaction object
			
			if(!transactions.containsKey(t_id))
			{
				this.logger.log("Transaction " + t_id + " not found.");
				return;
			}
			Transaction t = (Transaction)transactions.get(t_id);
			
			if(t.getAttribute()==TransactionType.ReadOnly)
			{
				//read from multiversion
				ReadOnly(t_id,xClassNum);
			}
			else
			{
				//set read lock
				ReadWithLock(xClassNum,t_id);
			}
			
		
		}
		catch(Exception e)
		{
			System.out.println("Error in Read-"+ e.getMessage());
		}
		
	}

	/**
	 * @param siteNum
	 */
	private void recover(int siteNum) {
		try
		{
			Site s_backup = null;
			//boolean blnOnce = false;//to know whether there is a source site up and running to recover
			
			this.logger.log("Processing recover(" + siteNum + ")");
			
			//check for valid site id
			if(!sites.containsKey(siteNum))
			{
				this.logger.log("Site " + siteNum + " not found.");
				return;
			}
			
			//call a site to recovery
			//get failed site
			Site s_down = this.sites.get(siteNum);
			//look for backup site
			if(siteNum==1)
			{
				//available at all 
				//but this site is a backup for another odd index site
				//so need to do back up from that site
				//one back up only
				s_backup = this.sites.get(10);
				
			}
			else
			{
				//one back up only
				s_backup = this.sites.get(siteNum-1);
				
				
			}
			//check whether backup is down also
			
			if(s_backup.isDown())
			{
				this.logger.log("Operation failed because backup site is down currently");
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
	 * @param transactionNum
	 */
	private void end(int transactionNum) {
		//extract transaction id ;譬如 end(T1)，要把T1拿出來
		//commit and end a particular transaction
		//test whether this Transaction can commit or not
		if(CanCommit(transactionNum))
		{
			
			Commit(transactionNum);
		}
		else
		{
			
		}
	}

	/**
	 * 
	 */
	private void fail(int siteNum) {
		try
		{
			this.logger.log("Processing fail(" + siteNum + ")");
			Site s_backup = null;
			//check for valid site id
			if(!sites.containsKey(siteNum))
			{
				this.logger.log("Site " + siteNum + " not found.");
				return;
			}
			
			//call backup before fail
			if(siteNum==1)
			{
				s_backup = this.sites.get(10);
			}
			else
			{
				s_backup = this.sites.get(siteNum-1);
			}
			//call a site to fail
			Site s = this.sites.get(siteNum);
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
	 * 
	 */
	private void beginOnly(int transactionNum) {
		this.CreateNewTransaction(transactionNum, intCurrentTimeStamp, TransactionType.ReadOnly);
		
	}

	/**
	 * 
	 */
	private void begin(int transactionNum) {
		this.CreateNewTransaction(transactionNum,intCurrentTimeStamp,TransactionType.ReadWrite);		
	}


	 
	/**
	 * WriteOutput method can write the output result to a file 
	 */
//	private void WriteOutput(String info)
//	{
//		boolean blnExist=false;
//		try 
//		{
//	        //check for existing file
//			File f = new File(outFile);
//			blnExist = f.exists();
//			if(!blnExist)
//			{
//				//create a new file if doesn't exist
//				blnExist = f.createNewFile();
//				f=null;
//			}
//			if(blnExist)
//			{
//				BufferedWriter out = new BufferedWriter(new FileWriter(outFile,true));
//				Date date = new Date();
//				out.write(date.toString() + ": " + info);
//				out.newLine();
//				out.flush();
//				out.close();
//			}
//			else
//			{
//				System.out.println("Failed to create output file");
//			}
//	    } 
//		catch (IOException e) 
//	    {
//			System.out.println("Error in WriteOutput-"+ e.getMessage());
//	    }
//
//
//	}
	

	/**
	 * 
	 */
//	private boolean WriteToAll(int intX,int intValue,String t_id)
//	{
//		//success if we make it to One site and not all sites down
//		boolean blnWriteOnce= false;
//		
//		ArrayList<Site> kk = new ArrayList<Site>();
//		int xEvenTargetSite = 0;
//		if(intX%2 == 0){
//			int temp = (intX/2)*3;
//			for (int i = (temp-2); i <= temp; i++){
//				if((i%10) == 0)
//				{
//					xEvenTargetSite = 10;
//					
//				}
//				else
//				{
//					int answerSite = (i%10);
//					xEvenTargetSite = answerSite;
//				}
//				Site s1 = sites.get(xEvenTargetSite);
//				kk.add(s1);
//			}
//		}
//		
//		Iterator<Site> iteratorSite = kk.iterator();
//		
//		//get iterator
//		Iterator<Site> k = sites.values().iterator();
//		
//		while(iteratorSite.hasNext())
//		{
//			Site _site = (Site)iteratorSite.next();
//			
//			//only if is not down
//			if(!_site.isDown())
//			{
//				_site.WriteData(intX, intValue, t_id);
//				
//				//check lock message
//				if(_site.getLockMsg().compareToIgnoreCase(t_id)==0)
//				{
//					//same id
//					//write again
//					_site.WriteData(intX, intValue, t_id);
//					blnWriteOnce = true;//set flag, indicating at least write once
//				}
//				else if(_site.getLockMsg() != "NULL")
//				{
//					
//					WriteOutput("Write X" + intX
//							//+ " from site" + _site.getID() 
//							+ " failed, because it is locked by transaction " 
//							+ _site.getLockMsg());				
//					//make decision on which transaction to be aborted
//					if(MakeDecision(_site.getLockMsg(),t_id))
//					{
//						//request-trasanction still alive	
//						//insert the stuck operation into the waiting list
//						//time stamp = 0 because hasn't written to site yet
//						Operation op = new Operation(Operation.action_type.write,intValue,intX,0);
//						InsertIntoWaitingList(op,t_id);
//					}
//					else
//					{
//						//current transaction aborted
//					}
//					//reset lock message
//					_site.resetLockMsg();
//					
//					//break the loop
//					break;
//				}
//				else
//				{
//					blnWriteOnce = true;//set flag, indicating at least write once
//				}
//			}//end if site is down
//		}
//
//		return blnWriteOnce;
//	}
	
	/**
	 * write the value to variable that is on target site and back up site(id+1)
	 */
	private boolean WriteToSingle(int intX,int intValue,String t_id,int targetSite)
	{
		//success if we make it to One site or backup
		boolean blnWriteOnce= false;
		//target + backup
		Site s_target = this.sites.get(targetSite);
		int backup=0;
		if(targetSite==1)			
		{
			backup = 10;
		}
		else						
		{
			backup = targetSite-1;
		}
		
		Site s_backup = this.sites.get(backup);
		
		s_target.WriteData(intX, intValue, t_id);
		
		//self locked
		if(s_target.getLockMsg().compareToIgnoreCase(t_id)==0)
		{
			s_target.WriteData(intX, intValue, t_id);
			blnWriteOnce  = true;
		}
		else if(s_target.getLockMsg() != "NULL")
		{
			this.logger.log("Write X" + intX
					//+ " from site" + s_target.getID() 
					+ " failed, because it is locked by transaction " 
					+ s_target.getLockMsg());
			
			//abort my self?
			if(MakeDecision(s_target.getLockMsg(),t_id))
			{
				
				//save this operation in waiting list
				//insert the stuck operation into the waiting list
				//time stamp = 0 because hasn't written to site yet
				Operation op = new Operation(OperationType.write,intValue,intX,0);
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
	private void Commit(int transactionNum)
	{
		try
		{
			this.logger.log("Processing Commit(" + transactionNum + ")");
			
			//extract transaction id
			String id = "" + transactionNum;
			if(transactions.containsKey(id))
			{
				//commit each operation under this transaction
				Transaction t = this.transactions.get(id); 
				ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
				
				for(int i = 0 ;i < ops.size(); i++)
				{
					Operation op = ops.get(i);
					if(op.getOperationType() == OperationType.write)
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
							Site _site = this.sites.get(answer);
							_site.Dump(op.getTarget());
							//_site = (Site)sites.get(answer+1);
							//_site.Dump(op.getTarget());
							
						}
					}
				}
				this.logger.log("Transaction " + id + " commited.");
				EndTransaction("end("+ id + ")");
			}
			else
			{
				this.logger.log("Cannot commit transaction " + id + " , not found.");
			}
			
			
		}
		catch(Exception e)
		{
			this.logger.log("Error in Commit-"+ e.getMessage());
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
				this.logger.log("Transaction " + id + " removed from the list.");
				
				//pending operation will be removed as well
				//impossible to have pending operation if the caller is commit()
				//and is ok to remove if the caller is abort()
				WaitingList.remove(id);
				//process waiting list
				ProcessWaitingList();
			}
			else
			{
				this.logger.log("Cannot end transaction " + id + " , not found.");
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
	private void CreateNewTransaction(int _id, int timestamp, TransactionType attri)
	{
		
		try
		{
			//extract transaction id
			String id = "" + _id;
			
			//check for existing transaction ID before create new one
			if(transactions.containsKey(id))
			{
				String msg = "Transaction " + id + " already existed in the collection.";
				this.logger.log(msg);
				//System.out.println(msg);
			}
			else
			{
				//create a new transaction object
				Transaction t = new Transaction(id, timestamp, attri);
				transactions.put(id, t);//insert into map
				this.logger.log("Transaction " + id + " has been created.");
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
//	private  void Abort(String info)
	private  void abort(String transactionId)
	{
		try
		{
			this.logger.log("Processing abort(" + transactionId + ")");
			//extract transaction id
//			int index = info.indexOf("(");
//			String id = info.substring(index+1, info.length()-1);
//			String id = "" + transactionNum;
			//abort the operation if the transaction not found
			if(!transactions.containsKey(transactionId))
			{
				this.logger.log("Transaction " + transactionId + " not found.");
				return;
			}
			
			Transaction t = this.transactions.get(transactionId);
			//get a list of operation belongs to this transaction id
			ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
			//loop through every operation
			for(int i = 0 ; i<ops.size();i++)
			{
				Operation op = ops.get(i);
				
				
				if((op.getOperationType() == OperationType.write) ||
						(op.getOperationType() == OperationType.read))
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
							
							s.AbortT(transactionId);
							//s.Abort(op.getTarget());
						}
						
						//can stop, because aborted from all sites
						//exit for loop
						break;
					}
					else
					{
						//x is at one particular site + backup site
						Site s = this.sites.get(answer);
						//s.Abort(op.getTarget());
						s.AbortT(transactionId);
					}
				}
			}
			
			//end the transaction
			EndTransaction("end("+ transactionId +")");
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
			Transaction t_locked = this.transactions.get(LockByID);
			Transaction t_request = this.transactions.get(RequestID);
			if(t_locked.getTimeStamp()<=t_request.getTimeStamp())
			{
				//t_request is younger or equal than the t_locked
				//keep t_locked 
				//abort request id
				this.abort(RequestID);
				
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
			this.logger.log("Cannot make decision because invalid transaction's ID");
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
			ops = this.WaitingList.get(t_id);
			ops.add(op);
		}
		else
		{
			//create one
			ops = new ArrayList<Operation>();
			ops.add(op);
			WaitingList.put(t_id, ops);
		}
		this.logger.log("Operation inserted into waiting list.");
	}

	/**
	 * this method can be used to commit a particular variable at all sites
	 */
	private void CommitVariable(int xClassNum)
	{
		try
		{
			//get variable id
			//extract variable id
//			int index = info.indexOf("(x");
//			String id = info.substring(index+2, info.length()-1);
			
			int intX = xClassNum;
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
					this.logger.log("Site " + answer + " not found.");
					return;
				}
				Site s = this.sites.get(answer);
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
	private void CommitSite(int siteNum)
	{
		try
		{
			//get site id
			//extract site id
//			int index = info.indexOf("(");
//			String id = info.substring(index+1, info.length()-1);
//			int intID = Integer.parseInt(id);
			//check if the site id valid
			if(!sites.containsKey(siteNum))
			{
				this.logger.log("Site " + siteNum +" not found.");
				return;
			}
			
			//every site has even index Xs
			//such as x:2,4,6,8,10,12,14,16,18,20
			for(int i = 1;i<=5;i++)
			{
				int evenIndex = i*2;
				
				CommitVariable(evenIndex);
				
			}
		
			//find that particular odd indexed x in this site
			//odd index = (site-1)
			//if odd index = 0 then is at site 10
			int oddVarIndex = siteNum -1;
			if(oddVarIndex < 1)
			{
				oddVarIndex = 10;
			}
			
			if(oddVarIndex % 2 > 0)
			{
				CommitVariable(oddVarIndex);
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
			this.logger.log("Site" + s.getID() + " " + s.ToString());
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
		if(intX % 2==0)
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
				this.logger.log("x" + intX + " is " + value);
			}
			else
			{
				//abort this transaction since none of the site is up
				this.logger.log("Cannot read x" + intX + " all sites are down");
				this.abort(t_id);
			}
		
			
		}
		else
		{
			//at one particular site
			if(!sites.containsKey(answer))
			{
				this.logger.log("Site " + answer + " doesn't contained variable x" + intX + ".");
				//impossible will be at backup site also
				return;
			}
			//target site
			s = this.sites.get(answer);
			if(!s.isDown())
			{
				value = s.ReadOnly(intX, t_id);
				this.logger.log("x" + intX + " is " + value);
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
				s = this.sites.get(backup);
				if(!s.isDown())
				{
					value = s.ReadOnly(intX,t_id);
					this.logger.log("x" + intX + " is " + value);
				}
				else
				{
					//abort this transaction since none of the site is up
					this.logger.log("Cannot read x" + intX + " all sites are down");
					this.abort(t_id);
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
			this.logger.log("Site " + TargetSite + " doesn't contained variable x" + intX + ".");
			//impossible will be at backup site, so won't check
			//abort the transaction
			this.abort(t_id);
			return;
		}
		//target site
		Site s = this.sites.get(TargetSite);
		this.logger.log("target site " + TargetSite);
		//if(!s.isDown())
		//{
			value = s.ReadData(intX, t_id);
			
			//check lock
			if(s.getLockMsg()=="NULL")
			{
				blnSuccess = true;
				this.logger.log("x" + intX + " is " + value);
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
				Transaction t = this.transactions.get(t_id);
				Operation op = new Operation(OperationType.read,0,intX,intCurrentTimeStamp);
				t.Insert_Operation(op);
				this.logger.log("Operation input into " + t_id);
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
					this.logger.log("x" + intX + " is lock by " + s.getLockMsg());
					
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
			Operation op = new Operation(OperationType.read,0,intX,0);
	    	InsertIntoWaitingList(op,t_id);
		}
	}
	/**
	 * this method can read a variable which is at all sites
	 */
//	private void ReadAll(int TargetSite,int intX,String t_id)
//	{
//		int value = 0;
//		boolean blnSuccess = false;
//		Site s = null;
//		//at one particular site
//		if(!sites.containsKey(TargetSite))
//		{
//			WriteOutput("Site " + TargetSite + " doesn't contained variable x" + intX + ".");
//			//impossible will be at backup site, so won't check
//			//abort the transaction
//			this.abort(t_id);
//			return;
//		}		
//		//check target site is up and running
//		//s = (Site)sites.get(TargetSite);
//		//if(!s.isDown())
//		//{
//		//read the value, and set lock at all sites
//		
//		ArrayList<Site> kk = new ArrayList<Site>();
//		
//		//int xOddTargetSite = (intX % 10) +1;
//		int xEvenTargetSite = 0;
//		if(intX%2 == 0){
//			int temp = (intX/2)*3;
//			for (int i = (temp-2); i <= temp; i++){
//				if((i%10) == 0)
//				{
//					xEvenTargetSite = 10;
//					
//				}
//				else
//				{
//					int answerSite = (i%10);
//					xEvenTargetSite = answerSite;
//				}
//				Site s1 = sites.get(xEvenTargetSite);
//				kk.add(s1);		
//			}			
//		}			
//		//Iterator<Site> k = sites.values().iterator();
//		
//		Iterator<Site> iteratorSite = kk.iterator();
//		
//		while(iteratorSite.hasNext())
//		{
//			s = (Site)iteratorSite.next();
//			if(!s.isDown())
//	    	{
//	    		//read + set lock
//		    	value = s.ReadData(intX, t_id);
//		    	if(s.getLockMsg().compareToIgnoreCase(t_id)==0)
//		    	{
//		    		//self locked
//		    		blnSuccess = true;
//		    		value = s.ReadData(intX, t_id);
//		    		this.WriteOutput("the target site is site"+ s.getID());
//		    		//return;
//		    	}
//		    	else if(s.getLockMsg()!="NULL")
//		    	{
//		    		WriteOutput("x" + intX + " is locked by " + s.getLockMsg());
//		    		
//		    		
//		    		//to abort current transaction?
//		    		if(MakeDecision(s.getLockMsg(),t_id))
//		    		{
//		    			//reset the lock msg
//			    		s.resetLockMsg();
//		    			//current transaction still alive
//		    			//write into waiting list at the end of method
//		    			break;
//		    		
//		    		}
//		    		else
//		    		{
//		    			//current transaction aborted
//		    			//reset the lock msg
//			    		s.resetLockMsg();
//		    			return;
//		    		}
//		    		
//		    		
//		    	}
//		    	else
//		    	{
//		    		//no lock on it
//		    		blnSuccess = true;
//		    	}
//	    	}//if site is up only
//			
//		}
//		if(!blnSuccess)
//		{
//			//save the operation to waiting list
//			//time stamp = 0, because hasn't written to site yet
//			Operation op = new Operation(Operation.action_type.read,0,intX,0);
//	    	InsertIntoWaitingList(op,t_id);
//		}
//		else
//		{
//			WriteOutput("x" + intX + " is " + value);
//			//write to transaction record
//			//increase time stamp so that won't conflict with regular transaction
//			//maybe this operation is invoke from waiting list
//			intCurrentTimeStamp++;
//			Transaction t = (Transaction)transactions.get(t_id);
//			Operation op = new Operation(Operation.action_type.read,0,intX,intCurrentTimeStamp);
//			t.Insert_Operation(op);
//			WriteOutput("Operation input into " + t_id);
//		}
//	}
	//read will set a read-lock
	private void ReadWithLock(int intX,String t_id)
	{
		
		//get target site
		int answer = 0;
		ArrayList<Site> evenSite = new ArrayList<Site>();
		
		if( (intX%2) == 1)
		{
			answer = (intX % 10) +1;
			Site s = this.sites.get(answer);
			if(!s.isDown())
			{
				ReadSingle(answer,intX,t_id);
			}
			else
			{
				//insert the current operation into the waiting list
				//insert the stuck operation into the waiting list
				//time stamp = 0, because hasn't written into site yet
				Operation op = new Operation(OperationType.read,0,intX,0);
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
				Site s1 = this.sites.get(answer);
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
					Operation op = new Operation(OperationType.read,0,intX,0);
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
					int downTime = this.SiteRecords.get(j);
					
					if(op.getTimeStamp()<downTime)
					{
						//the site has down before
						return true;
					}
					
				}
			}
			else
			{
				int downTime = this.SiteRecords.get(SiteIndex);
				if(op.getTimeStamp() < downTime)
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
	private boolean CanCommit(int transactionNum)
	{
		
		try
		{
			
			boolean blnSiteUp = true;//target site is up and running
			//extract transaction id

			String id = "" + transactionNum;
			
			this.logger.log("Test commit for transaction " + id);
			
			if(transactions.containsKey(id))
			{
				//commit each operation under this transaction
				Transaction t = this.transactions.get(id); 
				ArrayList<Operation> ops = (ArrayList<Operation>)t.getOperations();
				
				//no operation to be checked
				if(ops.size()==0)
				{
					blnSiteUp = true;
				}
				
				
				//abort if the target site just recovered
				if(TargetSiteWasDown(ops))
				{
					this.logger.log("target site was down before, lost local lock info");
					this.abort(id);
					return false;
				}
				
				//check target site is down
				for(int i = 0 ;i < ops.size(); i++)
				{
					Operation op = ops.get(i);
					if((op.getOperationType() == OperationType.write)||
							(op.getOperationType() == OperationType.read))
					{
						//get site id where this x belongs to
						//calculate the site id
						int answer = (op.getTarget() % 10) + 1;
						
						//abort if target site is down
						//or down previously
						Site s = (Site)sites.get(answer);
						if(s.isDown())
						{
							this.logger.log("target site is down");
							this.abort(id);
							return false;
						}
					}
					
					
					
				}//for loop
						
					//last check any pending operation under this transaction id
					if(WaitingList.containsKey(id))
					{
						this.logger.log("There is still operation pending for this transaction " + id);
						this.abort(id);
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
				this.logger.log("Transaction " + id + " , not found.");
				blnSiteUp = false;
			}
			
			
			return blnSiteUp;
			
		}
		catch(Exception e)
		{
			System.err.println("Error in CanCommit-"+ e.getMessage());
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
				t = this.transactions.get(trans.get(index));
				
			}
			else
			{
				index= getNext(trans);
				t = this.transactions.get(trans.get(index));
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
		List<Operation> ops = (ArrayList<Operation>)WaitingList.get(t_id);
		List<Operation> tempOps = new ArrayList<Operation>();
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
			if(op.getOperationType()==OperationType.commit)
			{
				//commit
//				Do("end(" + t_id + ")");
				this.end(Integer.parseInt(t_id));
//				this.execute(cp)
			}
			else if(op.getOperationType()==OperationType.write)
			{
				//write operation
//				Do("w(" + t_id + ",x" + op.getTarget()+"," +op.getValue()+ ")");
				this.write(Integer.parseInt(t_id), op.getTarget(), op.getValue());
			}
			else
			{
				//read
//				Do("r(" + t_id + ",x" + op.getTarget() +")");
				this.read(Integer.parseInt(t_id), op.getTarget());
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
				t_keep = this.transactions.get(t_id);
			}
			else
			{
				t_id = trans.get(i);
				t_temp = this.transactions.get(t_id);
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
