package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import com.hopeful117.cv_analyzer.exception.JobScrapperException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

@Service
public class JobScrapperService {
    public String extractTextFromUrl(String url) throws IOException {
        validatePublicHttpUrl(url);
        try{
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8")
                .get();
        doc.select("script,style,nav,header,footer").remove();
        return doc.body().text();
    } catch(IOException exception){
            throw new JobScrapperException("Echec du scraping depuis l'url: " + url,exception);
        }

        }

    public void validatePublicHttpUrl(String value) {
        final URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException | NullPointerException exception) {
            throw new InvalidJobOfferException("L’URL de l’offre est invalide.");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                || uri.getUserInfo() != null) {
            throw new InvalidJobOfferException("Seules les URL publiques HTTP et HTTPS sont autorisées.");
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (normalizedHost.equals("localhost") || normalizedHost.endsWith(".localhost")) {
            throw new InvalidJobOfferException("Les adresses locales ou privées ne sont pas autorisées.");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isSiteLocalAddress() || address.isLinkLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new InvalidJobOfferException("Les adresses locales ou privées ne sont pas autorisées.");
                }
            }
        } catch (UnknownHostException exception) {
            throw new InvalidJobOfferException("Le domaine de l’offre est introuvable.");
        }
    }
}
