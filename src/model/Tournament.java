package src.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Tournament {
    protected int id;
    protected StringProperty name;
    protected StringProperty startDate;
    protected StringProperty endDate;
    protected IntegerProperty maxSlots;
    protected IntegerProperty freeSlots;
    protected StringProperty status; // DODANE POLE

    /**
     * Konstruktor dla NOWYCH turniejów.
     * ID zostanie przypisane przez bazę danych po dodaniu.
     * Wolne miejsca (freeSlots) są domyślnie ustawiane na równi z maksymalnymi miejscami (maxSlots).
     * Status domyślny: "OTWARTY".
     *
     * @param name Nazwa turnieju.
     * @param startDate Data rozpoczęcia turnieju (String).
     * @param endDate Data zakończenia turnieju (String).
     * @param maxSlots Maksymalna liczba miejsc w turnieju.
     */
    public Tournament(String name, String startDate, String endDate, int maxSlots) {
        this.name = new SimpleStringProperty(name);
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
        this.maxSlots = new SimpleIntegerProperty(maxSlots);
        this.freeSlots = new SimpleIntegerProperty(maxSlots);
        this.status = new SimpleStringProperty("OTWARTY"); // Domyślny status dla nowego turnieju
    }

    /**
     * Konstruktor dla ISTNIEJĄCYCH turniejów pobieranych z bazy danych.
     * Wymaga podania wszystkich danych, w tym ID i aktualnej liczby wolnych miejsc oraz statusu.
     *
     * @param id ID turnieju.
     * @param name Nazwa turnieju.
     * @param startDate Data rozpoczęcia turnieju (String).
     * @param endDate Data zakończenia turnieju (String).
     * @param maxSlots Maksymalna liczba miejsc w turnieju.
     * @param freeSlots Aktualna liczba wolnych miejsc w turnieju.
     * @param status Aktualny status turnieju (np. "OTWARTY", "ZAMKNIĘTY", "W TRAKCIE", "ZAKOŃCZONY").
     */
    public Tournament(int id, String name, String startDate, String endDate, int maxSlots, int freeSlots, String status) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
        this.maxSlots = new SimpleIntegerProperty(maxSlots);
        this.freeSlots = new SimpleIntegerProperty(freeSlots);
        this.status = new SimpleStringProperty(status); // Ustaw status z bazy
    }

    // --- Gettery i Settery ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getStartDate() {
        return startDate.get();
    }

    public StringProperty startDateProperty() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate.set(startDate);
    }

    public String getEndDate() {
        return endDate.get();
    }

    public StringProperty endDateProperty() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate.set(endDate);
    }

    public int getMaxSlots() {
        return maxSlots.get();
    }

    public IntegerProperty maxSlotsProperty() {
        return maxSlots;
    }

    public void setMaxSlots(int maxSlots) {
        this.maxSlots.set(maxSlots);
    }

    public int getFreeSlots() {
        return freeSlots.get();
    }

    public IntegerProperty freeSlotsProperty() {
        return freeSlots;
    }

    public void setFreeSlots(int freeSlots) {
        this.freeSlots.set(freeSlots);
    }

    public String getStatus() { // GETTER DLA STATUSU
        return status.get();
    }

    public StringProperty statusProperty() { // PROPERTY DLA STATUSU
        return status;
    }

    public void setStatus(String status) { // SETTER DLA STATUSU
        this.status.set(status);
    }

    /**
     * Zwraca informację o wolnych/maksymalnych miejscach w formacie "wolne/maksymalne".
     * Przydatne do wyświetlania w UI.
     * @return String z informacją o miejscach.
     */
    public String getSlotsInfo() {
        return (freeSlots != null ? freeSlots.get() : "N/A") + "/" + (maxSlots != null ? maxSlots.get() : "N/A");
    }
}