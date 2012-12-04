package component;

import java.util.*;

import type.TransactionType;
/**
 * 
 * Transaction object class
 * @author Chia-Yen Hung Ming-Chien Kao
 */
public class Transaction 
{
	//class variable
	private int timestamp = 0;
	private String ID = "";
	private ArrayList<Operation> op_list;
	private TransactionType attribute;
	/**
	 * class constructor
	 * @param _ID
	 * @param _timestamp
	 * @param att
	 */
	public Transaction(String _ID,int _timestamp,TransactionType att)
	{
		op_list =new ArrayList<Operation>();
		//op_list.add(_op);
		ID = _ID;//assign transaction id
		timestamp = _timestamp; //assign timestamp, line # in this case
		attribute = att;
	}
	/**
	 * get transaction attribute
	 * @return
	 */
	public TransactionType getAttribute()
	{
		return attribute;
	}
	/**
	 * get transaction id
	 * @return
	 */
	public String getID()
	{
		return ID;
	}
	/**
	 * get timestamp
	 * @return
	 */
	public int getTimeStamp()
	{
		return timestamp;
	}
	/**
	 * return a collection of operation
	 * @return
	 */
	public Collection<Operation> getOperations()
	{
		return op_list;
	}
	/**
	 * insert new operation
	 * @param op
	 */
	public void Insert_Operation(Operation op)
	{
		op_list.add(op);
	}
	/**
	 * remove item
	 * @param location
	 */
	public void Remove_Operation(int location)
	{
		if(location>op_list.size()) { return; }//exit if the size is larger than the list length
		op_list.remove(location);
	}
	/**
	 * overwrite the list
	 * @param new_list
	 */
	public void Replace(ArrayList<Operation> new_list)
	{
		op_list = new_list;
	}

}
