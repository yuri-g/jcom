package im.yuri.jcom.util;

import im.yuri.jcom.Operation;
import im.yuri.jcom.Transaction;

public class Helpers {
    public static boolean isTransaction(Object result) {
        return result.getClass() == Transaction.class;
    }
    public static boolean isOperation(Object result) {
        return result.getClass() == Operation.class;
    }
    public static void logGetTransaction(Transaction transaction, Integer id) {
        System.out.println(Thread.currentThread().getId() + ": got transaction " + transaction.getId().toString().substring(0, 4) + " from node " + transaction.getNode());
    }
    public static void logSendTransaction(Transaction transaction, Integer id) {
        System.out.println(Thread.currentThread().getId() + ": sent transaction " + transaction.getId().toString().substring(0, 4) + " to node " + transaction.getNode());
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
