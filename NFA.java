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
				determiniseRec(determinised,state.transition(label),label,index,
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
		return determinised;
		
	}
	
	private void determiniseRec(DFA dfa,ArrayList<State> transitions,String letter,int index
			,HashSet<State> finalStatesRemaining){
		int count=1;
		while(dfa.getState(index+count)!=null&&dfa.getState(index+count).wasSeen())
			count++;
		dfa.addTransition(index,letter,index+count);
		int tmp=index;
		for(State state:transitions){
			if(state.isFinal()){
				dfa.setFinal(tmp+count);
				finalStatesRemaining.remove(state);
			}
			for(String label:state.arcs().keySet()){
				determiniseRec(dfa,state.transition(label),label,tmp+count,finalStatesRemaining);
				//dfa.addTransition(tmp,letter,tmp+count);
				//dfa.getState(tmp+count).setSeen();
				count=1;
				while(dfa.getState(tmp+count)!=null&&dfa.getState(tmp+count).wasSeen())
					count++;
			}
			tmp+=count;
		}
		if(dfa.getState(tmp)!=null)
			dfa.getState(tmp).setSeen();
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
		return false;
	}

	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String[] args){
		NFA f=new NFA();
		f.add("Hello");
		f.add("World");
		//System.out.println(f.accepts("Hello"));
		f.add("Hell");
		//f.add("Hi");
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

	@Override
	public String transitionList() {
		// TODO Auto-generated method stub
		return null;
	}
}
