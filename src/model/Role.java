package src.model;

public enum Role {
    GRACZ,
    ADMINISTRATOR; // Zostawiamy ADMINISTRATOR, bo tak jest w bazie

    @Override
    public String toString() {
        // Ta metoda służy do wyświetlania roli w interfejsie użytkownika.
        // Możesz tutaj zdecydować, jak ma być formatowany tekst.
        if (this == ADMINISTRATOR) {
            return "Administrator"; // Chcemy, aby ADMINISTRATOR wyświetlał się jako "Administrator"
        }
        // Dla GRACZ i innych ewentualnych przyszłych ról, zachowaj domyślne formatowanie
        // np. GRACZ -> Gracz
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}