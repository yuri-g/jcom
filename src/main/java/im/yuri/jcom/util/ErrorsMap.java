package im.yuri.jcom.util;

import im.yuri.jcom.Operation;

import java.util.HashMap;

public class ErrorsMap {
    private HashMap<String, Boolean> errors;

    public ErrorsMap() {
        errors = new HashMap<String, Boolean>();
    }

    public boolean any(Operation op) {
        return errors.get(op.getProperty()) != null;
    }

    public void put(String key, boolean value) {
        this.errors.put(key, value);

    }
}
