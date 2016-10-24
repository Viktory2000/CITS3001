import java.util.*;


public class BayesAgent implements Agent{

	private Scanner scanner;
	private String players = "";
	private String spies = "";
	private String name;
	private boolean spy = false;
	private Map<Character,Integer> spyState;
	private String[] worldSpies = {"AB", "AC","AD","AE","BC","BD","BE","CD","CE"};
	
	private double[] worldProb;
	
	private String proposed="";
	private String mission="";
	private int traitors=0;
	
	private double betrayProb = 0.8;
	
	public BayesAgent(){
		spyState = new HashMap<Character, Integer>();
		worldProb = new double[9];
		
		for(int i = 0; i<9; i++){
			worldProb[i] = 0.5;			
		}
		
	}  


	/**
	* Reports the current status, inlcuding players name, the name of all players, the names of the spies (if known), the mission number and the number of failed missions
	* @param name a string consisting of a single letter, the agent's names.
	* @param players a string consisting of one letter for everyone in the game.
	* @param spies a String consisting of the latter name of each spy, if the agent is a spy, or n questions marks where n is the number of spies allocated; this should be sufficient for the agent to determine if they are a spy or not. 
	* @param mission the next mission to be launched
	* @param failures the number of failed missions
	* */
	public void get_status(String name, String players, String spies, int mission, int failures){
		this.name = name;
		this.players = players;
		this.spies = spies;
		if(spies.indexOf(name)!=-1) spy = true;
		for(char c : players.toCharArray())
		{
			spyState.put(c,0);
		}
		if(spy) //If a spy, mark which other players are spies
		{
			for(char c : spies.toCharArray())
				if(!spyState.containsKey(c))
					spyState.put(c,1);
		}
	}
  
	/**
	* Nominates a group of agents to go on a mission.
	* If the String does not correspond to a legitimate mission (<i>number</i> of distinct agents, in a String), 
	* a default nomination of the first <i>number</i> agents (in alphabetical order) will be reported, as if this was what the agent nominated.
	* @param number the number of agents to be sent on the mission
	* @return a String containing the names of all the agents in a mission
	* */
	public String do_Nominate(int number){
		String nom = name; //Always nominates self
		int n= 1;
		double temp = 0;
		String World = "";
			
		if(spy){
			while (n < number) {
				for (char c : players.toCharArray()) {

					if (nom.indexOf(c) == -1 && spyState.get(c) == 0) // Nominate
																								
					{
						nom += c;
						n++;
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < 9; i++) { // Find lowest probability world
				temp = 1;
				if (worldProb[i] <= temp && worldSpies[i].indexOf(name) == -1) {
					temp = worldProb[i];
					World = worldSpies[i]; // Lowest Probability world ie lowest probability spies
					//System.out.println("Lowest Probability World found = " + World);
				}
			}
			nom = nom + World;
		}
		//System.out.println("Nomination Complete");
		return nom;
	}

	/**
	* Provides information of a given mission.
	* @param leader the leader who proposed the mission
	* @param mission a String containing the names of all the agents in the mission 
	**/
	public void get_ProposedMission(String leader, String mission){
		this.proposed = mission;
	}

	/**
	* Gets an agents vote on the last reported mission
	* @return true, if the agent votes for the mission, false, if they vote against it.
	* */
	public boolean do_Vote() {
		
		if (proposed.indexOf(name) == -1)
			return false; // Do not approve mission not containing self
		
		int spynum = 0;
		
		for (char c : proposed.toCharArray()) // Count known spies on proposed mission
		{
			if (spyState.get(c) != 0)
				spynum++;
		}
		if (spy) {
			if (spynum == proposed.length()) // Do not approve a mission
												// containing only spies
				return false;
			else
				return true;
		} else {
			return (spynum == 0); // Only approve missions containing no known
									// spies
		}
	}

	/**
	 * Reports the votes for the previous mission
	 * 
	 * @param yays
	 *            the names of the agents who voted for the mission
	 **/
  public void get_Votes(String yays){
    
  }
  /**
   * Reports the agents being sent on a mission.
   * Should be able to be infered from tell_ProposedMission and tell_Votes, but incldued for completeness.
   * @param mission the Agents being sent on a mission
   **/
  public void get_Mission(String mission){
		this.mission = mission;
  }

  /**
   * Agent chooses to betray or not.
   * @return true if agent betrays, false otherwise
   **/
  public boolean do_Betray(){
		if(!spy)
			return false;
		int spynum = 0;
		for(char c : proposed.toCharArray()) //Count spies on mission
		{
			if(spyState.get(c) != 0)
				spynum++;
		}
		return (spynum < mission.length()); //Betray only if the mission does not consist only of spies
   }  

  /**
   * Reports the number of people who betrayed the mission
   * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
   **/
  public void get_Traitors(int traitors){
	  double[] ProbAGivW = new double[9];
	  double ProbA = 0;
	  boolean solved = false;
	  
		this.traitors = traitors;
		if(traitors == mission.length()) //If everyone betrayed, everyone must be a spy
		{
			solved = true; //As world is now known
			for(int i = 0; i<9 ; i++){
				if(worldSpies[i].equals(mission)){
					worldProb[i] = 1;
				}else{
					worldProb[i] = 0;
				}
			}
			for(char c : mission.toCharArray())
			{
				spyState.put(c,1);
			}
			
		}else if(traitors>=0 && !solved){
			for(int i = 0; i<9 ; i++){
				
				if(mission.indexOf(worldSpies[i].charAt(0)) >= 0 && mission.indexOf(worldSpies[i].charAt(1))>=0){				
					// 2 Spies on the mission in this world
					
					if(traitors == 0){
						ProbAGivW[i] = (1-betrayProb)*(1-betrayProb);
					}else if(traitors == 1){
						ProbAGivW[i] = 2*betrayProb*(1-betrayProb);
					}else{
						ProbAGivW[i] = betrayProb*betrayProb;
					}
					
				}else if(mission.indexOf(worldSpies[i].charAt(0)) >= 0 || mission.indexOf(worldSpies[i].charAt(1))>=0){
					if(traitors>1){ 	//checks if more traitors than there are spies on the mission in this world
						worldProb[i] = 0;
						ProbAGivW[i] = 0;
					}
					
					//1 spy on the mission in this world
					
					if(traitors == 0){
						ProbAGivW[i] = (1-betrayProb);
					}else if(traitors == 1){
						ProbAGivW[i] = betrayProb;
					}
					
				}else{
					if(traitors>0){ 	//checks if more traitors than there are spies on the mission in this world
						worldProb[i] = 0;
						ProbAGivW[i] = 0;
					}
					
					//0 Spies on th mission in this world

					
					if(traitors == 0){
						ProbAGivW[i] = 1;
					}

				}
				ProbA += ProbAGivW[i];
			}
			
			for(int i = 0; i<9; i++){
				worldProb[i] = ProbAGivW[i]*worldProb[i]/ProbA;
				System.out.println(worldSpies[i]+ " HAS PROBABILITY = " + worldProb[i]);
			}
			
		}
			
  }


  /**
   * Optional method to accuse other Agents of being spies. 
   * Default action should return the empty String. 
   * Convention suggests that this method only return a non-empty string when the accuser is sure that the accused is a spy.
   * Of course convention can be ignored.
   * @return a string containing the name of each accused agent. 
   * */
  public String do_Accuse(){
	  String World = "";
	  double temp = 0;
	  
		for (int i = 0; i < 9; i++) { // Find lowest probability world
			temp = 1;
			if (worldProb[i] >= temp) {
				temp = worldProb[i];
				World = worldSpies[i]; // Lowest Probability world ie lowest probability spies
				//System.out.println("Lowest Probability World found = " + World);
			}
		}
		
		return World;
  }

  /**
   * Optional method to process an accusation.
   * @param accuser the name of the agent making the accusation.
   * @param accused the names of the Agents being Accused, concatenated in a String.
   * */
  public void get_Accusation(String accuser, String accused){

  }

}
