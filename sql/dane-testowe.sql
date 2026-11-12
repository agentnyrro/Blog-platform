PRAGMA foreign_keys = ON;

-- Użytkownicy
INSERT OR IGNORE INTO uzytkownicy (login, haslo, uprawnienia) VALUES ('admin',     'pbkdf2_sha256$600000$ALV57MBwwqCtJrcTfcUHVw==$scwzmoffVSRf6UWJNZ3LHLKgjFv2l+9AWplvu7sTgcc=', 2);
INSERT OR IGNORE INTO uzytkownicy (login, haslo, uprawnienia) VALUES ('moderator', 'pbkdf2_sha256$600000$kLNc9Zt1g1McjwGyzLFeyQ==$Fg6FPvkSmojg7wv3IVJldRX4Dz+5XTHe7wMs5NFOhHw=', 1);
INSERT OR IGNORE INTO uzytkownicy (login, haslo, uprawnienia) VALUES ('ania',      'pbkdf2_sha256$600000$bp1EjM7xLYKSEsIyhvnyfg==$JyFVy+vAiBIqeLj0dk6qwbYHkO8jaEGcUEeYr0/4yog=', 0);
INSERT OR IGNORE INTO uzytkownicy (login, haslo, uprawnienia) VALUES ('marek',     'pbkdf2_sha256$600000$SuOVlE2yk2tr6Mu+bOLOgQ==$PJg/p9T7a8/yLJoggK+h9NfFHaKthmQ++VJ0F/xdLjU=', 0);

-- Tagi
INSERT OR IGNORE INTO tagi (nazwa) VALUES ('java');
INSERT OR IGNORE INTO tagi (nazwa) VALUES ('sqlite');
INSERT OR IGNORE INTO tagi (nazwa) VALUES ('servlety');
INSERT OR IGNORE INTO tagi (nazwa) VALUES ('podroze');
INSERT OR IGNORE INTO tagi (nazwa) VALUES ('gotowanie');

-- Blogi
INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'admin'), 'Pierwszy wpis',
       'Witaj na platformie blogowej. To przykładowy wpis.', 1
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Pierwszy wpis');

INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'admin'), 'Szkic o SQLite',
       'Ten wpis jeszcze nie jest gotowy.', 0
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Szkic o SQLite');

INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'ania'), 'Tydzień w Lizbonie',
       'Tramwaj 28, pastéis de nata i zachody słońca nad Tagiem. Polecam każdemu.', 1
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Tydzień w Lizbonie');

INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'ania'), 'Pomysły na wpisy',
       'Lista rzeczy, o których chcę napisać. Na razie tylko notatki.', 0
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Pomysły na wpisy');

INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'marek'), 'Domowy chleb na zakwasie',
       'Mąka, woda, sól i trochę cierpliwości. Zakwas musi odpocząć co najmniej dobę.', 1
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Domowy chleb na zakwasie');

INSERT INTO blogi (autor_id, tytul, tresc, status)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'marek'), 'Servlety bez frameworka',
       'Czy do prostej aplikacji naprawdę potrzebujemy Springa? Krótkie podsumowanie.', 1
    WHERE NOT EXISTS (SELECT 1 FROM blogi WHERE tytul = 'Servlety bez frameworka');

-- Tagi blogów
INSERT OR IGNORE INTO blogi_tagi (blog_id, tag_id)
SELECT b.id, t.id FROM blogi b, tagi t
WHERE b.tytul = 'Pierwszy wpis' AND t.nazwa IN ('java', 'servlety');

INSERT OR IGNORE INTO blogi_tagi (blog_id, tag_id)
SELECT b.id, t.id FROM blogi b, tagi t
WHERE b.tytul = 'Szkic o SQLite' AND t.nazwa = 'sqlite';

INSERT OR IGNORE INTO blogi_tagi (blog_id, tag_id)
SELECT b.id, t.id FROM blogi b, tagi t
WHERE b.tytul = 'Tydzień w Lizbonie' AND t.nazwa = 'podroze';

INSERT OR IGNORE INTO blogi_tagi (blog_id, tag_id)
SELECT b.id, t.id FROM blogi b, tagi t
WHERE b.tytul = 'Domowy chleb na zakwasie' AND t.nazwa = 'gotowanie';

INSERT OR IGNORE INTO blogi_tagi (blog_id, tag_id)
SELECT b.id, t.id FROM blogi b, tagi t
WHERE b.tytul = 'Servlety bez frameworka' AND t.nazwa IN ('java', 'servlety');

-- Komentarze
INSERT INTO komentarze (blog_id, autor_id, tresc)
SELECT (SELECT id FROM blogi WHERE tytul = 'Pierwszy wpis'),
       (SELECT id FROM uzytkownicy WHERE login = 'ania'), 'Pierwszy komentarz!'
    WHERE NOT EXISTS (SELECT 1 FROM komentarze WHERE tresc = 'Pierwszy komentarz!');

INSERT INTO komentarze (blog_id, autor_id, tresc)
SELECT (SELECT id FROM blogi WHERE tytul = 'Tydzień w Lizbonie'),
       (SELECT id FROM uzytkownicy WHERE login = 'marek'), 'Świetne zdjęcia, też chcę tam pojechać.'
    WHERE NOT EXISTS (SELECT 1 FROM komentarze WHERE tresc = 'Świetne zdjęcia, też chcę tam pojechać.');

INSERT INTO komentarze (blog_id, autor_id, tresc)
SELECT (SELECT id FROM blogi WHERE tytul = 'Domowy chleb na zakwasie'),
       (SELECT id FROM uzytkownicy WHERE login = 'ania'), 'KUP TERAZ TANIE ZEGARKI -> spam.example'
    WHERE NOT EXISTS (SELECT 1 FROM komentarze WHERE tresc = 'KUP TERAZ TANIE ZEGARKI -> spam.example');

-- Komentarze
INSERT INTO komentarze (blog_id, autor_id, komentarz_nadrzedny_id, tresc)
SELECT k.blog_id, (SELECT id FROM uzytkownicy WHERE login = 'admin'), k.id, 'Odpowiedź na pierwszy komentarz.'
FROM komentarze k
WHERE k.tresc = 'Pierwszy komentarz!'
  AND NOT EXISTS (SELECT 1 FROM komentarze WHERE tresc = 'Odpowiedź na pierwszy komentarz.');

INSERT INTO komentarze (blog_id, autor_id, komentarz_nadrzedny_id, tresc)
SELECT k.blog_id, (SELECT id FROM uzytkownicy WHERE login = 'ania'), k.id, 'Polecam, najlepiej wiosną!'
FROM komentarze k
WHERE k.tresc = 'Świetne zdjęcia, też chcę tam pojechać.'
  AND NOT EXISTS (SELECT 1 FROM komentarze WHERE tresc = 'Polecam, najlepiej wiosną!');

-- Zgłoszenia komentarzy
INSERT INTO zgloszenia (zglaszajacy_id, komentarz_id, powod, opis)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'marek'), k.id, 'Spam', 'Komentarz reklamowy, prośba o usunięcie.'
FROM komentarze k
WHERE k.tresc = 'KUP TERAZ TANIE ZEGARKI -> spam.example'
  AND NOT EXISTS (SELECT 1 FROM zgloszenia WHERE opis = 'Komentarz reklamowy, prośba o usunięcie.');

-- Zgłoszenia blogów
INSERT INTO zgloszenia (zglaszajacy_id, blog_id, powod, opis)
SELECT (SELECT id FROM uzytkownicy WHERE login = 'ania'), b.id, 'Nieprawdziwe informacje', 'Przykładowe zgłoszenie testowe bloga.'
FROM blogi b
WHERE b.tytul = 'Servlety bez frameworka'
  AND NOT EXISTS (SELECT 1 FROM zgloszenia WHERE opis = 'Przykładowe zgłoszenie testowe bloga.');