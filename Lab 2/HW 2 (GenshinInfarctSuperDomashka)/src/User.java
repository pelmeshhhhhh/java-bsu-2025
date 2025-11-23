import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class User {
    private final UUID uuid;
    private String nickname;
    private final Map<String, Account> accounts;

    public User(UUID uuid, String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.accounts = new HashMap<>();
    }

    public UUID getUuid() { return uuid; }
    public String getNickname() { return nickname; }
    public Map<String, Account> getAccounts() { return accounts; }

    public void addAccount(Account account) {
        this.accounts.put(account.getAccountId(), account);
    }

    public Account getAccount(String accountId) {
        return this.accounts.get(accountId);
    }

    @Override
    public String toString() {
        return "User{" + "uuid=" + uuid + ", nickname='" +
                nickname + '\'' + ", accounts=" + accounts.keySet() + '}';
    }
}