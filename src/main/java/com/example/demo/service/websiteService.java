package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Vector;

import com.example.demo.searchengine.Ranker;
import com.example.demo.objects.OutputArray;
import com.example.demo.objects.ChoosenDocument;


@Service

public class websiteService {

    private ArrayList<String> websites = new ArrayList<String>();

    public websiteService() {
        websites.add("www.google.com");
        websites.add("www.facebook.com");
        websites.add("www.youtube.com");
        websites.add("www.twitter.com");
        websites.add("www.instagram.com");
        
    }

    public Vector<ChoosenDocument> getWebsite(String name) {
        Ranker ranker = new Ranker();
        return ranker.relevanceDetector(name).choosenDocuments;

    }
}
