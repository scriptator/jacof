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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class TPRunner {

    /**
     * The class logger
     */
    static final Logger LOGGER = Logger.getLogger(TPRunner.class);

    public static void main(String[] args) throws ParseException, IOException {
        String folder = "src/main/resources/problems/instances_02_TSP/ireland/";

        new File(folder + "sol").mkdir();
        for (String fileName : Lists.newArrayList(new File(folder).listFiles()).stream().map(file -> {
            if (file.isFile()) return file.getName();
            return null;
        }).filter(s -> s != null).collect(Collectors.toSet())) {
            runACS_TSP(folder, fileName);
        }


    }

    public static void runACS_TSP(String folder, String instance) throws IOException {
        List<String> results = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            Problem problem = new TravellingSalesmanProblem(folder + instance, true);

            AntColonySystem aco = new AntColonySystem(problem);

            aco.setNumberOfAnts(2);
            aco.setNumberOfIterations(100);
            aco.setAlpha(0.5);
            aco.setBeta(0.5);
            aco.setRho(0);
            aco.setOmega(0.2);
            aco.setQ0(0.9);


            ExecutionStats es = ExecutionStats.execute(aco, problem);
            es.printStats();
            results.add(String.format("%f;%f", es.aco.getGlobalBest().getTourLength(), es.executionTime));

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
        Files.write(new File(String.format("%s/sol/%s.sol", folder, instance)).toPath(), results);
    }

}
