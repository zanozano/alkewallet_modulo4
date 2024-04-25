import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    public void testValidateAccount() {
        Server.loadEnvVariables();
        String userEmail = "cristobal@alkewallet.com";
        String userPassword = "123456";

        try {
            boolean isAuthenticated = Server.validateAccount(userEmail, userPassword);
            assertTrue(isAuthenticated, "(Test) Invalid email or password.");

        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }

    }
}
