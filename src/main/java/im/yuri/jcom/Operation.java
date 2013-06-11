package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

public class Operation {
    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getNode() {
        return node;
    }

    public void setNode(Integer node) {
        this.node = node;
    }

    private OperationType type;
    private String value;
    private Integer node;
    public Operation() {

    }
}
