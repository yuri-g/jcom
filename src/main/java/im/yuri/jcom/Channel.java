package im.yuri.jcom;

import java.util.ArrayList;

public class Channel {
    public ArrayList<Transaction> stack;


    public Channel() {
        stack = new ArrayList<Transaction>();
    }

    public void push(Transaction item) {
        stack.add(item);

    }
    public Transaction pull() {
        if (!stack.isEmpty())
            return stack.remove(0);
        else
            return null;
    }

    public void inspect() {
        for (Transaction o: stack) {
            System.out.print(o + " ");
        }
    }
}
