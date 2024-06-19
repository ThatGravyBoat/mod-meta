package tech.thatgravyboat.modmeta;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public record ProjectMeta(
    String modrinthId,
    String curseforgeId,
    String summary
) {

    public static ProjectMeta of(String html) {
        Document doc = Jsoup.parse(html);
        String modrinthId = doc.select("meta[name=modrinth:id]").attr("content");
        String curseforgeId = doc.select("meta[name=curseforge:id]").attr("content");
        String summary = doc.select("meta[name=description]").attr("content");

        return new ProjectMeta(modrinthId, curseforgeId, summary);
    }
}
