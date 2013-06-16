package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.Random;

public class Process implements Runnable {


    private Channel[][] channels;
    private Resource resource;
    private Integer id;
    private Float faultProbability;
    private HashMap<UUID, Boolean> errors;
    private DistributedTransaction currentTransaction;
    public Process(Integer id, Channel[][] channels, Float faultProbability) {
      this.id = id;
      this.channels = channels;
      this.faultProbability = faultProbability;
        errors = new HashMap<UUID, Boolean>();
    }

    public void run() {
        resource = new Resource(this.id);
        if (this.id == 0) {
            currentTransaction = new DistributedTransaction();
            currentTransaction.setParticipants(new Integer[]{0, 1});
            Transaction[] transactions = new Transaction[2];
            transactions[0] = GenerateTransaction(0, "Z", "X", 69);
            transactions[1] = GenerateTransaction(1, "X","Y", 20);
            currentTransaction.setTransactions(transactions);
            //sending phase
            for (Transaction t : currentTransaction.getTransactions() ) {
                send(t.getNode(), t);
                System.out.println("Sent transaction " + t.getId().toString().substring(0, 4) + " to node " + t.getNode() + " [process " + id + "]");
            }

        }

        //reading phase
        while(true) {
            for (int i = 0; i <= 1; i++) {
                Object result = read(i);
                if (result != null) {
                        if (result.getClass() == Transaction.class) {
                            Transaction tr = (Transaction)result;
                            ((Transaction) result).getId();
                            System.out.println("Got transaction " + tr.getId().toString().substring(0, 4) + " from node " + tr.getNode() + " [process " + id + "]");
                            execute(tr);
                            startVoting();
                        }

                    }

                }


            }
    }

    private void startVoting() {

        //if currentTransaction != null it means that this node initiated this transaction
        if (currentTransaction != null) {
            for (Integer node: currentTransaction.getParticipants()) {
                send(node, new Operation(OperationType.VOTE_REQUEST, " "));
            }
        }
    }

    private boolean execute(Transaction transaction) {
        for (Operation o : transaction.getOperations()) {
            OperationType type = o.getType();
            switch (type) {
                case READ:
                    System.out.println("Reading value of " + o.getProperty() + ": " + this.resource.getValue(o.getProperty()) + " [process " + id + "]");
                    break;
                case WRITE:
                    if (resource.setValue(o.getProperty(), o.getValue(), faultProbability)) {
                        System.out.println("Writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                    }
                    else {
                        System.out.println("Error while writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                        errors.put(transaction.getId(), true);
                    }
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

    private Object read(Integer node) {

        Object result = channels[node][this.id].pull();
        if (result != null) {
            if (result.getClass() == Transaction.class) {
                return (Transaction)result;
            }
            else if (result.getClass() == Operation.class) {
                return (Operation)result;
            }
        }
        return null;


    }

    private void send(Integer node, Object o) {
        this.channels[this.id][node].push(o);
    }

}
