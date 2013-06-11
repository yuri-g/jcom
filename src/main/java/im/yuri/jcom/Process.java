package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.Random;

public class Process implements Runnable {


    private Channel[][] channels;
    private Resource resource;
    private Integer id;
    public Process(Integer id, Channel[][] channels) {
      this.id = id;
      this.channels = channels;
    }

    public void run() {
      resource = new Resource(this.id);
      Random randomGenerator = new Random();
        //writing phase
        Operation op = new Operation();
        for (int i = 0; i <= 1; i++) {
            //writing to random channels 5 times
            Integer chNumber = randomGenerator.nextInt(2);
            if (id != chNumber) {
                op.setType(OperationType.READ);
                op.setValue("X");
                send(chNumber, op);
                System.out.println("Process " + id + " pushed " + op.getType() + " to " + chNumber + " channel");


            }

        }
        //reading phase
        for (int i = 0; i <= 1; i++) {
            if (id != i) {
                Operation result = read(i);
                if (result != null) {
                    System.out.println("Process " + id + " got " + result.getType() + " from " + i);
                }


            }

        }
    }

    private Operation read(Integer node) {
        return channels[node][this.id].pull();
    }

    private void send(Integer node, Operation operation) {
        this.channels[this.id][node].push(operation);
    }

}
