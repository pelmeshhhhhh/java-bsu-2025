import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository {

    Account findAccountById(String accountId) throws Exception;

    void updateAccountBalance(Account account, BigDecimal newBalance) throws Exception;

    void logTransaction(Transaction transaction) throws Exception;

    void updateAccountStatus(Account account, boolean shouldFreeze) throws Exception;

    void saveAccount(Account account) throws Exception;

    List<Transaction> findTransactionsByAccount(String accountId);

    List<Transaction> findAllTransactions();

    User[] findAllUsers();
}