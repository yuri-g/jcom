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
        System.out.println("Got transaction " + transaction.getId().toString().substring(0, 4) + " from node " + transaction.getNode() + " [process " + id + "]");
    }
    public static void logSendTransaction(Transaction transaction, Integer id) {
        System.out.println("Sent transaction " + transaction.getId().toString().substring(0, 4) + " to node " + transaction.getNode() + " [process " + id + "]");
    }
//    public static void logGetVote()
}
