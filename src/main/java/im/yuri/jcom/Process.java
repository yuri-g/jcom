package im.yuri.jcom;

import im.yuri.jcom.util.ErrorsMap;
import im.yuri.jcom.util.OperationType;
import static im.yuri.jcom.util.Helpers.*;
import static im.yuri.jcom.util.TransactionParser.*;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import org.yaml.snakeyaml.*;

public class Process implements Runnable {

    private Channel[][] channels;
    private Resource resource;
    private Integer id;
    private Float faultProbability;
    private Queue<Object> executionQueue;
    private ErrorsMap errors;
    private Integer wastedTime;
    private DistributedTransaction currentTransaction;

    public Process(Integer id, Channel[][] channels, Float faultProbability) {
        this.id = id;
        this.channels = channels;
        this.faultProbability = faultProbability;
        errors = new ErrorsMap();
        executionQueue = new LinkedList();
        wastedTime = 0;
    }

    public void run() {
        resource = new Resource(this.id);


        //right now the node with id==0 is the only one that creates transactions
        if (this.id == 0) {
            //doesnt really do anything right now
            currentTransaction = parse("transactions.yaml");
//            currentTransaction.setParticipants(new Integer[]{0, 1});
//            Transaction[] transactions = new Transaction[2];
//            transactions[0] = generateTransaction(0, "Z", "X", 69);
//            transactions[1] = generateTransaction(1, "X", "Y", 20);
//            currentTransaction.setTransactions(transactions);
            //sending phase
            for (Transaction t : currentTransaction.getTransactions() ) {
                send(t.getNode(), t);
                logSendTransaction(t, this.id);
            }

        }

        //reading phase

        while(true) {
            if (!Thread.currentThread().isInterrupted()) {


                if (!channelIsEmpty()) {
                    for (int i = 0; i <= 1; i++) {
                        Object result = read(i);
                        if (result != null) {
                            executionQueue.add(result);
                        }
                    }
                }
                else {
                    try {
                        Thread.sleep(500);
                        if (channelIsEmpty()) {
                            Thread.sleep(1000);
                            wastedTime += 1000;
                            if (wastedTime > 5000) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
                execute(executionQueue);
            }
            else {
                System.out.println(Thread.currentThread().getId() + " is dead now.");
            }
        }

   }

    private boolean channelIsEmpty() {

        for(int i = 0; i <= 1; i++) {
            if (!channels[i][this.id].isEmpty()) {
                return false;
            }

        }
        return true;
    }


    private void execute(Queue<Object> executionQueue) {
        Iterator<Object> i = executionQueue.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (isTransaction(o)) {
                Transaction tr = (Transaction)o;
                logGetTransaction(tr, this.id);
                executeTransaction(tr);
                startVoting();
            }
            else if (isOperation(o)) {
                Operation op = (Operation) o;
                if (op.isVoteRequest())
                {
                    if (errors.any(op)){
                        vote(new Operation(OperationType.VOTE, "NO", this.id, op.getTransactionId()), op.getNode());
                        System.out.println(Thread.currentThread().getId() + ": vote: no");
                    } else {
                        System.out.println(Thread.currentThread().getId() + ": vote: yes");
                    }
                }
                else if (op.isVote()) {

                }

            }
            i.remove();
        }
        }








    private void vote(Operation answer, Integer node) {
        send(node, answer);
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




    private boolean executeTransaction(Transaction transaction) {
        for (Operation o : transaction.getOperations()) {
            OperationType type = o.getType();
            switch (type) {
                case READ:
                    System.out.println("Reading value of " + o.getProperty() + ": " + this.resource.getValue(o.getProperty()) + " [process " + id + "]");
                    break;
                case WRITE:
                    if (this.resource.setValue(o.getProperty(), o.getValue(), faultProbability)) {
                        System.out.println("Writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                    }
                    else {
                        System.out.println("Error while writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                        errors.put(transaction.getId().toString(), true);
                    }
                    break;
            }
        }
        return true;
    }

    private void startVoting() {

        //if currentTransaction != null it means that this node initiated this transaction
        if (currentTransaction != null) {
            for (Integer node: currentTransaction.getParticipants()) {
                System.out.println("Sending vote requests to:");
                System.out.println(currentTransaction.getParticipants());
                send(node, new Operation(OperationType.VOTE_REQUEST, currentTransaction.getId().toString(), this.id, currentTransaction.getId().toString()));
            }
        }
    }



    private Transaction generateTransaction(Integer node, String property1, String property2, Integer value) {
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
}

