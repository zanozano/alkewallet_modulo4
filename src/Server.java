import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Server {

    public static String userEmail;
    public static String userPassword;

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/", new GetData());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 3000");
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

            System.out.println("Request body: " + requestBody);

            Gson gson = new Gson();
            UserCredentials userCredentials = gson.fromJson(requestBody, UserCredentials.class);

            userEmail = userCredentials.getEmail();
            userPassword = userCredentials.getPassword();

            System.out.println("User Email: " + userEmail);
            System.out.println("User Password: " + userPassword);

            Main.validateAccount(userEmail, userPassword);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", Main.isLoggedIn);

            String responseString = jsonResponse.toString();

            sendResponse(exchange, new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8)), 200);
            System.out.println("Send Request");
        }
    }

    //credentials
    public static class UserCredentials {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }
    }
}
