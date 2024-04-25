import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Server {

    public static String url;
    public static String user;
    public static String password;

    //start server
    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/", new GetData());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 3000");
    }

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

        } catch (IOException e) {
            System.err.println("Error loading environment variables: " + e.getMessage());
        }
    }

    //read data
    static class GetData implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();

            if ("GET".equals(requestMethod)) {
                readFile(exchange);
            } else if ("POST".equals(requestMethod) && "/login".equals(exchange.getRequestURI().getPath())) {
                handleDataFromWeb(exchange);
            } else {
                System.out.println("Error");
            }
        }

        //load HTML
        private void readFile(HttpExchange exchange) throws IOException {
            try {
                File htmlFile = new File("index.html");
                System.out.println("Attempting to serve HTML file: " + htmlFile.getAbsolutePath());

                if (htmlFile.exists()) {
                    sendResponse(exchange, new FileInputStream(htmlFile), 200);
                } else {
                    System.out.println("HTML file does not exist.");
                    sendResponse(exchange, new ByteArrayInputStream("The file does not exist.".getBytes(StandardCharsets.UTF_8)), 404);
                }
            } catch (IOException e) {
                System.out.println("Error serving the HTML file: " + e.getMessage());
                sendResponse(exchange, new ByteArrayInputStream(("Error serving the file: " + e.getMessage()).getBytes(StandardCharsets.UTF_8)), 500);
            }
        }

        //send response
        private void sendResponse(HttpExchange exchange, InputStream responseStream, int statusCode) throws IOException {
            exchange.sendResponseHeaders(statusCode, 0);
            try (responseStream; OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = responseStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            } catch (IOException e) {
                System.out.println("Error sending response: " + e.getMessage());
                throw e;
            }
        }

        //handle data from web
        private void handleDataFromWeb(HttpExchange exchange) throws IOException {
            InputStream requestBodyStream = exchange.getRequestBody();
            byte[] requestBodyBytes = requestBodyStream.readAllBytes();
            requestBodyStream.close();

            String requestBody = new String(requestBodyBytes, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            User user = gson.fromJson(requestBody, User.class);

            JsonObject jsonResponse = new JsonObject();

            if (validateAccount(user.getUserEmail(), user.getUserPassword())) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Logged successfully");
                jsonResponse.addProperty("email", user.getUserEmail());
                new Thread(() ->AccountMenu.main(new String[]{user.getUserEmail()})).start();
            } else {
                jsonResponse.addProperty("error", false);
                jsonResponse.addProperty("message", "Invalid email or password");
                jsonResponse.addProperty("email", user.getUserEmail());
            }

            String responseString = jsonResponse.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseString.getBytes().length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(responseString.getBytes());
            }
        }
    }

    //login
    public static boolean validateAccount(String userEmail, String userPassword) {

        String selectQuery = "SELECT * FROM users WHERE email = ? AND password = ?";
        boolean isAuthenticated = false;

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setString(1, userEmail);
            preparedStatement.setString(2, userPassword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("User logged successfully");
                    isAuthenticated = true;
                } else {
                    System.out.println("Invalid email or password.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
        return isAuthenticated;
    }

}
