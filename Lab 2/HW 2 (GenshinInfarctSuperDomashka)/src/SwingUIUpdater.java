import javax.swing.SwingUtilities;

public class SwingUIUpdater implements TransactionListener {
    private final BankSwingUI ui;

    public SwingUIUpdater(BankSwingUI ui) {
        this.ui = ui;
    }

    @Override
    public void onTransactionCompleted(Transaction transaction) {
        // КЛЮЧЕВОЙ МОМЕНТ: Безопасное обновление UI в EDT
        SwingUtilities.invokeLater(() -> {
            ui.updateTransactionLog(transaction);

            // Если транзакция завершена, обновляем баланс
            if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                ui.refreshBalanceDisplay();
            }
        });
    }
}