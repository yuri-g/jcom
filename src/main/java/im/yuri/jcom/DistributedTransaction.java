package im.yuri.jcom;

import java.util.UUID;

public class DistributedTransaction {

    private Transaction[] transactions;
    private UUID id;
    private Integer[] participants;

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

    public Integer[] getParticipants() {
        return participants;
    }

    public void setParticipants(Integer[] participants) {
        this.participants = participants;
    }


}
