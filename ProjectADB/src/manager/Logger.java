/**
 * 
 */
package manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * @author ching-yingyang
 *
 */
public class Logger {
	
	private String filename;
	
	public Logger(String filename) {
		Date date = new Date();
		this.filename = "result/" + date.getTime() + filename;
	}
	
	public void log(String info){
		boolean blnExist=false;
		try 
		{
	        //check for existing file
			File f = new File(this.filename);
			blnExist = f.exists();
			if(!blnExist)
			{
				//create a new file if doesn't exist
				blnExist = f.createNewFile();
				f=null;
			}
			if(blnExist)
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(this.filename,true));
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

}
