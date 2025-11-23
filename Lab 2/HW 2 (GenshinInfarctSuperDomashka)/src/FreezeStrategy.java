import java.util.concurrent.locks.Lock;

public class FreezeStrategy implements TransactionStrategy {

    @Override
    public void execute(Transaction transaction, AccountRepository repository) throws Exception {
        String targetId = transaction.getTargetAccountId();
        Account targetAccount = repository.findAccountById(targetId);
        Lock lock = targetAccount.getLock();

        boolean shouldFreeze = transaction.getAmount().intValue() == 1;

        lock.lock();
        try {
            if (targetAccount.isFrozen() == shouldFreeze) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                return;
            }

            ((JdbcAccountRepository) repository).updateAccountStatus(targetAccount, shouldFreeze);
            targetAccount.setFrozen(shouldFreeze);

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
