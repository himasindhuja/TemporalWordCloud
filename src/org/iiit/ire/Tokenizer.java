package org.iiit.ire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;


public class Tokenizer {
	private final Pattern SUFFIX_PATTERN = Pattern
			.compile("(.*?)('|’|`)(s|re|ve|d|ll|cause|en|you|m)");
	private final String ELONG_STR = "(.)\\1+";

	private final String[] TOKEN_DELIM_CHARS = new String[]{".",";", ",", "!", ":", "-", "(", 
			")", "\"", "\'","’", "`", "?", "[", "]"};

	private boolean removeStopwords = false;
	private boolean removeSuffixes = false;
	private boolean includeHashTags = false;
	private boolean includeAtTags = false;
	private boolean removeURLs = false;
	private boolean elongationNorm = false;
	private boolean removeTokenDelimChars = false;

	private List<String> hashTags = new ArrayList<String>();
	private List<String> atTags = new ArrayList<String>();
	private List<String> urls = new ArrayList<String>();

	/**
	 * Default tokenizer constructor
	 */
	public Tokenizer() {
	}

	/**
	 * Creates Tokenizer instance, if removeStopwords is set, stopwords are
	 * removed. if removeSuffixes is set, suffixes are pruned from tokens.
	 * 
	 * @param removeStopwords true or false
	 * @param removeSuffixes true of false
	 */
	public Tokenizer(boolean removeStopwords, boolean removeSuffixes) {
		this.removeStopwords = removeStopwords;
		this.removeSuffixes = removeSuffixes;
	}

	/**
	 * Creates Tokenizer instance, if removeStopwords is set, stopwords are
	 * removed. if removeSuffixes is set, suffixes are pruned from tokens, options to include hash and at tags,
	 * , exclude URLs and elongationNormalization (social media)
	 * @param removeStopwords true or false
	 * @param removeSuffixes true or false
	 * @param includeHashTags true or false
	 * @param includeAtTags true or false
	 * @param removeURLs true or false
	 * @param elongationNorm true or false
	 */
	public Tokenizer(boolean removeStopwords, boolean removeSuffixes, 
			boolean includeHashTags, boolean includeAtTags, boolean removeURLs, boolean elongationNorm) {
		this.removeStopwords = removeStopwords;
		this.removeSuffixes = removeSuffixes;
		this.includeHashTags = includeHashTags;
		this.includeAtTags = includeAtTags;
		this.removeURLs = removeURLs;
		this.elongationNorm = elongationNorm;
	}
	
	/**
	 * Creates Tokenizer instance, if removeStopwords is set, stopwords are
	 * removed. if removeSuffixes is set, suffixes are pruned from tokens, options to include hash and at tags,
	 * , exclude URLs and elongationNormalization (social media)
	 * if removeTokenDelimChars is set, special characters are pruned from tokens.
	 * @param removeStopwords true or false
	 * @param removeSuffixes true or false
	 * @param includeHashTags true or false
	 * @param includeAtTags true or false
	 * @param removeURLs true or false
	 * @param elongationNorm true or false
	 * @param removeTokenDelimChars true or false
	 */
	public Tokenizer(boolean removeStopwords, boolean removeSuffixes, 
			boolean includeHashTags, boolean includeAtTags, boolean removeURLs, boolean elongationNorm,
			boolean removeTokenDelimChars) {
		this.removeStopwords = removeStopwords;
		this.removeSuffixes = removeSuffixes;
		this.includeHashTags = includeHashTags;
		this.includeAtTags = includeAtTags;
		this.removeURLs = removeURLs;
		this.elongationNorm = elongationNorm;
		this.removeTokenDelimChars=removeTokenDelimChars;
	}
	
	/**
	 * Returns an array of tokens of the input text.
	 * 
	 * @param text the input text
	 * @return the array of tokens
	 */
	public String[] tokenize(String text) {
		List<String> tokens = new ArrayList<String>();
		String[] tokensSplits = text.split("(\\s|\n|\r|\t)");
		
		for(String token : tokensSplits) {
			if(StringUtils.isBlank(token))
				continue;

			if(this.removeURLs) {
				if(StringUtils.startsWithIgnoreCase(token, "http://") || 
						StringUtils.startsWithIgnoreCase(token , "https://")) {
					this.urls.add(token);
					continue;
				}
			}

			if(token.startsWith("#") && token.length() > 2) {
				token = removeStartEndJunkChars(token);
				token = removeSuffixes(token);
				
				/*List<String> segments = tokenizeHashTag(token);
				if(segments != null && !segments.isEmpty()) {
					StringBuffer segBuffer = new StringBuffer();
					for(String segment : segments) {
						segBuffer.append(" ");
						segBuffer.append(segment);
					}
					
					if(StringUtils.isNotBlank(segBuffer.toString().trim()))
						token = segBuffer.toString().trim();
				}*/ 
				
				this.hashTags.add(token);
				if(this.includeHashTags)
					tokens.add(token);
			} else if(token.startsWith("@") && token.length() > 2) {
				token = removeStartEndJunkChars(token);
				token = removeSuffixes(token);
				this.atTags.add(token);
				
				if(this.includeAtTags)
					tokens.add(token);
			} else {
				if(this.elongationNorm) {
					token = noramalizeElongations(token);
				}

				if(this.removeTokenDelimChars) {
					token = removeStartEndJunkChars(token);
				}
				
				if(this.removeSuffixes)
					token = removeSuffixes(token);
				
				if(this.removeStopwords) {
					if(StopWordsLoader.getLoader().
							isStopWord(token))
						continue;
				}

				tokens.add(token);
			}
		}

		return tokens.toArray(new String[0]);
	}

	/**
	 * Returns the urls in the given input text
	 * @return a list of urls
	 */
	public List<String> getURLs() {
		return this.urls;
	}

	/**
	 * Returns the hashtags in the given input text
	 * @return a list of hashtags
	 */
	public List<String> getHashTags() {
		return this.hashTags;
	}
	
	/**
	 * This method breaks hash tag using some patterns of capital words etc...
	 * <br> Returns null if no segment found
	 * @param hashTag
	 * @return
	 */
    public List<String> tokenizeHashTag(String hashTag) {
        String tag = hashTag;
        
        if(tag.startsWith("#") && tag.length() > 2) {
            tag = tag.substring(1);
        }
        
        if(StringUtils.isBlank(tag))
        	return null;

        List<String> toReturn = new ArrayList<String>();
        boolean continuousAdd = false;
        int capitalcount = 0;
        
        StringBuffer segmentBuffer = new StringBuffer();
        for(int i = 0; i < tag.length(); i++) {
        	char c = tag.charAt(i);
        	if(CharUtils.isAsciiAlphaLower(c)) {
        		continuousAdd = false;
        		segmentBuffer.append(c);
        	} else if(CharUtils.isAsciiAlphaUpper(c) || CharUtils.isAsciiAlphanumeric(c)) {
        		capitalcount++;
        		if(segmentBuffer.toString().length() > 0 && !continuousAdd) {
        			toReturn.add(segmentBuffer.toString());
        			segmentBuffer = new StringBuffer();
        		}
        		segmentBuffer.append(c);
        		continuousAdd = true;
        	}
        }
        
        if(capitalcount <= 1)
        	return null;
        
        /* Final left out chars */
        if(segmentBuffer.toString().length() > 0) {
        	toReturn.add(segmentBuffer.toString());
        }
        
        if(toReturn.size() > 1)
        	return toReturn;
        
        return null;
    }
	
	/**
	 * Returns the attags in the given input text
	 * @return list of attags
	 */
	public List<String> getAtTags() {
		return this.atTags;
	}

	/**
	 * Removes the start or end junk characters such as !
	 * @param token the input word
	 * @return string with the junk characters removed
	 */
	public String removeStartEndJunkChars(String token) {
		while(StringUtils.endsWithAny(token, TOKEN_DELIM_CHARS)) {
			token = token.substring(0, token.length() - 1);
		}
		
		while(StringUtils.startsWithAny(token, TOKEN_DELIM_CHARS) && token.length() > 1) {
			int beginIndex = StringUtils.indexOfAny(token, TOKEN_DELIM_CHARS) + 1;
			if(beginIndex < token.length())
				token = token.substring(beginIndex);
		}
		
		return token;
	}

	/**
	 * Removes suffixes
	 * @param token the input word
	 * @return the string with suffixes removed
	 */
	public String removeSuffixes(String token) {
		Matcher m = SUFFIX_PATTERN.matcher(token);
		if(m.find()) {
			token = m.group(1);
		}
		return token;
	}

	/**
	 * Returns the elongation normalization token
	 * @param token the word
	 * @return the elongation normalized word
	 */
	public String noramalizeElongations(String token) {
		return token.replaceAll(ELONG_STR, "$1$1");
	}

	/**
	 * Returns ngram characters token of length len
	 * 
	 * @param text the input text
	 * @param len ngram length required
	 * @return the array of ngram char tokens
	 */
	public String[] getCharNGrams(String text, int len) {
		List<String> charngrams = new ArrayList<String>();
		char[] chars = text.toCharArray();

		for (int i = 0; i < chars.length - len + 1; i++) {
			charngrams.add(concat(chars, i, i + len));
		}
		return charngrams.toArray(new String[0]);
	}

	/**
	 * Returns the ngrams upto this length, please set length value to a maximum of 4, 
	 * many cases it is more than sufficient
	 * @param text
	 * @param len
	 * @return
	 */
	public String[] getNgramsTillLength(String text, int len) {
		List<String> ngrams = new ArrayList<String>();
		
		String[] tokens = tokenize(text);
		for(int i = 1; i <= len; i++) {
			ngrams.addAll(Arrays.asList(getNGrams(tokens, i)));
		}
		
		return ngrams.toArray(new String[0]);
	}
	
	/**
	 * Returns ngram tokens of given length
	 * 
	 * @param text the input text
	 * @param len ngram length required
	 * @return the array of ngram tokens
	 */
	public String[] getNGrams(String text, int len) {
		String[] tokens = tokenize(text);
		return getNGrams(tokens, len);
	}

	/**
	 * Returns ngram tokens of given length
	 * 
	 * @param tokens the input words
	 * @param len ngram length required
	 * @return the array of ngram tokens
	 */
	public String[] getNGrams(String[] tokens, int len) {
		List<String> ngrams = new ArrayList<String>();
		for (int i = 0; i < tokens.length - len + 1; i++) {
			ngrams.add(concat(tokens, i, i + len));
		}
		return ngrams.toArray(new String[0]);
	}

	/**
	 * Returns ngram tokens of given length with specified delim
	 * 
	 * @param tokens the input words
	 * @param len ngram length required
	 * @return the array of ngram tokens
	 */
	public String[] getNGrams(String[] tokens, int len, String delim) {
		List<String> ngrams = new ArrayList<String>();
		for (int i = 0; i < tokens.length - len + 1; i++) {
			ngrams.add(concat(tokens, i, i + len, delim));
		}
		return ngrams.toArray(new String[0]);
	}
	
	private String concat(char[] chars, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++)
			sb.append((i > start ? "" : "") + chars[i]);
		return sb.toString();
	}

	private String concat(String[] tokens, int start, int end) {
		return concat(tokens, start, end, " ");
	}
	
	private String concat(String[] tokens, int start, int end, String delim) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (tokens[i] != null)
				sb.append((i > start ? delim : "") + tokens[i]);
		}
		return sb.toString();
	}

	/**
	 * Returns the word subsequences of a given length
	 * @param tokens the array of words
	 * @param len required sequence length
	 * @return the array of word sequences
	 */
	public String[] getWordSubsequences(String[] tokens, int len) {
		HashSet<String> subsequences = new HashSet<String>();
		computeSubsequences("", tokens, len, subsequences);
		return subsequences.toArray(new String[0]);
	}

	private void computeSubsequences(String subsequence, String[] unigrams,
			int length, HashSet<String> subsequences) {
		if (length == 0) {
			if (!subsequence.trim().equals(""))
				subsequences.add(subsequence.trim().replaceAll(" ", "_"));
			return;
		}
		if (unigrams.length == 0) {
			if (!subsequence.trim().equals(""))
				subsequences.add(subsequence.trim().replaceAll(" ", "_"));
			return;
		}
		computeSubsequences(subsequence + " " + unigrams[0],
				Arrays.copyOfRange(unigrams, 1, unigrams.length), length - 1,
				subsequences);
		computeSubsequences(subsequence,
				Arrays.copyOfRange(unigrams, 1, unigrams.length), length,
				subsequences);
	}

	/**
	 * Converts array of words to one string with space delimitation
	 * @param the array of words
	 * @return string with words combined with space delimiation
	 */
	public String toString(String[] stringArray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < stringArray.length; i++) {
			sb.append(stringArray[i] + " ");
		}
		return sb.toString().trim();
	}
}
