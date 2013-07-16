package im.yuri.jcom.util;

import im.yuri.jcom.Operation;
import im.yuri.jcom.Transaction;
import im.yuri.jcom.Resource;

import java.util.ArrayList;

public class Helpers {
    public static boolean isTransaction(Object result) {
        return result.getClass() == Transaction.class;
    }
    public static boolean isOperation(Object result) {
        return result.getClass() == Operation.class;
    }
    public static void logGetTransaction(Transaction transaction) {
        System.out.println(currentThread() + ": got transaction " + transaction.getId().toString().substring(0, 4) + " from node " + transaction.getNode());
    }
    public static void logSendTransaction(Transaction transaction) {
        System.out.println(currentThread() + ": sent transaction " + transaction.getId().toString().substring(0, 4) + " to node " + transaction.getNode());
    }
    public static void logSendVoteRequests(ArrayList participants) {
        System.out.println(currentThread() + ": sending vote requests to: \n" + participants);
    }

    public static void logVotes(String result, String id) {
        System.out.println(currentThread() + ": votes " + result + " (transaction " + id + ")");
    }

    public static void logGetVote(String result, Integer node) {
        System.out.println(currentThread() + ": got " + result + " from " + node);
    }

    public static void logCommit(String id) {
        System.out.println(currentThread() + ": committing! (transaction " + id + ")");
    }

    public static void logReading(String property, Resource res) {
        System.out.println(currentThread() + ": reading value of " + property + ": " + res.getValue(property)+ " from " + res.getFileName());
    }


    public static void logWriting(Operation o) {
        System.out.println(currentThread()+ ": writing " + o.getValue() + " to " + o.getProperty());
    }

    public static void logWriteError(Operation o) {
        System.out.println(currentThread() + ": error while writing " + o.getValue() + " to " + o.getProperty());
    }

    public static void logInitCommit(String id) {
        System.out.println(currentThread() + ": got all YES, commit! (transaction " + id + ")");
    }

    public static void logAbort() {
        System.out.println(currentThread() + ": got NO, aborting!");
    }

    public static void logWaiting() {
        System.out.println(currentThread() + ": waiting for votes...");
    }

    private static String currentThread() {
        return "Thread: " + Thread.currentThread().getId();
    }



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
//    public static void logGetVote()
}
