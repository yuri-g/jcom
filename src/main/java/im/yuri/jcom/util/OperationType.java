package im.yuri.jcom.util;

public enum OperationType {
    //read operation
    READ,
    //write operation
    WRITE,
    //request for the participants to vote
    VOTE_REQUEST,
    //vote (can be YES or NO)
    VOTE,
    //abort the transaction
    ABORT,
    //commit the transaction
    COMMIT
}