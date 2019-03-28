import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.collect.Multimap;


public class Proximity {
	
	public static HashMap loadDocumentHash() throws IOException{
		
		HashMap<Integer, String> docHash = new HashMap<Integer, String>();
		String filepath = "C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/IndexWithStoppingAndWithStemming/documentHash";
		Path path = Paths.get(filepath);
		Scanner scanner = new Scanner(path);
		while(scanner.hasNextLine()){
			String document[] = scanner.nextLine().split(":");
			docHash.put(Integer.parseInt(document[1]), document[2]+":"+document[0]);
		}
		return docHash;
	}
	
	
	public static HashMap loadAllTermHash() throws IOException{
		
		HashMap<String, Integer> termHash = new HashMap<String, Integer>();
		String filepath = "C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/IndexWithStoppingAndWithStemming/allTermHash";
		Path path = Paths.get(filepath);
		Scanner scanner = new Scanner(path);
		while(scanner.hasNextLine()){
			String term[] = scanner.nextLine().split(":");
			termHash.put(term[0], Integer.parseInt(term[1]));
		}
		return termHash;
	}
	
	
	public static HashMap loadCatalogHash() throws IOException{
		
		HashMap<Integer, String> catHash = new HashMap<Integer, String>();
		String filepath = "C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/IndexWithStoppingAndWithStemming/finalCatalogHash";
		Path path = Paths.get(filepath);
		Scanner scanner = new Scanner(path);
		while(scanner.hasNextLine()){
			String cat[] = scanner.nextLine().split(":");
			catHash.put(Integer.parseInt(cat[0]), cat[1]+":"+cat[2]);
		}
		return catHash;
	}
	
	
	public static String stemmer(String word){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
	}
	
	
	public static String randomAccessCat(long startOffset, int Length) throws IOException{
		
		RandomAccessFile raFile = new RandomAccessFile("C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/IndexWithStoppingAndWithStemming/InvertedIndex", "r");
		//RandomAccessFile raFile1 = new RandomAccessFile("C:/Users/Naveen/Desktop/TestRuns/SecondRun/test2", "r");
		
		raFile.seek(startOffset);
		byte[] bytes = new byte[Length];
		raFile.readFully(bytes);
		raFile.close();
		String invertedIndex = new String(bytes, "US-ASCII");
		return invertedIndex;
	}
	

	// sort the hashmap and put into linked hashmap to maintain the order.
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        int rank = 1;
        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list)
        {
        	if(rank<=1000){
        	
        		sortedMap.put(entry.getKey() +" "+ String.valueOf(rank), entry.getValue());
        		rank++;
        	}
        	else break;
        }
           
        //Map<string, Double> updatedRank = new LinkedHashMap<String, Double>();

        return sortedMap;
    }
	
	
	// function to print the file to hdd
	public static void printMap(Map<String, Double> map, String filename) throws FileNotFoundException, UnsupportedEncodingException{
			
			PrintWriter writer = new PrintWriter("C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/IndexWithStoppingAndWithStemming/"+filename, "UTF-8");
	      for (Entry<String, Double> entry : map.entrySet()){
	    	  
	  		writer.println(entry.getKey()+" "+entry.getValue()+" Exp");
	      }
	      writer.close();
	  }

	public static void main(String[] args) throws IOException {
		
		
		int uniqueTerms = 178050;
		
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
		//System.out.println("stopwords: "+allStopwords);
		
		//read each query from the doc
		String filepath2 = "C:/Naveen/CCS/IR/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
		Path path2 = Paths.get(filepath2);
		Scanner scanner2 = new Scanner(path2);
		
		int querynumber = 0;
		String[] queryArray = new String[28];
		while (scanner2.hasNextLine()){
			String line = scanner2.nextLine();
			queryArray[querynumber] = line.replaceAll("\\p{P}", "").toLowerCase();
			querynumber++;
	      } 
		
		// to remove stopwords from the query 
		Pattern pattern = Pattern.compile("\\b(?:"+allStopwords+")\\b\\s*", Pattern.CASE_INSENSITIVE);
		for(int i=0;i<25;i++){
					
			Matcher matcher = pattern.matcher(queryArray[i]);
			queryArray[i] = matcher.replaceAll("");
			//System.out.println(queryArray[i]+"");
		}
		
		// linkedHashMap used to store the hash with order.
		// contains the final result.
		Map<String, Double> rankedOutput = new LinkedHashMap<String, Double>();
		
		// Load allTermHash
		// holds all the terms in the termhash with the termid
		// format(term, termid)
		System.out.println("Loading allTermHash");
		HashMap<String, Integer> allTermHash = loadAllTermHash();
		
		// Load document hash
		// documentHash contains the doclength of all the documents in the corpus
		// format(documentid, documentLength:documentNumber)
		System.out.println("Loading documentHash");
		HashMap<Integer, String> documentHash = loadDocumentHash();
		
		// Load catalogHash
		// catalogHash holds all the startOffset and length of the term indicated through termid
		// format(termid, startoffset:length)
		System.out.println("Loading catalogHash");
		HashMap<Integer, String> catalogHash = loadCatalogHash();
		
		//Hashmap for the proximity values of each document
		
		
		for(int i = 0; i < 25; i++){
			
			String[] strarray = queryArray[i].split("\\s+");
			
			
			// proximity
			HashMap<String, Double> proximity = new HashMap<String, Double>();
		
			//String str = "corrupt allegation  official report";
			
			//String[] strarray = str.split("\\s+");
			
			// holds the docid and the list of position for the particular term
			// required to do the span.
			HashMap<Integer, List<List<Integer>>> docPosHash = new HashMap<Integer, List<List<Integer>>>();
			
			for(int j = 1; j < strarray.length; j++){
			//for(int j = 1; j < 3; j++){
				
			
				System.out.println("Term: "+strarray[j]);
				
				String term = stemmer(strarray[j]);
				
				if(allTermHash.containsKey(term)){
				
					//termId contains the id of the term
					int termId = allTermHash.get(term);
					//indexData contains the startoffset and length
					String indexData[] = catalogHash.get(termId).split(":");
					// startoffset of the term
					long startOffset = Integer.parseInt(indexData[0]);
					// length of the term data in the inverted index.
					int length = Integer.parseInt(indexData[1]);
					
					// contains the invertedlist for the term.
					String invertedList[] = randomAccessCat(startOffset, length).split(":");
					
					int TTF = Integer.parseInt(invertedList[1]);
					int DF = Integer.parseInt(invertedList[2]);
					
					// dblocks of the term from invertedList
					String dBlocks[] = invertedList[3].substring(1, invertedList[3].length()-1).split("\\|");
					
					
					for(int k = 0; k < dBlocks.length; k++){
						
						//split dblock on = to get the docid and [position]
						String dblockData[] = dBlocks[k].split("=");
						int docId = Integer.parseInt(dblockData[0]);
						
						//System.out.println(" "+dblockData[0]+" "+dblockData[1]);
						List<Integer> termPosn = new ArrayList<Integer>();
						String position[] = dblockData[1].split(",");
						
						for(int m = 0; m < position.length; m++){
							int pos = Integer.parseInt(position[m].replaceAll("\\p{P}", ""));
							termPosn.add(pos);
							//System.out.println(" "+pos);
						}//end of arraylist for loop
						
						
						if(docPosHash.containsKey(docId)){
							
							List<List<Integer>> listOfPosn = docPosHash.get(docId);
							listOfPosn.add(termPosn);
							docPosHash.put(docId, listOfPosn);
						}
						else{
							List<List<Integer>> listOfPosn = new ArrayList<List<Integer>>();
							listOfPosn.add(termPosn);
							docPosHash.put(docId, listOfPosn);
						}						
						
					}//end of dblocks for loop
					
					//printHashILI(docPosHash);
					
				}//end of if loop when the term is persent in the alltermhash

								
			}//end of for loop of each term in the query
			
			
			// remove the docIds if they have positions only for one term in the 
			// arraylist of list.
			// to avoid concurrentModificationException using the sets
			Set<Integer> set = new HashSet<Integer> ();
			for(Map.Entry<Integer, List<List<Integer>>> entry : docPosHash.entrySet()){
				 
				 List<List<Integer>> singletons = entry.getValue();
				 // to avoid concurrentModificationException
				 if(singletons.size() == 1){
					 set.add(entry.getKey());
					 
				 }
			}
			
			
			// remove all the documents that have only one term
			// in their document.
			docPosHash.keySet().removeAll(set);
			
			//printHashILI(docPosHash);
			
			// Proximity Algorithm starts ------------------------------------------------------
			for(Map.Entry<Integer, List<List<Integer>>> entry : docPosHash.entrySet()){
			
				boolean canFindRange = true;
				
				List<List<Integer>> docList = entry.getValue();
				
				// list containing range of all the possible combinations
				List<Integer> rangeList = new ArrayList<Integer>();
				
				while(canFindRange){
					
					// come out of the loop if anyone of arraylist in the arraylist has
				    // only one element.
					for(int k =0; k<docList.size(); k++){
						
						int length = docList.get(k).size();
						if(length == 1){
							canFindRange = false;
						}
					}
					// list containing first elements of all the arraylist
					List<Integer> findMinMax = new ArrayList<Integer>();
								
					// to get the first element of all the list in arraylist
					for(int k =0; k<docList.size();k++){
						
						findMinMax.add(docList.get(k).get(0));
					}
					
					// print the first element of all the list in arraylist
					//printArraylist(findMinMax);
					
					// sor the arraylist to get the min and max
					Collections.sort(findMinMax);		
					int min = findMinMax.get(0);
				    int range = findMinMax.get(findMinMax.size()-1) - findMinMax.get(0); //Max-Min = range
				    rangeList.add(range);
				    
				    // to find and remove the first element of the arraylist in list that has the minimum element.
				    int arrlistWithleastVal = -1;
				    for(int k =0; k<docList.size(); k++){
						
				    	
						if(!(docList.get(k).indexOf(min) == -1)){
							docList.get(k).remove(0);
							arrlistWithleastVal = i;
							break;
						}
					}
				    
				}
				
				// sort the rangeList to get the mininmum
				Collections.sort(rangeList);	
				int range = rangeList.get(0);
				if(documentHash.containsKey(entry.getKey())){
					String[] docNumber = documentHash.get(entry.getKey()).split(":");
					String ranker = strarray[0]+" Q0 "+docNumber[1];
					
					List<List<Integer>> containTerms = docPosHash.get(entry.getKey());
					
					proximity.put(ranker,(15000 - (double)range) * (double)containTerms.size() /((double)Integer.parseInt(docNumber[0]) + (double)uniqueTerms));
					//(C - rangeOfWindow)/(lengthOfDocument + V)
				}
				
			}// proximity search algorithm ends ---------------------------------------------------------------------
			
			//printHashILI(docPosHash);
			
			Map<String, Double> sortedMapDsc = sortByComparator(proximity, false);
			
			for(Map.Entry<String, Double> entry : sortedMapDsc.entrySet()){
			 
					 rankedOutput.put(entry.getKey(), entry.getValue());
				 
			}
			
		
		}//end of for loop of each query
			
			//write the okapi score to the file
			System.out.println("writing model file");
			printMap(rankedOutput,"Proximity5.txt");

	}


 public static void printHash(HashMap<String, HashMap> token){
		
	
		for (String name: token.keySet()){
			
            String key =name.toString();
            String value = token.get(name).toString();  
            System.out.println(key + " " + value);  
            
		}
		
	}
 public static void printHashILI(HashMap<Integer, List<List<Integer>>> token){
		
		
		for (Integer name: token.keySet()){
			
         String key =name.toString();
         String value = token.get(name).toString();  
         System.out.println(key + " " + value);  
         
		}
		
	}

}
