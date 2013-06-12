package im.yuri.jcom;

public class DistributedTransaction {
    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
    }

    private Transaction[] transactions;
}
