import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final String accountId;
    private BigDecimal balance;
    private boolean isFrozen;
    private final Lock lock = new ReentrantLock();

    public Account(String accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
        this.isFrozen = false;
    }

    public String getAccountId() { return accountId; }
    public boolean isFrozen() { return isFrozen; }
    public void setFrozen(boolean frozen) { isFrozen = frozen; }

    public BigDecimal getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void setBalance(BigDecimal newBalance) {
        lock.lock();
        try {
            this.balance = newBalance;
        } finally {
            lock.unlock();
        }
    }

    public Lock getLock() { return lock; }

    @Override
    public String toString() {
        return "accountId=" + accountId + ", balance=" + balance + ", isFrozen=" + isFrozen;
    }
}