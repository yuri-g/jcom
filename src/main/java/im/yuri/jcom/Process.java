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

        // todo:
        // write generator of transactions
        DistributedTransaction d = new DistributedTransaction();
        Transaction[] transactions = new Transaction[2];
        transactions[0] = GenerateTransaction(0, "Z", "X", 69);
        transactions[1] = GenerateTransaction(1, "X","Y", 20);
        d.setTransactions(transactions);

        //writing phase
        for (int i = 0; i <= 1; i++) {

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

    private void GenerateTransacation1() {
        Transaction t1 = new Transaction();
        t1.setNode(1);
        Operation op1 = new Operation();
        op1.setType(OperationType.READ);
        op1.setProperty("X");
        Operation op2 = new Operation();
        op2.setType(OperationType.WRITE);
        op2.setProperty("Y");
        op2.setValue(20);
        Operation[] ops = new Operation[2];
        ops[0] = op1;
        ops[1] = op2;
        t1.setOperations(ops);
    }

    private Operation read(Integer node) {
        return channels[node][this.id].pull();
    }

    private void send(Integer node, Operation operation) {
        this.channels[this.id][node].push(operation);
    }

}
