import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.util.UUID;

public class BankApp {

    public static void main(String[] args) {

        JdbcSetup.initializeDatabase();

        AccountRepository repository = new JdbcAccountRepository();
        TransactionProcessor processor = TransactionProcessor.getInstance(repository);


    }
}