package pk.wieik.ti.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("")
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

                <p>Zalogowano jako: <strong>%1$s</strong></p>

                <p><a href="%2$s/blogi">Przeglądaj blogi</a></p>

                <form method="post" action="%2$s/wyloguj">
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
                    <a href="%1$s/rejestracja">Rejestracja</a>
                    |
                    <a href="%1$s/blogi">Przeglądaj blogi</a>
                </nav>
                """.formatted(path);
        }

        Html.send(response, "Strona główna", content);
    }
}