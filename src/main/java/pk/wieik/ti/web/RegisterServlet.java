package pk.wieik.ti.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pk.wieik.ti.repository.UserRepository;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/rejestracja")
public class RegisterServlet extends HttpServlet {
    private final UserRepository users = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        form(request, response, "", null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        String login = request.getParameter("login");
        String password = request.getParameter("haslo");
        String password2 = request.getParameter("haslo2");

        if (password == null || !password.equals(password2)) {
            form(request, response, login, "Hasła nie są takie same.");
            return;
        }

        try {
            // Nowe konto to uprawnienia 0 (zwykły ziutek)
            users.createUser(login, password, 0);

            // Automatyczne logowanie
            var user = users.authenticate(login, password);
            if (user.isEmpty()) {
                throw new ServletException("Nie udało się zalogować po rejestracji");
            }

            HttpSession session = request.getSession();
            session.setAttribute("userId", user.get().id());
            session.setAttribute("login", user.get().login());
            session.setAttribute("uprawnienia", user.get().uprawnienia());
            session.setMaxInactiveInterval(20 * 60);

            response.sendRedirect(request.getContextPath() + "/");
        } catch (IllegalArgumentException ex) {
            form(request, response, login, ex.getMessage());
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE")) {
                form(request, response, login, "Taki login jest już zajęty.");
            } else {
                throw new ServletException("Błąd bazy podczas rejestracji", ex);
            }
        }
    }

    private void form(HttpServletRequest request, HttpServletResponse response,
                      String login, String error) throws IOException {
        String path = Html.escape(request.getContextPath());
        String message = error == null ? "" : "<p class='error'>" + Html.escape(error) + "</p>";
        Html.send(response, "Rejestracja", """
            <h1>Platforma blogowa — rejestracja</h1>
            %s
            <form method="post" action="%s/rejestracja">
                <label>Login: <input name="login" value="%s" required maxlength="64" autocomplete="username"></label><br>
                <label>Hasło (min. 12 znaków): <input type="password" name="haslo" required autocomplete="new-password"></label><br>
                <label>Powtórz hasło: <input type="password" name="haslo2" required autocomplete="new-password"></label><br>
                <button type="submit">Załóż konto</button>
            </form>
            <p><a href="%s/logowanie">Wróć do logowania</a></p>
            """.formatted(message, path, Html.escape(login), path));
    }
}
