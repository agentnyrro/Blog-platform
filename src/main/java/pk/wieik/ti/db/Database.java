package pk.wieik.ti.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/** Jedno miejsce odpowiedzialne za konfigurację SQLite i schemat bazy. */
public final class Database {
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final Path DB_FILE = resolveDbFile();

    private static Path resolveDbFile() {
        String override = System.getProperty("blog.db.path");
        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }
        Path root = findProjectRoot();
        if (root != null) {
            return root.resolve("data").resolve("platforma-blogowa.db").toAbsolutePath().normalize();
        }
        return Path.of(
                System.getProperty("catalina.base", System.getProperty("user.home")),
                "data", "platforma-blogowa", "platforma-blogowa.db"
        ).toAbsolutePath().normalize();
    }

    private static Path findProjectRoot() {
        try {
            Path dir = Path.of(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            for (; dir != null; dir = dir.getParent()) {
                if (Files.exists(dir.resolve("pom.xml"))) return dir;
            }
        } catch (Exception ex) {
            System.err.println("Nie można ustalić katalogu projektu, użyto lokalizacji domyślnej");
            ex.printStackTrace();
        }
        return null;
    }

    private Database() {}

    public static Path getDatabaseFile() {
        return DB_FILE;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("No JDBC driver");
            e.printStackTrace();
        }

        // Przy pierwszym połączeniu SQLite utworzy brakujący plik bazy.
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        try (Statement statement = connection.createStatement()) {
            // PRAGMA foreign_keys obowiązuje osobno dla KAŻDEGO połączenia.
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    public static void initialize() throws IOException, SQLException {
        Files.createDirectories(DB_FILE.getParent());

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS uzytkownicy (
                    id INTEGER PRIMARY KEY,
                    login TEXT NOT NULL UNIQUE COLLATE NOCASE,
                    haslo TEXT NOT NULL,
                    uprawnienia INTEGER NOT NULL DEFAULT 0
                        CHECK (uprawnienia IN (0, 1, 2))
                )
                """);

            statement.execute("""           
                CREATE TABLE IF NOT EXISTS blogi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    autor_id INTEGER NOT NULL,
                    tytul TEXT NOT NULL,
                    tresc TEXT NOT NULL,
                    data_utworzenia TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    data_aktualizacji TEXT,
                    status INTEGER NOT NULL DEFAULT 0
                         CHECK (status IN (0, 1, 2)),
             
                    FOREIGN KEY (autor_id)
                        REFERENCES uzytkownicy(id)
                        ON DELETE CASCADE
                 )
                """);

            statement.execute("""               
                 CREATE TABLE IF NOT EXISTS komentarze (
                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                     blog_id INTEGER NOT NULL,
                     autor_id INTEGER NOT NULL,
                     komentarz_nadrzedny_id INTEGER,
                     tresc TEXT NOT NULL,
                     data_utworzenia TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     status INTEGER NOT NULL DEFAULT 1
                         CHECK (status IN (0, 1)),                 
                     FOREIGN KEY (blog_id)
                         REFERENCES blogi(id)
                         ON DELETE CASCADE,                 
                     FOREIGN KEY (autor_id)
                         REFERENCES uzytkownicy(id)
                         ON DELETE CASCADE,                 
                     FOREIGN KEY (komentarz_nadrzedny_id)
                         REFERENCES komentarze(id)
                         ON DELETE CASCADE
             );
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS tagi (
                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                     nazwa TEXT NOT NULL UNIQUE COLLATE NOCASE
                );
                """);

            statement.execute("""                
                CREATE TABLE IF NOT EXISTS blogi_tagi (
                    blog_id INTEGER NOT NULL,
                    tag_id INTEGER NOT NULL,             
                     PRIMARY KEY (blog_id, tag_id),                 
                     FOREIGN KEY (blog_id)
                         REFERENCES blogi(id)
                         ON DELETE CASCADE,                 
                     FOREIGN KEY (tag_id)
                         REFERENCES tagi(id)
                         ON DELETE CASCADE
                 );
                """);

            statement.execute("""                
                    CREATE TABLE IF NOT EXISTS zgloszenia (
                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                     zglaszajacy_id INTEGER NOT NULL,
                     blog_id INTEGER,
                     komentarz_id INTEGER,
                     powod TEXT NOT NULL,
                     opis TEXT,
                     data_utworzenia TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     status INTEGER NOT NULL DEFAULT 0
                         CHECK (status IN (0, 1, 2)),                 
                     CHECK (
                         (blog_id IS NOT NULL AND komentarz_id IS NULL)
                         OR
                         (blog_id IS NULL AND komentarz_id IS NOT NULL)
                     ),                 
                     FOREIGN KEY (zglaszajacy_id)
                         REFERENCES uzytkownicy(id)
                         ON DELETE CASCADE,                 
                     FOREIGN KEY (blog_id)
                         REFERENCES blogi(id)
                         ON DELETE CASCADE,                 
                     FOREIGN KEY (komentarz_id)
                         REFERENCES komentarze(id)
                         ON DELETE CASCADE
                 );
                """);

            statement.execute("CREATE INDEX IF NOT EXISTS idx_blogi_autor ON blogi(autor_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_blogi_status ON blogi(status)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_komentarze_blog ON komentarze(blog_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_komentarze_autor ON komentarze(autor_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_komentarze_nadrzedny ON komentarze(komentarz_nadrzedny_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_zgloszenia_status ON zgloszenia(status)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_zgloszenia_blog ON zgloszenia(blog_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_zgloszenia_komentarz ON zgloszenia(komentarz_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_zgloszenia_zglaszajacy ON zgloszenia(zglaszajacy_id)");        }
    }
}