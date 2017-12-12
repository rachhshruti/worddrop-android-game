package se.android.worddrop;
import java.util.HashMap;

/**
 * This class stores information about the trie that is created
 * to determine when words are formed
 * @author Soumya Achar
 */
public class TriElement {

	char letter;
	HashMap<Character, TriElement> myHash = new HashMap<Character, TriElement>();
	boolean isLeaf;
	
	public TriElement() { }
	
	/**
	 * Initializes letter for the trie element
	 * @param c
	 */
	public TriElement(char c) {
		this.letter = c;
	}
		
}
