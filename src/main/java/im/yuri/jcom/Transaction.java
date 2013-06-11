package im.yuri.jcom;
import java.util.UUID;

public class Transaction {

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    private Operation[] operations;
    private UUID id;

    public Transaction() {
        id = UUID.randomUUID();
    }


}
