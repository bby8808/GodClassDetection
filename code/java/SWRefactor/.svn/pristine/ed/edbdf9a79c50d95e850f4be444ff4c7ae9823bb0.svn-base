package swr.actions.combine.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Csdsdsdsd {

	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		String path = "D:\\��־��\\����ʵ��\\word-2-vec\\java_projects";
//		traverseFolder(path);
//	}
	public static void traverseFolder(String path){
		File folder = new File(path);
		
		if(folder.exists())
            for(File file : folder.listFiles())
                if(file.isDirectory()){
                	traverseFolder(file.getAbsolutePath());
                } 
                else {
                	if(file.getName().toLowerCase().endsWith(".java")){
                		readFiles(file);		
                	}	
                }             	
    }
	
	public static void readFiles(File sourceFile){
		BufferedReader reader = null;
		BufferedWriter writer = null;
        try{
            reader = new BufferedReader(new FileReader(sourceFile));
            System.out.println(sourceFile.getName()+"---------------------------");
            File txtFile = new File("D:\\��־��\\����ʵ��\\word-2-vec\\corpora\\allcode.txt");
            if(!txtFile.exists())
            	txtFile.createNewFile();
            //System.out.println("Writing file: " + txtFile.getPath());
            writer = new BufferedWriter(new FileWriter(txtFile, true));
            
            //writer.write("<s>");
            writer.newLine();
            
            String line = reader.readLine();
            while(line != null){
                String[] splitString = line.split("((?<=\\.)|(?=\\.))| |((?<=\\{)|(?=\\{))|((?<=\\})|(?=\\}))|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))|((?<=\\[)|(?=\\[))|((?<=\\])|(?=\\]))|((?<=\\;)|(?=\\;))|((?<=\\,)|(?=\\,))|((?<=\\>)|(?=\\>))|((?<=\\<)|(?=\\<))");
                line = "";
                int number = 0;
				for(String token : splitString){
					number++;
					token = token.trim();
					if (token != null && !(token.equals(""))){
						token = token.replaceAll("\t", "");
						token = token.replaceAll(" ", "");
						token = token.replaceAll("/", " ");
						token = token.replaceAll("\n", " ");
						boolean isWord=token.matches("[a-zA-Z]+");
						boolean isCapitalWord = token.matches("[A-Z]+");
						if(isWord && !isCapitalWord){
							token = token.replaceAll("[A-Z]", " $0");
							token = token.toLowerCase();
							line += token + " ";
						}
					}
					
				}
				line = line.trim();
				if(line.length() > 0){
					writer.write(line);
					writer.newLine();
					writer.flush();
				}
                line = reader.readLine();
                System.out.println("111");
            }
            
            writer.flush();
            
        }catch(IOException e){  
            e.printStackTrace();  
        }finally{
        	if(reader != null)
        		try{
        			reader.close();
        		}catch(IOException e){
        			e.printStackTrace();
        		}
        	if(writer != null)
        		try{
        			writer.close();
        		}catch(IOException e){
        			e.printStackTrace();
        		}
        }
    }
}
