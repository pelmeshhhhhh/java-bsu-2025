import java.util.concurrent.*;
import java.util.List;

public class TransactionProcessor {
    private static TransactionProcessor instance;
    private final ExecutorService executorService;
    private final TransactionStrategyFactory strategyFactory;
    private final AccountRepository repository; // Зависимость

    private final List<TransactionListener> listeners = new CopyOnWriteArrayList<>();

    private TransactionProcessor(AccountRepository repository) {
        this.executorService = Executors.newFixedThreadPool(10);
        this.strategyFactory = new TransactionStrategyFactory();
        this.repository = repository;
    }

    public static synchronized TransactionProcessor getInstance(AccountRepository repository) {
        if (instance == null) {
            instance = new TransactionProcessor(repository);
        }
        return instance;
    }

    public static TransactionProcessor getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Processor not initialized. Use getInstance(repository) first.");
        }
        return instance;
    }

    public void submitTransaction(Transaction transaction) {
        TransactionCommand command = new TransactionCommand(transaction, repository, strategyFactory);
        executorService.execute(command);
    }

    public void addListener(TransactionListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners(Transaction transaction) {
        for (TransactionListener listener : listeners) {
            listener.onTransactionCompleted(transaction);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}