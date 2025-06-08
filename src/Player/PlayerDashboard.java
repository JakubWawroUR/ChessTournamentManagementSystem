package src.Player;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane; // Możesz usunąć, jeśli nie używasz jawnie w kodzie
import javafx.scene.layout.VBox;    // Możesz usunąć, jeśli nie używasz jawnie w kodzie
import src.model.Player;
import src.model.Tournament; // Dodaj import dla obiektu Tournament

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PlayerDashboard implements Initializable {
    @FXML private AnchorPane contentPane; // Główny panel do ładowania widoków
    @FXML private PlayerNavigationController playerNavigationPanelController; // Wstrzyknięty kontroler nawigacji
    @FXML private PlayerTopBar playerTopBarController; // Wstrzyknięty kontroler paska górnego

    private Player currentPlayer; // Obiekt gracza zalogowanego w tej sesji
    private Timeline debugTimer; // Timer do celów debugowania
    private boolean initialized = false; // Flaga wskazująca, czy initialize() zostało uruchomione

    /**
     * Ustawia obiekt gracza dla tego Dashboardu. Wywoływane po załadowaniu FXML.
     * @param player Zalogowany obiekt gracza.
     */
    public void setPlayer(Player player) {
        this.currentPlayer = player;
        System.out.println("PlayerDashboard (setPlayer): Player data received: " + player.getFirstName() + " (currentPlayer set to " + (this.currentPlayer != null ? "NOT-NULL" : "NULL") + ").");

        // Jeśli initialize już się wykonało, zaktualizuj UI natychmiast
        if (initialized) {
            updateUIWithPlayerInfo();
            System.out.println("PlayerDashboard (setPlayer): UI updated immediately as initialize() already ran.");
        } else {
            System.out.println("PlayerDashboard (setPlayer): initialize() has not run yet. UI will be updated in initialize().");
        }
    }

    /**
     * Metoda initialize() jest wywoływana przez FXMLLoader po przetworzeniu wszystkich elementów FXML.
     * Jest to punkt, w którym można zainicjować kontroler i jego komponenty.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("PlayerDashboard (initialize): Metoda initialize() rozpoczyna działanie.");

        // Sprawdzenie, czy currentPlayer jest null na początku initialize (oczekiwane zachowanie)
        if (this.currentPlayer == null) {
            System.out.println("PlayerDashboard (initialize): currentPlayer jest NULL na początku initialize(). Jest to oczekiwane, jeśli setPlayer() zostanie wywołane później.");
        } else {
            System.out.println("PlayerDashboard (initialize): currentPlayer jest NIE-NULL na początku initialize(): " + currentPlayer.getFirstName() + ". (Niespodziewane, ale dobre).");
        }

        // Ustawienie referencji PlayerDashboard w PlayerNavigationController
        if (playerNavigationPanelController != null) {
            this.playerNavigationPanelController.setPlayerDashboardController(this);
            System.out.println("PlayerDashboard (initialize): PlayerNavigationController ustawiony (wstrzyknięty) i referencja przekazana.");
        } else {
            System.err.println("PlayerDashboard (initialize): BŁĄD! playerNavigationPanelController jest null po wstrzyknięciu FXML.");
        }

        // Ustawienie referencji PlayerDashboard w PlayerTopBarController
        if (playerTopBarController != null) {
            this.playerTopBarController.setPlayerDashboardController(this); // KLUCZOWA LINIA
            System.out.println("PlayerDashboard (initialize): PlayerTopBar ustawiony (wstrzyknięty) i referencja PlayerDashboard przekazana.");

            // Jeśli currentPlayer jest już dostępny, zaktualizuj UI (rzadki przypadek z obecnym przepływem)
            if (this.currentPlayer != null) {
                updateUIWithPlayerInfo();
                System.out.println("PlayerDashboard (initialize): UI updated from initialize() because currentPlayer was already available.");
            } else {
                System.out.println("PlayerDashboard (initialize): currentPlayer jest NULL. Aktualizacja UI odłożona do setPlayer().");
            }
        } else {
            System.err.println("PlayerDashboard (initialize): BŁĄD! playerTopBarController jest null po wstrzyknięciu FXML. Sprawdź fx:id w PlayerDashboard.fxml!");
        }

        // Ustawienie flagi na true po zakończeniu initialize
        initialized = true;

        // Uruchomienie timera debugowania
        startDebugTimer();
    }

    /**
     * Aktualizuje elementy UI (np. pasek górny) danymi gracza.
     */
    private void updateUIWithPlayerInfo() {
        if (currentPlayer != null) {
            if (playerTopBarController != null) {
                playerTopBarController.setWelcomeText("Witaj, " + currentPlayer.getFirstName() + " " + currentPlayer.getLastName());
                playerTopBarController.setRankingText("Twój ranking: " + currentPlayer.getRanking());
                System.out.println("PlayerDashboard: UI elements updated with player data.");
                // Tutaj można załadować domyślny widok, np. handleShowTournaments();
                // Jeśli chcesz, aby domyślnie po zalogowaniu widoczne były turnieje, odkomentuj poniższą linię.
                // handleShowTournaments();
            } else {
                System.err.println("PlayerDashboard: Nie można zaktualizować UI; playerTopBarController jest null w updateUIWithPlayerInfo().");
            }
        } else {
            System.err.println("PlayerDashboard: Nie można zaktualizować UI; currentPlayer jest null w updateUIWithPlayerInfo().");
        }
    }

    /**
     * Obsługuje wyświetlanie widoku turniejów.
     */
    @FXML
    public void handleShowTournaments() {
        if (currentPlayer == null) {
            System.err.println("PlayerDashboard: currentPlayer jest NULL w handleTournaments! Nie można załadować turniejów.");
            // Próba aktualizacji UI, jeśli gracz zostanie ustawiony później (np. przez setPlayer)
            if (initialized && this.currentPlayer != null) {
                System.out.println("PlayerDashboard: Ponawiam handleShowTournaments, ponieważ currentPlayer jest już dostępny.");
                updateUIWithPlayerInfo(); // Upewnij się, że ten krok jest potrzebny i nie powoduje pętli
                return;
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerTournaments.fxml"));
            Parent tournamentsView = loader.load();

            Object controller = loader.getController();
            if (controller instanceof PlayerTournaments tournamentsController) {
                tournamentsController.initData(currentPlayer);
                // NOWOŚĆ: Przekaż referencję do PlayerDashboard do PlayerTournaments
                tournamentsController.setPlayerDashboardController(this);
                System.out.println("PlayerDashboard: Kontroler PlayerTournaments zainicjowany danymi gracza i referencją do PlayerDashboard.");
            } else {
                System.err.println("PlayerDashboard: Kontroler dla PlayerTournaments.fxml nie jest instancją PlayerTournaments lub jest null!");
            }

            if (contentPane == null) {
                System.err.println("PlayerDashboard: contentPane jest null! Nie można załadować widoku turniejów.");
                return;
            }

            contentPane.getChildren().setAll(tournamentsView);
            AnchorPane.setTopAnchor(tournamentsView, 0.0);
            AnchorPane.setBottomAnchor(tournamentsView, 0.0);
            AnchorPane.setLeftAnchor(tournamentsView, 0.0);
            AnchorPane.setRightAnchor(tournamentsView, 0.0);
            System.out.println("PlayerDashboard: Pomyślnie załadowano i ustawiono widok turniejów.");

        } catch (IOException e) {
            System.err.println("PlayerDashboard: Krytyczny błąd podczas ładowania PlayerTournaments.fxml: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("PlayerDashboard: Nieoczekiwany błąd w handleShowTournaments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NOWA METODA: Obsługuje wyświetlanie widoku informacji o konkretnym turnieju.
     * Wywoływana przez PlayerTournaments, gdy użytkownik wybierze turniej.
     * @param tournament Turniej, którego informacje mają być wyświetlone.
     */
    public void handleShowTournamentInfo(Tournament tournament) {
        if (currentPlayer == null) {
            System.err.println("PlayerDashboard: currentPlayer jest NULL w handleShowTournamentInfo! Nie można załadować informacji o turnieju.");
            return;
        }
        if (tournament == null) {
            System.err.println("PlayerDashboard: tournament jest NULL w handleShowTournamentInfo! Nie można załadować informacji o turnieju.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerTournamentInfo.fxml"));
            Parent tournamentInfoView = loader.load();

            Object controller = loader.getController();
            if (controller instanceof PlayerTournamentInfo tournamentInfoController) {
                tournamentInfoController.initData(tournament, currentPlayer);
                // KLUCZOWE: Przekaż referencję do PlayerDashboard do PlayerTournamentInfo
                tournamentInfoController.setPlayerDashboardController(this); // 'this' odnosi się do PlayerDashboard
                System.out.println("PlayerDashboard: Kontroler PlayerTournamentInfo zainicjowany danymi turnieju, gracza i referencją do PlayerDashboard.");
            } else {
                System.err.println("PlayerDashboard: Kontroler dla PlayerTournamentInfo.fxml nie jest instancją PlayerTournamentInfo lub jest null!");
            }

            if (contentPane == null) {
                System.err.println("PlayerDashboard: contentPane jest null! Nie można załadować widoku informacji o turnieju.");
                return;
            }

            contentPane.getChildren().setAll(tournamentInfoView);
            AnchorPane.setTopAnchor(tournamentInfoView, 0.0);
            AnchorPane.setBottomAnchor(tournamentInfoView, 0.0);
            AnchorPane.setLeftAnchor(tournamentInfoView, 0.0);
            AnchorPane.setRightAnchor(tournamentInfoView, 0.0);
            System.out.println("PlayerDashboard: Pomyślnie załadowano i ustawiono widok informacji o turnieju.");

        } catch (IOException e) {
            System.err.println("PlayerDashboard: Krytyczny błąd podczas ładowania PlayerTournamentInfo.fxml: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("PlayerDashboard: Nieoczekiwany błąd w handleShowTournamentInfo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Obsługuje wyświetlanie widoku profilu gracza.
     */
    @FXML
    public void handleShowProfile() {
        if (currentPlayer == null) {
            System.err.println("PlayerDashboard: currentPlayer jest NULL w handleShowProfile! Nie można załadować profilu.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/Player/PlayerProfile.fxml"));
            Parent profileView = loader.load();

            Object controller = loader.getController();
            // Zakładam, że masz PlayerProfile.java i PlayerProfileController
            // Jeśli PlayerProfileController będzie potrzebował referencji do PlayerDashboard, dodaj to tutaj:
            // if (controller instanceof PlayerProfileController profileController) {
            //     profileController.initData(currentPlayer);
            //     profileController.setPlayerDashboardController(this); // Jeśli PlayerProfileController będzie mógł "wrócić"
            //     System.out.println("PlayerDashboard: Kontroler PlayerProfile zainicjowany danymi gracza.");
            // } else {
            //     System.err.println("PlayerDashboard: Kontroler dla PlayerProfile.fxml nie jest instancją PlayerProfileController lub jest null!");
            // }

            if (contentPane == null) {
                System.err.println("PlayerDashboard: contentPane jest null! Nie można załadować widoku profilu.");
                return;
            }

            contentPane.getChildren().setAll(profileView);
            AnchorPane.setTopAnchor(profileView, 0.0);
            AnchorPane.setBottomAnchor(profileView, 0.0);
            AnchorPane.setLeftAnchor(profileView, 0.0);
            AnchorPane.setRightAnchor(profileView, 0.0);
            System.out.println("PlayerDashboard: Pomyślnie załadowano i ustawiono widok profilu.");

        } catch (IOException e) {
            System.err.println("PlayerDashboard: Krytyczny błąd podczas ładowania PlayerProfile.fxml: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("PlayerDashboard: Nieoczekiwany błąd w handleShowProfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Uruchamia timer debugowania, który co 10 sekund loguje dane gracza.
     */
    private void startDebugTimer() {
        if (debugTimer != null) debugTimer.stop(); // Zatrzymaj poprzedni timer, jeśli istnieje

        debugTimer = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            if (currentPlayer != null) {
                System.out.println("DEBUG (PlayerDashboard): Player in debugTimer: " + currentPlayer.getFirstName() + " " + currentPlayer.getLastName() +
                        ", Ranking: " + currentPlayer.getRanking());
            } else {
                System.out.println("DEBUG (PlayerDashboard): currentPlayer is null in debugTimer.");
            }
        }));
        debugTimer.setCycleCount(Timeline.INDEFINITE); // Ustaw na nieskończoną liczbę cykli
        debugTimer.play(); // Rozpocznij odtwarzanie timera
    }

    /**
     * Metoda do sprzątania zasobów, np. zatrzymywania timerów, gdy Dashboard przestaje być aktywny.
     * Wywoływana przez PlayerTopBar przed wylogowaniem.
     */
    public void cleanup() {
        if (debugTimer != null) {
            debugTimer.stop();
            System.out.println("DEBUG (PlayerDashboard): Zatrzymano debugTimer.");
        }
        // Tutaj można dodać inne operacje czyszczenia, jeśli będą potrzebne
    }
}