package im.yuri.jcom;

import java.util.ArrayList;
import java.util.UUID;

public class DistributedTransaction {

    private Transaction[] transactions;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    private UUID id;

    public DistributedTransaction() {
        id = UUID.randomUUID();
    }

    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
        for (Transaction t : transactions) {
            t.setId(id);
        }
    }

    public ArrayList<Integer> getParticipants() {
        ArrayList<Integer> participants = new ArrayList<>();
        for(Transaction t: transactions) {
            participants.add(t.getNode());
        }
        return participants;
    }


}
