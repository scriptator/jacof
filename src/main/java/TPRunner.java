import com.google.common.collect.Lists;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import thiagodnf.jacof.aco.AntColonySystem;
import thiagodnf.jacof.problem.Problem;
import thiagodnf.jacof.problem.tsp.TravellingSalesmanProblem;
import thiagodnf.jacof.util.ExecutionStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class TPRunner {

    /**
     * The class logger
     */
    static final Logger LOGGER = Logger.getLogger(TPRunner.class);

    public static void main(String[] args) throws ParseException, IOException {
        String folder = "src/main/resources/problems/instances_02_TSP/panama/";

        new File(folder + "sol").mkdir();
        for (String fileName : Lists.newArrayList(new File(folder).listFiles()).stream().map(file -> {
            if (file.isFile()) return file.getName();
            return null;
        }).filter(s -> s != null).collect(Collectors.toSet())) {
            runACS_TSP(folder, fileName);
        }


    }

    public static void runACS_TSP(String folder, String instance) throws IOException {
        List<String> solution = Lists.newArrayList();
        solution.add("Objective Value,Runtime (s)");
        List<String> parameter = Lists.newArrayList();

        List<String> path = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            Problem problem = new TravellingSalesmanProblem(folder + instance, true);

            AntColonySystem aco = new AntColonySystem(problem);

            aco.setNumberOfAnts(35);
            aco.setNumberOfIterations(100);
            aco.setAlpha(1);                    //Exploitation parameter, sets how the ants are attracted to pheromone concentration.
                                                //at 0 algorithms becomes greedy (random spikes) - high: dig into local optima
            aco.setBeta(10);                    //Exploration parameter, sets how the ants are more attracted to try out shorter paths.
                                                //at 0 it will keep digging into local optimum
            aco.setRho(0.1);                    //parameter for tau - see also "ACS Local Updating Rule" in paper
            aco.setOmega(0.2);                  //local updating rule: used for shuffling tours
                                                //every time an ant uses an edge, this becomes slightly less desirable (since it loses some pheromone)
                                                // if = 0: ants search in narrow neighbourhood of best previous tour
            aco.setQ0(0.9);                     //0 <= q0 <= 1 : determines the relative importance of exploitation versus exploration.
                                                //lower: more likely to be chosen by exploitation
                                                //higher: more likely to by chosen by (biased) exploration
                                                // 0.9 seems to be a good value
            //save paramter
            if (parameter.size() != 7) {
                parameter.add("Number of ants: " + aco.getNumberOfAnts());
                parameter.add("Number of iterations: " + aco.getNumberOfIterations());
                parameter.add("alpha: " + aco.getAlpha());
                parameter.add("beta: " + aco.getBeta());
                parameter.add("rho: " + aco.getRho());
                parameter.add("omega: " + aco.getOmega());
                parameter.add("q0: " + aco.getQ0());
            }
            ExecutionStats es = ExecutionStats.execute(aco, problem);
            es.printStats();
            solution.add(String.format("%s,%s", String.valueOf(es.aco.getGlobalBest().getTourLength()), String.valueOf(es.executionTime/1000)));
            path.add(Arrays.toString(es.bestSolution) + "\n\n");
            XYChart chart = QuickChart
                    .getChart("Perfomance over time", "X", "Y",
                            new String[]{"global best", "current best"},
                            DoubleStream.iterate(1, d -> d + 1).limit((aco.getCurrentBestList().size())).toArray(),
                            new double[][]{
                                    aco.getGlobalBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray(),
                                    aco.getCurrentBestList().stream().mapToDouble(ant -> ant.getTourLength()).toArray()
                            });
            BitmapEncoder.saveBitmap(chart, String.format("%s/sol/%s%d.png", folder, instance, i), BitmapEncoder.BitmapFormat.PNG);

        }
        Files.write(new File(String.format("%s/sol/%s_solution.csv", folder, instance)).toPath(), solution);
        Files.write(new File(String.format("%s/sol/%s_path.txt", folder, instance)).toPath(), path);
        Files.write(new File(String.format("%s/sol/%s_parameter.txt", folder, instance)).toPath(), parameter);
    }

}
