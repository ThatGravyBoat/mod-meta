package tech.thatgravyboat.modmeta;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tech.thatgravyboat.modmeta.cloudflare.CFLinkObfuscator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static final String CURSEFORGE_TOKEN = System.getenv("CURSEFORGE_TOKEN");
    public static final String MODRINTH_TOKEN = System.getenv("MODRINTH_TOKEN");
    public static final String CLOUDFLARE_ACCOUNT = System.getenv("CLOUDFLARE_ACCOUNT");
    public static final String CLOUDFLARE_TOKEN = System.getenv("CLOUDFLARE_TOKEN");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar modmeta.jar <path-to-folder>");
            System.exit(1);
        }
        Path folder = Paths.get(args[0]);
        String html = Files.readString(folder.resolve("index.html"));
        ProjectMeta meta = ProjectMeta.of(html);

        Document document = Jsoup.parse(html);
        Set<String> links = document.select("a[obfuscate]").stream().map(e -> e.attr("href")).collect(Collectors.toSet());
        Map<String, String> obfuscated = CFLinkObfuscator.obfuscate(links);
        obfuscated.forEach((oldLink, newLink) -> {
            for (Element element : document.select("a[href=\"%s\"]".formatted(oldLink))) {
                element.attr("href", newLink);
                element.removeAttr("obfuscate");
            }
        });

        String page = document.body().html();
        CurseForgeUpdater.update(meta.curseforgeId(), page);
        ModrinthUpdater.update(meta.modrinthId(), page);
    }
}
