package src.model;

public class User {
    protected int id;
    protected String login;
    protected String password;
    protected String firstname;
    protected String lastname;
    protected Role role;

    // Ten konstruktor jest używany, gdy pobierasz użytkownika z bazy danych
    // lub gdy aktualizujesz istniejącego użytkownika (gdzie ID jest już znane).
    public User(int id, String login, String password, String firstname, String lastname, Role role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
    }

    // Ten konstruktor jest dla tworzenia zupełnie nowego obiektu User
    // zanim zostanie on zapisany w bazie danych. Baza danych wygeneruje ID.
    public User(String login, String password, String firstname, String lastname, Role role) {
        // Pole 'id' nie jest tutaj ustawiane, ponieważ jest zazwyczaj auto-generowane przez bazę danych
        // podczas wstawiania. Metoda addUser w UserDAO powinna później pobrać i ustawić to ID.
        this.login = login;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
    }

    // Usunięto tutaj problematyczny, pusty konstruktor:
    // public User(int id, String login, String password, Role role) { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Opcjonalnie: Możesz nadpisać metodę toString() dla łatwiejszego debugowania
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