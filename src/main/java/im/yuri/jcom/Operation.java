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


    private OperationType type;
    private String value;
    public Operation(OperationType type, String value) {
        this.type = type;
        this.value = value;
    }

    public Operation() {

    }
}
