package fsa;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class DFA extends FSA implements Iterable<DFA.State>{
	/**
	 * @author Eoin Murphy
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static class State{
		private Hashtable<String,State> arcs;
		private boolean isFinal;
		private String name;
		private boolean error;
		public State(){
			arcs=new Hashtable<String,State>();
			isFinal=false;
			error=false;
		}
		
		public void setName(String n){
			name=n;
		}
		
		public String getName(){
			return name;
		}
		
		public Hashtable<String,State> arcs(){
			return arcs;
		}
		
		public int hashCode(){
			return arcs.hashCode();
		}

		public State transition(String letter){
			if(!arcs.containsKey(letter))return null;
			return arcs.get(letter);
		}

		public State addArc(String letter){
			State next=new State();
			arcs.put(letter,next);
			return transition(letter);
		}

		public State addArc(String letter,State next){
			arcs.put(letter,next);
			return next;
		}

		/**
		 * @return the isFinal
		 */
		public boolean isFinal() {
			return isFinal;
		}
		
		public boolean isEquivalent(State other){
			return other.arcs.keySet()
					.containsAll(this.arcs.keySet())
					&&(this.isFinal==other.isFinal)
					&&(this.error==other.error);
		}

		/**
		 * @param isFinal the isFinal to set
		 */
		public void setFinal() {
			isFinal = true;
		}

		public void setError(boolean b) {
			error=b;
		}
	}
	
	private ArrayList<State> states;
	private State errorState;
	private HashSet<String> alphabet;
	public DFA(){
		State initialState=new State();
		states=new ArrayList<State>();
		states.add(initialState);
		alphabet=new HashSet<String>();
		errorState=null;
	}


	public void add(String word){
		State current=states.get(0);
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			alphabet.add(letter);
			if(current.transition(letter)==null||current.transition(letter)==errorState)
				states.add(current.addArc(letter));
			State nextState=current.transition(letter);
			current=nextState;
			if(i==length-1)
				current.setFinal();
		}
	}

	public void addTransition(int startState, String letter, int nextState){
		State next;
		if(nextState>=states.size()){
			int limit=(nextState-(states.size()-1));
			for(int i=0;i<limit;i++){
				next=new State();
				states.add(next);
			}
		}	
		next=states.get(nextState);
		states.get(startState).addArc(letter,next);
		alphabet.add(letter);
	}

	

	public void setFinal(int stateNumber){
		if(stateNumber<states.size())
			states.get(stateNumber).setFinal();
		else
			System.out.println("Etat "+stateNumber+" n'existe pas");
	}

	public boolean accepts(String word){
		State current=states.get(0);
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			State next=current.transition(letter);
			if(next==null||next==errorState)
				return false;
			current=next;
		}		
		return current.isFinal();
	}

	public void complete(){
		errorState=new State();
		errorState.setError(true);
		for(State state:this){
			Set<String> keySet=state.arcs.keySet();
			if(!(keySet.containsAll(alphabet))){
				Set<String> missingLetters=new HashSet<String>();
				missingLetters.addAll(alphabet);
				missingLetters.removeAll(keySet);
				for(String letter:missingLetters){
					state.addArc(letter,errorState);
				}
			}
		}
	}

	public void minimise(){
		complete();
		Object[] alpha=alphabet.toArray();
		ArrayList<State> temp=new ArrayList<State>();
		temp.addAll(states);
		int n=states.size();
		for(int x=0;x<n;x++){
			State state=temp.get(x);
			for(int i=0;i<alpha.length-1;i++)
				for(int j=i+1;j<alpha.length;j++){
					String letterA=(String)alpha[i];
					String letterB=(String)alpha[j];
					if(state.transition(letterA).isEquivalent(state.transition(letterB))){
						states.remove(state.transition(letterB));
						state.addArc(letterB,state.transition(letterA));
					}
				}		
		}
		
	}

	@Override
	public int size() {
		return states.size()+(errorState==null?0:1);
	}


	@Override
	public void save(String filename, boolean overwrite) {
		StringBuilder toSave=new StringBuilder();
		toSave.append("init 0"+FSABuilder.LS);
		setStateNames();
		for(State state:this){
			for(String letter:state.arcs().keySet()){
				toSave.append(state.getName());
				toSave.append(" "+letter+" ");
				toSave.append(state.transition(letter).getName());
				toSave.append(FSABuilder.LS);
			}
			if(state.isFinal())
				toSave.append("final "+state.getName()+FSABuilder.LS);
		}
		System.out.println(toSave);
		//FSABuilder.write(toSave.toString(),filename,overwrite);
	}

	public void setStateNames() {
		int i=0;
		for(State state:this){
			state.setName(""+i++);
		}
		if(errorState!=null)
			errorState.setName("Err");
		
	}


	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}
	


	@Override
	public boolean isDeterministic() {
		return true;
	}


	@Override
	public boolean isMinimal() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isComplete() {
		for(State state:this)
			if(!state.arcs.keySet().containsAll(alphabet))
				return false;
		return true;
	}

	/*protected void mergeAndLinkNFAStates(State state,String letter,ArrayList<NFA.State> nfaStates){
		State next=mergeNFAStates(nfaStates);
		state.addArc(letter,next);
		
	}*/
	
	
	protected State mergeNFAStates(ArrayList<NFA.State> nfaStates){
			State stateSet=new State();
			for(NFA.State nfaState:nfaStates){
				if(nfaState.isFinal())
					stateSet.setFinal();
				for(String label:nfaState.arcs().keySet()){
					if(nfaState.transition(label).size()==1)
						states.add(stateSet.addArc(label));
					else
						states.add(stateSet.addArc(label,mergeNFAStates(nfaState.transition(label))));
				}
			}
			//states.add(stateSet);
			return stateSet;
	}
	
	protected State getState(int index){
		if(index>=states.size()) return null;
		return states.get(index);
	}
	
	protected void addState(State state, int index){
		if(states.contains(state)) return;
		if(index>=states.size())
			states.add(state);
		else
			states.set(index,state);
	}
	
 	public static void main(String args[]){
		DFA f=new DFA();
		f.add("Hello");
		//f.add("World");
		//new FSAView(f);
		System.out.println(f.accepts("Hello"));
		//System.out.println(f.accepts("World"));
		//System.out.println(f.accepts("Worl"));
		//System.out.println(f.accepts("hi"));
		//System.out.println(f.size());
		System.out.println(f.accepts("Hello"));
		f.minimise();
		System.out.println(f.accepts("Hello"));
		//System.out.println(f.accepts("World"));
		//System.out.println(f.accepts("Worl"));
		//System.out.println(f.accepts("hi"));
		System.out.println(f.size());
		//f.save("",false);
		f.add("Hello");
		//f.save("",false);
		System.out.println(f.accepts("Hello"));
		System.out.println(f.size());
	}


	public void addAll(Collection<State> newStates) {
		states.addAll(newStates);
	}
	
}
