package tech.thatgravyboat.modmeta;

import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModrinthUpdater {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void update(String id, String html) throws Exception {
        System.out.println("Updating Modrinth project...");
        JsonObject obj = new JsonObject();
        obj.addProperty("body", html);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.modrinth.com/v2/project/%s".formatted(id)))
            .header("Content-Type", "application/json")
            .header("Authorization", Main.MODRINTH_TOKEN)
            .method("PATCH", HttpRequest.BodyPublishers.ofString(obj.toString()))
            .build();
        var res = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 204) {
            System.err.println("Failed to update CurseForge project: " + res.body());
        }
    }
}
