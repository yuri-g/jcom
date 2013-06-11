package im.yuri.jcom;
import java.util.UUID;

public class Transaction {

    private Operation[] operations;
    private UUID id;

    public Transaction() {
        id = UUID.randomUUID();
    }


}
