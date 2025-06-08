package src.model;

public class Admin extends User {

    // *** TEN KONSTRUKTOR JEST POTRZEBNY I OCZEKIWANY PRZEZ UserDAO ***
    // Przyjmuje 6 argumentów, włącznie z firstname, lastname i role,
    // i przekazuje je do odpowiedniego konstruktora klasy bazowej User.
    public Admin(int id, String login, String password, String firstname, String lastname, Role role) {
        super(id, login, password, firstname, lastname, role);
    }

    // Opcjonalny konstruktor dla tworzenia nowego obiektu Admina,
    // zanim zostanie on zapisany do bazy danych (ID zostanie wygenerowane przez bazę).
    // Domyślnie rola jest ustawiona na ADMINISTRATOR.
    public Admin(String login, String password, String firstname, String lastname) {
        super(login, password, firstname, lastname, Role.ADMINISTRATOR);
    }

    // Jeśli masz specyficzne pola dla administratora, dodaj je tutaj
    // i rozszerz odpowiednie konstruktory, aby je inicjalizować.
    // Na przykład:
    /*
    private String adminSpecificField;

    public Admin(int id, String login, String password, String firstname, String lastname, Role role, String adminSpecificField) {
        super(id, login, password, firstname, lastname, role);
        this.adminSpecificField = adminSpecificField;
    }
    */

    // Opcjonalnie: nadpisanie metody toString() dla łatwiejszego debugowania
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", login='" + getLogin() + '\'' +
                ", firstname='" + getFirstName() + '\'' +
                ", lastname='" + getLastName() + '\'' +
                ", role=" + getRole() +
                '}';
    }
}