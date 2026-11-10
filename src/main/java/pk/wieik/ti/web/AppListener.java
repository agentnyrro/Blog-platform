package pk.wieik.ti.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pk.wieik.ti.db.Database;
import pk.wieik.ti.repository.UserRepository;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Database.initialize();
            event.getServletContext().log("SQLite: " + Database.getDatabaseFile());
            System.out.println("SQLite: " + Database.getDatabaseFile());

            // Opcjonalne, jednorazowe konto do TESTÓW (nigdy hardcoded w kodzie).
            String login = System.getenv("ADMIN_LOGIN");
            String password = System.getenv("ADMIN_PASSWORD");
            if ((login == null) != (password == null)) {
                throw new IllegalStateException("Ustaw oba parametry ADMIN_LOGIN i ADMIN_PASSWORD");
            }
            if (login != null && new UserRepository().countUsers() == 0) {
                new UserRepository().createUser(login, password, 2);
                event.getServletContext().log("Utworzono pierwsze konto administratora");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Nie można zainicjalizować bazy SQLite", e);
        }
    }
}