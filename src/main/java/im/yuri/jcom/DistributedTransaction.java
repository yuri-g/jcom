package im.yuri.jcom;

import java.util.ArrayList;
import java.util.UUID;


//distributed transaction class
public class DistributedTransaction {


    //transactions that make up the distributed transaction
    private Transaction[] transactions;
    //unique ID of the transaction
    private UUID id;
    private Integer node;


    public DistributedTransaction() {
        id = UUID.randomUUID();
    }

    public Integer getNode() {
        return node;
    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    //set the id of the thread that initialized the distributed transaction to the transactions
    public void setNode(Integer node) {
        for (Transaction t: transactions) {
            t.setParentNode(node);
        }
    }



    public Transaction[] getTransactions() {
        return transactions;
    }


    //asssign transactions to this distributed transaction,
    //used in parsing
    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
        for (Transaction t : transactions) {
            t.setId(id);
        }
    }


    //get participants of the distributed transaction
    public ArrayList<Integer> getParticipants() {
        ArrayList<Integer> participants = new ArrayList<>();
        for(Transaction t: transactions) {
            participants.add(t.getNode());
        }
        return participants;
    }


}
