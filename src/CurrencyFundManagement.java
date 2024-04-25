import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class CurrencyFundManagement {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Select currency");
            System.out.println("1. USD");
            System.out.println("2. EUR");
            System.out.println("3. THB");
            System.out.println("4. CNY");
            System.out.println("5. Back");
            System.out.println("Choose an option:");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    changeCurrency("USD");
                    break;
                case 2:
                    changeCurrency("EUR");
                    break;
                case 3:
                    changeCurrency("THB");
                    break;
                case 4:
                    changeCurrency("CNY");
                    break;
                case 5:
                    AccountMenu.main(new String[]{});
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void changeCurrency(String toCurrency) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter amount in CLP to exchange:");
        double amountCLP = scanner.nextDouble();

        try (Connection connection = DriverManager.getConnection(Main.url, Main.user, Main.password)) {

            String selectQuery = "SELECT id " +
                    "FROM accounts " +
                    "WHERE user_id = (" +
                    "SELECT id " +
                    "FROM users " +
                    "WHERE email = ?" +
                    ") " +
                    "AND currency_code = 'CLP'";

            PreparedStatement preparedStatementAccountId = connection.prepareStatement(selectQuery);
            preparedStatementAccountId.setString(1, Server.userEmail);
            ResultSet resultSetAccountId = preparedStatementAccountId.executeQuery();

            UUID accountId;

            if (resultSetAccountId.next()) {
                accountId = (UUID) resultSetAccountId.getObject("id");

                String updateCurrenciesQuery = "UPDATE currencies " +
                        "SET balance = balance - ? " +
                        "WHERE currency_code = 'CLP'";

                PreparedStatement preparedStatementUpdateCurrencies = connection.prepareStatement(updateCurrenciesQuery);
                preparedStatementUpdateCurrencies.setDouble(1, amountCLP * getExchangeRate(toCurrency));
                preparedStatementUpdateCurrencies.executeUpdate();

                String updateToCurrencyQuery = "UPDATE currencies " +
                        "SET balance = balance + ? " +
                        "WHERE currency_code = ?";

                PreparedStatement preparedStatementUpdateToCurrency = connection.prepareStatement(updateToCurrencyQuery);
                preparedStatementUpdateToCurrency.setDouble(1, amountCLP * getExchangeRate(toCurrency));
                preparedStatementUpdateToCurrency.setString(2, toCurrency);
                preparedStatementUpdateToCurrency.executeUpdate();

                String insertCurrenciesExchangeQuery = "INSERT INTO currencies_exchange " +
                        "(account_id, from_currency_code, to_currency_code, amount, exchange_rate) " +
                        "VALUES (?, 'CLP', ?, ?, ?)";

                PreparedStatement preparedStatementInsertCurrenciesExchange = connection.prepareStatement(insertCurrenciesExchangeQuery);
                preparedStatementInsertCurrenciesExchange.setObject(1, accountId);
                preparedStatementInsertCurrenciesExchange.setString(2, toCurrency);
                preparedStatementInsertCurrenciesExchange.setDouble(3, amountCLP);
                preparedStatementInsertCurrenciesExchange.setDouble(4, getExchangeRate(toCurrency));
                preparedStatementInsertCurrenciesExchange.executeUpdate();

                System.out.println("Currency changed successfully.");
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    private static double getExchangeRate(String currency) {
        return switch (currency) {
            case "USD" -> 0.005;
            case "EUR" -> 0.006;
            case "THB" -> 0.15;
            case "CNY" -> 0.032;
            default -> 1.0;
        };
    }
}
