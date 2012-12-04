/**
 * 
 */
package manager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chia-Yen Hung Ming-Chien Kao
 *
 */
public class InputParser {
	
	private List<List<String>> commands = new ArrayList<List<String>>();
	
	public InputParser(String filePath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		String line = "";
		int i = 0;
		while((line = br.readLine()) != null){
			if(!line.trim().startsWith("//")){
				this.commands.add(new ArrayList<String>());
				String[] strings = line.split(";");
				for(String s: strings)
					this.commands.get(i).add(s.trim());
				i++;
			}
		}
	}
	

	/**
	 * @return
	 */
	public List<List<String>> getCommandList() {
//		System.out.println(this.commands.size());
		return this.commands;
	}

	/**
	 * @param line
	 * @return
	 */
//	public String getCommand(String line) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
