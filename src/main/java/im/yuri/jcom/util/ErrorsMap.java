package im.yuri.jcom.util;

import im.yuri.jcom.Operation;

import java.util.HashMap;

public class ErrorsMap {
    private HashMap<String, Boolean> errors;

    public ErrorsMap() {
        errors = new HashMap<>();
    }

    public boolean any(Operation op) {
        return errors.get(op.getProperty()) != null;
    }

    public void put(String key) {
        this.errors.put(key, true);

    }
}
