import java.io.IOException;
import java.util.stream.DoubleStream;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import thiagodnf.jacof.aco.AntColonySystem;
import thiagodnf.jacof.problem.Problem;
import thiagodnf.jacof.problem.kp.KnapsackProblem;
import thiagodnf.jacof.problem.tsp.TravellingSalesmanProblem;
import thiagodnf.jacof.util.ExecutionStats;

public class ACSRunner {

	/** The class logger*/
	static final Logger LOGGER = Logger.getLogger(ACSRunner.class);
	
	public static void main(String[] args) throws ParseException, IOException {

		String instance = "src/main/resources/problems/instances_01_KP/large_scale/knapPI_1_100_1000_1";

		Problem problem = new KnapsackProblem(instance);

		AntColonySystem aco = new AntColonySystem(problem);

		aco.setNumberOfAnts(20);
		aco.setNumberOfIterations(100);
		aco.setAlpha(1.0);
		aco.setBeta(2.0);
		aco.setRho(0.1);
		aco.setOmega(0.1);
		aco.setQ0(0.9);


		ExecutionStats es = ExecutionStats.execute(aco, problem);
		es.printStats();
		double[] times = DoubleStream.iterate(1, d -> d + 1).limit((aco.getCurrentBestList().size())).toArray();
		double[] x1Data = aco.getGlobalBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray();
		double[] x2Data = aco.getCurrentBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray();

		XYChart chart = QuickChart
				.getChart("Perfomance over time", "X", "Y",
						new String[]{"global best","current best"},
						times,
						new double[][] {
								aco.getGlobalBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray(),
								aco.getCurrentBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray()
				});

		new SwingWrapper(chart).displayChart();

	}

}
