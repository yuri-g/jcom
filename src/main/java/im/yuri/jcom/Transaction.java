package im.yuri.jcom;
import java.util.UUID;


//transaction class
public class Transaction {

    private Integer parentNode;
    private Operation[] operations;
    private UUID id;
    private Integer node;


    //id of the node that initialized transaction (coordinator)
    public Integer getParentNode() {
        return parentNode;
    }

    public void setParentNode(Integer parentNode) {
        this.parentNode = parentNode;
    }

    //get set of operations
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

    }


}
