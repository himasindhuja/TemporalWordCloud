package org.iiit.ire;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;


public class ReadFile {
	public static HashMap<Long, List<String>> tweetMap = new HashMap<Long, List<String>>();
	public static HashMap<String, String> stemMap = new HashMap<String, String>();
	public static String[] patterns = new String[]{"hh:mm aa - dd MMM, yyyy"};

	//{time = {token = {tag = {freq, lda score}}}}
	public static HashMap<Long, HashMap<String, HashMap<String,List<Integer>>>> freqMap = new HashMap<Long, HashMap<String,HashMap<String,List<Integer>>>>();
	public static final Charset charset = Charset.forName("UTF-8");
	HashMap<String, String>  hindiToEnglishMap = new HashMap<String, String>();
	//3:37 P.M - 6 Nov, 2012
	//	((19|20)\\d\\d([- /.])(1[012]|0[1-9]|[1-9])[- /.]([12][0-9]|3[01]|0[1-9]))
	static String regex = "HH:MM aa - DD mmm, yyyy";

	/*void buildHindiToEnglishHashMap() {
		hindiToEnglishMap.put("जनवरी", "january");
		hindiToEnglishMap.put("फ़रवरी", "february");
		hindiToEnglishMap.put("मार्च", "march");
		hindiToEnglishMap.put("अप्रैल", "april");
		hindiToEnglishMap.put("मई", "may");
		hindiToEnglishMap.put("जून", "june");
		hindiToEnglishMap.put("जुलाई", "july");
		hindiToEnglishMap.put("आगस्त", "august");
		hindiToEnglishMap.put("सितम्बर", "september");
		hindiToEnglishMap.put("अकतूबर", "october");
		hindiToEnglishMap.put("नवेम्बर", "november");
		hindiToEnglishMap.put("दिसम्बर", "december");
	}*/

	/**
	 * Returns the buffered reader object
	 * @param file
	 * @return
	 */
	public static List<String> getBufferedReader(File file) {
		Path path = Paths.get(file.getAbsolutePath());
		try {
			return Files.readAllLines(path, charset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void parseTime(String line){
		line = line.replace("अपराह्न","PM");
		line = line.replace("पूर्वाह्न","AM");
		line = line.replace("नवं","Nov");
		line = line.replace("अक्टू","Oct");
		String[] splits = line.split("\t");
		splits[9] = splits[9].trim();

		Date date = null;
		try {
			date = DateUtils.parseDateStrictly(splits[9], patterns);
		} catch (Exception e) {
			return;
		}

		List<String> tweets = new ArrayList<String>();
		try {
			tweets = tweetMap.get(date.getTime());
		} catch (Exception e) {
			tweets = new ArrayList<String>();
		}

		try {
			tweets.add(splits[8]);
		} catch (Exception e) {
			tweets = new ArrayList<String>();
			tweets.add(splits[8]);
		}

		tweetMap.put(date.getTime(), tweets);
	}

	public static void parseData(){

		for(Map.Entry<Long, List<String>> entries : tweetMap.entrySet()){
			freqMap = new HashMap<Long, HashMap<String,HashMap<String,List<Integer>>>>();

			long time = entries.getKey();
			System.out.println("time : "+ time +"\t size :" + entries.getValue().size());
			for(String tweet : entries.getValue()){
				Tokenizer tokenizer = new Tokenizer(true, false, true, true, true, false, true);
				String[] tokens = tokenizer.getNGrams(tweet, 1, stemMap);
				String[] tags = POSTagger.getInstance().tag(tokens);

				HashMap<String, List<Integer>> value1  = new HashMap<String, List<Integer>>();
				
				for(int i =0 ; i< tokens.length; i++){
					String token = tokens[i];
					String tag = tags[i];

					//{time = {token = {tag = {freq, lda score}}}}
					//				System.out.println("token : "+token+"\t tag: "+tag);
					HashMap<String, HashMap<String,List<Integer>>> value = freqMap.get(time);
					List<Integer>  value2 = new ArrayList<Integer>();


					if(value == null)
						value = new HashMap<String, HashMap<String,List<Integer>>>();
					
					value1 = value.get(token);
					if(value1 == null){
						value1 =  new HashMap<String, List<Integer>>();
					}

					try{
						value2.add(value1.get(tag).get(0) + 1);
						value2.add(value1.get(tag).get(1) + 1);// have to add lda code here
					}catch(Exception e){
						value2 = new ArrayList<Integer>();
						value2.add(1);
						value2.add(1);
					}

					value1.put(tag, value2);

					value.put(token, value1);
					freqMap.put(time, value);
				}
			}

			System.out.println(ReadFile.freqMap);
		}
	}

	/**
	 * Reads content from file to set
	 * @param file
	 * @param ignoreCase
	 * @param useId (Whether to generate id for each line)
	 * @return
	 */
	public static Set<String> readLinesToSet(File file, 
			boolean ignoreCase, boolean useId) {
		Set<String> set = new HashSet<String>();
		set.addAll(getBufferedReader(file));
		/*try {
			List<String> lines = getBufferedReader(file);
			String line = null;

			while((line = br.readLine()) != null) {
				if(ignoreCase) {
					line = line.toLowerCase();
				}

				if(StringUtils.isNotBlank(line))
					set.add(line);
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		return set;
	}

	public static void readLinesToList(File file) {

		List<String> lines = new ArrayList<String>();
		long start = System.currentTimeMillis();
		lines.addAll(getBufferedReader(file));
		/*try {
			BufferedReader br = getBufferedReader(file);
			String line = null;

			while((line = br.readLine().toLowerCase()) != null) {
				line = line.toLowerCase();
				lines.add(line);
//				parseData(line);
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		System.out.println("Time for reading the file "+(System.currentTimeMillis() - start));


		start = System.currentTimeMillis();
		for(String line : lines){
			parseTime(line);
		}

		System.out.println("Time for parsing a line "+(System.currentTimeMillis() - start));
		parseData();
	}

	public static void main(String[] args){
		long start = System.currentTimeMillis();
		ReadFile.readLinesToList(new File(args[0]));
		System.out.println((System.currentTimeMillis()-start));
		//		String line = "200	false	false	false	4064	O	265959246791864320	stlouisbiz	Men, will you be watching election results alone? http://www.bizjournals.com/stlouis/blog/2012/11/men-will-you-be-watching-election.html?ana=twt … #Election2012	3:30 अपराह्न - 6 नवं, 2012 	1	 0	null";
		//		parseData(line);
		System.out.println(ReadFile.stemMap);
	}
}
