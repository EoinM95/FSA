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
		private HashSet<State> previousStates;
		public State(){
			arcs=new Hashtable<String,ArrayList<State>>();
			isFinal=false;
			previousStates=new HashSet<State>();
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
		
		public Hashtable<String,ArrayList<State>> arcs(){
			return arcs;
		}
		
		public void addToPrevious(State state){
			if(!state.equals(this))
				previousStates.add(state);
		}
		
		public HashSet<State> previousStates(){
			return previousStates;
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
		public boolean isEquivalent(State other){
			if(this.isFinal&&other.isFinal){
				if(!this.arcs.keySet().equals(other.arcs.keySet()))
					return false;
				for(String letter:arcs.keySet())
					for(State stateA:this.transition(letter))
						for(State stateB:other.transition(letter))
							if(!stateA.isEquivalentRec(stateB))
								return false;
				return true;
			}
			return isEquivalentRec(other);
		}
		
		public boolean isEquivalentRec(State other){
			if(this.isFinal||other.isFinal)
				return isFinal==other.isFinal;
			if(!this.arcs.keySet().equals(other.arcs.keySet()))
				return false;
			for(String letter:arcs.keySet())
				for(State stateA:this.transition(letter))
					for(State stateB:other.transition(letter))
						if(!stateA.isEquivalentRec(stateB))
							return false;
			return true;
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
		this();
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
			State next=nextStates.get(nextStates.size()-1);
			next.addToPrevious(current);
			current=next;
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
		next.addToPrevious(states.get(startState));
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
		next.addToPrevious(states.get(startState));
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
	/**
	 * Rendre l'automate epsilon-libre
	 * 	-Pour chaque état
	 * 		-Si il a des transitions epilson
	 * 			-ajouter des transitions alphabétique vers ces états
	 * 			-si l'état intermédiate est final, rendre cet état final
	 * 			-ajouter des transitions des états qui précede l'état intérmediate à cet état
	 * 	-Supprimer tous les états non-nécessaire		
	 */
	public void epsilonFree(){
		for(State state:this){
			if(state.transition(EPSILON)!=null){
				ArrayList<State> intermediateStates=state.transition(EPSILON);
				for(State intermediateState:intermediateStates){
					if(intermediateState.isFinal()){
						state.setFinal();
						finalStates.add(state);
						finalStates.remove(intermediateState);
					}	
					for(String label:intermediateState.arcs().keySet()){
						for(State nextState:intermediateState.transition(label))
							state.addArc(label,nextState);
					}
					state.arcs().remove(EPSILON);
				}
			}
		}
	}
	

	/**
	 * Converter cet automate en DFA
	 * 	-Pour chaque état:
	 * 		-Pour chaque symbol qu'il reconnait
	 * 			-Ajouter une transition au DFA vers un nouvel état
	 * 			-Refaire l'opération entiere sur la liste des états state.transition(letter)
	 * 	-Arreter apres avoir vu tous les états finaux du NFA  
	 */
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
		if(finalStatesRemaining.isEmpty())
			return;
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
		int n=states.size();
		for(int i=0;i<n-1;i++){
			for(int j=i+1;j<n;j++){
				if(states.get(i).isEquivalent(states.get(j)))
						return false;
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
					if(letter.equals(EPSILON))
						transitions.append(" eps ");
					else
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
		FSA minimised=dfa.minimise();
		//System.out.println(minimised.transitionList());
		System.out.println(minimised.accepts("Hello"));
		System.out.println(minimised.accepts("Hell"));
		System.out.println(minimised.accepts("Hi"));
		System.out.println(minimised.accepts("World"));
	}
}
