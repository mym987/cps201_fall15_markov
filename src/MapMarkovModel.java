import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * The model for the Markov text generation assignment. See methods for
 * details.  This model should use a brute-force algorithm for
 * generating text, i.e., the entire training text is rescanned each
 * time a new character is generated.
 */
public class MapMarkovModel extends AbstractModel {
	
	public static void main(String[] args){
		MapMarkovModel myMark = new MapMarkovModel();
		myMark.initialize(new Scanner("qwertyuiopasdfghjklzxcvbnm"));
		myMark.process("1");
		String output = myMark.makeNGram(1, 100);
	}
    
	protected String myString;
    protected Random myRandom;
    public static final int DEFAULT_COUNT = 100; // default # random letters generated
    public static final int RANDOM_SEED = 1234; 
    
    private int myK;
    private Map<String,List<String>> prevNode;
    private Map<String,List<String>> nextNode;
    private Map<String,Integer> distEOF;
    
    
    public MapMarkovModel() {
        myRandom = new Random(RANDOM_SEED);
    }
    
    /**
     * Create a new training text for this model based on the information read
     * from the scanner.
     * 
     * @param s
     *            is the source of information
     */
    public void initialize(Scanner s) {
        double start = System.currentTimeMillis();
        int count = readChars(s);
        double end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        super.messageViews("#read: " + count + " chars in: " + time + " secs");
    }
    
    /**
     * Read characters from entire file into myString
     * @param s non-null Scanner at the beginning of a file
     * @return number of characters read
     */
    protected int readChars(Scanner s) {
        myString = s.useDelimiter("\\Z").next();
        s.close();
//        try {
//			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream("a.txt")));
//			out.println(myString);
//			out.close();
//        } catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
        return myString.length();
    }
    
    /**
     * Generate N letters using an order-K markov process where the parameter is
     * a String containing K and N separated by whitespace with K first. If N is
     * missing it defaults to some value.
     */
    public void process(Object o) {
        String temp = (String) o;
        String[] nums = temp.split("\\s+");
        int k = Integer.parseInt(nums[0]);
        int maxLetters = nums.length > 1?Integer.parseInt(nums[1]):DEFAULT_COUNT;
        
        double stime = System.currentTimeMillis();
        String text = makeNGram(k, maxLetters);
        double etime = System.currentTimeMillis();
        double time = (etime - stime) / 1000.0;
        this.messageViews("time to generate: " + time +" | chars generated:" + 
        		text.length()); //For benchmarking purposes
        this.notifyViews(text);
        
    }
    
    /**
     * Generates random text that is similar to the reference text (myString).
     * 
     * @param k order of n-gram       
     * @requires k > 0
     * @param maxLetters number of characters to generate      
     * @return maxLetters of randomly selected characters based on picking
     *         representative characters that follow each k characters
     */
    protected String makeNGram(int k, int maxLetters) {
    	if(myK!=k || prevNode == null) generateMap(k);
        //Appending to StringBuilder is faster than appending to String
        StringBuilder build = new StringBuilder();
        //Pick a random starting index
        String seed = "";
        do{
        	int start = myRandom.nextInt(myString.length() - k + 1);
        	seed = myString.substring(start, start + k);
        }while(distEOF.containsKey(seed)&&prevNode.size()>100);
        
        // generate at most maxLetters characters
        for (int i = 0; i < maxLetters; i++) {
        	if(nextNode.get(seed).size()==0)return build.toString();
        	String next = "";
        	do{
            	int pick = myRandom.nextInt(nextNode.get(seed).size());
            	next = nextNode.get(seed).get(pick);
            }while(distEOF.containsKey(next) && distEOF.get(next)<maxLetters-i-1&&prevNode.size()>100);
        	
        	char ch = next.charAt(k-1);
            build.append(ch);
            seed = seed.substring(1) + ch;
        }
        return build.toString();
    }
    
    private void generateMap(int k){
    	prevNode = new HashMap<>();
    	nextNode = new HashMap<>();
    	distEOF = new HashMap<>();
    	if(myString == null) return;
    	
    	putNode(myString.substring(0,k),null,myString.substring(1, k+1));
    	for(int p=1;p< myString.length()-k;p++){
    		String curr = myString.substring(p,p+k);
    		String prev = myString.substring(p-1,p+k-1);
    		String next = myString.substring(p+1,p+k+1);
    		putNode(curr,prev,next);
    	}
    	putNode(myString.substring(myString.length()-k),myString.substring(myString.length()-k-1,myString.length()-1),null);
    	
    	String eofNode = null;
    	for(String node:nextNode.keySet()){
    		if(nextNode.get(node).isEmpty()){
    			eofNode = node;
    			break;
    		}	
    	}
    	
    	if(eofNode==null)return;
    	String curr = eofNode;
    	for(int i=0;;i++){
    		if(nextNode.get(curr).size()>1)break;
    		distEOF.put(curr, i);
    		if(prevNode.get(curr).isEmpty())break;
    		curr = prevNode.get(curr).get(0);
    	}
    	
    }
    
    private boolean containsNode(String string){
    	return prevNode.containsKey(string);
    }
    
    private void putNode(String node, String prev, String next){
    	if(!containsNode(node)){
    		prevNode.put(node, new ArrayList<>());
    		nextNode.put(node, new ArrayList<>());
    	}
    	if(prev!=null)prevNode.get(node).add(prev);
    	if(next!=null)nextNode.get(node).add(next);
    }
    
//    private class Ngram{
//    	
//    	List<Ngram> myPrev,myNext;
//    	private String mySeed;
//
//    	public Ngram(String stream, int start, int n) {
//    		mySeed = stream.substring(start, start+n);
//    		myPrev = new ArrayList<>();
//    		myNext = new ArrayList<>();
//    	}
//
//    	@Override
//    	public boolean equals(Object o) {
//    		return mySeed.equals(o.toString());
//    	}
//    	
//    	@Override
//    	public int hashCode() {
//    		return mySeed.hashCode();
//    	}
//    	
//    	@Override
//    	public String toString(){
//    		return mySeed;
//    	}
//    }
}



