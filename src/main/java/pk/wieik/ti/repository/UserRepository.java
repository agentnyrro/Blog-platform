package pk.wieik.ti.repository;

import pk.wieik.ti.auth.PasswordUtil;
import pk.wieik.ti.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** SQL i obsługa kont użytkowników są odseparowane od Servletu. */
public class UserRepository {
    public record AuthUser(int id, String login, int uprawnienia) {}

    public int countUsers() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM uzytkownicy");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void createUser(String login, String password, int uprawnienia) throws SQLException {
        if (login == null || login.isBlank() || login.length() > 64) {
            throw new IllegalArgumentException("Login musi mieć od 1 do 64 znaków");
        }
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("Hasło musi mieć co najmniej 12 znaków");
        }
        if (uprawnienia < 0 || uprawnienia > 2) {
            throw new IllegalArgumentException("Nieznany poziom uprawnień");
        }

        String sql = "INSERT INTO uzytkownicy (login, haslo, uprawnienia) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login.trim());
            ps.setString(2, PasswordUtil.hashPassword(password));
            ps.setInt(3, uprawnienia);
            ps.executeUpdate();
        }
    }

    /** Zwraca użytkownika BEZ hasła, tylko przy poprawnych danych logowania. */
    public Optional<AuthUser> authenticate(String login, String password) throws SQLException {
        if (login == null || login.isBlank() || login.length() > 64 || password == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, login, haslo, uprawnienia FROM uzytkownicy WHERE login = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                if (!PasswordUtil.verifyPassword(password, rs.getString("haslo"))) {
                    return Optional.empty();
                }
                return Optional.of(new AuthUser(
                        rs.getInt("id"), rs.getString("login"), rs.getInt("uprawnienia")));
            }
        }
    }
}

