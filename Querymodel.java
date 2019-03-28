import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.PorterStemmer;

import com.google.common.collect.Multimap;


public class Querymodel {
	
	
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
		
		
		double averageDocLength = 247.8111;
		
		int totalDocCount = 84679;
		
		int uniqueTerms = 178050;
		
		double totalTermCount = 20984156;
		
		
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
		
		// for each query in the query file 
		// for all 25 queries in the file
		for(int i = 0; i < 25; i++){
			
			String[] strarray = queryArray[i].split("\\s+");
			
			
			//okapi
			//HashMap<String, Double> okapi = new HashMap<String, Double>();
			//TFIDF
			//HashMap<String, Double> tfidf = new HashMap<String, Double>();
			//okapi BM25
			//HashMap<String, Double> okapibm25 = new HashMap<String, Double>();
			//Unigram Laplace smoothing
			//HashMap<String, Double> unigramLS = new HashMap<String, Double>();
			//Unigram jelinek-mercer smoothing
			HashMap<String, Double> unigramJMS = new HashMap<String, Double>();
			
			//for each term in the query search it in the documents.
			for(int j = 1; j < strarray.length; j++){
			//for(int j = 1; j < strarray.length; j++){
				
				
				int queryFreq=0;
		        Pattern p = Pattern.compile(strarray[j]);
		        Matcher m = p.matcher(queryArray[i]);
		        while (m.find()) {
		        	queryFreq++;

		        }
				
		         
				
				// calculate Okapi for the term
				double okapiTfwd = 0.0;
				double tfidfwd = 0.0;
				double okapibm25Tfwd = 0.0;
				double unigramLSwd = 0.0;
				double unigramJMSwd = 0.0;
				
				
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
					// docid = {pos1, pos2,..}
					String dBlocks[] = invertedList[3].substring(1, invertedList[3].length()-1).split("\\|");

					
// for LM models
					
					//put all dBlocks in a dBlocks hash
					HashMap<Integer, Integer> dBlockhash = new HashMap<Integer, Integer>();
					for(int n = 0; n < dBlocks.length; n++ ){
						
						//split dblock on = to get the docid and [position]
						String dblockData[] = dBlocks[n].split("=");
						int docId = Integer.parseInt(dblockData[0]);
						
						//calculate TF for the document
						int TF = 0;
						for(int k=0;k<dblockData[1].length()-1;k++){
							if(dblockData[1].charAt(k)==',')
								TF++;
						}
						TF += 1;
						
						dBlockhash.put(docId, TF);
					}
					
					// for each document in the corpus give it a score 
					// if present in the dblockhash or not.
					for (Entry<Integer, String> entry : documentHash.entrySet()){
				    	  
						int docId = entry.getKey();
						
						if(dBlockhash.containsKey(docId)){
							
							int termFreq = dBlockhash.get(docId);
							
							// find out the documentLength from documentHash
							String documentData[] = documentHash.get(docId).split(":");
							
//							//Unigram with Laplace smoothing
//							//unigramLSwd = Math.log((termFreq+1)/(docLength+uniqueTerms)) for documents containing the term
//							unigramLSwd = Math.log((double)(termFreq+1)/(double)(Integer.parseInt(documentData[0])+uniqueTerms));
//					        String ranker = strarray[0]+" Q0 "+documentData[1];
//					        if(unigramLS.containsKey(ranker)){
//					        	unigramLS.put(ranker,unigramLS.get(ranker)+unigramLSwd);
//							}
//							else unigramLS.put(ranker,unigramLSwd);
					        
					        
					      //Unigram with Jelinek-Mercer smoothing
							unigramJMSwd = Math.log((0.5*((double)termFreq/(double)(Integer.parseInt(documentData[0])))) + (1.0-0.5)*((double)TTF/totalTermCount));
					        String ranker = strarray[0]+" Q0 "+documentData[1];
					        if(unigramJMS.containsKey(ranker)){
					        	unigramJMS.put(ranker,unigramJMS.get(ranker)+unigramJMSwd);
							}
							else unigramJMS.put(ranker,unigramJMSwd);
							
							
						}
						else{
							
							String documentData[] = documentHash.get(docId).split(":");
							
//							//unigramLSwd = Math.log((termFreq+1)/(docLength+uniqueTerms)) for documents containing the term
//							unigramLSwd = Math.log((double)(0+1)/(double)(Integer.parseInt(documentData[0])+uniqueTerms));
//					        String ranker = strarray[0]+" Q0 "+documentData[1];
//					        if(unigramLS.containsKey(ranker)){
//					        	unigramLS.put(ranker,unigramLS.get(ranker)+unigramLSwd);
//							}
//							else unigramLS.put(ranker,unigramLSwd);
							
							//Unigram with Jelinek-Mercer smoothing
							unigramJMSwd = Math.log(0 + (1.0-0.5)*((double)TTF/totalTermCount));
					        String ranker = strarray[0]+" Q0 "+documentData[1];
					        if(unigramJMS.containsKey(ranker)){
					        	unigramJMS.put(ranker,unigramJMS.get(ranker)+unigramJMSwd);
							}
							else unigramJMS.put(ranker,unigramJMSwd);
							
						}
				      }
					
// end of commenting for LM models
					
					
// for VSM models
/*					for(int l = 0;l<dBlocks.length;l++){ //for VSM
					
						
						//split dblock on = to get the docid and [position]
						String dblockData[] = dBlocks[l].split("=");
						int docId = Integer.parseInt(dblockData[0]);
						
						
						
						//calculate TF for the document
						int TF = 0;
						for(int k=0;k<dblockData[1].length()-1;k++){
							if(dblockData[1].charAt(k)==',')
								TF++;
						}
						TF += 1;
					
						//System.out.println("docId: "+docId);
						// find out the documentLength from documentHash
						if(documentHash.containsKey(docId)){
							String documentData[] = documentHash.get(docId).split(":");
						
						
							//Okapi Matching score
//							okapiTfwd = TF/(TF+.5+1.5*(Integer.parseInt(documentData[0])/averageDocLength));
//							String ranker = strarray[0]+" Q0 "+documentData[1];
//							//String ranker = "89"+" Q0 "+documentData[1];
//							if(okapi.containsKey(ranker)){
//								okapi.put(ranker,okapi.get(ranker)+okapiTfwd);
//							}
//							else okapi.put(ranker,okapiTfwd);
							
							//TF-IDF Matching score
//					        tfidfwd = okapiTfwd*(Math.log(totalDocCount/DF));
//					        String ranker = strarray[0]+" Q0 "+documentData[1];
//					        if(tfidf.containsKey(ranker)){
//					        	tfidf.put(ranker,tfidf.get(ranker)+tfidfwd);
//							}
//							else tfidf.put(ranker,tfidfwd);
							
							
							//Okapi BM 25 Matching score
//							double first,second,third = 0.0;
//							first = (Math.log((totalDocCount+0.5)/(DF+0.5)));
//							second = ((TF+1.2*TF)/(TF+1.2*((1-0.75)+(0.75*(Integer.parseInt(documentData[0])/averageDocLength)))));
//							third = ((queryFreq+100*queryFreq)/(queryFreq+100));
//							okapibm25Tfwd = first*second*third;
//							String ranker = strarray[0]+" Q0 "+documentData[1];
//							//String ranker = "89"+" Q0 "+documentData[1];
//							if(okapibm25.containsKey(ranker)){
//								okapibm25.put(ranker,okapibm25.get(ranker)+okapibm25Tfwd);
//							}
//							else okapibm25.put(ranker,okapibm25Tfwd);
							
							//unigramLSwd = Math.log((termFreq+1)/(docLength+uniqueTerms))
							unigramLSwd = Math.log((double)(TF+1)/(double)(Integer.parseInt(documentData[0])+uniqueTerms));
					        String ranker = strarray[0]+" Q0 "+documentData[1];
					        if(unigramLS.containsKey(ranker)){
					        	unigramLS.put(ranker,unigramLS.get(ranker)+unigramLSwd);
							}
							else unigramLS.put(ranker,unigramLSwd);
							
							//Unigram with Jelinek-Mercer smoothing
//					        unigramJMSwd = Math.log((0.5*((double)TF/(double)(Integer.parseInt(documentData[0])))) + (1.0-0.5)*((double)TTF/totalTermCount));
//							unigramJMSwd = Math.log((0.5*((double)TF/(double)(217))) + (1.0-0.5)*((double)TTF/totalTermCount));
//					        String ranker = strarray[0]+" Q0 "+documentData[1];
//					        if(unigramJMS.containsKey(ranker)){
//					        	unigramJMS.put(ranker,unigramJMS.get(ranker)+unigramJMSwd);
//							}
//							else unigramJMS.put(ranker,unigramJMSwd);
						
						}
						
						//System.out.println("dBlocks: "+dBlocks[i]+" docid: "+docId+" TF: "+TF+" length: "
						//+documentData[0]+" OkapiScore: "+okapiTfwd+" docNum: "+documentData[1]);					

					
					}//end of for loop of the dblocks metrics for each term */
// end of commenting for VSM models

				}//end of if loop when the term is present in the alltermhash
				
			}//end of for loop for each term
			
			//Sort the value by okapi and arrange according to the rank for each query
			// false descending order, true ascending order
			//Map<String, Double> sortedMapDsc = sortByComparator(okapi, false);
			//Map<String, Double> sortedMapDsc = sortByComparator(okapibm25, false);
			//Map<String, Double> sortedMapDsc = sortByComparator(unigramLS, false);
			//Map<String, Double> sortedMapDsc = sortByComparator(tfidf, false);
			Map<String, Double> sortedMapDsc = sortByComparator(unigramJMS, false);
			
			for(Map.Entry<String, Double> entry : sortedMapDsc.entrySet()){
			 
					 rankedOutput.put(entry.getKey(), entry.getValue());
				 
			}
		
		}//end of for loop for each query
		
		//write the okapi score to the file
		System.out.println("writing model file");
		printMap(rankedOutput,"UnigramJMS.txt");
	
	}//end of main

}
