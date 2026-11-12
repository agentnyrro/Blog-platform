package pk.wieik.ti.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class Auth {
    private Auth() {}

    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    public static int userId(HttpServletRequest request) {
        return isLoggedIn(request) ? (Integer) request.getSession(false).getAttribute("userId") : 0;
    }

    // TODO: placeholder, można zostawić albo może jakiś enum dodać
    public static int level(HttpServletRequest request) {
        return isLoggedIn(request) ? (Integer) request.getSession(false).getAttribute("uprawnienia") : -1;
    }

    public static String login(HttpServletRequest request) {
        return isLoggedIn(request) ? (String) request.getSession(false).getAttribute("login") : "";
    }

    // To be forbidden or not to be forbidden, that is the question. (Shakespeare podkoloryzowany, 1600)
    // W parametrach jest minLevel, jeżeli użytkownik nie ma uprawnień conajmniej minLevel to dupa
    public static boolean require(HttpServletRequest request, HttpServletResponse response,
                                  int minLevel) throws IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/logowanie");
            return false;
        }
        if (level(request) < minLevel) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
