package tech.thatgravyboat.modmeta.cloudflare;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tech.thatgravyboat.modmeta.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CFPagesCreater {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static String createNewProject(String id, String name) throws Exception {
        JsonObject body = new JsonObject();
        JsonObject buildConfig = new JsonObject();
        buildConfig.addProperty("destination_dir", "link_obfuscated_%s".formatted(id));
        buildConfig.addProperty("build_command", "");
        buildConfig.addProperty("build_caching", true);
        buildConfig.addProperty("root_dir", "/");
        body.add("build_config", buildConfig);
        body.addProperty("name", name);
        body.addProperty("production_branch", "main");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudflare.com/client/v4/accounts/%s/pages/projects".formatted(
                        Main.CLOUDFLARE_ACCOUNT
                )))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(Main.CLOUDFLARE_TOKEN))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RuntimeException("Failed to create new project: " + response.body());
        JsonObject object = GSON.fromJson(response.body(), JsonObject.class);
        return object.getAsJsonObject("result").get("subdomain").getAsString();
    }
}
