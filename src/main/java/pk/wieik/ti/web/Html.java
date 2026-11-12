package pk.wieik.ti.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class Html {
    private Html() {}

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static void send(HttpServletResponse response, String title, String body)
            throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("""
            <!doctype html><html lang="pl"><head><meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>%s</title><style>
            body{font:16px system-ui,sans-serif;max-width:960px;margin:3rem auto;
                 padding:0 1rem;line-height:1.5;color:#17212b}
            input,button{font:inherit;padding:.45rem;margin:.3rem 0}
            table{width:100%%;border-collapse:collapse}
            th,td{text-align:left;border:1px solid #aaa;padding:.55rem}
            th{background:#edf3f8}.error{color:#ae1a1a}
            </style></head><body>%s</body></html>
            """.formatted(escape(title), body));
    }
}

