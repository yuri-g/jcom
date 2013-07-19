package im.yuri.jcom.util;

import im.yuri.jcom.Operation;

import java.util.HashMap;

//class that is used to store all encountered errors, per transaction
public class ErrorsMap {
    private HashMap<String, Boolean> errors;

    public ErrorsMap() {
        errors = new HashMap<>();
    }


    //check if there are any errors while executing the transaction
    public boolean any(Operation op) {
        return errors.get(op.getProperty()) != null;
    }

    public void put(String key) {
        this.errors.put(key, true);

    }
}
