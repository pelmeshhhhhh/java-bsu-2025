import java.math.BigDecimal;

public class DepositStrategy implements TransactionStrategy {
    @Override
    public void execute(Transaction transaction, AccountRepository repository) throws Exception {
        String targetId = transaction.getTargetAccountId();
        Account targetAccount = repository.findAccountById(targetId);

        if (targetAccount.isFrozen()) {
            throw new IllegalStateException("Account is frozen. Deposit denied.");
        }

        targetAccount.getLock().lock();
        try {
            BigDecimal newBalance = targetAccount.getBalance().add(transaction.getAmount());

            repository.updateAccountBalance(targetAccount, newBalance);
            targetAccount.setBalance(newBalance);

            transaction.setStatus(TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;
        } finally {
            targetAccount.getLock().unlock();
            repository.logTransaction(transaction);
        }
    }
}
// Реализуйте аналогично WithdrawalStrategy, FreezeStrategy, TransferStrategy
// TransferStrategy - самая сложная, требует блокировки двух счетов (Source и Target)
// и использования ОДНОЙ транзакции JDBC!