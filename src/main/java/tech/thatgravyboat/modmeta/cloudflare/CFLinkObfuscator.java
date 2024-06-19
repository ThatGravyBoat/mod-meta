package tech.thatgravyboat.modmeta.cloudflare;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CFLinkObfuscator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";

    private static String randomId(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            var set = i % 2 == 0 || Math.random() < 0.5 ? LETTERS : NUMBERS;
            builder.append(set.charAt((int) (Math.random() * set.length())));
        }
        return builder.toString();
    }

    private static String createRedirectFile(Map<String, String> obfuscated) {
        StringBuilder _redirect = new StringBuilder();
        obfuscated.forEach((link, id) -> _redirect.append("/").append(id).append(" ").append(link).append("\n"));
        return _redirect.toString();
    }

    public static Map<String, String> obfuscate(String id, Set<String> links) throws Exception {
        if (links.isEmpty()) return Map.of();
        Map<String, String> obfuscated = links.stream().collect(Collectors.toMap(
                Function.identity(), link -> randomId(6)
        ));

        CFPagesDeleter.deleteOldLinks(id);
        String name = randomId(24);
        String subdomain = CFPagesCreater.createNewProject(id, name);
        CFPagesRedirectUploader.upload(name, createRedirectFile(obfuscated));

        return obfuscated.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> "https://%s/%s".formatted(subdomain, e.getValue())
        ));
    }
}
