package adb;

import java.io.*;
import java.util.*;
/**
 * Main Class
 * @author Chia-Ming Lin, Li-Yen Hung
 * 
 */
public class main {
	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) 
	{	
		Scanner in  = new Scanner(System.in);
	
		try
		{		
			System.out.println("Please key in full output path and filename:");
			String outputFile = in.next(); 
			//create transaction manager object
			TransactionManager TM = new TransactionManager(outputFile);
			for(int i = 1;i<11;i++)
			{
				Site s = new Site(i);
				TM.addSite(s);
			}
			
			 while(true)
			 {
				String line;
				
				System.out.println("Read from test input file? (y/n)");
				line = in.next();
				if(line.compareToIgnoreCase("y")==0)
				{
					System.out.println("Please input the full source path and filename:");
					line = in.next();
					FromFile(line,TM);
					
				}
				else if(line.compareToIgnoreCase("n")==0)
				{
					System.out.println("Please input command or 'exit' to terminate the program:");
					line = in.next();
					//exit if the user type exit
					if(line.compareToIgnoreCase("exit")==0)
					{
						break;
					}
					TM.Do(line);
				}
				else
				{
					System.out.println("Error: Please type 'Y/y' or 'N/n'");
				}
			 }
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		
		
		System.out.println("done");
	}
	/**
	 * read from input file
	 * @param inFile
	 * @param TM
	 */
	private static void FromFile(String inFile,TransactionManager TM)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(inFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine())!=null)
			{
				TM.Do(strLine);
				System.out.println(strLine);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error-" + e.getMessage());
		}
	}
}
