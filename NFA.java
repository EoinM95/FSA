package fsa;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class NFA extends FSA implements Iterable<NFA.State>{
	
	/**
	 * @author Eoin Murphy
	 */

	
	private static final long serialVersionUID = -137935198908520649L;
	static class State{
		private Hashtable<String,ArrayList<State>> arcs;
		private boolean isFinal;
		private String name;
		public State(){
			arcs=new Hashtable<String,ArrayList<State>>();
			isFinal=false;
		}
		
		public Hashtable<String,ArrayList<State>> arcs(){
			return arcs;
		}
		
		public ArrayList<State> transition(String letter){
			return arcs.get(letter);
		}
		
		public State addArc(String letter){
			State next=new State();
			ArrayList<State> list=transition(letter);
			if(list==null){
				list=new ArrayList<State>();
			}
			list.add(next);
			arcs.put(letter,list);
			return next;
		}
		
		public State addArc(String letter,State next){
			ArrayList<State> list=transition(letter);
			if(list==null){
				list=new ArrayList<State>();
			}
			if(!list.contains(next)){
				list.add(next);
				arcs.put(letter,list);
			}
			return next;
		}

		/**
		 * @return the isFinal
		 */
		public boolean isFinal() {
			return isFinal;
		}
		
		public int hashCode(){
			return arcs.hashCode();
		}
		
		public boolean isEquivalent(State other){
			return other.arcs.keySet()
					.containsAll(this.arcs.keySet())
					&&(this.isFinal==other.isFinal);
		}
		
		/**
		 * @param isFinal the isFinal to set
		 */
		public void setFinal() {
			isFinal = true;
		}

		public void setName(String string) {
			name=string;	
		}
		
		public String getName(){
			return name;
		}
	}
	
	private State initialState;
	private ArrayList<State> states;
	private HashSet<State> finalStates;
	private HashSet<String> alphabet;
	public NFA(){
		initialState=new State();
		states=new ArrayList<State>();
		states.add(initialState);
		finalStates=new HashSet<State>();
		alphabet=new HashSet<String>();
	}
	
	public NFA(String regex){
		//TO DO
	}
	
	public void add(String word){
		State current=initialState;
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			alphabet.add(letter);
			if(current.transition(letter)==null)
				states.add(current.addArc(letter));
			ArrayList<State> nextStates=current.transition(letter);
			current=nextStates.get(nextStates.size()-1);
			if(i==length-1){
				current.setFinal();
				finalStates.add(current);
			}
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
	
	public void addEpsilonTransition(int startState, int nextState){
		State next;
		if(nextState>=states.size()){
			int limit=(nextState-(states.size()-1));
			for(int i=0;i<limit;i++){
				next=new State();
				states.add(next);
			}
		}	
		
		next=states.get(nextState);
		states.get(startState).addArc(EPSILON,next);
	}
	
	public void setFinal(int stateNumber){
		if(stateNumber<states.size()){
			states.get(stateNumber).setFinal();
			finalStates.add(states.get(stateNumber));
		}
		else
			System.out.println("Etat "+stateNumber+" n'existe pas");
	}
	
	public boolean accepts(String word){
		epsilonFree();
		return acceptsRec(word,states.get(0));
	}
	
	private boolean acceptsRec(String word,State current){
		if(word.equals(""))
			return current.isFinal();
		String letter = String.valueOf(word.charAt(0));
		ArrayList<State> transitions=current.transition(letter);
		if(transitions==null)
			return false;
		for(State state:transitions)
			if(acceptsRec(word.substring(1,word.length()),state))
				return true;
		return false;
	}
	
	public void epsilonFree(){
		for(State state:this){
			if(state.transition(EPSILON)!=null){
				ArrayList<State> intermediateStates=state.transition(EPSILON);
				for(State intermediateState:intermediateStates){
					if(intermediateState.isFinal())
						state.setFinal();
					for(String label:intermediateState.arcs().keySet()){
						for(State nextState:intermediateState.arcs().get(label))
							state.addArc(label,nextState);
					}
				}
				states.removeAll(intermediateStates);
			}
		}
	}
	

	public DFA determinise(){
		epsilonFree();
		HashSet<State> finalStatesRemaining=new HashSet<State>();
		finalStatesRemaining.addAll(finalStates);
		DFA determinised=new DFA();
		int index=0;
		for(State state:this){
			if(finalStatesRemaining.isEmpty())
				break;
			for(String label:state.arcs().keySet()){
				determiniseRec(determinised,state,state.transition(label),label,index,
						finalStatesRemaining);
			}
			if(determinised.getState(index)!=null)
				determinised.getState(index).setSeen();
			int count=0;
			while(determinised.getState(index+count)!=null
					&&determinised.getState(index+count).wasSeen())
				count++;
			index+=count;
		}	
		determinised.removeUnreachableStates();
		return determinised;
		
	}
	
	private void determiniseRec(DFA dfa,State nfaState, ArrayList<State> transitions,String letter,int index
			,HashSet<State> finalStatesRemaining){
		int count=1;
		while(dfa.getState(index+count)!=null&&dfa.getState(index+count).wasSeen())
			count++;
		dfa.addTransition(index,letter,index+count);
		ArrayList<State> tmp=new ArrayList<State>();
		tmp.addAll(transitions);
		if(transitions.contains(nfaState)){
			dfa.addTransition(index,letter,index);
			tmp.remove(nfaState);
		}
		for(State state:tmp){
			if(state.isFinal()){
				dfa.setFinal(index+count);
				finalStatesRemaining.remove(state);
			}
			for(String label:state.arcs().keySet()){
				determiniseRec(dfa,state,state.transition(label),label,index+count,finalStatesRemaining);
				count=1;
				while(dfa.getState(index+count)!=null&&dfa.getState(index+count).wasSeen())
					count++;
			}
			index+=count;
		}
		if(dfa.getState(index)!=null)
			dfa.getState(index).setSeen();
	}
	
	public int size(){
		return states.size();
	}

	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}

	@Override
	public void save(String filename,boolean overwrite) {
		DFA dfa=determinise();
		dfa.minimise();
		dfa.save(filename,overwrite);	
	}
	
	public void setStateNames() {
		int i=0;
		for(State state:this){
			state.setName(""+i++);
		}
	}
	

	@Override
	public boolean isDeterministic() {
		for(State state:this){
			Set<String> keys=state.arcs().keySet();
			if(keys.contains(EPSILON))
				return false;
			for(String label:keys){
				if(state.transition(label).size()>1)
					return false;
			}
		}
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
					if(state.transition(letterA)!=null&&state.transition(letterB)!=null){
						for(State nextA:state.transition(letterA)){
							for(State nextB:state.transition(letterB)){
								if(nextA.isEquivalent(nextB))
									return false;		
							}
						}
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
	
	@Override
	public String transitionList() {
		StringBuilder transitions=new StringBuilder();
		setStateNames();
		for(State state:this){
			for(String letter:state.arcs().keySet()){
				for(State next:state.transition(letter)){
					transitions.append(state.getName());
					transitions.append(" "+letter+" ");
					transitions.append(next.getName());
					transitions.append(FSABuilder.LS);
				}
			}
			if(state.isFinal())
				transitions.append("final "+state.getName()+FSABuilder.LS);
		}
		return transitions.toString();
	}
	
	@Override
	public FSA minimise() {
		return determinise().minimise();
	}
	
	@Override
	public FSA complete() {
		return determinise().complete();
	}

	@Override
	public boolean isEpsilonFree() {
		for(State state:this){
			if(state.arcs().keySet().contains(EPSILON))
				return false;
		}
		return true;
	}
	
	public static void main(String[] args){
		NFA f=new NFA();
		f.add("Hello");
		f.add("World");
		//System.out.println(f.accepts("Hello"));
		f.add("Hell");
		f.add("Hi");
		//System.out.println(f.accepts("World"));
		//System.out.println(f.size());
		//System.out.println(f.accepts("Hi"));
		DFA dfa= f.determinise();
		System.out.println(dfa.transitionList());
		System.out.println(dfa.accepts("Hello"));
		System.out.println(dfa.accepts("Hell"));
		System.out.println(dfa.accepts("Hi"));
		System.out.println(dfa.accepts("World"));
	}
}
