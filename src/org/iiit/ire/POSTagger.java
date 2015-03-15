package org.iiit.ire;

import java.io.FileInputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public final class POSTagger extends Stemmer{
	private static final POSTagger INSTANCE = new POSTagger();
	public POSTaggerME postagger = null;

	/**
	 * Returns the object holding postagger instance
	 * @return the instance object
	 */
	public static POSTagger getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the POS tags for tokens, Synchronisation is necessary here
	 * @param tokens the input array of words
	 * @return the array of POS tags
	 */
	public synchronized String[] tag(String[] tokens) { 
		if(tokens == null || tokens.length == 0)
			return new String[0];
		
		String[] taggedTokens = this.postagger.tag(tokens);
		return taggedTokens;
	}

	/**
	 * Returns the text with POS tagged information, Synchronisation is necessary here
	 * @param text the input text
	 * @return the array of POS tags, the method may return null
	 */
	/*public synchronized String[] tag(String text) { 
		if(StringUtils.isBlank(text))
			return null;
		
		Tokenizer tokenizer = new Tokenizer();
		return tag(tokenizer.tokenize(text));
	}*/

	/**
	 * Private constructor for singleton instance
	 */
	private POSTagger() {
		try {
			FileInputStream fis = new FileInputStream("conf/model.pos");
			POSModel model = new POSModel(fis);
			this.postagger = new POSTaggerME(model);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
