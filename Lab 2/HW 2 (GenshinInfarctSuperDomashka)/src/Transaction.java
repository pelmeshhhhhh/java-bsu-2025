import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final UUID transactionId;
    private final String sourceAccountId;
    private final String targetAccountId;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private TransactionStatus status;

    public Transaction(String source, String target, BigDecimal amount, TransactionType type) {
        this.transactionId = UUID.randomUUID();
        this.sourceAccountId = source;
        this.targetAccountId = target;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.status = TransactionStatus.PENDING; // По умолчанию в ожидании
    }

    public TransactionType getType() { return type; }
    public UUID getTransactionId() { return transactionId; }
    public String getSourceAccountId() { return sourceAccountId; }
    public String getTargetAccountId() { return targetAccountId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public  LocalDateTime getTimestamp() { return timestamp; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "UUID: " + transactionId + "sourceAccountId: " + sourceAccountId + "targetAccountId: "
                + targetAccountId + "amount: " + amount + "timestamp: " + timestamp
                + "type: " + type + "status: " + status;
    }
}