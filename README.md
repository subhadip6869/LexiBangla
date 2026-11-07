# LexiBangla Desktop

[![Java](https://img.shields.io/badge/Java-21%20LTS-blue.svg)](https://www.oracle.com/java/technologies/downloads/#java21)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-teal.svg)](https://openjfx.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

> **Words Connect Worlds.**  
> *Discover the meaning of every word.*

**LexiBangla Desktop** is an open-source, offline-first English-to-Bengali desktop dictionary developed by **Subhadip Dutta** and published by **OriginSD** (Bankura, West Bengal, India).

The application is engineered for high performance, Bengali typographic fidelity, intuitive ribbon navigation, and complete data privacy without requiring an internet connection.

---

## 🌟 Key Features

- **Offline-First Master Dictionary:**
  - Fast, read-only SQLite JDBC search across **93,421 verified words**.
  - Case-insensitive exact matching, prefix matching, and partial substring search.
  - Zero external translation APIs, ads, or background analytics tracking.
- **Pure Bengali Typography:**
  - Robust Unicode text preservation with font fallback supporting *Noto Sans Bengali*, *Kalpurush*, *SolaimanLipi*, *Vrinda*, and system fonts.
  - High-readability card layouts with clear phonetic and glyph rendering.
- **Modern Ribbon Navigation:**
  - Clean ribbon interface with dedicated **Home**, **View**, **Tools**, and **Help** command tabs.
- **Comprehensive Personal Features:**
  - **Favorites:** One-click starring (`Ctrl+D`) and dedicated Favorites Manager (`Ctrl+B`).
  - **Search History:** Automatic timestamped history with History Manager (`Ctrl+H`).
  - **Personal Notes:** Add, edit, and persist vocabulary notes for any dictionary word.
  - Dedicated managers for reviewing, filtering, editing, and deleting personal data.
- **Enterprise-Grade Dual-Database Architecture:**
  - **Master Dictionary (`dictionary.sqlite`):** Read-only direct SQLite JDBC connection with prepared statements and memory temp storage.
  - **User Database (`lexibangla-user.sqlite`):** Hibernate ORM 6 with Jakarta Persistence (JPA) storing user data in OS-compliant application directories.
- **Theme & Typography System:**
  - Light Theme, Dark Theme, and System Theme with blue-and-teal visual design system.
  - Dynamic font-size scaling.
  - Collapsible sidebar and configurable status bar.
- **Update Verification:**
  - Integrated GitHub Releases API updater with Semantic Versioning and platform asset matching.
- **Keyboard Shortcuts:**
  - `Ctrl + F`: Focus search bar
  - `Ctrl + L` / `Esc`: Clear search
  - `Ctrl + D`: Add / remove favorite
  - `Ctrl + C`: Copy entry
  - `Ctrl + H`: Search history
  - `Ctrl + B`: Favorites manager
  - `Ctrl + ,`: Preferences
  - `F1`: Keyboard shortcuts cheat-sheet

---

## 🏗️ Architecture & Storage Locations

LexiBangla Desktop maintains strict separation between the read-only dictionary data and user-generated data.

### Storage Paths

| Operating System | User Database & Settings Location |
|---|---|
| **Windows** | `%LOCALAPPDATA%\OriginSD\LexiBangla\` |
| **Linux** | `~/.local/share/lexibangla/` (or `$XDG_DATA_HOME/lexibangla/`) |
| **macOS** | `~/Library/Application Support/LexiBangla/` |

The master dictionary database is strictly read-only and never modified during runtime.

---

## 🛠️ Technology Stack

- **Runtime:** Java 21 LTS
- **UI Framework:** JavaFX 21.0.6 (`javafx-controls`, `javafx-fxml`, `javafx-graphics`)
- **UI Components:** ControlsFX 11.2.1
- **Icons:** Ikonli 12.3.1 (`ikonli-feather-pack`, `ikonli-javafx`)
- **Master Database:** SQLite JDBC 3.47.2.0 (Direct Prepared Statements)
- **User Database:** Hibernate ORM 6.6.9.Final + `hibernate-community-dialects` (JPA 3.1)
- **JSON Processing:** Jackson 2.18.2
- **Logging:** SLF4J 2.0.16 + Logback 1.5.16
- **Testing:** JUnit Jupiter 5.11.0 + AssertJ 3.27.3

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 21 LTS or newer.
- **Apache Maven:** Version 3.9+ installed and configured.

### Building from Source

```bash
# Clone repository
git clone https://github.com/subhadip6869/LexiBangla.git
cd LexiBangla

# Compile project
mvn clean compile

# Run automated tests
mvn test

# Package standard and shaded runnable JAR
mvn package
```

### Running the Application

Using Maven JavaFX plugin:
```bash
mvn javafx:run
```

Or running the packaged JAR:
```bash
java -jar target/lexibangla-1.0.1-SNAPSHOT-all.jar
```

---

## 📦 Native Distribution with Bundled Minimal JRE

LexiBangla Desktop includes automated packaging scripts in the [`scripts/`](scripts/) directory for Windows, Linux, and macOS.

Each script produces an optimized, self-contained native package with a **bundled, stripped JRE runtime generated via `jlink`**. End users can install and run the application immediately without installing Java.

### Packaging by Operating System

| Target Platform | Package Format | Execution Instruction | Output Location |
|---|---|---|---|
| **Windows** | Native `.exe` Installer *(Setup Wizard)* | `.\scripts\package-windows.ps1`<br>*(or `scripts\package-windows.bat`)* | `dist\*.exe` |
| **Linux** | Native `.deb` & `.rpm` Packages | `chmod +x scripts/package-linux.sh`<br>`./scripts/package-linux.sh` | `dist/*.deb` & `dist/*.rpm` |
| **macOS** | Native `.dmg` Disk Image *(ARM64 & Intel)* | `chmod +x scripts/package-mac.sh`<br>`./scripts/package-mac.sh` | `dist/*.dmg` |

### What the Packaging Scripts Automate

1. **Build:** Compiles the source and builds the complete shaded Fat JAR (`mvn clean package -DskipTests`).
2. **JRE Optimization (`jlink`):** Extracts only the strictly necessary modules (`java.base`, `java.desktop`, `java.sql`, `java.naming`, `java.xml`, etc.), stripping debug symbols and documentation to yield a lightweight runtime (~35-45 MB).
3. **Native Packaging (`jpackage`):** Integrates the application with operating system launchers, icons, start menu shortcuts, and installation wizards.

---

## 📄 License

This project is open-source software licensed under the **[MIT License](LICENSE)**.

```
Copyright (c) 2026 OriginSD (Developer: Subhadip Dutta)
```

---

## 👤 Publisher & Author

- **Application:** LexiBangla Desktop
- **Publisher:** OriginSD
- **Developer:** Subhadip Dutta
- **Website:** [https://originsd.org](https://originsd.org)
- **GitHub:** [https://github.com/subhadip6869/LexiBangla.git](https://github.com/subhadip6869/LexiBangla.git)
- **Location:** Bankura, West Bengal, India
