package com.example.demo.objects;
import org.apache.commons.lang3.tuple.Pair;

import java.security.KeyPair;
import java.util.Vector;

public class ChoosenDocument {
    public String id;
    public double tf;
    public double idf;
    public double tf_idf;
    public String url;
    public String description;
    public Vector<Pair<String,int[]>> availableWords;
    
    public ChoosenDocument() {
        this.id = "";
        this.tf = 0;
        this.idf = 0;
        this.tf_idf = 0;
        availableWords = new Vector<>();
        this.description= "";
    }
    
    public void setTF(double TF) {
        this.tf = TF;
    }
    public void setIDF(double IDF) {
        this.idf =  IDF;
    }
    public void setTF_IDF(double TF_IDF) {
        this.tf_idf = TF_IDF;
    }
    public void setID(String id) {
        this.id = id;
    }
    public void setDescription(String desc){
        this.description = desc;
    }
    public String getDoc_id() {
        return id;
    }
    public double gettf() {
        return tf;
    }
    public double getIDF() {
        return idf;
    }
    public double getTF_IDF() {
        return tf_idf;
    }

    public String description() {
        return description;
    }
    @Override
    public String toString() {
        System.out.println("ChoosenDocument{ " + "id='" + id + '\'' + ", TF=" + tf + ", IDF=" + idf + ", TF_IDF=" + tf_idf + " " + "url= " + url + '}');
        return "ChoosenDocument{" + "id='" + id + '\'' + ", TF=" + tf + ", IDF=" + idf + ", TF_IDF=" + tf_idf + '}';
    }
}