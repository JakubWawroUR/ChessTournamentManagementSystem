package src.model;

public enum Role {
    GRACZ,
    ADMINISTRATOR;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}