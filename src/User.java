public class User {

    private final String email;
    private final String password;

    public User(String userEmail, String userPassword) {
        this.email = userEmail;
        this.password = userPassword;
    }

    public String getUserEmail() {
        return email;
    }


    public String getUserPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "userEmail='" + email + '\'' +
                ", userPassword='" + password + '\'' +
                '}';
    }
}


