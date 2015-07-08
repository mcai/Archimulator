package archimulator.util.transactional;

import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;

import java.util.Date;

import static org.multiverse.api.StmUtils.*;

public class Account {
    private final TxnRef<Date> lastUpdate;
    private final TxnInteger balance;

    public Account(int balance) {
        this.lastUpdate = newTxnRef(new Date());
        this.balance = newTxnInteger(balance);
    }

    public void incrementBalance(final int amount, final Date date) {
        atomic(() -> {
            balance.increment(amount);
            lastUpdate.set(date);

            if (balance.get() < 0) {
                throw new IllegalStateException("Not enough money");
            }
        });
    }

    public Date getLastUpdate() {
        return lastUpdate.atomicGet();
    }

    public int getBalance() {
        return balance.atomicGet();
    }

    public static void transfer(final Account from, final Account to, final int amount) {
        atomic(() -> {
            Date date = new Date();

            from.incrementBalance(-amount, date);
            to.incrementBalance(amount, date);
        });
    }

    public static void main(String[] args) {
        Account a = new Account(10);
        Account b = new Account(20);

        transfer(a, b, 10);

        System.out.printf("a.balance: %d, a.lastUpdate: %s%n", a.getBalance(), a.getLastUpdate());
        System.out.printf("b.balance: %d, b.lastUpdate: %s%n", b.getBalance(), b.getLastUpdate());
    }
}