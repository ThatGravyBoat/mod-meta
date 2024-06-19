package tech.thatgravyboat.modmeta.cloudflare;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.thatgravyboat.modmeta.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CFPagesDeleter {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    private static boolean isObfuscated(String id, JsonElement element) {
        if (!(element instanceof JsonObject obj)) return false;
        JsonElement build = obj.get("build_config");
        if (!(build instanceof JsonObject buildObj)) return false;
        return buildObj.get("destination_dir").getAsString().equals("link_obfuscated_%s".formatted(id));
    }

    public static void deleteOldLinks(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudflare.com/client/v4/accounts/%s/pages/projects".formatted(
                        Main.CLOUDFLARE_ACCOUNT
                )))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(Main.CLOUDFLARE_TOKEN))
                .GET()
                .build();

        var res = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new RuntimeException("Failed to fetch Cloudflare projects: " + res.body());
        JsonObject object = GSON.fromJson(res.body(), JsonObject.class);
        if (!object.has("result")) return;
        List<String> toDelete = new ArrayList<>();
        for (JsonElement result : object.getAsJsonArray("result")) {
            if (isObfuscated(id, result)) {
                toDelete.add(result.getAsJsonObject().get("name").getAsString());
            }
        }

        for (String name : toDelete) {
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudflare.com/client/v4/accounts/%s/pages/projects/%s".formatted(
                            Main.CLOUDFLARE_ACCOUNT,
                            name
                    )))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer %s".formatted(Main.CLOUDFLARE_TOKEN))
                    .DELETE()
                    .build();
            CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}
