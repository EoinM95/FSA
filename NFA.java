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
	public NFA(){
		initialState=new State();
		states=new ArrayList<State>();
		states.add(initialState);
	}
	
	public NFA(String regex){
		//TO DO
	}
	
	public void add(String word){
		State current=initialState;
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			if(current.transition(letter)==null)
				states.add(current.addArc(letter));
			ArrayList<State> nextStates=current.transition(letter);
			current=nextStates.get(nextStates.size()-1);
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
		if(stateNumber<states.size())
			states.get(stateNumber).setFinal();
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
		DFA determinised=new DFA();
		int index=0;
		for(State state:this){
			if(state.isFinal())
				determinised.setFinal(index);
			for(String label:state.arcs().keySet()){
				determiniseRec(determinised,state.transition(label),label,index);
			}
			index++;
		}	
		//int index=0;
		//DFA.State dfaState= new DFA.State();
		//HashSet<DFA.State> newStates=new HashSet<DFA.State>();
		/*for(State state:this){
			DFA.State stateSet=null;
			for(String label:state.arcs().keySet()){
				stateSet=determinised.mergeNFAStates(state.transition(label));
				dfaState.addArc(label,stateSet);
				newStates.add(stateSet);
			}
			determinised.addState(dfaState,index++);
			dfaState=stateSet;
		}*/	
		return determinised;
		
	}
	
	private void determiniseRec(DFA dfa,ArrayList<State> transitions,String letter,int index){
		dfa.addTransition(index,letter,index+1);
		for(State state:transitions){
			for(String label:state.arcs().keySet()){
				//dfa.addTransition(index,label,index+1);
				determiniseRec(dfa,state.transition(label),label,index+1);
				index++;
			}
		}
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
		//f.add("Hell");
		//f.add("Hi");
		//System.out.println(f.accepts("World"));
		//System.out.println(f.size());
		DFA dfa= f.determinise();
		dfa.save("",false);
		System.out.println(dfa.accepts("Hello"));
		//System.out.println(dfa.accepts("Hell"));
		//System.out.println(dfa.accepts("Hi"));
		System.out.println(dfa.accepts("World"));
	}
}
