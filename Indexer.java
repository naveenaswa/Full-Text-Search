

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class Indexer {	
	
	public static void printHash1(HashMap<String, String> token){
		
		for (String name: token.keySet()){
            String key =name.toString();
            String value = token.get(name).toString();  
            System.out.println(key + " " + value);  
		}
	}
	
	public static void printDocHash(HashMap<String, List<String>> token){
		
		for (String name: token.keySet()){
            String key =name.toString();
            String value = token.get(name).toString();  
            System.out.println(key + " " + value);  
		}
	}
	
	public static void writeInvertedIndex(Map<Integer, Multimap> map, String filename) throws IOException{
		
		//to append the inverted index file.
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("C:/Users/Naveen/Desktop/TestRuns/LastRun/"+filename, true)));
		for (Entry<Integer, Multimap> entry : map.entrySet())
	      {	  		
	  		writer.println(entry.getKey()+":"+entry.getValue());
	      }
	      writer.close();
	}	
	
	public static void printHashMapSS(Map<String, String> map) throws IOException{
		
		//to append the inverted index file.
		PrintWriter writer = new PrintWriter("C:/Users/Naveen/Desktop/TestRuns/LastRun/documentHash");
		for (Entry<String, String> entry : map.entrySet())
	      {	  		
	  		writer.println(entry.getKey()+":"+entry.getValue());
	      }
	      writer.close();
	}

	public static void printHashMapSI(Map<String, Integer> map) throws IOException{
	
		//to append the inverted index file.
		PrintWriter writer = new PrintWriter("C:/Users/Naveen/Desktop/TestRuns/LastRun/allTermHash");
		for (Entry<String, Integer> entry : map.entrySet())
	      {	  		
	  		writer.println(entry.getKey()+":"+entry.getValue());
	      }
	      writer.close();
	}
	
	public static void printHashMapIS(Map<Integer, String> map) throws IOException{
		
		//to append the inverted index file.
		PrintWriter writer = new PrintWriter("C:/Users/Naveen/Desktop/TestRuns/LastRun/finalCatalogHash");
		for (Entry<Integer, String> entry : map.entrySet())
	      {	  		
	  		writer.println(entry.getKey()+":"+entry.getValue());
	      }
	      writer.close();
	}
	
	public static void loadFileHash(String path) throws IOException{
		
		//Path filepath = Paths.get(path);
		
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        BufferedReader in = new BufferedReader(new FileReader(path));
        String line = "";
        while ((line = in.readLine()) != null) {
            String parts[] = line.split(":");
            List<String> docIdLen = new ArrayList<String>();
            docIdLen.add(parts[1]);
            docIdLen.add(parts[2]);
            map.put(parts[0], docIdLen);
        }
        in.close();
        
        printDocHash(map);
	}
	
	
	public static String getStopWords() throws IOException{
		//read each word from the stoplist to create regex string
		String filepath1 = "C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/stoplist.txt";
		Path path1 = Paths.get(filepath1);
		Scanner scanner1 = new Scanner(path1);
		StringBuffer stopWords = new StringBuffer();
		
		while (scanner1.hasNextLine()){
			String line = scanner1.nextLine();
			stopWords.append(line);
			stopWords.append("|");
	      } 
		
		stopWords.append("document|discuss|identify|report|describe|predict|cite");
		String allStopwords = stopWords.toString();
		//System.out.println(""+allStopwords);
		return allStopwords;
	}
	
	public static String stemmer(String word){
		StringBuffer stemmed = new StringBuffer();
        PorterStemmer stemmer = new PorterStemmer();
        String text[] = word.split(" ");
        for(int i = 0;i<text.length;i++){
        	stemmer.setCurrent(text[i]);
        	stemmer.stem();
        	stemmed.append(stemmer.getCurrent());
        	stemmed.append(" ");
        }
        return stemmed.toString();
	}
	
	

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		

//		printHashMapSS(documentHash);
//		printHashMapSI(allTokenHash);
//		loadFileHash("C:/Users/Naveen/Desktop/TestRuns/SecondRun/documentHash");
		
				
		// termIndex holds the termid for the terms present
		// entire corpus.
		int termIndex = 1;
		
		// startOffset holds the start offset for each term in
		// the corpus.
		long startOffset = 0;
			
		// unique_terms is true if there are atleast one more
		// unique term in corpus.
		boolean unique_terms = true;
		
		
		//load the stopwords from the HDD
		String stopwords = getStopWords();
		
		// allTokenHash contains all the terms and its termid
		HashMap<String, Integer> allTermHash = new HashMap<String, Integer>();
		
		// catalogHash contains the term and its position in
		// the inverted list present in the HDD.
		HashMap<Integer, Multimap> catalogHash = new HashMap<Integer, Multimap>();
		
		// finalCatalogHash contains the catalogHash for the invertedIndexHash
		// present in the HDD.
		HashMap<Integer, String> finalCatalogHash = new HashMap<Integer, String>();
		
		// token1000Hash contains the term and Dblocks for the first 1000
		HashMap<Integer, Multimap> token1000DocHash = new HashMap<Integer, Multimap>();
		
		// documentHash contains the doclength of all the documents in the corpus
		HashMap<String, String> documentHash = new HashMap<String, String>();	
		
		// id holds the document id corresponding to the docNumber.
		int id = 1;
		
		// until all unique terms are exhausted
		// pass through the documents and index the terms.
		//while(unique_terms){
			
			File folder = new File("C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/ap89_collection");
			//File folder = new File("C:/Users/Naveen/Desktop/TestFolder");
			File[] listOfFiles = folder.listFiles();
			
			//for each file in the folder 
			for (File file : listOfFiles) {
			    if (file.isFile()) {
			    	
			    	String str = file.getPath().replace("\\","//");
			    	
			    	
					//Document doc = new Document();
			        //System.out.println(""+str);
					//variables to keep parse document.
					int docCount = 0;
					int docNumber = 0;
					int endDoc = 0;
					int textCount = 0;
					int noText = 0;
					Path path = Paths.get(str);
					
					//Document id and text
					String docID = null;
					String docTEXT = null;
					
					//Stringbuffer to hold the text of each document
					StringBuffer text = new StringBuffer();
					
					Scanner scanner = new Scanner(path);
					while(scanner.hasNext()){
						String line = scanner.nextLine();
						if(line.contains("<DOC>")){
							++docCount;
							
						}
						else if(line.contains("</DOC>")){
							++endDoc;
							docTEXT =  text.toString();
							//docTEXT = docTEXT.replaceAll("[\\n]"," ");
							System.out.println("ID: "+id);
							//System.out.println("TEXT: "+docTEXT);
							
							// text with stop words removed
							Pattern pattern1 = Pattern.compile("\\b(?:"+stopwords+")\\b\\s*", Pattern.CASE_INSENSITIVE);
							Matcher matcher1 = pattern1.matcher(docTEXT);
							docTEXT = matcher1.replaceAll("");
							
							// text with stemming
							//docTEXT = stemmer(docTEXT);
							
											
							int docLength = 1;
							// regex to build the tokens from the document text
							Pattern pattern = Pattern.compile("\\w+(\\.?\\w+)*");
							Matcher matcher = pattern.matcher(docTEXT.toLowerCase());
							while (matcher.find()) {
								
								for (int i = 0; i < matcher.groupCount(); i++) {
									
											
									
									// alltermHash contains term and term id
									// if term is present in the alltermHash
									if(allTermHash.containsKey(matcher.group(i))){
										
										int termId = allTermHash.get(matcher.group(i));
										
										
										//if term is present in the token1000Hash
										//then update the dblock of the term.
										if(token1000DocHash.containsKey(termId)){
											Multimap<String, String> dBlockUpdate = token1000DocHash.get(termId);
											//Multimap<Integer, Integer> dBlockUpdate = token1000DocHash.get(matcher.group(i));
											if(dBlockUpdate.containsKey(id)){
												dBlockUpdate.put(String.valueOf(id), String.valueOf(matcher.start()));
											}
											else{
												dBlockUpdate.put(String.valueOf(id), String.valueOf(matcher.start()));
											}
										}
										//if term is not present in the token1000hash
										//then add the token with its dBlock
										else{
											Multimap<String, String> dBlockInsert = ArrayListMultimap.create();
											dBlockInsert.put(String.valueOf(id), String.valueOf(matcher.start()));
											token1000DocHash.put(termId, dBlockInsert);	
										}	
									}
									// if the term is not present I will put the term into allTermHash
									// put corresponding value into the token1000DocHash and increment
									// termIndex
									else{
										allTermHash.put(matcher.group(i),termIndex);
										
										Multimap<String, String> dBlockInsert = ArrayListMultimap.create();
										dBlockInsert.put(String.valueOf(id), String.valueOf(matcher.start()));
										token1000DocHash.put(termIndex, dBlockInsert);
										
										termIndex++;
									}
									
									docLength++;
								}
							}
							
								if(id%1000 == 0){
									
									// then dump index file to HDD
									writeInvertedIndex(token1000DocHash,"token1000DocHash");
									
									// update catalog file
									// to populate catalogHash with the offset
									for(Entry<Integer, Multimap> entry : token1000DocHash.entrySet()){
										if(catalogHash.containsKey(entry.getKey())){
											int len = (entry.getKey()+":"+entry.getValue()).length();
											//String offset = String.valueOf(startOffset)+":"+String.valueOf(len);
											Multimap<String, String> catUpdate = catalogHash.get(entry.getKey());
											catUpdate.put(String.valueOf(startOffset), String.valueOf(len));
											catalogHash.put(entry.getKey(), catUpdate);
											startOffset += len+2;
											
										}
										else{
											int len = (entry.getKey()+":"+entry.getValue()).length();
											//String offset = String.valueOf(startOffset)+":"+String.valueOf(len);
											Multimap<String, String> catInsert = ArrayListMultimap.create();
											catInsert.put(String.valueOf(startOffset), String.valueOf(len));
											catalogHash.put(entry.getKey(), catInsert);//entry.getValue());
											startOffset += len+2;
											
										}										
									}
									
									//clear the token1000DocHash
									token1000DocHash.clear();
								}
							
							documentHash.put(docID, ""+id+":"+docLength);
							id++;
							text = new StringBuffer();
							
						}
						else if(line.contains("<DOCNO>")){
							++docNumber;
							docID = line.substring(8, 21);
															
						}
						else if(line.contains("<TEXT>")){
							++textCount;
						}
						else if((line.contains("<DOC>"))||
								(line.contains("</DOC>"))||
								(line.contains("<DOCNO>"))||
								(line.contains("<FILEID>"))||
								(line.contains("<FIRST>"))||
								(line.contains("<SECOND>"))||
								(line.contains("<HEAD>"))||
								(line.contains("</HEAD>"))||
								(line.contains("<BYLINE>"))||
								(line.contains("</BYLINE>"))||
								(line.contains("<UNK>"))||
								(line.contains("</UNK>"))||
								(line.contains("<DATELINE>"))||
								(line.contains("<NOTE>"))||
								(line.contains("</NOTE>"))||
								(line.contains("</TEXT>"))||
								(line.contains("<TEXT>"))){
							     ++noText;
							
						}
						else if(endDoc == docCount - 1){
							text.append(line);
							text.append(" ");
							
						}
					}
			    	
			    }//end of if - to check if this is a file and parse.
			    
			}//end of for loop to load each file
			
		//}//end of while loop 
		
	// write catalogfile to the hdd to check.
			
			// then dump index file to HDD
			writeInvertedIndex(token1000DocHash,"token1000DocHash");
			
			// update catalog file
			// to populate catalogHash with the offset
			for(Entry<Integer, Multimap> entry : token1000DocHash.entrySet()){
				if(catalogHash.containsKey(entry.getKey())){
					int len = (entry.getKey()+":"+entry.getValue()).length();
					//String offset = String.valueOf(startOffset)+":"+String.valueOf(len);
					Multimap<String, String> catUpdate = catalogHash.get(entry.getKey());
					catUpdate.put(String.valueOf(startOffset), String.valueOf(len));
					catalogHash.put(entry.getKey(), catUpdate);
					startOffset += len+2;
					
				}
				else{
					int len = (entry.getKey()+":"+entry.getValue()).length();
					//String offset = String.valueOf(startOffset)+":"+String.valueOf(len);
					Multimap<String, String> catInsert = ArrayListMultimap.create();
					catInsert.put(String.valueOf(startOffset), String.valueOf(len));
					catalogHash.put(entry.getKey(), catInsert);//entry.getValue());
					startOffset += len+2;
					
				}										
			}
			
			
			
			writeInvertedIndex(catalogHash,"catalogHash");
			printHashMapSI(allTermHash);
			printHashMapSS(documentHash);
			
			long InvIndstartOffset = 0;
			
			//write it to file
            
			//change the finalcatalogHash to the form termid:startoffset:length
			for (Integer name: catalogHash.keySet()){
	            String key = name.toString();
	            String value = catalogHash.get(name).toString();  
	            //System.out.println(key + " " + value);  
	            String finalTermIndex = genInvertedIndex(key, value);
	            finalTermIndex = key+":"+finalTermIndex;
	            int indexLength = finalTermIndex.length();
	            
	            
	            PrintWriter writer = new PrintWriter(new BufferedWriter
	            		(new FileWriter("C:/Users/Naveen/Desktop/TestRuns/LastRun/InvertedIndex", true)));
	            writer.println(finalTermIndex);
	            writer.close();
	            
	            //update the finalcatalogHash
	            //Multimap<String, String> catInsert = ArrayListMultimap.create();
				//catInsert.put(String.valueOf(InvIndstartOffset), String.valueOf(indexLength));
				finalCatalogHash.put(name, String.valueOf(InvIndstartOffset)+":"+String.valueOf(indexLength));//entry.getValue());
				InvIndstartOffset += indexLength+2;
	            
			}
			
			
			
			printHashMapIS(finalCatalogHash);
			
	}
	
	public static String genInvertedIndex(String catKey, String catValue) throws IOException{
		
		RandomAccessFile raFile = new RandomAccessFile("C:/Users/Naveen/Desktop/TestRuns/LastRun/token1000DocHash", "r");
		
		StringBuffer allDblocks = new StringBuffer();
		String InvertedIndex = "";
		String[] splitToPair = catValue.split(",");
		for (int i = 0; i<splitToPair.length; i++){
			//System.out.println(" "+splitToPair[i]);
			
			// split the string and take the startOffset and length
			String[] offset = splitToPair[i].split("=");
			int startOffset = Integer.parseInt((offset[0].replaceAll("\\p{P}", "")).replaceAll("\\s+", ""));
			int length = Integer.parseInt((offset[1].replaceAll("\\p{P}", "")).replaceAll("\\s+", ""));
			//System.out.println("startOffset: "+startOffset+"length: "+length);
			
			
			// read the offset and store it in string for 
			// further processing
			raFile.seek(startOffset);
			byte[] bytes = new byte[length];
			raFile.readFully(bytes);
			String newOffset = new String(bytes, "UTF-8");
			
			// process the offset string to merge them as single 
			// string in catalog and index file.
			String[] dBlocks = newOffset.split(":");
			allDblocks.append(dBlocks[1].substring(1, dBlocks[1].length()-1));
			allDblocks.append(", ");
			InvertedIndex = allDblocks.toString().substring(0, allDblocks.length()-2);
			
			//System.out.println("From File: "+newOffset);
			//System.out.println("offset1: "+offset[0]+" offset2: "+offset[1]);
			//System.out.println(""+startOffset+":"+length);
			
		}
		raFile.close();
		int TTF = 0;
		for(int i=0;i<InvertedIndex.length()-1;i++){
			if(InvertedIndex.charAt(i)==',')
				TTF++;
		}	
		InvertedIndex = InvertedIndex.replaceAll("\\],", "] |").replaceAll("\\s+", "");
		int DF = 0;
		for(int i=0;i<InvertedIndex.length()-1;i++){
			if(InvertedIndex.charAt(i)=='=')
				DF++;
		}
		TTF += 1;
		InvertedIndex = TTF+":"+DF+":{"+InvertedIndex+"}";
		//System.out.println("InvertedIndex: "+InvertedIndex);
		return InvertedIndex;
		
	}
	
	
	
}

