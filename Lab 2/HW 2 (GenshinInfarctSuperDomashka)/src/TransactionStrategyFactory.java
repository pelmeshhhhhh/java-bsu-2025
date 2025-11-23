public class TransactionStrategyFactory {
    public TransactionStrategy getStrategy(TransactionType type) {
        switch (type) {
            case DEPOSIT:
                return new DepositStrategy();
            case WITHDRAWAL:
                return new WithdrawalStrategy();
            case FREEZE:
                return new FreezeStrategy();
            case TRANSFER:
                return new TransferStrategy();
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
    }
}