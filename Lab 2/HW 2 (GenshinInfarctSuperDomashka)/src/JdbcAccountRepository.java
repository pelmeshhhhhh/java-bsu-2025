import java.sql.*;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcAccountRepository implements AccountRepository {
    private final String JDBC_URL = "jdbc:h2:mem:bankdb";
    private final String JDBC_USER = "sa";
    private final String JDBC_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @Override
    public void updateAccountBalance(Account account, BigDecimal newBalance) throws SQLException {
        try (Connection conn = getConnection()) {
            // Атомарность: Начинаем транзакцию JDBC
            conn.setAutoCommit(false);

            // Запрос UPDATE
            String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, newBalance);
                stmt.setString(2, account.getAccountId());
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Update failed, account not found.");
                }

                conn.commit(); // Атомарность: Фиксируем изменения
            } catch (SQLException e) {
                conn.rollback(); // Атомарность: Откатываем в случае ошибки
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void logTransaction(Transaction transaction) throws Exception {
        String sql = "INSERT INTO transactions (transaction_id, timestamp, type, amount, source_id, target_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaction.getTransactionId().toString());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(transaction.getTimestamp()));
            stmt.setString(3, transaction.getType().name());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getSourceAccountId());
            stmt.setString(6, transaction.getTargetAccountId());
            stmt.setString(7, transaction.getStatus().name());

            stmt.executeUpdate();
        }
    }

    public void transferFunds(Account source, Account target, BigDecimal amount) throws SQLException {
        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);
            try {
                BigDecimal newSourceBalance = source.getBalance().subtract(amount);

                String sqlDebit = "UPDATE accounts SET balance = ? WHERE account_id = ?";
                try (PreparedStatement stmtDebit = conn.prepareStatement(sqlDebit)) {
                    stmtDebit.setBigDecimal(1, newSourceBalance);
                    stmtDebit.setString(2, source.getAccountId());
                    int rows = stmtDebit.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Debit failed: Source account not found or lock conflict.");
                    }
                }

                BigDecimal newTargetBalance = target.getBalance().add(amount);

                String sqlCredit = "UPDATE accounts SET balance = ? WHERE account_id = ?";
                try (PreparedStatement stmtCredit = conn.prepareStatement(sqlCredit)) {
                    stmtCredit.setBigDecimal(1, newTargetBalance);
                    stmtCredit.setString(2, target.getAccountId());
                    int rows = stmtCredit.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Credit failed: Target account not found.");
                    }
                }

                conn.commit();
                System.out.println("COMMITTED: Transfer " + source.getAccountId() + " -> " + target.getAccountId() + " committed.");

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("ROLLED BACK: Transfer failed. Reason: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public Account findAccountById(String accountId) throws Exception {
        String sql = "SELECT account_id, balance, is_frozen FROM accounts WHERE account_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String id = rs.getString("account_id");
                    BigDecimal balance = rs.getBigDecimal("balance");
                    boolean isFrozen = rs.getBoolean("is_frozen");

                    Account account = new Account(id, balance);
                    account.setFrozen(isFrozen);

                    System.out.println("INFO: Account found: " + id + ", Balance: " + balance);
                    return account;
                } else {
                    throw new IllegalArgumentException("Account with ID " + accountId + " not found in the database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("DB Error while finding account: " + e.getMessage());
            throw new Exception("Database error occurred.", e);
        }
    }

    @Override
    public void updateAccountStatus(Account account, boolean isFrozen) throws SQLException {
        String sql = "UPDATE accounts SET is_frozen = ? WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isFrozen);
            stmt.setString(2, account.getAccountId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Update status failed: Account not found.");
            }
        }
    }

    public void saveAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_id, user_uuid, balance, is_frozen) VALUES (?, ?, ?, ?)";

        String userUuid = UUID.randomUUID().toString();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccountId());
            stmt.setString(2, userUuid);
            stmt.setBigDecimal(3, account.getBalance());
            stmt.setBoolean(4, account.isFrozen());

            stmt.executeUpdate();

            System.out.println("INFO: Account " + account.getAccountId() + " successfully SAVED to DB.");
        }
    }

    @Override
    public List<Transaction> findTransactionsByAccount(String accountId) {
        return List.of();
    }

    @Override
    public List<Transaction> findAllTransactions() {
        return List.of();
    }

    @Override
    public User[] findAllUsers() {
        return new User[0];
    }
}