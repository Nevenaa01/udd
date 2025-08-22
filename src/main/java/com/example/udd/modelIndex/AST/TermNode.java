package com.example.udd.modelIndex.AST;

public class TermNode extends Node{
    public String field;
    public String value;

    public TermNode(String field, String value) {
        this.field = field;
        this.value = value;
    }
}
