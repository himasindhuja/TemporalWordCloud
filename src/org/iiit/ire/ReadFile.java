package org.iiit.ire;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;


public class ReadFile {
	public static HashMap<String, Long> tweetMap = new HashMap<String, Long>();
	public static HashMap<String, String> stemMap = new HashMap<String, String>();
	public static HashMap<Long, HashMap<String, Integer>> freqMap = new HashMap<Long, HashMap<String,Integer>>();
	public static final Charset charset = Charset.forName("UTF-8");
	HashMap<String, String>  hindiToEnglishMap = new HashMap<String, String>();
	//3:37 P.M - 6 Nov, 2012
	//	((19|20)\\d\\d([- /.])(1[012]|0[1-9]|[1-9])[- /.]([12][0-9]|3[01]|0[1-9]))
	static String regex = "HH:MM aa - DD mmm, yyyy";

	void buildHindiToEnglishHashMap() {
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
	}

	/**
	 * Returns the buffered reader object
	 * @param file
	 * @return
	 */
	public static BufferedReader getBufferedReader(File file) {
		Path path = Paths.get(file.getAbsolutePath());
		try {
			return Files.newBufferedReader(path, charset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void parseData(String line){
		line = line.replace("अपराह्न","PM");
		line = line.replace("पूर्वाह्न","AM");
		line = line.replace("नवं","Nov");
		line = line.replace("अक्टू","Oct");
		String[] splits = line.split("\t");
		//		System.out.println(splits.length);

		//		System.out.println(splits[9]);
		splits[9] = splits[9].trim();
		String[] patterns = new String[]{"hh:mm aa - dd MMM, yyyy"};
		try {
			Tokenizer tokenizer = new Tokenizer(true, false, true, true, true, false, true);
			System.out.println(splits[9]);
			Date date = DateUtils.parseDateStrictly(splits[9], patterns);
			String[] tokens = tokenizer.getNGrams(splits[8], 1, stemMap);
			String[] tags = POSTagger.getInstance().tag(tokens);

			System.out.println(Arrays.asList(tokens)+"\n"+Arrays.asList(tags));
			tweetMap.put(splits[8], date.getTime());

			HashMap<String, Integer> value  = new HashMap<String, Integer>();
			for(String tag :tokens){
				value = freqMap.get(date.getTime());

				if(value != null){
					try{
						int freq = freqMap.get(date.getTime()).get(tag) + 1;
						System.out.println("tag is::::"+ tag);
						value.put(tag, freq);
					}catch(Exception e){
						System.out.println("In catch " + tag);
						value.put(tag, 1);
					}
				}else{
					value = new HashMap<String, Integer>();
					value.put(tag, 1);
				}
				System.out.println(value);
				freqMap.put(date.getTime(), value);
			}
			//			System.out.println(date);
			//			System.out.println(date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//		System.out.println(splits[8]+"\t"+splits[9]);
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

		try {
			BufferedReader br = getBufferedReader(file);
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
		}

		return set;
	}

	public static List<String> readLinesToList(File file, 
			boolean ignoreCase) {
		List<String> list = new ArrayList<String>();

		try {
			BufferedReader br = getBufferedReader(file);
			String line = null;

			while((line = br.readLine()) != null) {
				if(ignoreCase) {
					line = line.toLowerCase();
					//					System.out.println(line);
					line = line.replace("अपराह्न","PM");
					line = line.replace("नवं","Nov");
				}

				if(StringUtils.isNotBlank(line))
					list.add(line);

				parseData(line);
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}


	public static void main(String[] args){
		//		ReadFile.readLinesToList(new File(args[0]), true);
		String line = "200	false	false	false	4064	O	265959246791864320	stlouisbiz	Men, will you be watching election results alone? http://www.bizjournals.com/stlouis/blog/2012/11/men-will-you-be-watching-election.html?ana=twt … #Election2012	3:30 अपराह्न - 6 नवं, 2012 	1	 0	null";
		parseData(line);
		System.out.println(ReadFile.stemMap);
		System.out.println(ReadFile.freqMap);
	}
}
