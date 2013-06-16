package im.yuri.jcom;

import java.util.ArrayList;

public class Channel {
    public ArrayList<Object> stack;


    public Channel() {
        stack = new ArrayList<Object>();
    }

    public void push(Object item) {
        stack.add(item);

    }
    public Object pull() {
        if (!stack.isEmpty())
            return stack.remove(0);
        else
            return null;
    }

    public void inspect() {
        for (Object o: stack) {
            System.out.print(o + " ");
        }
    }
}
