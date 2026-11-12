package pk.wieik.ti.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        String path = Html.escape(request.getContextPath());

        String content;

        if (Auth.isLoggedIn(request)) {
            String login = Html.escape(Auth.login(request));

            content = """
                <h1>Platforma blogowa</h1>

                <p>Zalogowano jako: <strong>%s</strong></p>

                <form method="post" action="%s/wyloguj">
                    <button type="submit">Wyloguj</button>
                </form>
                """.formatted(login, path);

        } else {
            content = """
                <h1>Platforma blogowa</h1>

                <p>Witaj! Zaloguj się lub utwórz konto.</p>

                <nav>
                    <a href="%s/logowanie">Logowanie</a>
                    |
                    <a href="%s/rejestracja">Rejestracja</a>
                </nav>
                """.formatted(path, path);
        }

        Html.send(response, "Strona główna", content);
    }
}