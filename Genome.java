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
	
	public double goodNomValue;
	public double goodVoteValue;
	public double goodMissionValue;
	public double badNomValue;
	public double badVoteValue;
	public double badMissionValue;
	
	
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
		
		goodNomValue = r.nextDouble();
		goodVoteValue = r.nextDouble();
		goodMissionValue = r.nextDouble();
		badNomValue = r.nextDouble();
		badVoteValue = r.nextDouble();
		badMissionValue = r.nextDouble();
		
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
		
		goodNomValue = (r.nextBoolean()) ? parent1.goodNomValue : parent2.goodNomValue;
		goodVoteValue = (r.nextBoolean()) ? parent1.goodVoteValue : parent2.goodVoteValue;
		goodMissionValue = (r.nextBoolean()) ? parent1.goodMissionValue : parent2.goodMissionValue;
		badNomValue = (r.nextBoolean()) ? parent1.badNomValue : parent2.badNomValue;
		badVoteValue = (r.nextBoolean()) ? parent1.badVoteValue : parent2.badVoteValue;
		badMissionValue = (r.nextBoolean()) ? parent1.badMissionValue : parent2.badMissionValue;
		
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
		str.append("\ngoodNomValue = "+goodNomValue);
		str.append("\ngoodVoteValue = "+goodVoteValue);
		str.append("\ngoodMissionValue = "+goodMissionValue);
		str.append("\nbadNomValue = "+badNomValue);
		str.append("\nbadVoteValue = "+badVoteValue);
		str.append("\nbadMissionValue = "+badMissionValue);
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
		
		goodNomValue += d*(r.nextDouble()-0.5);
		goodVoteValue += d*(r.nextDouble()-0.5);
		goodMissionValue += d*(r.nextDouble()-0.5);
		badNomValue += d*(r.nextDouble()-0.5);
		badVoteValue += d*(r.nextDouble()-0.5);
		badMissionValue += d*(r.nextDouble()-0.5);
		
		
		//Clamp values to 0-1
		multSpyVoteProb = Math.max(0, Math.min(1, multSpyVoteProb));
		noSpyVoteProb = Math.max(0, Math.min(1, noSpyVoteProb));
		onlySpyBetrayProb = Math.max(0, Math.min(1, onlySpyBetrayProb));
		multSpyBetrayProb = Math.max(0, Math.min(1, multSpyBetrayProb));
		allSpyBetrayProb = Math.max(0, Math.min(1, allSpyBetrayProb));
		
		goodNomValue = Math.max(0, Math.min(1, goodNomValue));
		goodVoteValue = Math.max(0, Math.min(1, goodVoteValue));
		goodMissionValue = Math.max(0, Math.min(1, goodMissionValue));
		badNomValue = Math.max(0, Math.min(1, badNomValue));
		badVoteValue = Math.max(0, Math.min(1, badVoteValue));
		badMissionValue = Math.max(0, Math.min(1, badMissionValue));
		
	}
	
	public int compareTo(Genome other)
	{
		return -Integer.compare(fitness,other.fitness);
	}
	
}