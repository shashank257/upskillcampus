import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
class User {
    private String username;
    private String password;
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}
public class BankingInformationSystem {
    private static List<User> users = new ArrayList<>();
    private static List<Account> accounts = new ArrayList<>();
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("1. Register\n2. Login\n3. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character
            switch (choice) {
                case 1:
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    registerUser(username, password);
                    break;
                case 2:
                    System.out.print("Enter username: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String loginPassword = scanner.nextLine();
                    loginUser(loginUsername, loginPassword);
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    private static void registerUser(String username, String password) {
        User newUser = new User(username, password);
        users.add(newUser);
        Account newAccount = new Account(newUser);
        accounts.add(newAccount);
        System.out.println("User registered with account number: " + newAccount.getAccountNumber());
    }
    private static void loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                Account userAccount = getAccountByUser(user);
                if (userAccount != null) {
                    performBankingOperations(userAccount);
                } else {
                    System.out.println("Account not found.");
                }
                return;
            }
        }
        System.out.println("Invalid credentials.");
    }
    private static void performBankingOperations(Account account) {
        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = true;
        BigDecimal balance = account.getBalance(); // Declare balance variable
        while (loggedIn) {
            System.out.println("1. Deposit\n2. Withdraw\n3. Transfer\n4. Account Statement\n5. Logout");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character
            switch (choice) {
                case 1:
                    System.out.print("Enter deposit amount: ");
                    BigDecimal depositAmount = scanner.nextBigDecimal();
                    BigDecimal newBalance = account.deposit(depositAmount);
                    BigDecimal initialBalance = newBalance.subtract(depositAmount);
                    balance = newBalance; // Update balance variable
                    account.addTransaction(new Transaction(new Date(), TransactionType.DEPOSIT, depositAmount));
                    System.out.println("Deposit successful.");
                    System.out.println("New balance: " + newBalance);
                    System.out.println("Initial balance: " + initialBalance);
                    break;
                case 2:
                    System.out.print("Enter withdrawal amount: ");
                    BigDecimal withdrawalAmount = scanner.nextBigDecimal();
                    account.withdraw(withdrawalAmount);
                    balance = account.getBalance(); // Update balance variable
                    System.out.println("New balance: " + balance);
                    break;
                case 3:
                    System.out.print("Enter destination account number: ");
                    String destinationAccountNumber = scanner.nextLine();
                    Account destinationAccount = getAccountByAccountNumber(destinationAccountNumber);
                    if (destinationAccount != null) {
                        System.out.print("Enter transfer amount: ");
                        BigDecimal transferAmount = scanner.nextBigDecimal();
                        account.transfer(destinationAccount, transferAmount);
                    } else {
                        System.out.println("Destination account not found.");
                    }
                    break;
                case 4:
                    account.printAccountStatement();
                    break;
                case 5:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    private static Account getAccountByUser(User user) {
        for (Account account : accounts) {
            if (account.getUser().equals(user)) {
                return account;
            }
        }
        return null;
    }
    private static Account getAccountByAccountNumber(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }
}
class Account {
    private static int accountCounter = 0;
    private String accountNumber;
    private User user;
    private BigDecimal balance;
    private List<Transaction> transactionHistory;
    public Account(User user) {
        this.accountNumber = generateAccountNumber();
        this.user = user;
        this.balance = BigDecimal.ZERO;
        this.transactionHistory = new ArrayList<>();
    }
    private static String generateAccountNumber() {
        String accountNumber = String.valueOf(++accountCounter);
        accountNumber = String.format("%08d", Integer.parseInt(accountNumber));
        return accountNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public User getUser() {
        return user;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public BigDecimal deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invalid deposit amount.");
            return balance;
        } else {
            BigDecimal initialBalance = balance;
            balance = balance.add(amount);
            return balance;
        }
    }
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invalid withdrawal amount.");
        } else if (amount.compareTo(balance) > 0) {
            System.out.println("Insufficient funds.");
        } else {
            balance = balance.subtract(amount);
            addTransaction(new Transaction(new Date(), TransactionType.WITHDRAWAL, amount));
            System.out.println("Withdrawal successful. New balance: " + balance);
        }
    }
    public void transfer(Account destinationAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invalid transfer amount.");
        } else if (amount.compareTo(balance) > 0) {
            System.out.println("Insufficient funds.");
        } else {
            BigDecimal senderInitialBalance = balance;
            BigDecimal recipientInitialBalance = destinationAccount.getBalance();
            balance = balance.subtract(amount);
            destinationAccount.deposit(amount);
            Transaction transferOutTransaction = new Transaction(new Date(), TransactionType.TRANSFER_OUT, amount.negate());
            addTransaction(transferOutTransaction);
            Transaction transferInTransaction = new Transaction(new Date(), TransactionType.TRANSFER_IN, amount);
            destinationAccount.addTransaction(transferInTransaction);
            System.out.println("Transfer successful.");
            System.out.println("Sender's account balance: " + balance);
            System.out.println("Recipient's account balance: " + destinationAccount.getBalance());
        }
    }
    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }
    public void printAccountStatement() {
        System.out.println("Account Statement for Account Number: " + accountNumber);
        System.out.println("-----------------------------------------------------");
        for (Transaction transaction : transactionHistory) {
            System.out.println(transaction);
        }
        System.out.println("-----------------------------------------------------");
        System.out.println("Current balance: " + balance);
    }
}
enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT
}
class Transaction {
    private Date timestamp;
    private TransactionType type;
    private BigDecimal amount;
    public Transaction(Date timestamp, TransactionType type, BigDecimal amount) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public TransactionType getType() {
        return type;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    @Override
    public String toString() {
        return "Transaction{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", amount=" + amount +
                '}';
    }
}
