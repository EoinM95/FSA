package fsa;
import java.io.Serializable;

public abstract class FSA implements Serializable{
	
	/**
	 * @author Eoin Murphy
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8347403257983367063L;
	

	public static final String EPSILON="#";
	public static final String HASH_SYMBOL="##";
	
	abstract public void add(String word);
	abstract public void addTransition(int startState, String letter, int nextState);
	abstract public void setFinal(int stateNumber);	
	abstract public boolean accepts(String word);
	abstract public int size();
	abstract public boolean isDeterministic();
	abstract public boolean isMinimal();
	abstract public boolean isComplete();
	abstract public void save(String filename, boolean overwrite);
	abstract public void setStateNames();
	
	

	
	
}
