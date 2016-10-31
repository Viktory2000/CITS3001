import java.util.*;

class Genome implements Comparable<Genome>
{
	public int id;
	public int fitness;
	public double multSpyVoteProb;
	public double noSpyVoteProb;
	public double onlySpyBetrayProb;
	public double multSpyBetrayProb;
	public double allSpyBetrayProb;
	
	public double teamThreshold;
	
	
	public Genome(int id) //Create a random genome
	{
		this.id = id;
		fitness = 0;
		Random r = new Random();
		multSpyVoteProb = r.nextDouble();
		noSpyVoteProb = r.nextDouble();
		onlySpyBetrayProb = r.nextDouble();
		multSpyBetrayProb = r.nextDouble();
		allSpyBetrayProb = r.nextDouble();
		
		teamThreshold = r.nextDouble();
	}
	
	public Genome(int id, Genome parent1, Genome parent2) //Create a genome by random crossover between parents
	{
		this.id = id;
		fitness = 0;
		Random r = new Random();
		multSpyVoteProb = (r.nextBoolean()) ? parent1.multSpyVoteProb : parent2.multSpyVoteProb ;
		noSpyVoteProb = (r.nextBoolean()) ? parent1.noSpyVoteProb : parent2.noSpyVoteProb;
		onlySpyBetrayProb = (r.nextBoolean()) ? parent1.onlySpyBetrayProb : parent2.onlySpyBetrayProb;
		multSpyBetrayProb = (r.nextBoolean()) ? parent1.multSpyBetrayProb : parent2.multSpyBetrayProb;
		allSpyBetrayProb = (r.nextBoolean()) ? parent1.allSpyBetrayProb : parent2.allSpyBetrayProb;
		
		teamThreshold = (r.nextBoolean()) ? parent1.teamThreshold : parent2.teamThreshold;
	}
	
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append("id = "+id);
		str.append("\nmultSpyVoteProb = "+multSpyVoteProb);
		str.append("\nnoSpyVoteProb = "+noSpyVoteProb);
		str.append("\nonlySpyBetrayProb = "+onlySpyBetrayProb);
		str.append("\nmultSpyBetrayProb = "+multSpyBetrayProb);
		str.append("\nallSpyBetrayProb = "+allSpyBetrayProb);
		str.append("\nteamThreshold = "+teamThreshold);
		str.append("\nFitness = "+fitness);
		return str.toString();
	}
	
	public void mutate(double d)
	{
		Random r = new Random();
		multSpyVoteProb += d*(r.nextDouble()-0.5);
		noSpyVoteProb += d*(r.nextDouble()-0.5);
		onlySpyBetrayProb += d*(r.nextDouble()-0.5);
		multSpyBetrayProb += d*(r.nextDouble()-0.5);
		allSpyBetrayProb += d*(r.nextDouble()-0.5);
		
		teamThreshold += d*(r.nextDouble()-0.5);
		
		//Clamp values to 0-1
		multSpyVoteProb = Math.max(0, Math.min(1, multSpyVoteProb));
		noSpyVoteProb = Math.max(0, Math.min(1, noSpyVoteProb));
		onlySpyBetrayProb = Math.max(0, Math.min(1, onlySpyBetrayProb));
		multSpyBetrayProb = Math.max(0, Math.min(1, multSpyBetrayProb));
		allSpyBetrayProb = Math.max(0, Math.min(1, allSpyBetrayProb));
		
		teamThreshold = Math.max(0, Math.min(1, teamThreshold));
	}
	
	public int compareTo(Genome other)
	{
		return -Integer.compare(fitness,other.fitness);
	}
	
}