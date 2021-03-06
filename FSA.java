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
	public void addEpsilonTransition(int startState,int nextState){
		//System.out.println("Cette opération est seulement disponible pour des NFA");
	}
	abstract public void setFinal(int stateNumber);	
	abstract public boolean accepts(String word);
	abstract public int size();
	abstract public boolean isDeterministic();
	abstract public boolean isMinimal();
	abstract public boolean isComplete();
	abstract public boolean isEpsilonFree();
	public boolean save(String filename, boolean overwrite){
		StringBuilder toSave=new StringBuilder();
		toSave.append("init 0"+FSABuilder.LS);
		toSave.append(transitionList());
		return FSABuilder.write(toSave.toString(),filename,overwrite);
	}
	abstract public void setStateNames();
	abstract public String transitionList();
	abstract public FSA determinise();
	abstract public FSA minimise();
	abstract public FSA complete();
	abstract public void epsilonFree();
}
