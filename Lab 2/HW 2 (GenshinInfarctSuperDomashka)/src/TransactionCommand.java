import java.lang.Runnable;

public class TransactionCommand implements Runnable {
    private final Transaction transaction;
    private final AccountRepository repository;
    private final TransactionStrategyFactory strategyFactory;

    public TransactionCommand(Transaction transaction, AccountRepository repository, TransactionStrategyFactory strategyFactory) {
        this.transaction = transaction;
        this.repository = repository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void run() {
        TransactionStrategy strategy = strategyFactory.getStrategy(transaction.getType());
        try {
            System.out.println("Processing " + transaction.getType() + " for " + transaction.getTargetAccountId());
            strategy.execute(transaction, repository);
            TransactionProcessor.getInstance().notifyListeners(transaction);
        } catch (Exception e) {
            System.err.println("Transaction " + transaction.getTransactionId() + " FAILED: " + e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            TransactionProcessor.getInstance().notifyListeners(transaction);
        }
    }
}