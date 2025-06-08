package src.model;

public class User {
    protected int id;
    protected String login;
    protected String password;
    protected String firstname; // Pole z małej litery 'f'
    protected String lastname;  // Pole z małej litery 'l'
    protected Role role;

    // Konstruktor dla istniejących użytkowników (z bazy danych)
    public User(int id, String login, String password, String firstname, String lastname, Role role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
    }

    // Konstruktor dla nowych użytkowników (przed zapisem do bazy)
    public User(String login, String password, String firstname, String lastname, Role role) {
        this.login = login;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
        // ID nie jest ustawiane, będzie generowane przez bazę
    }

    // --- Gettery ---
    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    // Poprawione nazwy metod zgodnie z konwencją Java (CamelCase)
    public String getFirstName() { // Zmieniono z getFirstname()
        return firstname;
    }

    public String getLastName() { // Zmieniono z getLastname()
        return lastname;
    }

    public Role getRole() {
        return role;
    }

    // --- Settery ---
    public void setId(int id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Poprawione nazwy metod zgodnie z konwencją Java (CamelCase)
    public void setFirstName(String firstname) { // Zmieniono z setFirstname()
        this.firstname = firstname;
    }

    public void setLastName(String lastname) { // Zmieniono z setLastname()
        this.lastname = lastname;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", role=" + role +
                '}';
    }
}