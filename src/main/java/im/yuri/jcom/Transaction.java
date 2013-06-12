package im.yuri.jcom;
import java.util.UUID;

public class Transaction {


    private Operation[] operations;
    private UUID id;
    private Integer node;


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

    public Integer getNode() {
        return node;
    }

    public void setNode(Integer node) {
        this.node = node;
    }

    public Transaction() {
        id = UUID.randomUUID();
    }


}
