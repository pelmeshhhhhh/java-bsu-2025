public class TransactionService {
/*
    public Transaction ExecuteTransfer(Account sender, Account receiver, int money) {
        Transaction transaction = new Transaction(sender, receiver, money);
        transaction.setStatus(TransactionStatus.PENDING);
        if (sender.getSumOfMMoney() - money >= sender.getLimit()) {
            sender.setSumOfMMoney(sender.getSumOfMMoney() - money);
            receiver.setSumOfMMoney(receiver.getSumOfMMoney() + money);
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            System.out.println("Not enough money");
            transaction.setStatus(TransactionStatus.FAILED);
        }
        return transaction;
    }

    public Transaction ExecutePayment(Account sender, int money) {
        Transaction transaction = new Transaction(sender, money);
        if (sender.getSumOfMMoney() - money >= sender.getLimit()) {
            sender.setSumOfMMoney(sender.getSumOfMMoney() + money);
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            System.out.println("Not enough money");
            transaction.setStatus(TransactionStatus.FAILED);
        }
        return transaction;
    }

    public Transaction ExecuteCashingOut(Account sender, int money) {
        Transaction transaction = new Transaction(sender, money);
        if (sender.getSumOfMMoney() - money >= sender.getLimit()) {
            sender.setSumOfMMoney(sender.getSumOfMMoney() - money);
            transaction.setStatus(TransactionStatus.COMPLETED);
        } else {
            System.out.println("Not enough money");
            transaction.setStatus(TransactionStatus.FAILED);
        }
        return transaction;
    }
    */
}