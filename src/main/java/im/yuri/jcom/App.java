package im.yuri.jcom;


import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;

public class App
{
    public static Channel[][] channels;
    public static Process[] processes;
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
//        Float faultProbability = Float.parseFloat(args[0]);
        //todo:
        // remove this!
        Float faultProbability = .5f;
        channels = new Channel[5][5];
        processes = new Process[5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                channels[i][j] = new Channel();
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);
//        executor.setKeepAliveTime(4, TimeUnit.SECONDS);
        for (int i = 0; i < 2; i++) {
//            processes[i] = new Process(i, channels, faultProbability);
            executor.submit(new Process(i, channels, faultProbability));
//            executor.
//            executor.submit()



             //            executor.submit(new Process(i, channels, faultProbability)).get(10, TimeUnit.SECONDS);
//            (new Thread(processes[i])).start();
        }

    }
}
