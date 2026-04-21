# Arabic Text Editor

A Java desktop application for composing, editing, and analysing Arabic text documents. Built with Java Swing and backed by a MySQL database, it goes well beyond a simple text editor — offering document management with duplicate detection, automatic pagination, Arabic-to-Latin transliteration, morphological analysis (lemmatisation, stemming, POS tagging), and NLP statistical metrics (TF-IDF, PMI, PKL).

---

## Features

| Category | Details |
|---|---|
| **Document management** | Create, import (single or bulk), update, delete Arabic text documents |
| **Duplicate detection** | SHA-1 content hashing prevents storing identical files |
| **Pagination** | Documents auto-split into pages (max 500 words / 30 lines per page) on save |
| **Full-text search** | Search any word across all documents; results show title, page number, and surrounding words |
| **Transliteration** | Arabic Unicode → Latin script using a built-in character mapping table; result persisted to DB |
| **Morphological analysis** | Lemmatisation via external NLP API, plus rule-based stemming (prefix/suffix stripping), normalisation, and tokenisation |
| **POS tagging** | Part-of-speech tagging via the Oujda NLP Team REST API |
| **Statistical NLP** | TF, IDF, TF-IDF, PMI (Pointwise Mutual Information), and PKL (KL Divergence approximation) per document |
| **Export** | Convert document content to RTL-aware HTML/Markdown (ordered lists, unordered lists, links, paragraphs) |
| **RTL GUI** | Java Swing interface configured for right-to-left Arabic editing |

---

## Architecture

The project follows a strict three-layer architecture with DTO objects for data transfer between layers.

```
Arabic-Text-Editor/
├── src/
│   ├── main/
│   │   └── Main.java                    # Entry point — wires all layers, launches Swing GUI
│   ├── pl/                              # Presentation Layer
│   │   ├── TextEditorApp.java           # Main Swing window (58 KB — full editor UI)
│   │   └── WordAnalysis.java            # DTO-like model for per-word analysis results
│   ├── bll/                             # Business Logic Layer
│   │   ├── DocumentFacadeService.java   # Facade — single entry point for the UI
│   │   ├── DocumentBO.java              # Core document ops, transliteration, POS tagging
│   │   ├── StatisticalAnalysisBO.java   # TF, IDF, TF-IDF, PMI, PKL calculations
│   │   ├── LemmaBO.java                 # Stemming, lemmatisation, normalisation, tokenisation
│   │   ├── ExportBO.java                # Markdown/HTML export with RTL list support
│   │   ├── DuplicateFileException.java  # Custom exception for duplicate content
│   │   ├── DocumentService.java         # Interface: document CRUD + search + tagging
│   │   ├── StatService.java             # Interface: statistical NLP methods
│   │   ├── LemmaService.java            # Interface: morphological analysis methods
│   │   └── IExport.java                 # Interface: export methods
│   ├── dal/                             # Data Access Layer
│   │   ├── DatabaseConnection.java      # JDBC MySQL connection (static helper)
│   │   ├── DocumentDAO.java             # Document CRUD, search, batch insert, pagination trigger
│   │   ├── DocumentFacade.java          # DAL facade — delegates to specific DAOs
│   │   ├── PageDAO.java                 # Pagination logic — splits content into page rows
│   │   ├── TransliterateDAO.java        # Read/write transliterated content in `pages` table
│   │   ├── LemmaDAO.java                # Calls Oujda NLP Lemmatisation REST API
│   │   ├── MySqlFactory.java            # DAL factory (pattern stub)
│   │   ├── IDal.java                    # Core DAL interface
│   │   ├── IDALFacade.java              # Facade interface
│   │   ├── IDALFactory.java             # Factory interface
│   │   ├── ILemma.java                  # Lemma DAO interface
│   │   ├── IPage.java                   # Page DAO interface
│   │   ├── ITransliterate.java          # Transliterate DAO interface
│   │   └── Iimport.java                 # Import interface
│   ├── dto/
│   │   ├── DocumentDTO.java             # Transfers document data (title, content, hash, timestamps, wordCount)
│   │   └── SearchResult.java            # Transfers search hit data (title, page, wordBefore, wordAfter)
│   └── tests/
│       ├── DocumentDAOtests.java        # JUnit 5 tests for DocumentDAO (CRUD, search, duplicate checks)
│       ├── LemmaDAOTest.java            # JUnit 5 tests for LemmaDAO (API integration)
│       ├── PageDAOtests.java            # JUnit 5 tests for PageDAO
│       └── TransliterateDAOTest.java    # JUnit 5 tests for TransliterateDAO
├── bin/                                 # Compiled .class files (Eclipse output)
├── .classpath                           # Eclipse classpath config
├── .project                             # Eclipse project descriptor
└── .settings/                          # Eclipse IDE settings
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (100%) |
| GUI | Java Swing / AWT |
| Database | MySQL (via JDBC) |
| Build / IDE | Eclipse IDE |
| Testing | JUnit 5 |
| NLP APIs | Oujda NLP Team — Lemmatisation (`/api/Apilmm/`) and POS Tagging (`/api/pos`) |

---

## Prerequisites

- Java 8 or higher (JDK for building, JRE for running)
- MySQL Server running locally on port `3306`
- Eclipse IDE (recommended) or any Java IDE / command-line build setup
- Internet access for NLP API calls (lemmatisation and POS tagging)

---

## Database Setup

The application connects to a local MySQL instance with these hardcoded credentials in `DatabaseConnection.java`:

```
URL:      jdbc:mysql://localhost:3306/dummyDatabase
User:     root
Password: (empty)
```

Create the database and required tables before running:

```sql
CREATE DATABASE dummyDatabase;
USE dummyDatabase;

CREATE TABLE document (
  ID           INT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(255) NOT NULL,
  hash         VARCHAR(40)  NOT NULL,
  creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  upadation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  word_count   INT
);

CREATE TABLE pages (
  page_id            INT AUTO_INCREMENT PRIMARY KEY,
  document_id        INT NOT NULL,
  page_number        INT NOT NULL,
  page_content       TEXT,
  transliterateContent TEXT,
  FOREIGN KEY (document_id) REFERENCES document(ID) ON DELETE CASCADE
);
```

> **Note:** The column name in the `document` table is `upadation_time` (not `updation_time`) — this matches the typo in the source code and must be used exactly.

---

## Getting Started

### Option 1 — Eclipse (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/ayesha-Saeed-0/Arabic-Text-Editor.git
   ```
2. Open Eclipse → **File → Import → Existing Projects into Workspace**.
3. Select the cloned folder and click **Finish**.
4. Add the MySQL JDBC driver (`mysql-connector-j-x.x.x.jar`) to the build path:
   - Right-click project → **Build Path → Add External JARs**.
5. Add JUnit 5 to the build path if running tests.
6. Right-click `Main.java` → **Run As → Java Application**.

### Option 2 — Command Line

```bash
git clone https://github.com/ayesha-Saeed-0/Arabic-Text-Editor.git
cd Arabic-Text-Editor

# Compile (adjust paths as needed; mysql-connector JAR must be on classpath)
javac -cp ".:lib/mysql-connector-j.jar" -d bin src/**/*.java

# Run
java -cp "bin:lib/mysql-connector-j.jar" main.Main
```

---

## External NLP APIs

Two REST APIs from the [Oujda NLP Team](http://oujda-nlp-team.net) are called at runtime:

| Feature | Endpoint | Method |
|---|---|---|
| Lemmatisation | `http://oujda-nlp-team.net:8080/api/Apilmm/{word}` | GET |
| POS Tagging | `http://oujda-nlp-team.net:8082/api/pos?textinput={word}` | POST |

These are called word-by-word. Internet connectivity is required; if the APIs are unavailable the application will fall back gracefully (returning error strings rather than crashing).

---

## Transliteration Mapping

The built-in `DocumentBO` transliteration table maps Arabic characters to Latin equivalents used in academic Arabic romanisation:

| Arabic | Latin | Arabic | Latin |
|---|---|---|---|
| ا | a | ص | S |
| ب | b | ض | D |
| ت | t | ط | T |
| ث | th | ظ | DH |
| ج | j | ع | ʿ |
| ح | H | غ | gh |
| خ | kh | ف | f |
| د | d | ق | q |
| ذ | dh | ك | k |
| ر | r | ل | l |
| ز | z | م | m |
| س | s | ن | n |
| ش | sh | ه | h |
| | | و | w |
| | | ي | y |

Diacritics (فتحة، ضمة، كسرة) are also mapped to `a`, `u`, `i` respectively.

---

## Running Tests

Tests are written with JUnit 5 and require a live MySQL connection with test data. From Eclipse, right-click the `tests/` package → **Run As → JUnit Test**.

| Test Class | Coverage |
|---|---|
| `DocumentDAOtests` | Fetch, insert, batch insert, update, delete, search, duplicate detection |
| `LemmaDAOTest` | Lemmatisation API — valid word, empty word, non-existent word, invalid URL |
| `PageDAOtests` | Pagination logic |
| `TransliterateDAOTest` | Content retrieval and update for transliteration |

---

## Known Issues & Improvements

- Database credentials are hardcoded in `DatabaseConnection.java` — move to a `.properties` or `.env` file.
- The `upadation_time` column name is a typo; renaming it to `updated_at` would improve consistency.
- `DocumentDAO` is instantiated with `new DocumentDAO()` inside `DocumentBO.search()`, bypassing the injected dependency — this should use the existing `documentFacade` instead.
- The NLP API calls in `DocumentBO.tagText()` are made word-by-word with a new HTTP connection per word, which is slow for long texts; batching or reusing connections would improve performance.
- No authentication or multi-user support — the application is single-user by design.

---

## Authors

- **Ayesha Saeed** — [@ayesha-Saeed-0](https://github.com/ayesha-Saeed-0)
- **Malaika Naveed**
- **Khushbakht Naeem**
