package com.example.demo.searchengine;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import com.example.demo.objects.Doc;
import com.example.demo.objects.CrawledItem;
import com.example.demo.objects.DocumentItem;
import com.example.demo.objects.ChoosenDocument;
import com.example.demo.objects.OutputArray;
import com.example.demo.objects.DocumentSource;
import com.example.demo.objects.Word;
import com.example.demo.objects.IndexerItem;

import org.jsoup.Jsoup;
import java.time.Instant;
import java.time.Duration;

import java.io.IOException;
import java.util.*;
import java.util.Arrays;

import java.util.Comparator;
import java.util.Vector;
import java.util.Collections;

public class Ranker {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> mockDB;

    //MongoClient mongoClient, MongoDatabase db, MongoCollection<Document> mockDB
    public Ranker() {
        this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
        this.db = mongoClient.getDatabase("SampleDB");
        this.mockDB = db.getCollection("wordsIndices");
    }
    
    public OutputArray relevanceDetector(String query) {
        query = query.toLowerCase();
        String[] words = query.split("\\W+");
        OutputArray output = new OutputArray();


        //looping on the number of the words in the query
        for (int i = 0; i < words.length; i++) {
            Document doc = mockDB.find(new Document("word", words[i])).first();

            Gson gson = new Gson();
            IndexerItem Item = gson.fromJson(doc.toJson(), IndexerItem.class);
            double IDF = Math.log10(6000/Item.df);

            for (int j = 0; j < Item.documents.length; j++) {
                ChoosenDocument DocumentChoosen = new ChoosenDocument();
                double N_TF = (Item.documents[j].tf) / Double.parseDouble(Item.documents[j].doc_length);
                double TF_IDF = N_TF * IDF;
                
                DocumentChoosen.setID(Item.documents[j].doc_id);
                DocumentChoosen.url = Item.documents[j].doc_link;
                DocumentChoosen.tf=(N_TF);
                DocumentChoosen.setIDF(IDF);
                DocumentChoosen.setTF_IDF(TF_IDF    );
                DocumentChoosen.setDescription(Item.documents[j].first_occurrence);
                
    
    
                output.AddDoucment(DocumentChoosen);
            }
            
        }
    
        Collections.sort(output.choosenDocuments, new Comparator<ChoosenDocument>() {
            public int compare(ChoosenDocument obj1, ChoosenDocument obj2) {
                return Double.compare(obj2.getTF_IDF(), obj1.getTF_IDF());
            }
        });

        return output;
    }
    
    
    public OutputArray intersectionDetector(String query) {
        query = query.toLowerCase();
        String[] words = query.split("\\W+");
        OutputArray output = new OutputArray();
        
        //looping on the number of the words in the query
        for (int i = 0; i < words.length; i++) {
            Document doc = mockDB.find(new Document("word", words[i])).first();
            Gson gson = new Gson();
            IndexerItem Item = gson.fromJson(doc.toJson(), IndexerItem.class);
            double IDF = Math.log10(6000/Item.df);
            
            for (int j = 0; j < Item.documents.length; j++) {
                ChoosenDocument DocumentChoosen = new ChoosenDocument();
              //I have an indexer item, Item . I am looping on the doucment within it to create a different data structure
                // Array of intersected documents
                DocumentChoosen.setID(Item.documents[j].doc_id);
                DocumentChoosen.url = Item.documents[j].doc_link;
                DocumentChoosen.setIDF(IDF);
                DocumentChoosen.description=Item.documents[j].first_occurrence;
                
                DocumentChoosen.availableWords.add(Pair.of(words[i], Item.documents[j].positions));
    
                if (i == 0) {
                    output.AddDoucment(DocumentChoosen);
                } else {
                    output.getIntersectedDocuments(DocumentChoosen);
                }
                
            }
            output.choosenDocuments.clear();
            output.choosenDocuments.addAll(output.intersectedDocuments);
            output.intersectedDocuments.clear();
            
        }
    
        return fastPhraseRelevanceDetector(output, words);
    }
    
    public OutputArray fastPhraseRelevanceDetector(OutputArray output, String[] query){
        
        int numOfPhrases = 0;
        int isPhraseFound = 0;
        int founded =0;
        OutputArray phraseSearchOutput = new OutputArray();
    
        //loop over number of documents to know which one is gotta be deprived
        for (int i=0; i<output.choosenDocuments.size(); i++){
            
            //loop over number of occ. of the first word in the query
            numOfPhrases = 0;
            for (int j=0; j<output.choosenDocuments.get(i).availableWords.get(0).getRight().length; j++){
                
                int firstOccurance = output.choosenDocuments.get(i).availableWords.get(0).getRight()[j];
                
                 isPhraseFound = 0;
                for (int k=1; k< query.length; k++){
                    int [] positions = output.choosenDocuments.get(i).availableWords.get(k).getRight();
                    
                    if (Arrays.binarySearch(positions,(firstOccurance+k)) >=0)
                        isPhraseFound++;
                    else
                        break;
                }
                
                if (isPhraseFound == query.length-1) {
                    numOfPhrases++;
                    founded=1;
                }
            }
            if (founded == 1) {
                output.choosenDocuments.get(i).setIDF(Math.log10(6001/output.choosenDocuments.size()));
                output.choosenDocuments.get(i).setTF(numOfPhrases);
                output.choosenDocuments.get(i).setTF_IDF(numOfPhrases*output.choosenDocuments.get(i).getIDF());
                phraseSearchOutput.choosenDocuments.add(output.choosenDocuments.get(i));
            }
            founded = 0;
        }
    
        return phraseSearchOutput;
    }
    
    public void slowPhraseRelavanceDetector(OutputArray output, String[] query)  {
        OutputArray phraseSearchOutput = new OutputArray();
    
        for (int i=0; i<output.choosenDocuments.size(); i++) {
            String url = output.choosenDocuments.get(i).url;
            org.jsoup.nodes.Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String htmlString = doc.toString();
            String inputQuery = String.join(" ", query);
            
            if (htmlString.contains(inputQuery)) {
                phraseSearchOutput.choosenDocuments.add(output.choosenDocuments.get(i));
            }
        }
        phraseSearchOutput.toString();
    }
    
    public void populairty() {
        //Getting the number of documents in the database
        // Initiating the treemap that will hold the database items
        TreeMap<String, DocumentSource> treeMap_i = new TreeMap<>();
        TreeMap<String, DocumentSource> treeMap_ii = new TreeMap<>();
        
        //Document update = new Document("$set", new Document("pop", (float) 1 / 6001));
        //mockDB.updateMany(new Document(), update);
        
        //looping on the number of the documents in the database and store them in the treemap "i"
        for (Document document : mockDB.find().limit(6000)) {
            Gson gson = new Gson();
            CrawledItem Crawled = gson.fromJson(document.toJson(), CrawledItem.class);
            DocumentSource DocumentSource = new DocumentSource(Crawled.parentPages, Crawled.includedLinks, Crawled.pop);
            //get the treemap full of entries
            System.out.println(Crawled.url);
            treeMap_i.put(Crawled.url, DocumentSource);
        }
        
        //number of iterations to be done
       
            //looping on the number of the documents in the database and store them in the treemap "ii"
            for (String key : treeMap_i.keySet()) {
                //Do calculations for each document
                String parentPages[] = treeMap_i.get(key).getCameFrom();
                double newPopularity = 0;
                //This means that the document has no parent pages, and its popularity will still as it is
                if (parentPages[0] == null) treeMap_ii.put(key, treeMap_i.get(key));
                else {
                    for (int i = 0; i < parentPages.length; i++) {
                        System.out.println(treeMap_i.get(parentPages[i]).getPop());
                        double parentPop = treeMap_i.get(parentPages[i]).getPop();
                        int parentOutgoingLinks = treeMap_i.get(parentPages[i]).getHyperLinkCount();
                        newPopularity = newPopularity + (parentPop / parentOutgoingLinks);
                        treeMap_ii.put(key, new DocumentSource(treeMap_i.get(key).getCameFrom(), treeMap_i.get(key).getHyperLinkCount(), newPopularity));
                        mockDB.updateOne(new Document("url", key), Updates.set("pop", newPopularity));
                    }
                }
            }
        
    }
    
    public static void main(String[] args) {
        //create an object of the class
        Ranker searchEngine = new Ranker();
        //call the function
        searchEngine.intersectionDetector("soccer club");
    }
  
    
}