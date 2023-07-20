import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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

public class BankingInformationSystemGUI extends JFrame implements ActionListener {
    private List<User> users;
    private List<Account> accounts;

    private JTextField usernameField, passwordField;
    private JButton registerButton, loginButton;
    private JTextArea outputTextArea;

    private User currentUser;
    private Account currentAccount;

    public BankingInformationSystemGUI() {
        users = new ArrayList<>();
        accounts = new ArrayList<>();

        // Create and configure the GUI components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        registerButton = new JButton("Register");
        loginButton = new JButton("Login");
        outputTextArea = new JTextArea(20, 40);
        outputTextArea.setEditable(false);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Username: "));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password: "));
        inputPanel.add(passwordField);
        inputPanel.add(registerButton);
        inputPanel.add(loginButton);

        // Register action listeners
        registerButton.addActionListener(this);
        loginButton.addActionListener(this);

        // Add components to the JFrame
        getContentPane().add(inputPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        // Set window properties
        setTitle("Banking Information System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == registerButton) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            registerUser(username, password);
        } else if (event.getSource() == loginButton) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            loginUser(username, password);
        }
    }

    private void registerUser(String username, String password) {
        User newUser = new User(username, password);
        users.add(newUser);
        Account newAccount = new Account(newUser);
        accounts.add(newAccount);
        outputTextArea.append("User registered with account number: " + newAccount.getAccountNumber() + "\n");
    }

    private void loginUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                currentAccount = getAccountByUser(user);
                if (currentAccount != null) {
                    displayBankingOperations();
                } else {
                    outputTextArea.append("Account not found.\n");
                }
                return;
            }
        }
        outputTextArea.append("Invalid credentials.\n");
    }

    private void displayBankingOperations() {
        JFrame bankingOperationsFrame = new JFrame("Banking Operations");
        bankingOperationsFrame.setLayout(new BorderLayout());

        JTextArea balanceTextArea = new JTextArea(5, 20);
        balanceTextArea.setEditable(false);

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        JButton statementButton = new JButton("Account Statement");
        JButton logoutButton = new JButton("Logout");

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1));
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(transferButton);
        buttonPanel.add(statementButton);
        buttonPanel.add(logoutButton);

        bankingOperationsFrame.add(balanceTextArea, BorderLayout.NORTH);
        bankingOperationsFrame.add(buttonPanel, BorderLayout.CENTER);

        depositButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                BigDecimal depositAmount = new BigDecimal(JOptionPane.showInputDialog("Enter deposit amount:"));
                BigDecimal newBalance = currentAccount.deposit(depositAmount);
                BigDecimal initialBalance = newBalance.subtract(depositAmount);
                balanceTextArea.setText("Deposit successful.\n"
                        + "New balance: " + newBalance + "\n"
                        + "Initial balance: " + initialBalance);
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                BigDecimal withdrawalAmount = new BigDecimal(JOptionPane.showInputDialog("Enter withdrawal amount:"));
                currentAccount.withdraw(withdrawalAmount);
                balanceTextArea.setText("New balance: " + currentAccount.getBalance());
            }
        });

        transferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String destinationAccountNumber = JOptionPane.showInputDialog("Enter destination account number:");
                Account destinationAccount = getAccountByAccountNumber(destinationAccountNumber);
                if (destinationAccount != null) {
                    BigDecimal transferAmount = new BigDecimal(JOptionPane.showInputDialog("Enter transfer amount:"));
                    currentAccount.transfer(destinationAccount, transferAmount);
                } else {
                    balanceTextArea.setText("Destination account not found.");
                }
                balanceTextArea.setText("New balance: " + currentAccount.getBalance());
            }
        });

        statementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentAccount.printAccountStatement();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                bankingOperationsFrame.dispose();
                currentUser = null;
                currentAccount = null;
                usernameField.setText("");
                passwordField.setText("");
                outputTextArea.append("Logged out.\n");
            }
        });

        bankingOperationsFrame.pack();
        bankingOperationsFrame.setLocationRelativeTo(null);
        bankingOperationsFrame.setVisible(true);

        balanceTextArea.setText("Current balance: " + currentAccount.getBalance());
    }

    private Account getAccountByUser(User user) {
        for (Account account : accounts) {
            if (account.getUser().equals(user)) {
                return account;
            }
        }
        return null;
    }

    private Account getAccountByAccountNumber(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BankingInformationSystemGUI();
            }
        });
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
            JOptionPane.showMessageDialog(null, "Invalid deposit amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return balance;
        }
        balance = balance.add(amount);
        Transaction transaction = new Transaction(TransactionType.DEPOSIT, new Date(), amount);
        transactionHistory.add(transaction);
        return balance;
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(null, "Invalid withdrawal amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (amount.compareTo(balance) > 0) {
            JOptionPane.showMessageDialog(null, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        balance = balance.subtract(amount);
        Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, new Date(), amount.negate());
        transactionHistory.add(transaction);
    }

    public void transfer(Account destinationAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(null, "Invalid transfer amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (amount.compareTo(balance) > 0) {
            JOptionPane.showMessageDialog(null, "Insufficient funds.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        balance = balance.subtract(amount);
        destinationAccount.balance = destinationAccount.balance.add(amount);

        Transaction transferOutTransaction = new Transaction(TransactionType.TRANSFER_OUT, new Date(), amount.negate());
        Transaction transferInTransaction = new Transaction(TransactionType.TRANSFER_IN, new Date(), amount);

        transactionHistory.add(transferOutTransaction);
        destinationAccount.transactionHistory.add(transferInTransaction);

        JOptionPane.showMessageDialog(null, "Transfer successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void printAccountStatement() {
        StringBuilder statement = new StringBuilder("Account Statement:\n");
        statement.append("-----------------------------------------------------\n");
        for (Transaction transaction : transactionHistory) {
            statement.append(transaction.toString()).append("\n");
        }
        statement.append("-----------------------------------------------------\n");
        statement.append("Balance: ").append(balance);
        JOptionPane.showMessageDialog(null, statement.toString(), "Account Statement", JOptionPane.INFORMATION_MESSAGE);
    }
}

enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT
}

class Transaction {
    private TransactionType type;
    private Date date;
    private BigDecimal amount;

    public Transaction(TransactionType type, Date date, BigDecimal amount) {
        this.type = type;
        this.date = date;
        this.amount = amount;
    }

    public String toString() {
        String transactionTypeString = "";
        switch (type) {
            case DEPOSIT:
                transactionTypeString = "Deposit";
                break;
            case WITHDRAWAL:
                transactionTypeString = "Withdrawal";
                break;
            case TRANSFER_IN:
                transactionTypeString = "Credit Transfer";
                break;
            case TRANSFER_OUT:
                transactionTypeString = "Debit Transfer";
                break;
        }
        return "Type: " + transactionTypeString + ", Date: " + date + ", Amount: " + amount;
    }
}
