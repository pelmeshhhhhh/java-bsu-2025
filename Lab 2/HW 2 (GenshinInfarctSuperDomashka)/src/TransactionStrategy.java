public interface TransactionStrategy {
    void execute(Transaction transaction, AccountRepository repository) throws Exception;
}
