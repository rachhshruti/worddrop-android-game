package se.android.worddrop;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This class contains methods that help to determine when words are formed on
 * the game grid
 * @author Soumya Achar
 */
public class WordTrie {

	private TriElement root;
	private TriElement t;
	
	/**
	 * Builds the trie from a list of all words stored in words.txt file
	 * @param context used to get words.txt asset
	 * @param myTree wordtrie object
	 */
	public void buildTrie(Context context, WordTrie myTree){

		try  {
			AssetManager assetmgr = context.getAssets();
			InputStream is = assetmgr.open("words.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line;
		    while ((line = br.readLine()) != null) {
		       myTree.insertElement(line.toLowerCase());
		    }
		} catch (Exception e) {
			System.out.println("Exception!"+e.getMessage());
		}

	}
	
	/**
	 * Initializes an empty root element for the trie
	 */
	public WordTrie() {
		root = new TriElement();
	}
	
	/**
	 * Inserts element into trie given a word
	 * @param word word to be inserted into the trie
	 */
	public void insertElement(String word) {
		HashMap<Character,TriElement> hashRange = root.myHash;
		
		for(int i=0; i<word.length(); i++){
	            char c = word.charAt(i);
	 
	            TriElement t;
	            if(hashRange.containsKey(c)){
	                    t = hashRange.get(c);
	            }else{
	                t = new TriElement(c);
	                hashRange.put(c, t);
	            }
	 
	            hashRange = t.myHash;
	 
	            //set leaf node
	            if(i==word.length()-1)
	                t.isLeaf = true;    
	        }
	    } 
	
	/**
	 * Searches if the word is present in the trie
	 * @param word word to be searched
	 * @return true if word is found, false otherwise
	 */
	public boolean searchWord(String word){
		int flag=findPossibleWords(word);
		if(t!=null){
			if (flag ==0 & t.isLeaf==true ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Finds if the word exists in the trie
	 * @param word word to be found
	 * @return 0 when word is found, 1 otherwise
	 */
	public int findPossibleWords(String word){
		int flag=0;
		HashMap<Character, TriElement> myRange = root.myHash;
		t = root;
		
		for (int i=0; i<word.length() ; i++){
			if(myRange.containsKey(word.charAt(i))){
				t = myRange.get(word.charAt(i));
				myRange = t.myHash;
			}else {
				flag = 1;
			}
			
		}
		return flag;		
	}
}
