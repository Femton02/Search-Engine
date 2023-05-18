package com.example.demo.api.controller;

import com.example.demo.objects.ChoosenDocument;
import com.example.demo.service.websiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.objects.OutputArray;

import java.util.Vector;

@RestController
@CrossOrigin
public class websiteController {
    private websiteService websiteService;

    @Autowired
    public websiteController(websiteService websiteService) {
        this.websiteService = websiteService;
    }

    @GetMapping("/website")
    public Vector<ChoosenDocument> getWebsite(@RequestParam(value = "query") String query) {
        return websiteService.getWebsite(query);
    }
}
