package im.yuri.jcom;

import im.yuri.jcom.util.OperationType;

public class Operation {
    private OperationType type;
    private String property;
    private Integer node;
    private Integer value;

    public Operation(OperationType type, String property) {
        this.type = type;
        this.property = property;
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


}
