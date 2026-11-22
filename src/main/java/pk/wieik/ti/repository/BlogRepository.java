package pk.wieik.ti.repository;

import pk.wieik.ti.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlogRepository {
    public record BlogRow(int id, String tytul, String autor, String tagi,
                          String dataUtworzenia, int liczbaKomentarzy) {}

    /** Tylko opublikowane blogi (status = 1). */
    public List<BlogRow> findAll() throws SQLException {
        String sql = """
            SELECT b.id, b.tytul, u.login AS autor,
                   (SELECT GROUP_CONCAT(t.nazwa, ', ')
                    FROM blogi_tagi bt
                    JOIN tagi t ON t.id = bt.tag_id
                    WHERE bt.blog_id = b.id) AS tagi,
                   b.data_utworzenia,
                   (SELECT COUNT(*) FROM komentarze k
                    WHERE k.blog_id = b.id AND k.status = 1) AS liczba_komentarzy
            FROM blogi b
            JOIN uzytkownicy u ON u.id = b.autor_id
            WHERE b.status = 1
            ORDER BY b.data_utworzenia DESC, b.id DESC
            LIMIT 200
            """;
        List<BlogRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new BlogRow(
                    rs.getInt("id"), rs.getString("tytul"),
                    rs.getString("autor"), rs.getString("tagi"),
                    rs.getString("data_utworzenia"), rs.getInt("liczba_komentarzy")
                ));
            }
        }
        return rows;
    }
}
