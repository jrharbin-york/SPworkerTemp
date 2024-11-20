package main;

import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import uk.ac.york.sesame.testing.evolutionary.EvolutionaryExpt;
import uk.ac.york.sesame.testing.evolutionary.distributed.PreInitFailed;

public class EvolutionaryRunnerTurtlebotTwoOperations {
	//	JMetal manages a population of Test models - containing a single test
	//	These models are referenced by TestModelSolution - which references a particular model
	//
	//	Some metrics can be computed from the Test model itself - not just from the computation
	//
	//	How to write back the metric values into the models at the end of the evaluation?
	
	public static void main(String [] args) {
		// TODO: this parameters need to go into the model TestCampaign
		// the nature of the evolutionary experiment and its parameters,
		// the structure of the grmamar itself
		double timingProbMut = 0.666;
		double participantProbMut = 0.333;
		double paramProbMut = 0.333;
				
		int maxIterations = 256;
		int populationSize = 16;
		int offspringSize = 16;
		//ExperimentType etype = ExperimentType.FIXED_TIME_FUZZING;
		
		// Sets the space model file and the campaign to run here
		
		// PATHS HERE
		final String spaceModelFileName = "/home/simtesting/workspace/turtleBot/models/turtleMRS-twoOperations.model";
		final String CODE_GENERATION_DIRECTORY = "/home/simtesting/workspace/turtleBot";
		final String orchestratorBasePath = "/home/simtesting/simtesting/simulationBasedTesting/uk.ac.york.sesame.testing.generator/";
		String grammarPath = "/home/simtesting/workspace/turtleSim/grammar/turtlesim-grammar.bnf";
		
		String campaignToRun = "validatePredictor";

		final boolean conditionBased = false;
		final int maxConditionDepth = 0;
		
		EvolutionaryExpt jmetalExpt;
		try {
			jmetalExpt = new EvolutionaryExpt(orchestratorBasePath, spaceModelFileName, campaignToRun, CODE_GENERATION_DIRECTORY, maxIterations, populationSize, offspringSize, conditionBased, maxConditionDepth, grammarPath);
			jmetalExpt.runExperiment();
		} catch (EolModelLoadingException e) {
			e.printStackTrace();
		} catch (PreInitFailed e) {
			e.printStackTrace();
		}
	}
}
