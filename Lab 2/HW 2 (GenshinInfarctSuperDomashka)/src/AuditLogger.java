import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuditLogger implements TransactionListener {
    @Override
    public void onTransactionCompleted(Transaction transaction) {
        System.out.println("AUDIT: Transaction " + transaction.getTransactionId() +
                " completed with status " + transaction.getStatus());
    }
}