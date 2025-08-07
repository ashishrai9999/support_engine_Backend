package org.example;

import org.bson.Document;

public class LoginFaqs {
    private String query;
    private String answer;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }


    public String getAnswer() {
        return answer;
    }


    public LoginFaqs(String query, String answer) {
        this.query = query;
        this.answer = answer;
    }

    // Add this method for MongoDB compatibility
    public Document toLoginDocument() {
        return new Document("query", this.query)
                .append("answer", this.answer);
    }
}