package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

public class Operation {
    private OperationType type;
    private String property;
    private Integer node;
    private Integer value;
    private String transactionId;

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

    public boolean isVoteRequest() {
        return this.type == OperationType.VOTE_REQUEST;
    }

    public boolean isVote() {
        return this.type == OperationType.VOTE;
    }


}
