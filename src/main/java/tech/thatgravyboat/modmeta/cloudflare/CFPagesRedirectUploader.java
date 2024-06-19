package tech.thatgravyboat.modmeta.cloudflare;

import tech.thatgravyboat.modmeta.Main;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;

public class CFPagesRedirectUploader {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static ByteArrayOutputStream multipartBody(String boundary, String redirect) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(("--%s\r\n".formatted(boundary)).getBytes());
        stream.write(("Content-Disposition: form-data; name=\"_redirects\"; filename=\"/path/to/file\"\n").getBytes());
        stream.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());
        stream.write(redirect.getBytes());
        stream.write("\r\n".getBytes());

        stream.write(("--%s\r\n".formatted(boundary)).getBytes());
        stream.write("Content-Disposition: form-data; name=\"manifest\"\r\n".getBytes());
        stream.write("\r\n{}\r\n".getBytes());

        stream.write(("--%s--\r\n".formatted(boundary)).getBytes());
        return stream;
    }

    public static void upload(String name, String redirectFile) throws Exception {
        String boundary = new BigInteger(256, new SecureRandom()).toString();
        byte[] body = multipartBody(boundary, redirectFile).toByteArray();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.cloudflare.com/client/v4/accounts/%s/pages/projects/%s/deployments".formatted(
                                Main.CLOUDFLARE_ACCOUNT,
                                name
                        )
                ))
                .header("Content-Type", "multipart/form-data; boundary=%s".formatted(boundary))
                .header("Authorization", "Bearer %s".formatted(Main.CLOUDFLARE_TOKEN))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Failed to upload redirect file: %s".formatted(response.body()));
        }
    }
}
