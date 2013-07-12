package im.yuri.jcom;

import java.util.ArrayList;

class Channel {
    private ArrayList<Object> stack;


    public Channel() {
        stack = new ArrayList<>();
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

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
