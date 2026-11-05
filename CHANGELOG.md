# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2026-11-06

### Added
- **Core Master Dictionary Search Engine:**
  - Integrated read-only SQLite JDBC access to `dictionary.sqlite` containing 93,421 verified words.
  - Multi-mode search: Smart Search (ranked exact, prefix, partial), Exact match, Prefix match, and Partial match.
  - Asynchronous search task execution with debouncing (default 200 ms) and real-time task cancellation on keystrokes.
- **Dual Database Architecture:**
  - Separated read-only master dictionary from writable user database (`lexibangla-user.sqlite`).
  - Configured Hibernate ORM 6.6.9.Final with Jakarta Persistence (JPA) and `SQLiteDialect`.
  - JPA entities for `Favorite`, `SearchHistory`, and `PersonalNote` with automated schema creation and transaction rollback support.
  - OS-compliant storage directory resolution (`%LOCALAPPDATA%`, `~/.local/share/`, `~/Library/Application Support/`).
- **User Personal Features:**
  - One-click Star/Favorite toggle (`Ctrl+D`) and dedicated `FavoritesManagerDialog`.
  - Automatic timestamped search history and dedicated `HistoryManagerDialog`.
  - Custom note creation, editing, and deletion for any vocabulary word with `NotesManagerDialog`.
- **Modern Ribbon Navigation:**
  - Modular JavaFX ribbon implementation inspired by modern desktop UX without legacy Java 8/11 dependencies.
  - Command tabs: **Home**, **View**, **Tools**, and **Help** with grouped tools, icons, tooltips, and accelerators.
- **Visual Design System & Bengali Typography:**
  - Blue-and-teal corporate palette (Primary `#3659C9`, Secondary `#0F9D91`, Accent `#19B5A5`).
  - Complete Light Theme and Dark Theme stylesheets.
  - Bengali font-fallback chain (*Noto Sans Bengali*, *Kalpurush*, *SolaimanLipi*, *Vrinda*).
  - Dynamic font size adjustment (-4px to +12px).
- **Application Views & Dialogs:**
  - Welcome screen for first-time onboarding with "Words Connect Worlds" branding.
  - About dialog with Maven-injected build metadata, publisher information, and clickable links.
  - Preferences & Settings dialog with persistent JSON serialization.
  - Database status and integrity information dialog.
  - Keyboard shortcuts cheat-sheet dialog.
- **Release & Updater Infrastructure:**
  - GitHub Releases API integration for update verification.
  - Semantic versioning comparison and platform-specific asset matching.
  - Shaded fat JAR packaging configuration (`maven-shade-plugin`).
- **Automated Testing Suite:**
  - Unit tests verifying master dictionary record count, Bengali Unicode text preservation, and search ranking.
  - Integration tests for Hibernate JPA CRUD operations and transaction rollbacks.
  - Unit tests for Semantic Version comparison and platform asset matching.
