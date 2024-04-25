import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.UUID;

public class AccountMenu {

    public static void main(String[] args) {
        String userEmail = args[0];
        Navigation navigation = new Navigation() {

            // menu
            @Override
            public void showMenu(String userEmail) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("""
                            1. View Balance
                            2. Deposit
                            3. Withdraw
                            4. Currency Exchange
                            5. Logout
                            Choose an option:""");

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            viewBalance(userEmail);
                            break;
                        case 2:
                            deposit(userEmail);
                            break;
                        case 3:
                            withdraw(userEmail);
                            break;
                        case 4:
                            CurrencyFundManagement.main(new String[]{userEmail});
                            break;
                        case 5:
                            System.out.println("Logged out.");
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            // view balances
            public void viewBalance(String userEmail) {
                String selectQuery = "SELECT c.currency_code, b.amount " +
                        "FROM users u " +
                        "JOIN accounts a ON u.id = a.user_id " +
                        "JOIN balances b ON a.id = b.account_id " +
                        "JOIN currencies c ON b.currency_code = c.currency_code " +
                        "WHERE u.email = ?";

                try (Connection connection = DriverManager.getConnection(Server.url, Server.user, Server.password)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                    preparedStatement.setString(1, userEmail);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        System.out.println("Your balances:");
                        do {
                            String currencyCode = resultSet.getString("currency_code");
                            double balance = resultSet.getDouble("amount");
                            System.out.println(currencyCode + ": $" + balance);
                        } while (resultSet.next());
                    } else {
                        System.out.println("No balances found for the user with email: " + userEmail);
                    }
                    // show menuW
                    showMenu(userEmail);
                } catch (SQLException e) {
                    System.out.println("Error connecting to database: " + e.getMessage());
                }
            }

            // deposit CLP
            public void deposit(String userEmail) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("Enter deposit amount:");
                    double amount = scanner.nextDouble();

                    try (Connection connection = DriverManager.getConnection(Server.url, Server.user, Server.password)) {

                        String userIdQuery = "SELECT id FROM users WHERE email = ?";
                        PreparedStatement userIdStatement = connection.prepareStatement(userIdQuery);
                        userIdStatement.setString(1, userEmail);
                        ResultSet userIdResult = userIdStatement.executeQuery();
                        String userId;
                        if (userIdResult.next()) {
                            userId = userIdResult.getString("id");
                        } else {
                            System.out.println("User not found with email: " + userEmail);
                            return;
                        }

                        String updateQuery = "UPDATE balances AS b " +
                                "SET amount = amount + ? " +
                                "FROM accounts AS a " +
                                "JOIN users AS u ON a.user_id = u.id " +
                                "WHERE b.account_id = a.id " +
                                "AND u.email = ? " +
                                "AND b.currency_code = 'CLP'";

                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        updateStatement.setDouble(1, amount);
                        updateStatement.setString(2, userEmail);
                        int rowsUpdated = updateStatement.executeUpdate();

                        if (rowsUpdated > 0) {

                            String insertQuery = "INSERT INTO transactions (sender_id, receiver_id, amount, currency_code) " +
                                    "VALUES (?, ?, ?, 'CLP')";
                            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                            insertStatement.setObject(1, UUID.fromString(userId));
                            insertStatement.setObject(2, UUID.fromString(userId));
                            insertStatement.setDouble(3, amount);
                            insertStatement.executeUpdate();

                            System.out.println("Deposit to your CLP account successful.");
                        } else {
                            System.out.println("Error depositing.");
                        }
                        // show menu
                        showMenu(userEmail);
                    } catch (SQLException e) {
                        System.out.println("Error connecting to database: " + e.getMessage());
                    }
                }
            }


            // withdraw CLP
            public void withdraw(String userEmail) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("Enter withdrawal amount:");
                    double amount = scanner.nextDouble();
                    try (Connection connection = DriverManager.getConnection(Server.url, Server.user, Server.password)) {

                        String userIdQuery = "SELECT id FROM users WHERE email = ?";
                        PreparedStatement userIdStatement = connection.prepareStatement(userIdQuery);
                        userIdStatement.setString(1, userEmail);
                        ResultSet userIdResult = userIdStatement.executeQuery();
                        String userId;

                        if (userIdResult.next()) {
                            userId = userIdResult.getString("id");
                        } else {
                            System.out.println("User not found with email: " + userEmail);
                            return;
                        }

                        String updateQuery  = "UPDATE balances AS b " +
                                "SET amount = amount - ? " +
                                "FROM accounts AS a " +
                                "JOIN users AS u ON a.user_id = u.id " +
                                "WHERE b.account_id = a.id " +
                                "AND u.email = ? " +
                                "AND b.currency_code = 'CLP'";

                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery );
                        updateStatement.setDouble(1, amount);
                        updateStatement.setString(2, userEmail);
                        int rowsUpdated = updateStatement.executeUpdate();

                        if (rowsUpdated > 0) {

                            String insertQuery = "INSERT INTO transactions (sender_id, receiver_id, amount, currency_code) " +
                                    "VALUES (?, ?, ?, 'CLP')";
                            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                            insertStatement.setObject(1, UUID.fromString(userId));
                            insertStatement.setObject(2, UUID.fromString(userId));
                            insertStatement.setDouble(3, amount);
                            insertStatement.executeUpdate();
                            System.out.println("Withdrawal successful.");
                        } else {
                            System.out.println("Error withdrawing.");
                        }
                        // show menu
                        showMenu(userEmail);
                    } catch (SQLException e) {
                        System.out.println("Error connecting to database: " + e.getMessage());
                    }
                }
            }
        };
        navigation.showMenu(userEmail);
    }
}
