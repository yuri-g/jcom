package im.yuri.jcom;

import java.util.ArrayList;


//communication channel, shared between all threads
class Channel {
    private ArrayList<Object> stack;


    public Channel() {
        stack = new ArrayList<>();
    }


    //push object to stack
    public void push(Object item) {
        stack.add(item);

    }

    //pull object from stack
    public Object pull() {
        if (!stack.isEmpty())
            return stack.remove(0);
        else
            return null;
    }


    //inspect the stack, print all elements
    public void inspect() {
        for (Object o: stack) {
            System.out.print(o + " ");
        }
    }

    //check if the stack is empty
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
