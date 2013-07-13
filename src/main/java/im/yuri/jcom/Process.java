package im.yuri.jcom;

import im.yuri.jcom.util.ErrorsMap;
import im.yuri.jcom.util.OperationType;
import static im.yuri.jcom.util.Helpers.*;
import static im.yuri.jcom.util.TransactionParser.*;

import java.io.IOException;
import java.util.*;

public class Process implements Runnable {

    private Channel[][] channels;
    private Resource resource;
    private Integer id;
    private Float faultProbability;
    private ArrayList<Object> executionQueue;
    private ErrorsMap errors;
    private Integer wastedTime;
    private DistributedTransaction currentTransaction;
    private ArrayList<String> votes;
    private ArrayList<Resource> resources;

    public Process(Integer id, Channel[][] channels, Float faultProbability) {
        this.id = id;
        this.channels = channels;
        this.faultProbability = faultProbability;
        errors = new ErrorsMap();
        executionQueue = new ArrayList<>();
        wastedTime = 0;
        votes = new ArrayList<>();
        resources = new ArrayList<>();
    }

    public void run() {
        //right now the node with id==0 is the only one that creates transactions
        if (this.id == 0) {
            currentTransaction = parse("transactions.yaml");
            currentTransaction.setNode(this.id);
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
                if (currentTransaction != null) {
                    if(checkVotes()) {
                        Operation o = new Operation();
                        if(decideCommit()) {
                            o.setType(OperationType.COMMIT);
                            broadcast(o, currentTransaction.getParticipants());
                            System.out.println("got all YES, commit!");

                        }
                        else
                        {
                            o.setType(OperationType.ABORT);
                            broadcast(o, currentTransaction.getParticipants());
                            System.out.println("got NO, aborting!");
                        }
                        currentTransaction = null;
                    }

                }


            }
            else {
                System.out.println(Thread.currentThread().getId() + " is dead now.");
            }
        }

   }

    private void broadcast(Object o, ArrayList<Integer> participants) {
        for(Integer node: participants) {
            send(node, o);
        }
    }

    private boolean decideCommit() {
        for (String vote: votes) {
            if (vote.equals("NO")) {
                return false;
            }
        }
        return true;
    }

    private boolean checkVotes() {
        return currentTransaction.getParticipants().size() == votes.size();
    }

    private boolean channelIsEmpty() {

        for(int i = 0; i <= 1; i++) {
            if (!channels[i][this.id].isEmpty()) {
                return false;
            }

        }
        return true;
    }


    private void execute(ArrayList<Object> executionQueue) {
        Iterator<Object> i = executionQueue.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            System.out.println(o);
            if (isTransaction(o)) {
                Transaction tr = (Transaction)o;
                logGetTransaction(tr, this.id);
                try {
                    executeTransaction(tr);
                }
                catch (IOException e) {

                }
                if (tr.getParentNode().equals(this.id)) {
                    startVoting();
                }

            }
            else if (isOperation(o)) {
                Operation op = (Operation) o;
                if (op.isVoteRequest())
                {
                    if (errors.any(op)){
                        vote(new Operation(OperationType.VOTE, "NO", this.id, op.getTransactionId()), op.getNode());
                        System.out.println(Thread.currentThread().getId()+ ": vote: no");
                    } else {
                        vote(new Operation(OperationType.VOTE, "YES", this.id, op.getTransactionId()), op.getNode());
                        System.out.println(Thread.currentThread().getId() + ": vote: yes");
                    }
                }
                else if (op.isVote()) {
                    System.out.println("vote?");
                    votes.add(op.getProperty());
                    System.out.println(Thread.currentThread().getId() + ": got " + op.getProperty() + " from " + op.getNode());
                    System.out.println(votes);
                }
                else if(op.isCommit()) {
                    System.out.println(Thread.currentThread().getId() + ": commiting!");
                    for(Resource r: resources) {
                        r.saveResource();
                    }
                    //commit!
                }

            }
            i.remove();
        }
        
     }



    private boolean executeTransaction(Transaction transaction) throws IOException {
        for (Operation o : transaction.getOperations()) {
            OperationType type = o.getType();
            Resource res = new Resource();
            switch (type) {
                case READ:
                    res = new Resource(o.getResource() + ".yaml");
                    System.out.println(Thread.currentThread().getId() + ": reading value of " + o.getProperty() + ": " + res.getValue(o.getProperty()) + " from " + res.getFileName());
                    break;
                case WRITE:
                    res = new Resource(o.getResource() + ".yaml");
                    if (res.setValue(o.getProperty(), o.getValue(), faultProbability)) {
                        System.out.println("Writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                    }
                    else {
                        System.out.println("Error while writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                        errors.put(transaction.getId().toString());
                    }
                    resources.add(res);
                    break;
            }
        }
        return true;
    }


    private void vote(Operation answer, Integer node) {
        send(node, answer);
    }

    private Object read(Integer node) {

        Object result = channels[node][this.id].pull();
        if (result != null) {
            if (result.getClass() == Transaction.class) {
                return result;
            }
            else if (result.getClass() == Operation.class) {
                return result;
            }
        }
        return null;


    }

    private void send(Integer node, Object o) {
        this.channels[this.id][node].push(o);
    }


    private void startVoting() {

        //if currentTransaction != null it means that this node initiated this transaction
            for (Integer node: currentTransaction.getParticipants()) {
                System.out.println("Sending vote requests to:");
                System.out.println(currentTransaction.getParticipants());
                send(node, new Operation(OperationType.VOTE_REQUEST, currentTransaction.getId().toString(), this.id, currentTransaction.getId().toString()));
            }
    }
}