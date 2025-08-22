package com.example.udd.modelIndex.AST;

import java.util.ArrayList;
import java.util.List;

public class OperatorNode extends Node{
    public String operator; // AND, OR, NOT
    public List<Node> children = new ArrayList<>();
    public OperatorNode(String operator) {
        this.operator = operator;
    }
}
