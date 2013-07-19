package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

public class Operation {

    //type of operation, defined in util/OperationType enum
    private OperationType type;
    //property to read or to write
    private String property;
    //thread responsible for execution
    private Integer node;
    //resource to use while reading/writing
    private String resource;
    //value to write
    private Integer value;
    //id of transaction
    private String transactionId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Operation(OperationType type, String property, Integer id, String transactionId) {
        this.type = type;
        this.property = property;
        this.node = id;
        this.transactionId = transactionId;

    }

    public Operation() {

    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getNode() {
        return node;
    }

    public void setNode(Integer node) {
        this.node = node;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }


    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }



    //is operation a vote request?
    public boolean isVoteRequest() {
        return this.type == OperationType.VOTE_REQUEST;
    }

    //vote?
    public boolean isVote() {
        return this.type == OperationType.VOTE;
    }

    //commit message?
    public boolean isCommit() {
        return this.type == OperationType.COMMIT;
    }

    //abort message?
    public boolean isAbort() {
        return this.type == OperationType.ABORT;
    }


}
