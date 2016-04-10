package fsa;
import java.util.ArrayList;
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
		private boolean seen;
		public State(){
			arcs=new Hashtable<String,State>();
			isFinal=false;
			seen=false;
			error=false;
			name=null;
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
		
		public boolean equals(Object o){
			if(!(o instanceof State))
				return false;
			State other = (State)o;
			if(other==this)
				return true;
			if(name!=null&&other.name!=null)
				return name.equals(other.name);
			return false;
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
		
		public boolean wasSeen(){
			return seen;
		}
		
		public void setSeen(){
			seen=true;
		}
		
		public boolean isEquivalent(State other){
			if(this.isFinal&&other.isFinal){
				if(!this.arcs.keySet().equals(other.arcs.keySet()))
					return false;
				for(String letterA:arcs.keySet())
					if(!this.transition(letterA).isEquivalentRec(other.transition(letterA)))
						return false;
				return true;
			}
			return isEquivalentRec(other);
		}
		
		public boolean isEquivalentRec(State other){
			if(this.error||other.error)
				return error==other.error;
			if(this.isFinal||other.isFinal)
				return isFinal==other.isFinal;
			if(!this.arcs.keySet().equals(other.arcs.keySet()))
				return false;
			for(String letterA:arcs.keySet())
				if(!this.transition(letterA).isEquivalentRec(other.transition(letterA)))
					return false;
			return true;
		}

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

	/**
	 * @param word, ajouté un mot entier à l'automate
	 */
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

	/**
	 * Ajoute une transition entre les états spécifié sur une lettre
	 * @param startState
	 * @param letter, la lettre
	 * @param nextState
	 */ 
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

	
	/**
	 * 
	 */
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

	public FSA complete(){
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
		return this;
	}

	public FSA minimise(){
		complete();
		//setStateNames();
		Hashtable<State,ArrayList<State>> combinableStates=new Hashtable<State,ArrayList<State>>();
		int n=states.size();
		for(int i=0;i<n-1;i++){
			boolean alreadySeen=false;
			for(ArrayList<State> values:combinableStates.values()){
				if(values.contains(states.get(i))){
					alreadySeen=true;
					break;
				}	
			}
			if(!alreadySeen){
				ArrayList<State> equivalentStates= new ArrayList<State>();
				for(int j=i+1;j<n;j++){
					if((states.get(i).isFinal()==states.get(j).isFinal())&&
							states.get(i).isEquivalent(states.get(j)))
						equivalentStates.add(states.get(j));
				}
				combinableStates.put(states.get(i),equivalentStates);
			}
		}
		if(!combinableStates.isEmpty()){
			/*for(State state:combinableStates.keySet()){
				ArrayList<State> equivalentStates=combinableStates.get(state);
				for(State equiv:equivalentStates){
					System.out.println(state.name+"+"+equiv.name);
				}
			}*/
			Hashtable<ArrayList<State>,State> replacements=new Hashtable<ArrayList<State>,State>();
			for(State state:combinableStates.keySet()){
				replacements.put(combinableStates.get(state),state);
			}
			for(State state:this){
				for(String label:state.arcs().keySet()){
					State next=state.transition(label);
					for(ArrayList<State> equivalents:replacements.keySet())
						if(equivalents.contains(next)){
							state.addArc(label,replacements.get(equivalents));
						}
							
				}
			}
			for(ArrayList<State> equivalents:replacements.keySet())
				states.removeAll(equivalents);
		}	
		//removeUnreachableStates();
		return this;
	}
	
	protected void removeUnreachableStates(){
		HashSet<State> reachableStates=new HashSet<State>();
		reachableStates.add(states.get(0));
		for(State state:this){
			for(String letter:state.arcs().keySet()){
				reachableStates.add(state.transition(letter));
			}
		}
		states.retainAll(reachableStates);
	}
	
	@Override
	public int size() {
		return states.size()+(errorState==null?0:1);
	}

	
	@Override
	public String transitionList(){
		StringBuilder transitions=new StringBuilder();
		setStateNames();
		for(State state:this){
			for(String letter:state.arcs().keySet()){
				transitions.append(state.getName());
				transitions.append(" "+letter+" ");
				transitions.append(state.transition(letter).getName());
				transitions.append(FSABuilder.LS);
			}
			if(state.isFinal())
				transitions.append("final "+state.getName()+FSABuilder.LS);
		}
		return transitions.toString();
	}
	
	@Override
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
		Object[] alpha=alphabet.toArray();
		for(State state:this){
			for(int i=0;i<alpha.length-1;i++)
				for(int j=i+1;j<alpha.length;j++){
					String letterA=(String)alpha[i];
					String letterB=(String)alpha[j];
					if(state.transition(letterA).isEquivalent(state.transition(letterB))){
						return false;
					}
				}		
		}
		return true;
	}


	@Override
	public boolean isComplete() {
		for(State state:this)
			if(!state.arcs.keySet().containsAll(alphabet))
				return false;
		return true;
	}
	
	protected State getState(int index){
		if(index>=states.size()) return null;
		return states.get(index);
	}
	
	protected void addTransition(State a, String letter, State b){
		if(states.contains(a)&&states.contains(b)){
			a.addArc(letter,b);
		}
	}
	
	@Override
	public void epsilonFree() {
		return;
	}
	
	@Override
	public DFA determinise() {
		return this;
	}
	
	@Override
	public boolean isEpsilonFree() {
		return true;
	}
	
 	public static void main(String args[]){
		DFA f=new DFA();
		f.add("Hello");
		f.add("World");
		//System.out.println(f.accepts("Hello"));
		//System.out.println(f.accepts("World"));
		System.out.println(f.size());
		//System.out.println(f.accepts("Hello"));
		f.minimise();
		//System.out.println(f.transitionList());
		System.out.println(f.accepts("Hello"));
		System.out.println(f.accepts("World"));
		System.out.println(f.size());
		f.add("Hello");
		System.out.println(f.accepts("Hello"));
		System.out.println(f.size());
	}
}
