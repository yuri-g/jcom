#Jcom
## Atomic commitment model in Java

###Usage:
* For each distributed transaction create a [yaml](http://yaml.org/) file named `node #id transactions.yaml`
where `#id` is id of the node that will be a coordinator of transaction.
* Example of `node 0 transactions.yaml`:

```YAML
transactions:
-
    who:
      0
    operations:
        - type: read
          value:
               x from res0
        - type: write
          value:
               y=23 to res1
        - type: read
          value:
               z from res0
-
    who:
      1
    operations:
       - type: write
         value:
             x=7 to res0
       - type: read
         value:
             y from res1
```

In this file there are two entries (two transactions) that make up distributed transaction.
Each transaction has set of parameters:
* `who:` id of the node who is responsible for executing the transaction
* `operations:` set of the operations of transaction
  * `type:` type of operation. Can be `read` or `write`
  * `value:` value to `read` from `resource` (`res0`, for example)
  * or `value:` can specify value to `write`

Note that indentation in yaml files is important.

* Create needed resource files (files that will be used in transactions to read or write values). Basically, the resource
file models shared resource (database, etc). For example, assume that you have operation `read z from res0` in the 
transactions file, so the resource `res0.yaml` has to be created.

* Example of `res0.yaml`:
```YAML
--- 
z: 5
y: 1
x: 7
```
In this file 3 properties are specified and they can be read or changed during the transaction.

Note that all files has to be created in the same directory as the `jar` file.

* Next, launch the application:

`java -jar main.jar -f 0.5 -c 3`

Arguments: 
* `-f` : fault probability
* `-c` : threads to be created

So, after executing this command 3 threads will be created, and the probabilty to fail while writing to the resource is
0.5.

Execution log:

First, all the nodes read and parse the transaction files, and then start to sending the transactions to the participants.
Here, node 0 sends transaction to himeslf and then to node 1, since in the transactions file this node is defined as
participant (i.e. it has a transaction to execute). While node 1 sends transactions to node 0 and node 2, but not to himself,
because in the `node 1 transactions` file he has no transactions to execute. After sending the transactions, nodes send
vote requests to participants. 
Note that each transaction has unique identifier (UUID), in this log only first 4 haracters are displayed.

```
Node 1: sent transaction d36d to node 0
Node 0: sent transaction 9aaf to node 0
Node 1: sent transaction d36d to node 2
Node 0: sent transaction 9aaf to node 1
Node 1: sending vote requests to: 
[0, 2]
Node 0: sending vote requests to: 
[0, 1]
```

Next, the nodes are starting to receive and execute transactions:

```
Node 0: got transaction 9aaf from node 0
Node 1: got transaction 9aaf from node 0
Node 1: writing 7 to x
Node 0: reading value of x: 7 from res0.yaml
Node 1: reading value of y: 23 from res1.yaml
Node 0: writing 23 to y
```

After sending the transaction coordinator waits for the votes from participants:

```
Node 1: waiting for votes...
Node 1: votes YES (transaction 9aaf)
```
Here, the coordinator (node 1) is participant of other transaction (9aaf, the one that was initialized by node 0), so 
he votes YES, since while writing there was no errors.

Still waiting...
```
Node 1: waiting for votes...

```

Node 0 executes the last operation of the transaction that he initialized, and 
starts to execute transaction that he received from node 1:
```
Node 0: reading value of z: 5 from res0.yaml
Node 0: got transaction d36d from node 1
Node 0: reading value of z: 0 from res1.yaml
Node 0: writing 5 to z
```

At the same time he is still waiting for the votes from others, since he also is coordinator (transaction 9aaf):

```
Node 0: waiting for votes...
```

Node 0 votes YES for both transactions he received (one from himself and one from node 1) and gets answer from himself and
node 1 (both YES)

```
Node 0: votes YES (transaction 9aaf)
Node 0: votes YES (transaction d36d)
Node 0: waiting for votes...
Node 0: got YES from 0
Node 0: got YES from 1
```

He got two out of two YES, which means that there were no errors, so it's safe to commit. He broadcasts commit message to
all participants of the transaction:

```
Node 0: got all YES, commit! (transaction 9aaf)
Node 0: committing! (transaction 9aaf)
```

In the meantime node 2 gets transaction from node 1:
```
Node 2: got transaction d36d from node 1
Node 2: reading value of y: 23 from res1.yaml
Node 2: error while writing 1 to y
Node 2: votes NO (transaction d36d)
```
But while writing there was an error, so he votes NO. Node 1 gets one YES from node 0 and NO from node 2, so the
transaction has to be aborted. He broadcasts abort message to all participants:
```
Node 1: waiting for votes...
Node 1: got YES from 0
Node 1: got NO from 2
Node 1: got NO, aborting!
```

After that node 1 finally receives COMMIT message from the node 0 (coordinator of transaction 9aaf):

```
Node 1: committing! (transaction 9aaf)
```

So in this example one transaction was successful (transaction 9aaf, initialized by node 0) and one unsuccessful
(transaction d36d, initialized by node 1).

So the changes made by transaction 9aaf were committed, and both files res0.yaml and res1.yaml are changed.


