package src.model;

public enum Role {
    GRACZ,
    ADMINISTRATOR; // Tutaj musi być CAŁKOWICIE wielkimi literami

    @Override
    public String toString() {
        // To jest do celów wyświetlania, a nie do Role.valueOf()
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}