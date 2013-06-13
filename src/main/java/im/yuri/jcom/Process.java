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
    private Float faultProbability;
    public Process(Integer id, Channel[][] channels, Float faultProbability) {
      this.id = id;
      this.channels = channels;
      this.faultProbability = faultProbability;
    }

    public void run() {
        resource = new Resource(this.id);
        Random randomGenerator = new Random();

        // todo:
        // write generator of transactions
        if (this.id == 0) {
            DistributedTransaction d = new DistributedTransaction();
            Transaction[] transactions = new Transaction[2];
            transactions[0] = GenerateTransaction(0, "Z", "X", 69);
            transactions[1] = GenerateTransaction(1, "X","Y", 20);
            d.setTransactions(transactions);
            //sending phase
            for (Transaction t : d.getTransactions() ) {
                send(t.getNode(), t);
                System.out.println("Sent transaction " + t.getId().toString().substring(0, 4) + " to node " + t.getNode() + " [process " + id + "]");
            }

        }

        //reading phase
        while(true) {
            for (int i = 0; i <= 1; i++) {
                Transaction result = read(i);
                if (result != null) {
                    System.out.println("Got transaction " + result.getId().toString().substring(0, 4) + " from node " + result.getNode() + " [process " + id + "]");
                    execute(result.getOperations());
                }

            }
        }


    }

    private boolean execute(Operation[] operations) {
        for (Operation o : operations) {
            OperationType type = o.getType();
            switch (type) {
                case READ:

                    System.out.println("Reading value of " + o.getProperty() + ": " + this.resource.getValue(o.getProperty()) + " [process " + id + "]");
                    break;
                case WRITE:
                    System.out.println("Writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                    break;
            }
        }


        return true;
    }

    private Transaction GenerateTransaction(Integer node, String property1, String property2, Integer value) {
        Transaction t = new Transaction();
        t.setNode(node);
        Operation op1 = new Operation();
        op1.setType(OperationType.READ);
        op1.setProperty(property1);
        Operation op2 = new Operation();
        op2.setType(OperationType.WRITE);
        op2.setProperty(property2);
        op2.setValue(value);
        Operation[] ops = new Operation[2];
        ops[0] = op1;
        ops[1] = op2;
        t.setOperations(ops);
        return t;
    }

    private Transaction read(Integer node) {
        return channels[node][this.id].pull();
    }

    private void send(Integer node, Transaction transaction) {
        this.channels[this.id][node].push(transaction);
    }

}
