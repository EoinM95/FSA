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
		public State(){
			arcs=new Hashtable<String,State>();
			isFinal=false;
		}

		public Hashtable<String,State> arcs(){
			return arcs;
		}

		public State transition(String letter){
			if(!arcs.containsKey(letter))return null;
			return arcs.get(letter);
		}

		public State addArc(String letter){
			if(transition(letter)==null){
				State next=new State();
				arcs.put(letter,next);
			}
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
					&&(this.isFinal==other.isFinal);
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
	private State errorState;
	private HashSet<String> alphabet;
	public DFA(){
		initialState=new State();
		states=new ArrayList<State>();
		states.add(initialState);
		alphabet=new HashSet<String>();
		errorState=null;
	}


	public void add(String word){
		State current=initialState;
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			alphabet.add(letter);
			if(current.transition(letter)==null)
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
			next=new State();
			states.add(next);
		}	
		else
			next=states.get(nextState);
		states.get(startState).addArc(letter,next);
	}

	

	public void setFinal(int stateNumber){
		states.get(stateNumber).setFinal();
	}

	public boolean contains(String word){
		State current=initialState;
		int length=word.length();
		for(int i=0;i<length;i++){
			String letter = String.valueOf(word.charAt(i));
			if(current.transition(letter)==null)
				return false;
			current=current.transition(letter);
		}		
		return current.isFinal();
	}

	public void complete(){
		errorState=new State();
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
		for(int x=0;x<states.size();x++){
			State state=states.get(x);
			for(int i=0;i<alpha.length-1;i++)
				for(int j=i+1;j<alpha.length;j++){
					String letterA=(String)alpha[i];
					String letterB=(String)alpha[j];
					if(state.transition(letterA).isEquivalent(state.transition(letterB))){
						state.addArc(letterB,state.transition(letterB));
						states.remove(state.transition(letterB));
					}
				}		
		}
	}

	@Override
	public int size() {
		return states.size()+(errorState==null?0:1);
	}


	@Override
	public void save(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}
	
	public static void main(String args[]){
		DFA f=new DFA();
		f.add("Hello");
		f.add("World");
		//new FSAView(f);
		System.out.println(f.contains("Hello"));
		System.out.println(f.contains("World"));
		System.out.println(f.contains("Worl"));
		System.out.println(f.contains("hi"));
		System.out.println(f.size());
		f.minimise();
		System.out.println(f.contains("Hello"));
		System.out.println(f.contains("World"));
		System.out.println(f.contains("Worl"));
		System.out.println(f.contains("hi"));
		System.out.println(f.size());
		f.add("Hello");
		System.out.println(f.contains("Hello"));
		System.out.println(f.size());
	}

}
