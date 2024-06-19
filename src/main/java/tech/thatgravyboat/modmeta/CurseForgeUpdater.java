package tech.thatgravyboat.modmeta;

import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurseForgeUpdater {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static String fix(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("center").forEach(e -> e.tagName("div").attr("style", "text-align:center;"));
        doc.select("blockquote").forEach(e -> e.tagName("div").attr("style", "padding-left:40px;"));
        return doc.body().html();
    }

    public static void update(String id, String html) throws Exception {
        System.out.println("Updating CurseForge project...");
        JsonObject obj = new JsonObject();
        obj.addProperty("description", fix(html));
        obj.addProperty("descriptionType", 5);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://authors.curseforge.com/_api/projects/description/%s".formatted(id)))
            .header("Content-Type", "application/json")
            .header("Cookie", "CobaltSession=%s;".formatted(Main.CURSEFORGE_TOKEN))
            .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
            .build();
        var res = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            System.err.println("Failed to update CurseForge project: " + res.body());
        }
    }
}
