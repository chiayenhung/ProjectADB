/**
 * 
 */
package manager;

import type.CommandType;

/**
 * @author Chia-Yen Hung Ming-Chien Kao
 *
 */
public class CommandParser {
	private CommandType commandtype;
	private int transactionNum;
	private int siteNum;
	private int XclassNum;
	private int value;
	
	/**
	 * @param line
	 */
	public CommandParser(String line){
		this.run(line.toLowerCase());
	}
	
	/**
	 * @param line
	 */
	private void run(String line){
		if(line.startsWith("beginro")){
			this.commandtype = CommandType.beginReadOnly;
			String tmp = line.substring(line.indexOf("t") + 1, line.length() - 1);
			this.transactionNum = Integer.parseInt(tmp.trim());
			System.err.println(this.commandtype + " " + this.transactionNum);
			return;
		}
		if(line.startsWith("begin")){
			this.commandtype = CommandType.begin;
//			System.err.println(this.commandtype);
			String tmp = line.substring(line.indexOf("t") + 1, line.length() - 1);
//			System.err.println(tmp);
			this.transactionNum = Integer.parseInt(tmp.trim());
//			System.err.println(this.transactionNum);
			System.err.println(this.commandtype + " " + this.transactionNum);
			return;
		}
		if(line.startsWith("fail")){
			this.commandtype = CommandType.fail;
			String tmp = line.substring(line.indexOf("(") + 1, line.length() -1);
			this.siteNum = Integer.parseInt(tmp.trim());
			System.err.println(this.commandtype + " " + this.siteNum);
			return;
		}
		if(line.startsWith("recover")){
			this.commandtype = CommandType.recover;
			String tmp = line.substring(line.indexOf("(") + 1, line.length() - 1);
			this.siteNum = Integer.parseInt(tmp.trim());
			System.err.println(this.commandtype + " " + this.siteNum);
			return;
		}
		if(line.startsWith("end")){
			this.commandtype = CommandType.end;
			String tmp = line.substring(line.indexOf("t") + 1, line.length() - 1);
			this.transactionNum = Integer.parseInt(tmp.trim());
			System.err.println(this.commandtype + " " + this.transactionNum);
			return;
		}
		if(line.startsWith("r")){
			this.commandtype = CommandType.read;
			String tmp = line.substring(line.indexOf("(") + 1, line.length() - 1);
//			System.out.println(tmp);
			String[] tmps = tmp.split(",");
			if(tmps.length == 2){
				this.transactionNum = Integer.parseInt(tmps[0].substring(tmps[0].indexOf("t") + 1).trim());
				this.XclassNum = Integer.parseInt(tmps[1].substring(tmps[1].indexOf("x") + 1).trim());
				System.err.println(this.commandtype + " " + this.transactionNum + " " + this.XclassNum);
				return;
			}
			else
				return;
		}
		if(line.startsWith("w")){
			this.commandtype = CommandType.write;
			String tmp = line.substring(line.indexOf("(") + 1, line.length() -1);
			String[] tmps = tmp.split(",");
			if(tmps.length == 3){
				this.transactionNum = Integer.parseInt(tmps[0].substring(tmps[0].indexOf("t") + 1).trim());
				this.XclassNum = Integer.parseInt(tmps[1].substring(tmps[1].indexOf("x") + 1).trim());
				this.value = Integer.parseInt(tmps[2].trim());
				System.err.println(this.commandtype + " " + this.transactionNum + " " + this.XclassNum + " " + this.value);
				return;
			}
			else
				return;
		}
		if(line.startsWith("dump")){
			this.commandtype = CommandType.dump;
			this.siteNum = -1;
			this.XclassNum = -1;
			String tmp = line.substring(line.indexOf("(") + 1, line.length() - 1).trim();
			if(tmp.contains("x"))
				this.XclassNum = Integer.parseInt(line.substring(line.indexOf("x") + 1, line.length() - 1).trim());
			if(tmp.matches("\\d+"))
				this.siteNum = Integer.parseInt(tmp);
			System.err.println(this.commandtype);
			return;
		}
	}
	
	/**
	 * @return
	 */
	public CommandType getCommandType(){
		return this.commandtype;
	}
	
	/**
	 * @return
	 */
	public int getTransactionNum(){
		return this.transactionNum;
	}
	
	/**
	 * @return 
	 */
	public int getXClassNum(){
		return this.XclassNum;
	}
	
	/**
	 * @return site Number
	 */
	public int getSiteNum(){
		return this.siteNum;
	}
	
	/**
	 * @return value
	 */
	public int getValue(){
		return this.value;
	}
	
	

}
