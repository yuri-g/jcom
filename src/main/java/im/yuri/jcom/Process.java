package im.yuri.jcom;

import im.yuri.jcom.util.ErrorsMap;
import im.yuri.jcom.util.OperationType;
import static im.yuri.jcom.util.Helpers.*;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import org.yaml.snakeyaml.*;

public class Process implements Runnable {


    private Channel[][] channels;
    private Resource resource;
    private Integer id;
    private Float faultProbability;
    private Random randomGenerator;
    private Queue<Object> executionQueue;
    private ErrorsMap errors;
//    private ArrayList<DistributedTransaction> transactions;
    private DistributedTransaction currentTransaction;
    public Process(Integer id, Channel[][] channels, Float faultProbability) {
      this.id = id;
      this.channels = channels;
      this.faultProbability = faultProbability;
      errors = new ErrorsMap();
        randomGenerator = new Random();
        executionQueue = new LinkedList();
    }

    public void stop() {
        System.out.println("no");
    }

    public void run() {
        resource = new Resource(this.id);


        //right now the node with id==0 is the only one that creates transactions
        if (this.id == 0) {
            //doesnt really do anything right now
            generateTransactions("transactions.yaml");
            currentTransaction = new DistributedTransaction();
            currentTransaction.setParticipants(new Integer[]{0, 1});
            Transaction[] transactions = new Transaction[2];
            transactions[0] = generateTransaction(0, "Z", "X", 69);
            transactions[1] = generateTransaction(1, "X", "Y", 20);
            currentTransaction.setTransactions(transactions);
            //sending phase
            for (Transaction t : currentTransaction.getTransactions() ) {
                send(t.getNode(), t);
                logSendTransaction(t, this.id);
            }

        }

        //reading phase

        while(true) {
            if (!channelIsEmpty()) {
                for (int i = 0; i <= 1; i++) {
                    Object result = read(i);
                    if (result != null) {
                        executionQueue.add(result);
                    }
                }
            }
            else {
                try {
                    Thread.sleep(500);
                    System.out.println(Thread.currentThread().getId() + ": waiting.");
                    if (channelIsEmpty()) {
                        System.out.println(Thread.currentThread().getId() + ": channel is empty, waiting");
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            execute(executionQueue);
        }

   }

    private boolean channelIsEmpty() {

        for(int i = 0; i <= 1; i++) {
            if (!channels[i][this.id].isEmpty()) {
                return false;
            }

        }
        return true;
    }


    private void execute(Queue<Object> executionQueue) {
        Iterator<Object> i = executionQueue.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (isTransaction(o)) {
                Transaction tr = (Transaction)o;
                logGetTransaction(tr, this.id);
                executeTransaction(tr);
                startVoting();
            }
            else if (isOperation(o)) {
                Operation op = (Operation) o;
                if (op.isVoteRequest())
                {
                    if (errors.any(op)){
                        vote(new Operation(OperationType.VOTE, "NO", this.id, op.getTransactionId()), op.getNode());
                        System.out.println(Thread.currentThread().getId() + ": vote: no");
                    } else {
                        System.out.println(Thread.currentThread().getId() + ": vote: yes");
                    }
                }
                else if (op.isVote()) {

                }

            }
            i.remove();
        }
        }






    private void generateTransactions(String fileName) {
        Object parseTransactions = loadTransactions(fileName);
        DistributedTransaction distributedTransaction = new DistributedTransaction();
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        LinkedHashMap<String, ArrayList<Object>> parsedTransactions = (LinkedHashMap<String, ArrayList<Object>>) parseTransactions;
        for (ArrayList<Object> t : parsedTransactions.values()) {
            for (Object o : t) {

                System.out.println(o);
                HashMap<String, Object> innerTransaction = (HashMap<String, Object>) o;
                Transaction finalTransaction = new Transaction();
                Integer who = (Integer) innerTransaction.get("who");
                ArrayList<Operation> finalOperations = new ArrayList<Operation>();
                ArrayList<Object> operations = (ArrayList<Object>) innerTransaction.get("operations");
                for (Object op : operations) {

                    Operation operation = new Operation();
                    HashMap<String, String> parsedOperation = (HashMap<String, String>) op;
                    operation.setType(getOperationType(parsedOperation.get("type")));
                    String type = parsedOperation.get("type");
                    String value = parsedOperation.get("value");
                    String delimeters = "[ ]+";
                    String[] tokens = value.split(delimeters);
                    operation.setValue(null);
                    operation.setResource(tokens[2]);
                    if (type.equals("write")) {
                        delimeters = "[=]";
                        String[] writeTokens = tokens[0].split(delimeters);
                        operation.setProperty(writeTokens[0]);
                        operation.setValue(Integer.parseInt(writeTokens[1]));
                    } else {
                        operation.setProperty(tokens[0]);
                    }
                    finalOperations.add(operation);
                    Operation[] operationsArray = new Operation[finalOperations.size()];
                    finalTransaction.setOperations(finalOperations.toArray(operationsArray));


                }
                transactions.add(finalTransaction);


//                distributedTransaction.setTransactions(transactions);
            }
            Transaction[] transactionsArray = new Transaction[transactions.size()];
            distributedTransaction.setTransactions(transactions.toArray(transactionsArray));
        }
    }


    private void parseTransactions(Object transactions) {


    }

    private Object loadTransactions(String filename) {
        Object o = new Object();
        Yaml yaml = new Yaml();
        try {
             o = yaml.load(new FileInputStream(filename));

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return o;
    }

    private void vote(Operation answer, Integer node) {
        send(node, answer);
    }

    private Object read(Integer node) {

        Object result = channels[node][this.id].pull();
        if (result != null) {
            if (result.getClass() == Transaction.class) {
                return (Transaction)result;
            }
            else if (result.getClass() == Operation.class) {
                return (Operation)result;
            }
        }
        return null;


    }

    private void send(Integer node, Object o) {
        this.channels[this.id][node].push(o);
    }




    private boolean executeTransaction(Transaction transaction) {
        for (Operation o : transaction.getOperations()) {
            OperationType type = o.getType();
            switch (type) {
                case READ:
                    System.out.println("Reading value of " + o.getProperty() + ": " + this.resource.getValue(o.getProperty()) + " [process " + id + "]");
                    break;
                case WRITE:
                    if (resource.setValue(o.getProperty(), o.getValue(), faultProbability)) {
                        System.out.println("Writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                    }
                    else {
                        System.out.println("Error while writing " + o.getValue() + " to " + o.getProperty()   + " [process " + id + "]");
                        errors.put(transaction.getId().toString(), true);
                    }
                    break;
            }
        }
        return true;
    }

    private void startVoting() {

        //if currentTransaction != null it means that this node initiated this transaction
        if (currentTransaction != null) {
            for (Integer node: currentTransaction.getParticipants()) {
                System.out.println("Sending vote requests to:");
                System.out.println(Arrays.toString(currentTransaction.getParticipants()));
                send(node, new Operation(OperationType.VOTE_REQUEST, currentTransaction.getId().toString(), this.id, currentTransaction.getId().toString()));
            }
        }
    }



    private Transaction generateTransaction(Integer node, String property1, String property2, Integer value) {
        Transaction t = new Transaction();
        t.setNode(node);
        Operation op1 = new Operation();
        op1.setType(OperationType.READ);
        op1.setProperty(property1);
        Operation op2 = new Operation();
        op2.setType(OperationType.WRITE);
        op2.setProperty(property2);
        op2.setValue(value);
        Operation[] ops = new Operation[2];
        ops[0] = op1;
        ops[1] = op2;
        t.setOperations(ops);
        return t;
    }
}

