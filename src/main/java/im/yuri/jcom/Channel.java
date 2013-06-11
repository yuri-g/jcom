package im.yuri.jcom;

import java.util.ArrayList;

public class Channel {
    public ArrayList<Operation> stack;


    public Channel() {
        stack = new ArrayList<Operation>();
    }

    public void push(Operation item) {
        stack.add(item);

    }
    public Operation pull() {
        if (!stack.isEmpty())
            return stack.remove(0);
        else
            return null;
    }

    public void inspect() {
        for (Operation o: stack) {
            System.out.print(o + " ");
        }
    }
}
