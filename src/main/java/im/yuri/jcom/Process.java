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

    //communication channel
    private Channel[][] channels;
    //id of the thread
    private Integer id;
    //probability to fail while writing
    private final Float faultProbability;
    //list of operations that thread has to execute
    private ArrayList<Object> executionQueue;
    //encountered errors
    private ErrorsMap errors;
    //time wasted by thread
    private Integer wastedTime;
    //time spent waiting for response
    private Integer waitingTime;
    //current distributed transaction
    private DistributedTransaction currentTransaction;
    //all received votes
    private ArrayList<String> votes;
    //resources that thread is using
    private ArrayList<Resource> resources;
    //total number of threads
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
    }

    public void run() {
        //check if there are any transactions for this thread
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
            //send vote requests and wait for answers
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
                            //add operation from the channel to execution queue
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



    //executing operations from execution queue
    private void execute(ArrayList<Object> executionQueue) {
        Iterator<Object> i = executionQueue.iterator();
        //iterate over the queue
        while (i.hasNext()) {
            Object o = i.next();
            //check if thread received transaction
            if (isTransaction(o)) {
                Transaction tr = (Transaction)o;
                logGetTransaction(tr, this.id);
                try {
                    //execute it
                    executeTransaction(tr);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }


            }
            //if received object is operation
            else if (isOperation(o)) {
                Operation op = (Operation) o;
                //is it a vote request?
                if (op.isVoteRequest())
                {
                    //check if there were errors while executing transaction
                    if (errors.any(op)){
                        //if yes, vote NO
                        vote(new Operation(OperationType.VOTE, "NO", this.id, op.getTransactionId()), op.getNode());
                        logVotes("NO", op.getTransactionId().substring(0, 4), this.id);
                    } else {
                        //otherwise vote YES
                        vote(new Operation(OperationType.VOTE, "YES", this.id, op.getTransactionId()), op.getNode());
                        logVotes("YES", op.getTransactionId().substring(0, 4), this.id);
                    }
                }

                //if it's a vote
                else if (op.isVote()) {
                    //save vote
                    votes.add(op.getProperty());
                    logGetVote(op.getProperty(), op.getNode(), this.id);

                }

                //is it request to commit
                else if(op.isCommit()) {
                    logCommit(op.getTransactionId(), this.id);
                    //commit, i.e. save all changes to resources (files)
                    for(Resource r: resources) {
                        if (r.getTransactionId().equals(op.getTransactionId())) {
                            r.saveResource();
                        }

                    }
                    //commit!
                }
                //otherwise, do nothing
                else if (op.isAbort()) {
                    //make current transaction null
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

    //voting method, just sends answer to specified node
    private void vote(Operation answer, Integer node) {
        send(node, answer);
    }


    //read something from communication channel
    private Object read(Integer node) {
        Object result = channels[node][this.id].pull();
        if (result != null) {
//            if (result.getClass() == Transaction.class) {
//                return result;
//            }
//            else if (result.getClass() == Operation.class) {
//                return result;
//            }
            return result;
        }
        return null;


    }


    //send something to specified node
    private void send(Integer node, Object o) {
        this.channels[this.id][node].push(o);
    }



    //broadcast vote requests to participants
    private void startVoting() {
        logSendVoteRequests(currentTransaction.getParticipants(), this.id);
        for (Integer node: currentTransaction.getParticipants()) {
            send(node, new Operation(OperationType.VOTE_REQUEST, currentTransaction.getId().toString(), this.id, currentTransaction.getId().toString()));
        }
    }
}