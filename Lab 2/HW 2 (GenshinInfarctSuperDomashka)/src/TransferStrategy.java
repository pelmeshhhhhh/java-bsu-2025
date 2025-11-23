import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

public class TransferStrategy implements TransactionStrategy {
    @Override
    public void execute(Transaction transaction, AccountRepository repository) throws Exception {
        String sourceId = transaction.getSourceAccountId();
        String targetId = transaction.getTargetAccountId();
        BigDecimal amount = transaction.getAmount();

        Account sourceAccount = repository.findAccountById(sourceId);
        Account targetAccount = repository.findAccountById(targetId);

        if (sourceAccount.isFrozen() || targetAccount.isFrozen()) {
            throw new IllegalStateException("One of the accounts is frozen. Transfer denied.");
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds on source account.");
        }

        Lock lock1 = sourceAccount.getLock();
        Lock lock2 = targetAccount.getLock();

        if (sourceId.compareTo(targetId) < 0) {
            lock1.lock();
            lock2.lock();
        } else {
            lock2.lock();
            lock1.lock();
        }

        try {
            ((JdbcAccountRepository) repository).transferFunds(
                    sourceAccount,
                    targetAccount,
                    amount
            );

            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
            targetAccount.setBalance(targetAccount.getBalance().add(amount));

            transaction.setStatus(TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;
        } finally {
            lock1.unlock();
            lock2.unlock();
            repository.logTransaction(transaction);
        }
    }
}