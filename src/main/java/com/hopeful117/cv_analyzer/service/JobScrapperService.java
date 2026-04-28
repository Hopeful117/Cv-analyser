package com.hopeful117.cv_analyzer.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JobScrapperService {
    public String extractTextFromUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        doc.select("script,style,nav,header,footer").remove();
        return doc.body().text();
    }
}
