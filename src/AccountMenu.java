import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountMenu {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. View Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Currency Exchange");
            System.out.println("5. Logout");
            System.out.println("Choose an option:");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    viewBalance(Server.userEmail);
                    break;
                case 2:
                    deposit(Server.userEmail);
                    break;
                case 3:
                    withdraw(Server.userEmail);
                    break;
                case 4:
                    CurrencyFundManagement.main(new String[]{});
                    return;
                case 5:
                    System.out.println("Logged out.");
                    Main.logout();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewBalance(String userEmail) {
        try (Connection connection = DriverManager.getConnection(Main.url, Main.user, Main.password)) {
            String selectQuery = "SELECT * FROM currencies";

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Your balances:");

                do {
                    String currencyCode = resultSet.getString("currency_code");
                    double balance = resultSet.getDouble("balance");
                    System.out.println(currencyCode + ": $" + balance);
                } while (resultSet.next());

            } else {
                System.out.println("Error retrieving balance.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    private static void deposit(String userEmail) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter deposit amount:");
        double amount = scanner.nextDouble();

        try (Connection connection = DriverManager.getConnection(Main.url, Main.user, Main.password)) {
            String query = "UPDATE currencies " +
                    "SET balance = balance + ? " +
                    "FROM users " +
                    "JOIN accounts ON users.id = accounts.user_id " +
                    "WHERE users.email = ? AND accounts.currency_code = 'CLP'";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, userEmail);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Deposit successful.");
            } else {
                System.out.println("Error depositing.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    private static void withdraw(String userEmail) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter withdrawal amount:");
        double amount = scanner.nextDouble();

        try (Connection connection = DriverManager.getConnection(Main.url, Main.user, Main.password)) {
            String query = "UPDATE currencies " +
                    "SET balance = balance - ? " +
                    "FROM users " +
                    "JOIN accounts ON users.id = accounts.user_id " +
                    "WHERE users.email = ? AND accounts.currency_code = 'CLP'";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, userEmail);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Error withdrawing.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }
}
