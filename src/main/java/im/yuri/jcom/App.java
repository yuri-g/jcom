package im.yuri.jcom;

import java.util.concurrent.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App
{
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        Integer nodesCount = 3;
        Float faultProbability = .0f;
        try {
            //parsing of the command line arguments
            Options opt = new Options();
            opt.addOption("f", true, "Fault probability");
            opt.addOption("c", false, "Number of nodes, default = 3");
            BasicParser parser = new BasicParser();
            CommandLine cli = parser.parse(opt, args);
            faultProbability = Float.parseFloat(cli.getOptionValue("f"));
            nodesCount = Integer.parseInt(cli.getOptionValue("c", "3"));
        } catch (ParseException e) {
            e.printStackTrace();
        }



        //creating of the communication channel
        Channel[][] channels = new Channel[nodesCount][nodesCount];
        for (int i = 0; i < nodesCount; i++) {
            for (int j = 0; j < nodesCount; j++) {
                channels[i][j] = new Channel();
            }
        }
        //start the threads
        ExecutorService executor = Executors.newFixedThreadPool(nodesCount);
        for (int i = 0; i < nodesCount; i++) {
            executor.submit(new Process(i, channels, faultProbability, nodesCount));
        }


    }
}
