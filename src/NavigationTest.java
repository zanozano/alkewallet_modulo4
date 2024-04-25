import org.junit.jupiter.api.Test;

class MockNavigation implements Navigation {

    @Override
    public void showMenu(String userEmail) {
        System.out.println("Menu: " + userEmail);
    }
}

class NavigationTest {

    @Test
    void testShowMenu() {

        Navigation menuNavigation = new MockNavigation();
        String userEmail = "test@alkewallet.com";

        menuNavigation.showMenu(userEmail);
    }
}
