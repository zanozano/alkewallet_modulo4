import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;

public class Main {

    public static String url;
    public static String user;
    public static String password;

    public static boolean isLoggedIn = false;

    //load variables
    public static void loadEnvVariables() {
        try {
            Properties envProperties = new Properties();

            try (InputStream input = Main.class.getResourceAsStream("/resources/env.properties")) {
                if (input == null) {
                    throw new IOException("File not found");
                }
                envProperties.load(input);
            }

            url = envProperties.getProperty("DATABASE_URL");
            user = envProperties.getProperty("DATABASE_USER");
            password = envProperties.getProperty("DATABASE_PASSWORD");
            System.out.println(url);
            System.out.println(user);
            System.out.println(password);

        } catch (IOException e) {
            System.err.println("Error loading environment variables: " + e.getMessage());
        }
    }

    //main
    public static void main(String[] args) throws IOException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading SQL JDBC driver: " + e.getMessage());
            return;
        }

        Server.startServer();
        loadEnvVariables();
    }

    //login
    public static void validateAccount(String userEmail, String userPassword) {
        String selectQuery = "SELECT name, email FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setString(1, userEmail);
            preparedStatement.setString(2, userPassword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("User logged successfully");
                    isLoggedIn = true;
                    // init menu
                    AccountMenu.main(new String[]{});
                } else {
                    System.out.println("Invalid email or password.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    //logout
    public static void logout() {
        isLoggedIn = false;
    }
}
