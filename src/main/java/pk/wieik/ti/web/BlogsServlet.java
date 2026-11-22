package pk.wieik.ti.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pk.wieik.ti.repository.BlogRepository;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/blogi")
public class BlogsServlet extends HttpServlet {
    private final BlogRepository blogs = new BlogRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/logowanie");
            return;
        }
        try {
            StringBuilder html = new StringBuilder();
            html.append("<h1>Lista blogów</h1><p>Zalogowano jako: <strong>")
                .append(Html.escape((String) session.getAttribute("login")))
                .append("</strong> (uprawnienia: ")
                .append(session.getAttribute("uprawnienia"))
                .append(")</p><table><thead><tr><th>Tytuł</th><th>Autor</th>")
                .append("<th>Tagi</th><th>Data</th><th>Komentarze</th>")
                .append("</tr></thead><tbody>");

            var rows = blogs.findAll();
            for (var row : rows) {
                html.append("<tr><td>").append(Html.escape(row.tytul()))
                    .append("</td><td>").append(Html.escape(row.autor()))
                    .append("</td><td>").append(Html.escape(row.tagi()))
                    .append("</td><td>").append(Html.escape(row.dataUtworzenia()))
                    .append("</td><td>").append(row.liczbaKomentarzy())
                    .append("</td></tr>");
            }
            html.append("</tbody></table>");
            if (rows.isEmpty()) {
                html.append("<p>Nie ma jeszcze blogów. Uruchom skrypt sql/dane-testowe.sql.</p>");
            }
            html.append("<form method='post' action='")
                .append(Html.escape(request.getContextPath()))
                .append("/wyloguj'><button>Wyloguj</button></form>");
            Html.send(response, "Lista blogów", html.toString());
        } catch (SQLException ex) {
            throw new ServletException("Błąd odczytu blogów", ex);
        }
    }
}
