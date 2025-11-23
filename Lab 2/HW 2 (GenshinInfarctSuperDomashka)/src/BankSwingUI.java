import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BankSwingUI extends JFrame {

    private final AccountRepository repository;
    private final TransactionProcessor processor;
    private final DefaultListModel<User> userListModel;

    private JList<User> userList;
    private JPanel detailPanel;
    private JTextArea allLogArea;

    public BankSwingUI(AccountRepository repository, TransactionProcessor processor) throws Exception {
        this.repository = repository;
        this.processor = processor;
        this.userListModel = new DefaultListModel<>();

        setTitle("Multi-User Bank Transaction Simulator (Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadUserList();

        detailPanel = new JPanel(new BorderLayout());
        detailPanel.add(new JLabel("Выберите пользователя для просмотра деталей.", SwingConstants.CENTER), BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createUserPanel(), detailPanel);
        mainSplitPane.setDividerLocation(200);

        setLayout(new BorderLayout());
        add(mainSplitPane, BorderLayout.CENTER);
        add(createTransactionLogPanel(), BorderLayout.SOUTH); // Общий лог транзакций

        pack();
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadUserList() throws Exception {
        userListModel.clear();
        for (User user : repository.findAllUsers()) {
            userListModel.addElement(user);
        }
    }


    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Пользователи"));

        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedIndex() != -1) {
                User selectedUser = userList.getSelectedValue();
                showUserDetails(selectedUser);
            }
        });

        panel.add(new JScrollPane(userList), BorderLayout.CENTER);
        return panel;
    }


    private JScrollPane createTransactionLogPanel() {
        allLogArea = new JTextArea(8, 0);
        allLogArea.setEditable(false);
        allLogArea.setBorder(BorderFactory.createTitledBorder("Общая история транзакций (Аудит)"));

        try {
            List<Transaction> allTxs = repository.findAllTransactions();
            for (Transaction tx : allTxs) {
                allLogArea.append(formatTransaction(tx) + "\n");
            }
        } catch (Exception e) {
            allLogArea.append("Ошибка загрузки истории: " + e.getMessage());
        }

        return new JScrollPane(allLogArea);
    }
    private void showUserDetails(User user) {
        detailPanel.removeAll();
        detailPanel.setLayout(new BorderLayout(5, 5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Детали пользователя: " + user.getNickname()));

        String accountId = user.getAccounts().keySet().iterator().next();

        detailPanel.add(createAccountControlPanel(accountId), BorderLayout.NORTH);

        detailPanel.add(createTransferPanel(accountId), BorderLayout.CENTER);

        detailPanel.add(createHistoryPanel(accountId), BorderLayout.SOUTH);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createAccountControlPanel(String accountId) {
        JPanel fullPanel = new JPanel(new BorderLayout(5, 5));
        fullPanel.setBorder(BorderFactory.createTitledBorder("Счет: " + accountId));

        // Метки для статуса
        JLabel balanceLabel = new JLabel("---", SwingConstants.CENTER);
        JLabel freezeLabel = new JLabel("---", SwingConstants.CENTER);

        Runnable updateStatus = () -> {
            try {
                Account dbAcc = repository.findAccountById(accountId);
                balanceLabel.setText(String.format("Баланс: %,.2f RUB", dbAcc.getBalance()));
                freezeLabel.setText("Статус: " + (dbAcc.isFrozen() ? "ЗАМОРОЖЕН" : "АКТИВЕН"));
            } catch (Exception e) {
                balanceLabel.setText("Ошибка загрузки!");
            }
        };

        processor.addListener(new TransactionListener() {
            @Override
            public void onTransactionCompleted(Transaction transaction) {
                if (accountId.equals(transaction.getTargetAccountId()) || accountId.equals(transaction.getSourceAccountId())) {
                    SwingUtilities.invokeLater(updateStatus); // Обновляем в EDT
                }
            }
        });

        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.add(balanceLabel);
        statusPanel.add(freezeLabel);
        SwingUtilities.invokeLater(updateStatus);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JTextField amountField = new JTextField(8);
        JButton depositBtn = new JButton("Зачислить");
        JButton withdrawBtn = new JButton("Снять");
        JButton freezeBtn = new JButton("Заморозить/Разморозить");

        depositBtn.addActionListener(e -> submitTransaction(TransactionType.DEPOSIT, accountId, null, amountField.getText()));
        withdrawBtn.addActionListener(e -> submitTransaction(TransactionType.WITHDRAWAL, null, accountId, amountField.getText()));
        freezeBtn.addActionListener(e -> toggleFreeze(accountId));

        controlPanel.add(new JLabel("Сумма:"));
        controlPanel.add(amountField);
        controlPanel.add(depositBtn);
        controlPanel.add(withdrawBtn);
        controlPanel.add(freezeBtn);

        fullPanel.add(statusPanel, BorderLayout.NORTH);
        fullPanel.add(controlPanel, BorderLayout.CENTER);

        return fullPanel;
    }

    private JPanel createTransferPanel(String sourceAccountId) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Перевод со счета " + sourceAccountId));

        JTextField targetField = new JTextField(8);
        JTextField amountField = new JTextField(8);
        JButton transferBtn = new JButton("Перевести");

        transferBtn.addActionListener(e -> {
            String targetId = targetField.getText();
            String amountStr = amountField.getText();
            submitTransaction(TransactionType.TRANSFER, targetId, sourceAccountId, amountStr);
        });

        panel.add(new JLabel("Целевой ID:"));
        panel.add(targetField);
        panel.add(new JLabel("Сумма:"));
        panel.add(amountField);
        panel.add(transferBtn);
        return panel;
    }

    private JScrollPane createHistoryPanel(String accountId) {
        JTextArea historyArea = new JTextArea(8, 0);
        historyArea.setEditable(false);
        historyArea.setBorder(BorderFactory.createTitledBorder("История транзакций счета " + accountId));

        Runnable updateHistory = () -> {
            try {
                historyArea.setText("");
                List<Transaction> userTxs = repository.findTransactionsByAccount(accountId);
                for (Transaction tx : userTxs) {
                    historyArea.append(formatTransaction(tx) + "\n");
                }
            } catch (Exception e) {
                historyArea.append("Ошибка загрузки истории: " + e.getMessage());
            }
        };

        processor.addListener(new TransactionListener() {
            @Override
            public void onTransactionCompleted(Transaction transaction) {
                if (accountId.equals(transaction.getTargetAccountId()) || accountId.equals(transaction.getSourceAccountId())) {
                    SwingUtilities.invokeLater(updateHistory);
                }
            }
        });

        SwingUtilities.invokeLater(updateHistory); // Первое обновление

        return new JScrollPane(historyArea);
    }

    private void submitTransaction(TransactionType type, String targetId, String sourceId, String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Сумма должна быть положительной.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (type == TransactionType.TRANSFER) {
                repository.findAccountById(targetId);
            }

            Transaction tx = new Transaction(sourceId, targetId, amount, type);
            processor.submitTransaction(tx);

            JOptionPane.showMessageDialog(this, type.name() + " отправлена на обработку. ID: " + tx.getTransactionId().toString().substring(0, 8), "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверный формат суммы.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Счет не найден или неверный ID. " + e.getMessage(), "Ошибка БД", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка обработки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleFreeze(String accountId) {
        try {
            Account account = repository.findAccountById(accountId);
            boolean currentlyFrozen = account.isFrozen();

            BigDecimal freezeAction = currentlyFrozen ? BigDecimal.ZERO : BigDecimal.ONE;

            Transaction tx = new Transaction(null, accountId, freezeAction, TransactionType.FREEZE);
            processor.submitTransaction(tx);

            String action = currentlyFrozen ? "Разморозка" : "Заморозка";
            JOptionPane.showMessageDialog(this, action + " счета отправлена на обработку.", "Статус", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при получении статуса счета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void updateTransactionLog(Transaction transaction) {
        SwingUtilities.invokeLater(() -> {
            String logEntry = formatTransaction(transaction) + "\n";
            allLogArea.append(logEntry);
            allLogArea.setCaretPosition(allLogArea.getDocument().getLength());
        });
    }

    private String formatTransaction(Transaction tx) {
        return String.format("[%s] %s: %s -> %s (%.2f). Status: %s",
                tx.getTimestamp().toLocalTime(),
                tx.getType().name(),
                tx.getSourceAccountId() == null ? "EXT" : tx.getSourceAccountId(),
                tx.getTargetAccountId() == null ? "EXT" : tx.getTargetAccountId(),
                tx.getAmount(),
                tx.getStatus().name()
        );
    }

    public void refreshBalanceDisplay() {
    }
}