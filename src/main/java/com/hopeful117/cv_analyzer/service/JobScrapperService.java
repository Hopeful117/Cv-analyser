package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import com.hopeful117.cv_analyzer.exception.JobScrapperException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JobScrapperService {
    public String extractTextFromUrl(String url) throws IOException {
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
}
