import com.google.common.collect.Lists;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import thiagodnf.jacof.aco.AntColonySystem;
import thiagodnf.jacof.problem.Problem;
import thiagodnf.jacof.problem.kp.KnapsackProblem;
import thiagodnf.jacof.util.ExecutionStats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class KPRunner {

    /**
     * The class logger
     */
    static final Logger LOGGER = Logger.getLogger(KPRunner.class);

    public static void main(String[] args) throws ParseException, IOException {
        Executor executor = Executors.newFixedThreadPool(3);
        String[] folders = new String[]{
                "src/main/resources/problems/instances_01_KP/large_scale/"
        };
        for (String folder : folders) {
            new File(folder + "sol").mkdir();
            for (String fileName : Lists.newArrayList(new File(folder).listFiles()).stream().map(file -> {
                if (file.isFile()) return file.getName();
                return null;
            }).filter(s -> s != null).collect(Collectors.toSet())) {
                executor.execute(() -> {
                    try {
                        runACS_KP(folder, fileName);
                    } catch (IOException e) {
                        LOGGER.error(e);
                    }
                });
            }
        }

    }

    public static void runACS_KP(String folder, String instance) throws IOException {
        List<String> solution = Lists.newArrayList();
        solution.add("Objective Value,Runtime (s)");
        List<String> parameter = Lists.newArrayList();
        List<String> path = new LinkedList<>();

        for (int i = 0; i < 5; i++) {
            Problem problem = new KnapsackProblem(folder + instance);

            AntColonySystem aco = new AntColonySystem(problem);

            aco.setNumberOfAnts(50);
            aco.setNumberOfIterations(1);
            aco.setAlpha(1);
            aco.setBeta(10);
            aco.setRho(0.1);
            aco.setOmega(0.2);
            aco.setQ0(0.9);

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

            //new SwingWrapper(chart).displayChart();

            BitmapEncoder.saveBitmap(chart, String.format("%s/sol/%s%d.png", folder, instance, i), BitmapEncoder.BitmapFormat.PNG);

        }
        Files.write(new File(String.format("%s/sol/%s_solution.csv", folder, instance)).toPath(), solution);
        Files.write(new File(String.format("%s/sol/%s_path.txt", folder, instance)).toPath(), path);
        Files.write(new File(String.format("%s/sol/%s_parameter.txt", folder, instance)).toPath(), parameter);
    }

}
