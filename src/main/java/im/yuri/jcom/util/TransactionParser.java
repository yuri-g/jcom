package im.yuri.jcom.util;

import im.yuri.jcom.DistributedTransaction;
import im.yuri.jcom.Operation;
import im.yuri.jcom.Transaction;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static im.yuri.jcom.util.Helpers.getOperationType;


//class that parses the transactions from yaml file
public class TransactionParser {

    public static DistributedTransaction parse(String fileName) throws FileNotFoundException {
        //parse yaml to Object
        Object transactionsToParse = loadTransactions(fileName);
        //distributed transaction to be returned
        DistributedTransaction distributedTransaction = new DistributedTransaction();
        ArrayList<Transaction> transactions = new ArrayList<>();
        //get transactions from yaml file (convert Object to LinkedHashMap)
        LinkedHashMap<String, ArrayList<Object>> parsedTransactions = (LinkedHashMap<String, ArrayList<Object>>) transactionsToParse;
        //iterating over root transactions
        for (ArrayList<Object> t : parsedTransactions.values()) {
            //iterating over inner transactions
            for (Object o : t) {
                //coverting current transaction from Object to HashMap
                HashMap<String, Object> innerTransaction = (HashMap<String, Object>) o;
                //final transaction that will be added to final DistributedTransaction
                Transaction finalTransaction = new Transaction();
                Integer who = (Integer) innerTransaction.get("who");
                //parsing operations from transaction
                ArrayList<Operation> finalOperations = new ArrayList<>();
                ArrayList<Object> operations = (ArrayList<Object>) innerTransaction.get("operations");
                //iterating over operations, adding them to finalTransaction
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
                    //checking if type of operation is Writing
                    //since the syntax is different:
                    //x=20 to res0
                    //we have to get the value (20) that needs to be written
                    if (type.equals("write")) {
                        delimeters = "[=]";
                        String[] writeTokens = tokens[0].split(delimeters);
                        operation.setProperty(writeTokens[0]);
                        operation.setValue(Integer.parseInt(writeTokens[1]));
                    }
                    //syntax for Reading:
                    //z from res0
                    //so value is null here, since we don't have to write anything
                    else {
                        operation.setProperty(tokens[0]);
                    }
                    finalOperations.add(operation);
                    Operation[] operationsArray = new Operation[finalOperations.size()];
                    finalTransaction.setOperations(finalOperations.toArray(operationsArray));


                }
                finalTransaction.setNode(who);
                transactions.add(finalTransaction);

            }
            Transaction[] transactionsArray = new Transaction[transactions.size()];
            distributedTransaction.setTransactions(transactions.toArray(transactionsArray));
        }
        return distributedTransaction;
    }

    //load the transaction from yaml filew
    private static Object loadTransactions (String filename) throws FileNotFoundException {
        Object o;
        Yaml yaml = new Yaml();
        o = yaml.load(new FileInputStream(filename));

        return o;
    }

}
