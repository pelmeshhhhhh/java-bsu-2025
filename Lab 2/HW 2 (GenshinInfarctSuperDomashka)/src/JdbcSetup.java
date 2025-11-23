import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcSetup {

    private static final String JDBC_URL = "jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String createAccountsTable = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_id VARCHAR(36) PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    balance DECIMAL(19, 2) NOT NULL,
                    is_frozen BOOLEAN NOT NULL
                );
            """;
            stmt.execute(createAccountsTable);

            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id VARCHAR(36) PRIMARY KEY,
                    timestamp TIMESTAMP NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    amount DECIMAL(19, 2) NOT NULL,
                    source_id VARCHAR(36),
                    target_id VARCHAR(36),
                    status VARCHAR(20) NOT NULL
                );
            """;
            stmt.execute(createTransactionsTable);

            System.out.println("Успешно");

        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}