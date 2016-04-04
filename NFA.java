package fsa;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;


public class NFA extends FSA implements Iterable<NFA.State>{
	
	/**
	 * @author Eoin Murphy
	 */

	
	private static final long serialVersionUID = -137935198908520649L;
	static class State{
		private Hashtable<String,ArrayList<State>> arcs;
		private boolean isFinal;
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
			next=new State();
			states.add(next);
		}	
		else
			next=states.get(nextState);
		states.get(startState).addArc(letter,next);
	}
	
	public void addEpsilonTransition(int startState, int nextState){
		State next;
		if(nextState>=states.size())
			next=new State();
		else
			next=states.get(nextState);
		states.get(startState).addArc(EPSILON,next);
	}
	
	public void setFinal(int stateNumber){
		states.get(stateNumber).setFinal();
	}
	
	public boolean accepts(String word){
		determinise();
		State current=initialState;
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			if(current.transition(letter)==null)
				return false;
			current=current.transition(letter).get(0);
		}		
		return current.isFinal();
	}
	
	public void epsilonFree(){
		for(State state:this){
			if(state.transition(EPSILON)!=null){
				ArrayList<State> intermediateStates=state.transition(EPSILON);
				for(State intermediateState:intermediateStates){
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
			Hashtable<String,ArrayList<State>> arcs=state.arcs();
			ArrayList<State> statesList=new ArrayList<State>();
			for(String label:arcs.keySet()){
				if(arcs.get(label).size()>1){
					
				}
				
			}
		}
		return determinised;
		
	}
	
	
	public int size(){
		return states.size();
	}

	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}

	@Override
	public void save(String filename) {
		// TODO Auto-generated method stub
		
	}
	
	
}
