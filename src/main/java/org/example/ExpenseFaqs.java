package org.example;

import org.bson.Document;


public class ExpenseFaqs {

    private String processedQuery;
    private String query;
    private String answer;
    private String module;
    private String tag;

    public String getProcessedQuery() {
        return processedQuery;
    }

    public void setProcessedQuery(String processedQuery) {
        this.processedQuery = processedQuery;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

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

    public ExpenseFaqs(String query, String answer, String module, String tag) {
        this.query = query;
        this.answer = answer;
        this.module = module;
        this.tag = tag;
    }

    // Add this method for MongoDB compatibility
    public Document toExpenseDocument() {
        return new Document("query", this.query)
                .append("answer", this.answer)
                .append("module", this.module)
                .append("tag", this.tag)
                .append("processedQuery", this.processedQuery);

    }
}