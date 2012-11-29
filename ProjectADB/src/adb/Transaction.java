package adb;

import java.util.*;
/**
 * 
 * Transaction object class
 * @author Chia-Ming Lin, Li-Yen Hung
 */
public class Transaction 
{
	enum Attribute
	{
		ReadOnly,
		ReadWrite
	}
	//class variable
	private int timestamp = 0;
	private String ID = "";
	private ArrayList<Operation> op_q;
	private Transaction.Attribute attribute;
	/**
	 * class constructor
	 * @param _ID
	 * @param _timestamp
	 * @param att
	 */
	public Transaction(String _ID,int _timestamp,Transaction.Attribute att)
	{
		op_q =new ArrayList<Operation>();
		//op_q.add(_op);
		ID = _ID;//assign transaction id
		timestamp = _timestamp; //assign timestamp, line # in this case
		attribute = att;
	}
	/**
	 * get transaction attribute
	 * @return
	 */
	public Transaction.Attribute getAttribute()
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
		return op_q;
	}
	/**
	 * insert new operation
	 * @param op
	 */
	public void Insert_Operation(Operation op)
	{
		op_q.add(op);
	}
	/**
	 * remove item
	 * @param location
	 */
	public void Remove_Operation(int location)
	{
		if(location>op_q.size()){return;}//exit if the size is larger than the list length
		op_q.remove(location);
	}
	/**
	 * overwrite the list
	 * @param new_list
	 */
	public void Replace(ArrayList<Operation> new_list)
	{
		op_q = new_list;
	}

}
