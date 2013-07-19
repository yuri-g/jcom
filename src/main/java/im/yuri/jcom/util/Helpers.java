package im.yuri.jcom.util;

import im.yuri.jcom.Operation;
import im.yuri.jcom.Transaction;
import im.yuri.jcom.Resource;

import java.util.ArrayList;


//helpers class
public class Helpers {

    //check if received object is transaction
    public static boolean isTransaction(Object result) {
        return result.getClass() == Transaction.class;
    }

    //check if received object is operation
    public static boolean isOperation(Object result) {
        return result.getClass() == Operation.class;
    }

    //methods to log different information
    public static void logGetTransaction(Transaction transaction, Integer currentNode) {
        System.out.println("Node " + currentNode + ": got transaction " + transaction.getId().toString().substring(0, 4) + " from node " + transaction.getParentNode());
    }
    public static void logSendTransaction(Transaction transaction, Integer currentNode) {
        System.out.println("Node " + currentNode + ": sent transaction " + transaction.getId().toString().substring(0, 4) + " to node " + transaction.getNode());
    }
    public static void logSendVoteRequests(ArrayList participants, Integer currentNode) {
        System.out.println("Node " + currentNode +  ": sending vote requests to: \n" + participants);
    }

    public static void logVotes(String result, String id, Integer currentNode) {
        System.out.println("Node " + currentNode +  ": votes " + result + " (transaction " + id + ")");
    }

    public static void logGetVote(String result, Integer node, Integer currentNode) {
        System.out.println("Node " + currentNode + ": got " + result + " from " + node);
    }

    public static void logCommit(String id, Integer currentNode) {
        System.out.println("Node " + currentNode + ": committing! (transaction " + id + ")");
    }

    public static void logReading(String property, Resource res, Integer currentNode) {
        System.out.println("Node " + currentNode +  ": reading value of " + property + ": " + res.getValue(property)+ " from " + res.getFileName());
    }

    public static void logWriting(Operation o, Integer currentNode) {
        System.out.println("Node " + currentNode +  ": writing " + o.getValue() + " to " + o.getProperty());
    }

    public static void logWriteError(Operation o, Integer currentNode) {
        System.out.println("Node " + currentNode + ": error while writing " + o.getValue() + " to " + o.getProperty());
    }

    public static void logInitCommit(String id, Integer currentNode) {
        System.out.println("Node " + currentNode + ": got all YES, commit! (transaction " + id + ")");
    }

    public static void logAbort(Integer currentNode) {
        System.out.println("Node " + currentNode + ": got NO, aborting!");
    }

    public static void logWaiting(Integer currentNode) {
        System.out.println("Node " + currentNode +  ": waiting for votes...");
    }


    //determine operation type
    public static OperationType getOperationType(String type) {
        OperationType operationType = null;
        switch (type) {
            case "write":
                operationType = OperationType.WRITE;
                break;
            case "read":
                operationType = OperationType.READ;
                break;
            case "vote":
                operationType = OperationType.VOTE;
                break;
            case "vote_request":
                operationType = OperationType.VOTE_REQUEST;
                break;
        }
        return operationType;

    }
}
