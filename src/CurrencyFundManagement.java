import java.sql.*;
import java.util.Scanner;

public class CurrencyFundManagement {

    public static void main(String[] args) {

        String userEmail = args[0];
        Navigation navigation = new Navigation() {

            //menu
            @Override
            public void showMenu(String userEmail) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("""
                    Select currency
                    1. USD
                    2. EUR
                    3. THB
                    4. CNY
                    5. Back
                    Choose an option:""");

                    int choice = scanner.nextInt();
                    switch (choice) {
                        case 1:
                            changeCurrency("USD", userEmail);
                            break;
                        case 2:
                            changeCurrency("EUR", userEmail);
                            break;
                        case 3:
                            changeCurrency("THB", userEmail);
                            break;
                        case 4:
                            changeCurrency("CNY", userEmail);
                            break;
                        case 5:
                            AccountMenu.main(new String[]{userEmail});
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            public void changeCurrency(String toCurrency, String userEmail) {
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.println("Enter amount in CLP to exchange:");
                    double amountCLP = scanner.nextDouble();

                    try (Connection connection = DriverManager.getConnection(Server.url, Server.user, Server.password)) {

                        // reduce money from CLP Account
                        String updateCurrenciesQuery = "UPDATE balances AS b " +
                                "SET amount = amount - ? " +
                                "FROM accounts AS a " +
                                "JOIN users AS u ON a.user_id = u.id " +
                                "WHERE b.account_id = a.id " +
                                "AND u.email = ? " +
                                "AND b.currency_code = 'CLP'";

                        PreparedStatement preparedStatementUpdateCurrencies = connection.prepareStatement(updateCurrenciesQuery);
                        preparedStatementUpdateCurrencies.setDouble(1, amountCLP * getExchangeRate(toCurrency));
                        preparedStatementUpdateCurrencies.setString(2, userEmail);
                        preparedStatementUpdateCurrencies.executeUpdate();

                        // add money to [currency_code] Account
                        String updateToCurrencyQuery = "UPDATE balances AS b " +
                                "SET amount = amount + ? " +
                                "FROM accounts AS a " +
                                "JOIN users AS u ON a.user_id = u.id " +
                                "WHERE b.account_id = a.id " +
                                "AND u.email = ? " +
                                "AND b.currency_code = ? ";

                        PreparedStatement preparedStatementUpdateToCurrency = connection.prepareStatement(updateToCurrencyQuery);
                        preparedStatementUpdateToCurrency.setDouble(1, amountCLP * getExchangeRate(toCurrency));
                        preparedStatementUpdateToCurrency.setString(2, userEmail);
                        preparedStatementUpdateToCurrency.setString(3, toCurrency);
                        preparedStatementUpdateToCurrency.executeUpdate();

                        System.out.printf("Successfully exchanged %.2f CLP to %.2f %s at a rate of %.4f%n", amountCLP, amountCLP * getExchangeRate(toCurrency), toCurrency, getExchangeRate(toCurrency));

                        // show menu
                        showMenu(userEmail);
                    } catch (SQLException e) {
                        System.out.println("Error connecting to database: " + e.getMessage());
                    }
                }
            }

            public double getExchangeRate(String currency) {
                return switch (currency) {
                    case "USD" -> 0.00108;
                    case "EUR" -> 0.00100;
                    case "THB" -> 0.0396045;
                    case "CNY" -> 0.00780;
                    default -> 1.0;
                };
            }
        };

        navigation.showMenu(userEmail);
    }
}
