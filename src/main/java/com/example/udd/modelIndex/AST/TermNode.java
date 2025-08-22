package com.example.udd.modelIndex.AST;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;

public class TermNode extends Node{
    public String field;
    public String value;
    public boolean isPhrase;

    public TermNode(String field, String value, boolean isPhrase) {
        this.field = field;
        this.value = value;
        this.isPhrase = isPhrase;
    }
}
