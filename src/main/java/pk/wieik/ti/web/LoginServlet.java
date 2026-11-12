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

@WebServlet("/logowanie")
public class LoginServlet extends HttpServlet {
    private final UserRepository users = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        form(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        try {
            var user = users.authenticate(request.getParameter("login"),
                    request.getParameter("haslo"));
            if (user.isEmpty()) {
                form(request, response, "Nieprawidłowy login lub hasło.");
                return;
            }

            // Usunięcie poprzedniej sesji zapobiega utrwaleniu jej identyfikatora.
            HttpSession previous = request.getSession(false);
            if (previous != null) previous.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.get().id());
            session.setAttribute("login", user.get().login());
            session.setAttribute("uprawnienia", user.get().uprawnienia());
            session.setMaxInactiveInterval(20 * 60);
            response.sendRedirect(request.getContextPath() + "/");
        } catch (SQLException ex) {
            throw new ServletException("Błąd bazy podczas logowania", ex);
        }
    }

    private void form(HttpServletRequest request, HttpServletResponse response,
                      String error) throws IOException {
        String path = Html.escape(request.getContextPath());
        String message = error == null ? "" : "<p class='error'>" + Html.escape(error) + "</p>";

        String login = request.getParameter("login");
        if (login == null) login = "";
        login = Html.escape(login);

        Html.send(response, "Logowanie", """
            <h1>Platforma blogowa — logowanie</h1>
            %2$s
    
            <form method="post" action="%1$s/logowanie">
                <label>Login:
                    <input name="login" value="%3$s" required maxlength="64"
                           autocomplete="username">
                </label><br>
    
                <label>Hasło:
                    <input type="password" name="haslo" required
                           autocomplete="current-password">
                </label><br>
    
                <button type="submit">Zaloguj</button>
            </form>
    
            <p><a href="%1$s/rejestracja">Nie masz konta? Zarejestruj się</a></p>
            """.formatted(path, message, login));
    }
}