package src.model;

public class Admin extends User {

    public Admin(int id, String login, String password, String firstname, String lastname, Role role) {
        super(id, login, password, firstname, lastname, role);
    }

    public Admin(String login, String password, String firstname, String lastname) {
        super(login, password, firstname, lastname, Role.ADMINISTRATOR);
    }

}