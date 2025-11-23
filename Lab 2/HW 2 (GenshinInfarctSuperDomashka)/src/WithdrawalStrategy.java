import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

public class WithdrawalStrategy implements TransactionStrategy {

    @Override
    public void execute(Transaction transaction, AccountRepository repository) throws Exception {
        String sourceId = transaction.getSourceAccountId();
        BigDecimal amount = transaction.getAmount();

        Account sourceAccount = repository.findAccountById(sourceId);
        Lock lock = sourceAccount.getLock();

        if (sourceAccount.isFrozen()) {
            throw new IllegalStateException("Account is frozen. Withdrawal denied.");
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }

        lock.lock();
        try {
            BigDecimal newBalance = sourceAccount.getBalance().subtract(amount);
            repository.updateAccountBalance(sourceAccount, newBalance);
            sourceAccount.setBalance(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;
        } finally {
            lock.unlock();
            repository.logTransaction(transaction);
        }
    }
}