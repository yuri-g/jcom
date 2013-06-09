package im.yuri.jcom;

import java.util.ArrayList;

public class Channel {
    //public Operation[] stack;
    public ArrayList<String> stack;


    public Channel() {
        stack = new ArrayList<String>();
    }

    public void push(String item) {
        stack.add(item);

    }
    public String pull() {
        if (!stack.isEmpty())
            return stack.remove(0);
        else
            return "Nothing";
    }

    public void inspect() {
        for (String s: stack) {
            System.out.print(s + " ");
        }
    }
}
