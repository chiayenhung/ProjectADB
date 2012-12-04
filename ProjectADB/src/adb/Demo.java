package adb;

import java.io.*;
import java.util.*;

import manager.CommandParser;
import manager.InputParser;
import manager.TransactionManager;
/**
 * Main Class
 * @author Chia-Yen Hung Ming-Chien Kao
 * 
 */
public class Demo {
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

			TransactionManager tm = new TransactionManager(outputFile);
			 while(true)
			 {
				String line;
				
				System.out.println("Read from test input file? (y/n)");
				line = in.next();
				if(line.compareToIgnoreCase("y")==0)
				{
					System.out.println("Please input the full source path and filename:");
					line = in.next();
					InputParser ip = new InputParser(line);
					tm.setCommandList(ip.getCommandList());
					
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
					tm.run(line);
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
}
