package org.originsd.lexibangla.ui;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.config.BuildInfo;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.config.UserSettingsService;
import org.originsd.lexibangla.db.HibernateUtil;
import org.originsd.lexibangla.db.MasterDictionaryDao;
import org.originsd.lexibangla.db.UserDataRepository;
import org.originsd.lexibangla.model.DictionaryEntry;
import org.originsd.lexibangla.model.Favorite;
import org.originsd.lexibangla.model.PersonalNote;
import org.originsd.lexibangla.model.SearchHistory;
import org.originsd.lexibangla.service.ClipboardService;
import org.originsd.lexibangla.service.DictionarySearchService;
import org.originsd.lexibangla.service.UpdateService;
import org.originsd.lexibangla.ui.dialog.*;
import org.originsd.lexibangla.ui.ribbon.Ribbon;
import org.originsd.lexibangla.ui.ribbon.RibbonButton;
import org.originsd.lexibangla.ui.ribbon.RibbonGroup;
import org.originsd.lexibangla.ui.ribbon.RibbonTab;
import org.originsd.lexibangla.ui.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application window for LexiBangla Desktop.
 */
public class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private final Stage stage;
    private final UserSettingsService settingsService;
    private final MasterDictionaryDao masterDao;
    private final UserDataRepository userRepo;
    private final DictionarySearchService searchService;
    private final ClipboardService clipboardService;
    private final UpdateService updateService;

    private UserSettings settings;
    private Scene scene;

    // UI Nodes
    private TextField searchField;
    private ComboBox<DictionarySearchService.SearchMode> modeComboBox;
    private ListView<DictionaryEntry> resultsListView;
    private final ObservableList<DictionaryEntry> resultsData = FXCollections.observableArrayList();
    private PauseTransition debounceTimer;

    // Detail Panel Nodes
    private Label detailWordLabel;
    private Button favoriteStarBtn;
    private FontIcon starIcon;
    private Label meaningContentLabel;
    private TextArea noteTextArea;
    private Label noteStatusLabel;
    private Button saveNoteBtn;
    private VBox detailContainer;
    private VBox emptyDetailPlaceholder;

    // Layout containers
    private VBox sidebarContainer;
    private HBox statusBar;
    private Label statusTextLabel;
    private Label wordCountStatusLabel;
    private SplitPane mainSplitPane;

    // Active Selection
    private DictionaryEntry currentEntry;

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.settingsService = new UserSettingsService();
        this.settings = settingsService.getSettings();
        this.masterDao = new MasterDictionaryDao();
        this.userRepo = new UserDataRepository();
        this.searchService = new DictionarySearchService(masterDao);
        this.clipboardService = new ClipboardService();
        this.updateService = new UpdateService();
    }

    public void show() {
        stage.setTitle(AppConfig.APP_NAME + " - " + AppConfig.TAGLINE);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setWidth(1100);
        stage.setHeight(750);

        // Application icons
        try {
            Image appIcon = new Image(MainWindow.class.getResourceAsStream("/icons/lexibangla-logo.png"));
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            log.warn("Could not set application stage icon", e);
        }

        BorderPane root = new BorderPane();

        // 1. Top Area (Header + Ribbon + Search Bar)
        VBox topArea = new VBox();
        topArea.getChildren().addAll(createHeaderBar(), createRibbonBar(), createSearchBar());
        root.setTop(topArea);

        // 2. Center Area (Sidebar + SplitPane [Results | Detail View])
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);

        sidebarContainer = createSidebar();
        if (settings.isShowSidebar()) {
            mainSplitPane.getItems().add(sidebarContainer);
        }

        mainSplitPane.getItems().add(createResultsPane());
        mainSplitPane.getItems().add(createDetailPane());
        mainSplitPane.setDividerPositions(settings.isShowSidebar() ? new double[]{0.18, 0.48} : new double[]{0.35});

        root.setCenter(mainSplitPane);

        // 3. Bottom Status Bar
        statusBar = createStatusBar();
        if (settings.isShowStatusBar()) {
            root.setBottom(statusBar);
        }

        scene = new Scene(root);
        ThemeManager.applyTheme(scene, settings);

        setupKeyboardShortcuts(scene);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        // Load initial browse words
        loadInitialWords();

        // Show welcome screen on first launch
        if (settings.isShowWelcomeOnStartup()) {
            Platform.runLater(() -> WelcomeDialog.show(stage, settings, this::updateSettings));
        }

        // Automatic update check if enabled
        if (settings.isAutoCheckUpdates()) {
            checkUpdatesSilently();
        }
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(12);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(MainWindow.class.getResourceAsStream("/icons/lexibangla-logo.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(28);
            logoView.setFitHeight(28);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            log.warn("Could not load header logo", e);
        }

        Label title = new Label(AppConfig.APP_NAME);
        title.getStyleClass().add("app-title");

        Label tagline = new Label(AppConfig.TAGLINE);
        tagline.getStyleClass().add("app-tagline");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label offlineBadge = new Label("Offline Ready");
        offlineBadge.getStyleClass().add("status-badge-offline");

        header.getChildren().addAll(logoView, title, new Label("•"), tagline, spacer, offlineBadge);
        return header;
    }

    private Ribbon createRibbonBar() {
        Ribbon ribbon = new Ribbon();

        // --- HOME TAB ---
        RibbonGroup searchGroup = new RibbonGroup("Search",
                RibbonButton.large("Search", Feather.SEARCH, () -> searchField.requestFocus(), "Focus Search Bar (Ctrl+F)"),
                RibbonButton.large("Clear", Feather.X_CIRCLE, this::clearSearch, "Clear Search (Ctrl+L / Esc)"),
                RibbonButton.large("History", Feather.CLOCK, () -> HistoryManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Search History (Ctrl+H)")
        );

        RibbonGroup dictGroup = new RibbonGroup("Dictionary",
                RibbonButton.large("Favorites", Feather.STAR, () -> FavoritesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Favorites Manager (Ctrl+B)"),
                RibbonButton.large("Browse", Feather.BOOK_OPEN, this::loadInitialWords, "Browse Dictionary Alphabetically")
        );

        RibbonGroup clipGroup = new RibbonGroup("Clipboard",
                RibbonButton.large("Copy Word", Feather.COPY, () -> {
                    if (clipboardService.copyWord(currentEntry)) showStatus("Copied word: " + currentEntry.word());
                }, "Copy English Word (Ctrl+C)"),
                RibbonButton.large("Copy Meaning", Feather.FILE_TEXT, () -> {
                    if (clipboardService.copyMeaning(currentEntry)) showStatus("Copied Bengali meaning");
                }, "Copy Bengali Meaning"),
                RibbonButton.large("Copy Entry", Feather.CLIPBOARD, () -> {
                    if (clipboardService.copyFullEntry(currentEntry)) showStatus("Copied complete entry");
                }, "Copy Full Entry")
        );

        RibbonGroup notesGroup = new RibbonGroup("Notes",
                RibbonButton.large("Add Note", Feather.EDIT_3, () -> noteTextArea.requestFocus(), "Edit Note for Current Word"),
                RibbonButton.large("Notes List", Feather.FILE_TEXT, () -> NotesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Notes Manager")
        );

        RibbonTab homeTab = new RibbonTab("Home", searchGroup, dictGroup, clipGroup, notesGroup);

        // --- VIEW TAB ---
        RibbonGroup themeGroup = new RibbonGroup("Appearance",
                RibbonButton.large("Light", Feather.SUN, () -> setTheme("LIGHT"), "Light Theme"),
                RibbonButton.large("Dark", Feather.MOON, () -> setTheme("DARK"), "Dark Theme"),
                RibbonButton.large("System", Feather.MONITOR, () -> setTheme("SYSTEM"), "System Theme")
        );

        RibbonGroup layoutGroup = new RibbonGroup("Layout",
                RibbonButton.large("Sidebar", Feather.SIDEBAR, this::toggleSidebar, "Toggle Sidebar"),
                RibbonButton.large("Status Bar", Feather.LAYOUT, this::toggleStatusBar, "Toggle Status Bar"),
                RibbonButton.large("Reset", Feather.REFRESH_CW, this::resetLayout, "Reset Layout to Default")
        );

        RibbonGroup typoGroup = new RibbonGroup("Typography",
                RibbonButton.large("Increase", Feather.ZOOM_IN, () -> adjustFontSize(1), "Increase Font Size"),
                RibbonButton.large("Decrease", Feather.ZOOM_OUT, () -> adjustFontSize(-1), "Decrease Font Size"),
                RibbonButton.large("Reset Font", Feather.TYPE, () -> resetFontSize(), "Reset Font Size")
        );

        RibbonTab viewTab = new RibbonTab("View", themeGroup, layoutGroup, typoGroup);

        // --- TOOLS TAB ---
        RibbonGroup dataGroup = new RibbonGroup("Personal Data",
                RibbonButton.large("Favorites", Feather.STAR, () -> FavoritesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Manage Favorites"),
                RibbonButton.large("Notes", Feather.EDIT_2, () -> NotesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Manage Personal Notes"),
                RibbonButton.large("History", Feather.ARCHIVE, () -> HistoryManagerDialog.show(stage, userRepo, settings, this::performDirectSearch), "Manage Search History")
        );

        RibbonGroup dbGroup = new RibbonGroup("Database",
                RibbonButton.large("DB Info", Feather.DATABASE, () -> DatabaseInfoDialog.show(stage, masterDao, userRepo, settings), "Database Information"),
                RibbonButton.large("Integrity", Feather.SHIELD, () -> {
                    long c = masterDao.getTotalWordCount();
                    showStatus("Database intact: " + String.format("%,d", c) + " verified entries.");
                }, "Check Database Status")
        );

        RibbonGroup prefGroup = new RibbonGroup("Settings",
                RibbonButton.large("Settings", Feather.SETTINGS, () -> SettingsDialog.show(stage, settings, this::updateSettings), "Open Preferences (Ctrl+,)")
        );

        RibbonTab toolsTab = new RibbonTab("Tools", dataGroup, dbGroup, prefGroup);

        // --- HELP TAB ---
        RibbonGroup helpGroup = new RibbonGroup("Help",
                RibbonButton.large("Shortcuts", Feather.COMMAND, () -> KeyboardShortcutsDialog.show(stage, settings), "Keyboard Shortcuts (F1)"),
                RibbonButton.large("Welcome", Feather.COMPASS, () -> WelcomeDialog.show(stage, settings, this::updateSettings), "Show Welcome Screen")
        );

        RibbonGroup onlineGroup = new RibbonGroup("Online",
                RibbonButton.large("Website", Feather.GLOBE, () -> openUrl(AppConfig.WEBSITE_URL), "Visit OriginSD Website"),
                RibbonButton.large("GitHub", Feather.GITHUB, () -> openUrl(AppConfig.REPO_URL), "Visit GitHub Repository")
        );

        RibbonGroup updateGroup = new RibbonGroup("Updates",
                RibbonButton.large("Check Updates", Feather.DOWNLOAD_CLOUD, this::checkUpdatesInteractive, "Check for Updates")
        );

        RibbonGroup appGroup = new RibbonGroup("Application",
                RibbonButton.large("About", Feather.INFO, () -> AboutDialog.show(stage, settings), "About LexiBangla Desktop")
        );

        RibbonTab helpTab = new RibbonTab("Help", helpGroup, onlineGroup, updateGroup, appGroup);

        ribbon.addTab(homeTab);
        ribbon.addTab(viewTab);
        ribbon.addTab(toolsTab);
        ribbon.addTab(helpTab);

        return ribbon;
    }

    private HBox createSearchBar() {
        HBox container = new HBox(12);
        container.getStyleClass().add("search-container");
        container.setAlignment(Pos.CENTER_LEFT);

        HBox inputBox = new HBox(8);
        inputBox.getStyleClass().add("search-input-box");
        inputBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(inputBox, Priority.ALWAYS);

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(16);
        searchIcon.getStyleClass().add("ribbon-icon");

        searchField = new TextField();
        searchField.setPromptText("Search English word (e.g. abandon, freedom, knowledge)...");
        searchField.getStyleClass().add("search-text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button clearBtn = new Button();
        FontIcon clearIcon = new FontIcon(Feather.X);
        clearIcon.setIconSize(14);
        clearBtn.setGraphic(clearIcon);
        clearBtn.getStyleClass().add("search-clear-btn");
        clearBtn.setTooltip(new Tooltip("Clear search (Esc)"));
        clearBtn.setOnAction(e -> clearSearch());

        inputBox.getChildren().addAll(searchIcon, searchField, clearBtn);

        // Search Mode Selector
        modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll(
                DictionarySearchService.SearchMode.SMART,
                DictionarySearchService.SearchMode.EXACT,
                DictionarySearchService.SearchMode.PREFIX,
                DictionarySearchService.SearchMode.PARTIAL
        );
        modeComboBox.setValue(DictionarySearchService.SearchMode.SMART);
        modeComboBox.getStyleClass().add("mode-selector");
        modeComboBox.setTooltip(new Tooltip("Select Search Mode (Default: Smart)"));
        modeComboBox.setOnAction(e -> triggerSearch());

        // Debounce timer setup
        debounceTimer = new PauseTransition(Duration.millis(settings.getSearchDebounceMs()));
        debounceTimer.setOnFinished(e -> executeSearchTask());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearBtn.setVisible(newVal != null && !newVal.isEmpty());
            debounceTimer.playFromStart();
        });

        // Pressing Enter immediately triggers search
        searchField.setOnAction(e -> {
            debounceTimer.stop();
            executeSearchTask();
        });

        container.getChildren().addAll(inputBox, modeComboBox);
        return container;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(12));
        sidebar.getStyleClass().add("app-header");
        sidebar.setMinWidth(150);
        sidebar.setPrefWidth(180);

        Label title = new Label("QUICK ACCESS");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -color-text-muted;");

        Button favBtn = createSidebarButton("Favorites", Feather.STAR, () ->
                FavoritesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch));

        Button notesBtn = createSidebarButton("Personal Notes", Feather.EDIT_2, () ->
                NotesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch));

        Button histBtn = createSidebarButton("Search History", Feather.CLOCK, () ->
                HistoryManagerDialog.show(stage, userRepo, settings, this::performDirectSearch));

        Button browseBtn = createSidebarButton("Browse All", Feather.BOOK_OPEN, this::loadInitialWords);

        sidebar.getChildren().addAll(title, favBtn, notesBtn, histBtn, browseBtn);
        return sidebar;
    }

    private Button createSidebarButton(String text, Feather icon, Runnable action) {
        Button btn = new Button(text);
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(15);
        fontIcon.getStyleClass().add("ribbon-icon");
        btn.setGraphic(fontIcon);
        btn.getStyleClass().add("ribbon-button-small");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private VBox createResultsPane() {
        VBox container = new VBox(6);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("app-header");
        container.setMinWidth(220);
        container.setPrefWidth(300);

        Label header = new Label("DICTIONARY RESULTS");
        header.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -color-text-muted;");

        resultsListView = new ListView<>(resultsData);
        resultsListView.getStyleClass().add("results-list-view");
        VBox.setVgrow(resultsListView, Priority.ALWAYS);

        resultsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DictionaryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellBox = new HBox(8);
                    cellBox.setAlignment(Pos.CENTER_LEFT);

                    VBox textCol = new VBox(2);
                    Label wordLabel = new Label(item.word());
                    wordLabel.getStyleClass().add("result-cell-word");

                    Label meaningSnippet = new Label(item.meaning());
                    meaningSnippet.getStyleClass().add("result-cell-meaning");
                    meaningSnippet.setMaxWidth(200);

                    textCol.getChildren().addAll(wordLabel, meaningSnippet);
                    HBox.setHgrow(textCol, Priority.ALWAYS);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Favorite indicator
                    if (userRepo.isFavorite(item.word())) {
                        FontIcon favStar = new FontIcon(Feather.STAR);
                        favStar.setIconSize(12);
                        favStar.setStyle("-fx-icon-color: #E6B45C;");
                        cellBox.getChildren().addAll(textCol, spacer, favStar);
                    } else {
                        cellBox.getChildren().addAll(textCol);
                    }

                    setGraphic(cellBox);
                }
            }
        });

        resultsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayEntry(newVal);
            }
        });

        container.getChildren().addAll(header, resultsListView);
        return container;
    }

    private StackPane createDetailPane() {
        StackPane root = new StackPane();
        root.getStyleClass().add("detail-container");

        // 1. Placeholder when nothing is selected
        emptyDetailPlaceholder = new VBox(16);
        emptyDetailPlaceholder.setAlignment(Pos.CENTER);
        emptyDetailPlaceholder.setPadding(new Insets(40));

        FontIcon bookIcon = new FontIcon(Feather.BOOK_OPEN);
        bookIcon.setIconSize(64);
        bookIcon.setStyle("-fx-icon-color: -color-border;");

        Label placeholderTitle = new Label("LexiBangla Desktop");
        placeholderTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -color-text-muted;");

        Label placeholderDesc = new Label("Search an English word to view its Bengali meaning and add personal notes.");
        placeholderDesc.setStyle("-fx-text-fill: -color-text-muted;");

        emptyDetailPlaceholder.getChildren().addAll(bookIcon, placeholderTitle, placeholderDesc);

        // 2. Active Detail View Card
        detailContainer = new VBox(16);
        detailContainer.getStyleClass().add("detail-card");
        detailContainer.setVisible(false);

        // Header: English Word + Star Toggle + Copy Word
        HBox wordHeaderBox = new HBox(12);
        wordHeaderBox.setAlignment(Pos.CENTER_LEFT);

        detailWordLabel = new Label();
        detailWordLabel.getStyleClass().add("detail-word-header");
        HBox.setHgrow(detailWordLabel, Priority.ALWAYS);

        favoriteStarBtn = new Button();
        starIcon = new FontIcon(Feather.STAR);
        starIcon.setIconSize(22);
        favoriteStarBtn.setGraphic(starIcon);
        favoriteStarBtn.getStyleClass().add("favorite-star-btn");
        favoriteStarBtn.setTooltip(new Tooltip("Add / Remove from Favorites (Ctrl+D)"));
        favoriteStarBtn.setOnAction(e -> toggleFavoriteCurrentWord());

        Button copyWordBtn = new Button("Copy");
        FontIcon copyIcon = new FontIcon(Feather.COPY);
        copyIcon.setIconSize(14);
        copyWordBtn.setGraphic(copyIcon);
        copyWordBtn.getStyleClass().add("action-btn-secondary");
        copyWordBtn.setOnAction(e -> {
            if (clipboardService.copyWord(currentEntry)) {
                showStatus("Copied word to clipboard");
            }
        });

        wordHeaderBox.getChildren().addAll(detailWordLabel, favoriteStarBtn, copyWordBtn);

        // Meaning Card
        VBox meaningCard = new VBox(8);
        meaningCard.getStyleClass().add("meaning-card");

        HBox meaningHeader = new HBox(8);
        meaningHeader.setAlignment(Pos.CENTER_LEFT);
        Label meaningTitle = new Label("Bengali Meaning (বাংলা অর্থ)");
        meaningTitle.getStyleClass().add("meaning-label-title");
        Region mSpacer = new Region();
        HBox.setHgrow(mSpacer, Priority.ALWAYS);

        Button copyMeaningBtn = new Button("Copy Meaning");
        FontIcon mCopyIcon = new FontIcon(Feather.FILE_TEXT);
        mCopyIcon.setIconSize(13);
        copyMeaningBtn.setGraphic(mCopyIcon);
        copyMeaningBtn.getStyleClass().add("action-btn-secondary");
        copyMeaningBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
        copyMeaningBtn.setOnAction(e -> {
            if (clipboardService.copyMeaning(currentEntry)) {
                showStatus("Copied Bengali meaning to clipboard");
            }
        });

        meaningHeader.getChildren().addAll(meaningTitle, mSpacer, copyMeaningBtn);

        meaningContentLabel = new Label();
        meaningContentLabel.getStyleClass().add("meaning-text");
        meaningContentLabel.setWrapText(true);

        meaningCard.getChildren().addAll(meaningHeader, meaningContentLabel);

        // Personal Note Card
        VBox noteCard = new VBox(8);
        noteCard.getStyleClass().add("notes-card");

        HBox noteHeader = new HBox(8);
        noteHeader.setAlignment(Pos.CENTER_LEFT);

        Label noteTitle = new Label("Personal Note");
        noteTitle.getStyleClass().add("notes-title");

        noteStatusLabel = new Label();
        noteStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-text-muted;");

        Region nSpacer = new Region();
        HBox.setHgrow(nSpacer, Priority.ALWAYS);

        saveNoteBtn = new Button("Save Note");
        FontIcon saveIcon = new FontIcon(Feather.CHECK);
        saveIcon.setIconSize(13);
        saveNoteBtn.setGraphic(saveIcon);
        saveNoteBtn.getStyleClass().add("action-btn-primary");
        saveNoteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 10 3 10;");
        saveNoteBtn.setOnAction(e -> saveCurrentNote());

        noteHeader.getChildren().addAll(noteTitle, noteStatusLabel, nSpacer, saveNoteBtn);

        noteTextArea = new TextArea();
        noteTextArea.setPromptText("Add your own personal notes, examples, or vocabulary hints for this word...");
        noteTextArea.setWrapText(true);
        noteTextArea.setPrefRowCount(4);
        noteTextArea.getStyleClass().add("notes-text-area");

        noteCard.getChildren().addAll(noteHeader, noteTextArea);

        // Bottom Actions
        HBox bottomActions = new HBox(10);
        bottomActions.setAlignment(Pos.CENTER_RIGHT);

        Button copyFullEntryBtn = new Button("Copy Complete Entry");
        FontIcon fullCopyIcon = new FontIcon(Feather.CLIPBOARD);
        fullCopyIcon.setIconSize(14);
        copyFullEntryBtn.setGraphic(fullCopyIcon);
        copyFullEntryBtn.getStyleClass().add("action-btn-secondary");
        copyFullEntryBtn.setOnAction(e -> {
            if (clipboardService.copyFullEntry(currentEntry)) {
                showStatus("Copied complete dictionary entry");
            }
        });

        bottomActions.getChildren().add(copyFullEntryBtn);

        detailContainer.getChildren().addAll(wordHeaderBox, meaningCard, noteCard, bottomActions);

        root.getChildren().addAll(emptyDetailPlaceholder, detailContainer);
        return root;
    }

    private HBox createStatusBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("status-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        statusTextLabel = new Label("Ready");
        statusTextLabel.getStyleClass().add("status-label");
        HBox.setHgrow(statusTextLabel, Priority.ALWAYS);

        long count = masterDao.getTotalWordCount();
        wordCountStatusLabel = new Label(String.format("%,d Words", count));
        wordCountStatusLabel.getStyleClass().add("status-badge");

        Label versionLabel = new Label("v" + BuildInfo.getVersion());
        versionLabel.getStyleClass().add("status-badge");

        bar.getChildren().addAll(statusTextLabel, wordCountStatusLabel, versionLabel);
        return bar;
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), () -> searchField.requestFocus());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), this::clearSearch);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), this::toggleFavoriteCurrentWord);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN), () ->
                HistoryManagerDialog.show(stage, userRepo, settings, this::performDirectSearch));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN), () ->
                FavoritesManagerDialog.show(stage, userRepo, settings, this::performDirectSearch));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () ->
                SettingsDialog.show(stage, settings, this::updateSettings));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> KeyboardShortcutsDialog.show(stage, settings));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), this::clearSearch);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && searchField.isFocused() && !resultsData.isEmpty()) {
                resultsListView.requestFocus();
                resultsListView.getSelectionModel().select(0);
                e.consume();
            }
        });
    }

    private void executeSearchTask() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            loadInitialWords();
            return;
        }

        DictionarySearchService.SearchMode mode = modeComboBox.getValue();
        showStatus("Searching for '" + query.trim() + "'...");

        searchService.search(query, mode, settings.getMaxSearchResults(), entries -> {
            resultsData.setAll(entries);
            if (!entries.isEmpty()) {
                showStatus("Found " + entries.size() + " matches for '" + query.trim() + "'");
                resultsListView.getSelectionModel().select(0);
                // Record in search history
                userRepo.recordSearch(query.trim());
            } else {
                showStatus("No matching words found for '" + query.trim() + "'");
                emptyDetailPlaceholder.setVisible(true);
                detailContainer.setVisible(false);
            }
        }, err -> {
            showStatus("Search failed: " + err.getMessage());
        });
    }

    public void performDirectSearch(String query) {
        if (query == null) return;
        searchField.setText(query.trim());
        executeSearchTask();
    }

    private void displayEntry(DictionaryEntry entry) {
        this.currentEntry = entry;
        emptyDetailPlaceholder.setVisible(false);
        detailContainer.setVisible(true);

        detailWordLabel.setText(entry.word());
        meaningContentLabel.setText(entry.meaning());

        updateFavoriteStarState(entry.word());

        // Load personal note
        Optional<PersonalNote> note = userRepo.getNoteForWord(entry.word());
        if (note.isPresent()) {
            noteTextArea.setText(note.get().getContent());
            noteStatusLabel.setText("(Last saved: " + note.get().getUpdatedAt().toString().substring(0, 10) + ")");
        } else {
            noteTextArea.clear();
            noteStatusLabel.setText("(No note yet)");
        }
    }

    private void updateFavoriteStarState(String word) {
        boolean isFav = userRepo.isFavorite(word);
        if (isFav) {
            starIcon.setStyle("-fx-icon-color: #E6B45C;");
            favoriteStarBtn.setTooltip(new Tooltip("Remove from Favorites (Ctrl+D)"));
        } else {
            starIcon.setStyle("-fx-icon-color: -color-text-muted;");
            favoriteStarBtn.setTooltip(new Tooltip("Add to Favorites (Ctrl+D)"));
        }
    }

    private void toggleFavoriteCurrentWord() {
        if (currentEntry == null) return;
        String word = currentEntry.word();
        boolean isFav = userRepo.isFavorite(word);

        if (isFav) {
            userRepo.removeFavorite(word);
            showStatus("Removed '" + word + "' from favorites");
        } else {
            userRepo.addFavorite(currentEntry.wordId(), word);
            showStatus("Added '" + word + "' to favorites");
        }

        updateFavoriteStarState(word);
        resultsListView.refresh();
    }

    private void saveCurrentNote() {
        if (currentEntry == null) return;
        String content = noteTextArea.getText();
        PersonalNote note = userRepo.saveOrUpdateNote(currentEntry.wordId(), currentEntry.word(), content);
        if (note != null) {
            noteStatusLabel.setText("(Saved just now)");
            showStatus("Saved note for '" + currentEntry.word() + "'");
        }
    }

    private void clearSearch() {
        searchField.clear();
        loadInitialWords();
        searchField.requestFocus();
    }

    private void triggerSearch() {
        debounceTimer.stop();
        executeSearchTask();
    }

    private void loadInitialWords() {
        List<DictionaryEntry> initial = masterDao.browseEntries(settings.getMaxSearchResults(), 0);
        resultsData.setAll(initial);
        if (!initial.isEmpty()) {
            resultsListView.getSelectionModel().select(0);
            showStatus("Browsing dictionary (Total: " + String.format("%,d", masterDao.getTotalWordCount()) + " entries)");
        }
    }

    private void toggleSidebar() {
        boolean showing = mainSplitPane.getItems().contains(sidebarContainer);
        if (showing) {
            mainSplitPane.getItems().remove(sidebarContainer);
            settings.setShowSidebar(false);
        } else {
            mainSplitPane.getItems().add(0, sidebarContainer);
            settings.setShowSidebar(true);
            mainSplitPane.setDividerPositions(0.18, 0.48);
        }
        settingsService.save(settings);
    }

    private void toggleStatusBar() {
        BorderPane bp = (BorderPane) scene.getRoot();
        boolean showing = bp.getBottom() != null;
        if (showing) {
            bp.setBottom(null);
            settings.setShowStatusBar(false);
        } else {
            bp.setBottom(statusBar);
            settings.setShowStatusBar(true);
        }
        settingsService.save(settings);
    }

    private void resetLayout() {
        if (!mainSplitPane.getItems().contains(sidebarContainer)) {
            mainSplitPane.getItems().add(0, sidebarContainer);
        }
        mainSplitPane.setDividerPositions(0.18, 0.48);
        BorderPane bp = (BorderPane) scene.getRoot();
        if (bp.getBottom() == null) {
            bp.setBottom(statusBar);
        }
        settings.setShowSidebar(true);
        settings.setShowStatusBar(true);
        settingsService.save(settings);
        showStatus("Layout reset to default");
    }

    private void setTheme(String themeName) {
        settings.setTheme(themeName);
        ThemeManager.applyTheme(scene, settings);
        settingsService.save(settings);
        showStatus("Switched to " + themeName + " theme");
    }

    private void adjustFontSize(int delta) {
        int newDelta = settings.getFontSizeDelta() + delta;
        newDelta = Math.clamp(newDelta, -4, 12);
        settings.setFontSizeDelta(newDelta);
        ThemeManager.applyTheme(scene, settings);
        settingsService.save(settings);
        showStatus("Font size adjusted (" + (newDelta >= 0 ? "+" + newDelta : newDelta) + "px)");
    }

    private void resetFontSize() {
        settings.setFontSizeDelta(0);
        ThemeManager.applyTheme(scene, settings);
        settingsService.save(settings);
        showStatus("Font size reset to normal");
    }

    private void updateSettings(UserSettings newSettings) {
        this.settings = newSettings;
        settingsService.save(newSettings);
        ThemeManager.applyTheme(scene, newSettings);

        // Update layout toggles
        BorderPane bp = (BorderPane) scene.getRoot();
        if (settings.isShowStatusBar() && bp.getBottom() == null) {
            bp.setBottom(statusBar);
        } else if (!settings.isShowStatusBar() && bp.getBottom() != null) {
            bp.setBottom(null);
        }

        boolean hasSidebar = mainSplitPane.getItems().contains(sidebarContainer);
        if (settings.isShowSidebar() && !hasSidebar) {
            mainSplitPane.getItems().add(0, sidebarContainer);
            mainSplitPane.setDividerPositions(0.18, 0.48);
        } else if (!settings.isShowSidebar() && hasSidebar) {
            mainSplitPane.getItems().remove(sidebarContainer);
        }

        debounceTimer.setDuration(Duration.millis(settings.getSearchDebounceMs()));
        showStatus("Preferences saved successfully");
    }

    private void showStatus(String message) {
        if (statusTextLabel != null) {
            statusTextLabel.setText(message);
        }
    }

    private void checkUpdatesSilently() {
        updateService.checkForUpdatesAsync().thenAccept(result -> {
            if (result.updateAvailable()) {
                Platform.runLater(() -> {
                    showStatus("Update available: v" + result.latestVersion() + "! Check Help -> Updates.");
                });
            }
        });
    }

    private void checkUpdatesInteractive() {
        showStatus("Checking for updates from GitHub...");
        updateService.checkForUpdatesAsync().thenAccept(result -> {
            Platform.runLater(() -> {
                if (result.updateAvailable()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(stage);
                    alert.setTitle("Update Available");
                    alert.setHeaderText("New version v" + result.latestVersion() + " is available!");
                    alert.setContentText("Current version: " + result.currentVersion() + "\n\n" +
                            "Release Notes:\n" + result.releaseNotes());

                    ButtonType downloadBtn = new ButtonType("Download Release");
                    ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(downloadBtn, closeBtn);

                    alert.showAndWait().ifPresent(btn -> {
                        if (btn == downloadBtn) {
                            String url = result.matchingAsset().isPresent()
                                    ? result.matchingAsset().get().getBrowserDownloadUrl()
                                    : (result.releaseUrl().isEmpty() ? AppConfig.REPO_URL + "/releases" : result.releaseUrl());
                            openUrl(url);
                        }
                    });
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(stage);
                    alert.setTitle("No Updates Found");
                    alert.setHeaderText("You are using the latest version");
                    alert.setContentText("Current version: " + result.currentVersion() + "\n" + result.releaseNotes());
                    alert.showAndWait();
                    showStatus("LexiBangla is up to date");
                }
            });
        });
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            log.warn("Could not open URL: {}", url, e);
        }
    }

    public void shutdown() {
        log.info("Initiating application shutdown sequence");
        if (searchService != null) {
            searchService.shutdown();
        }
        if (masterDao != null) {
            masterDao.close();
        }
        HibernateUtil.shutdown();
    }
}
