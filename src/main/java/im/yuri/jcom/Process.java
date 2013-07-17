package im.yuri.jcom;

import im.yuri.jcom.util.ErrorsMap;
import im.yuri.jcom.util.OperationType;
import static im.yuri.jcom.util.Helpers.*;
import static im.yuri.jcom.util.Helpers.logWriteError;
import static im.yuri.jcom.util.TransactionParser.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Process implements Runnable {

    private Channel[][] channels;
    private Integer id;
    private Long threadId;
    private final Float faultProbability;
    private ArrayList<Object> executionQueue;
    private ErrorsMap errors;
    private Integer wastedTime;
    private Integer waitingTime;
    private DistributedTransaction currentTransaction;
    private ArrayList<String> votes;
    private ArrayList<Resource> resources;
    private Integer nodesCount;

    public Process(Integer id, Channel[][] channels, Float faultProbability, Integer nodesCount) {
        this.id = id;
        this.channels = channels;
        this.faultProbability = faultProbability;
        this.nodesCount = nodesCount;
        errors = new ErrorsMap();
        executionQueue = new ArrayList<>();
        wastedTime = 0;
        waitingTime = 0;
        votes = new ArrayList<>();
        resources = new ArrayList<>();
        this.threadId = Thread.currentThread().getId();
    }

    public void run() {
        //right now the node with id==0 is the only one that creates transactions
        try {
            currentTransaction = parse("node " + this.id + " transactions.yaml");
        } catch (FileNotFoundException e) {
            currentTransaction = null;
        }
        //if this node has any transactions to initialize, do so
        if (currentTransaction != null) {
            currentTransaction.setNode(this.id);
            //sending phase
            //broadcast the transaction to participants
            for (Transaction t : currentTransaction.getTransactions() ) {
                send(t.getNode(), t);
                logSendTransaction(t, this.id);
            }
            startVoting();
        }



        //reading phase
        while(true) {
            //checking if the thread is still alive
            if (!Thread.currentThread().isInterrupted()) {
                //check if thread has any messages from others
                if (!channelIsEmpty()) {
                    //if has, read them and add to execution queue
                    for (int i = 0; i < nodesCount; i++) {
                        Object result = read(i);
                        if (result != null) {
                            executionQueue.add(result);
                        }
                    }
                }
                //otherwise - wait
                else {
                    try {
                        //fist, wait fo 500ms
                        Thread.sleep(500);
                        //if channel is still empty, wait of another 500ms
                        if (channelIsEmpty()) {
                            Thread.sleep(500);
                            //increase wasted time variable
                            wastedTime += 500;
                            //if it's larger than 5000, interrupt thread, because it's doing nothing
                            if (wastedTime > 5000) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
                //execute the operations in executionQueue
                execute(executionQueue);
                //if this thread has any transactions in process
                if (currentTransaction != null) {
                    Operation o = new Operation();
                    //check if thread has any votes from others
                    if(checkVotes()) {
                        //if has, check if there is a NO vote
                        if(decideCommit()) {
                            //if there is no NO vote, broadcast COMMIT message to all participant
                            o.setType(OperationType.COMMIT);
                            o.setTransactionId(currentTransaction.getId().toString().substring(0, 4));
                            broadcast(o, currentTransaction.getParticipants());
                            logInitCommit(currentTransaction.getId().toString().substring(0, 4), this.id);
                        }
                        else
                        {
                            //if there is a NO vote, broadcast ABORT to all participant
                            o.setType(OperationType.ABORT);

                            broadcast(o, currentTransaction.getParticipants());
                            logAbort(this.id);

                        }
                        //transaction is finished, so make it null
                        currentTransaction = null;
                    }
                    //no votes arrived, so thread should wait for them
                    else {
                        //increase waiting time
                        waitingTime += 500;
                        logWaiting(this.id);
                        if (waitingTime == 5000) {
                            //if thread didn't receive any votes in 5 seconds, broadcast ABORT
                            o.setType(OperationType.ABORT);
                            broadcast(o, currentTransaction.getParticipants());
                            currentTransaction = null;
                        }

                    }

                }


            }
            else {
                System.exit(0);
            }
        }

   }


    private void execute(ArrayList<Object> executionQueue) {
        Iterator<Object> i = executionQueue.iterator();
        while (i.hasNext()) {

            Object o = i.next();
            if (isTransaction(o)) {
                Transaction tr = (Transaction)o;
//                String what = ((Transaction) o).getNode() + "/" + ((Transaction) o).getParentNode() + "(node " + this.id + ")";
//                System.out.println(what);
                logGetTransaction(tr, this.id);
                try {
                    executeTransaction(tr);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else if (isOperation(o)) {
                Operation op = (Operation) o;
                if (op.isVoteRequest())
                {
                    if (errors.any(op)){
                        vote(new Operation(OperationType.VOTE, "NO", this.id, op.getTransactionId()), op.getNode());
                        logVotes("NO", op.getTransactionId().substring(0, 4), this.id);
                    } else {
                        vote(new Operation(OperationType.VOTE, "YES", this.id, op.getTransactionId()), op.getNode());
                        logVotes("YES", op.getTransactionId().substring(0, 4), this.id);
                    }
                }
                else if (op.isVote()) {
                    votes.add(op.getProperty());
                    logGetVote(op.getProperty(), op.getNode(), this.id);

                }
                else if(op.isCommit()) {
                    logCommit(op.getTransactionId(), this.id);
                    for(Resource r: resources) {
                        if (r.getTransactionId().equals(op.getTransactionId())) {
                            r.saveResource();
                        }

                    }
                    //commit!
                }
                else if (op.isAbort()) {
                    currentTransaction = null;
                }

            }
            i.remove();
        }

    }


    //Execute Transaction method
    private boolean executeTransaction(Transaction transaction) throws IOException {
        //iterate over all operations of transaction and
        //execute them
        for (Operation o : transaction.getOperations()) {
            //check the type of operation
            OperationType type = o.getType();
            Resource res = new Resource(o.getResource() + ".yaml");
            switch (type) {
                //if READ, then open needed resource and read the value
                case READ:
                    logReading(o.getProperty(), res, this.id);
                    break;
                //if WRITE, open needed resource and try to write the value
                //can fail! depends on the faultProbability constant
                case WRITE:
                    if (res.setValue(o.getProperty(), o.getValue(), faultProbability)) {
                        logWriting(o, this.id);
                    }
                    else {
                        logWriteError(o, this.id);
                        errors.put(transaction.getId().toString());
                    }

                    break;
            }
            res.setTransactionId(transaction.getId().toString().substring(0, 4));
            resources.add(res);
        }
        return true;
    }



    //method to broadcast objects (Operations, Transactions) to the set of nodes (participants)
    private void broadcast(Object o, ArrayList<Integer> participants) {
        for(Integer node: participants) {
            send(node, o);
        }
    }


    //method to check if there is a NO vote received
    private boolean decideCommit() {
        for (String vote: votes) {
            if (vote.equals("NO")) {
                return false;
            }
        }
        return true;
    }

    //check if thread received all the votes needed
    private boolean checkVotes() {
        return currentTransaction.getParticipants().size() == votes.size();
    }


    //check if the channel is empty
    private boolean channelIsEmpty() {
        for(int i = 0; i < nodesCount; i++) {
            if (!channels[i][this.id].isEmpty()) {
                return false;
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
        logSendVoteRequests(currentTransaction.getParticipants(), this.id);
        for (Integer node: currentTransaction.getParticipants()) {
            send(node, new Operation(OperationType.VOTE_REQUEST, currentTransaction.getId().toString(), this.id, currentTransaction.getId().toString()));
        }
    }
}