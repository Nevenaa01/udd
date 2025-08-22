package com.example.udd.utils;

import com.example.udd.modelIndex.AST.Node;
import com.example.udd.modelIndex.AST.OperatorNode;
import com.example.udd.modelIndex.AST.TermNode;

import java.util.List;
import java.util.Map;

public class Parser {
    private List<String> tokens;
    private int pos = 0;

    public Parser(List<String> tokens) {
        this.tokens = tokens;
    }

    public Node parse() {
        return parseOr(); // start from the lowest precedence
    }

    // Lowest precedence: OR
    private Node parseOr() {
        Node right = parseAnd();
        while (pos < tokens.size() && tokens.get(pos).equals("OR")) {
            String op = tokens.get(pos++);
            OperatorNode orNode = new OperatorNode(op);
            orNode.children.add(parseAnd());// right-hand side
            orNode.children.add(right);

            right = orNode;
        }
        return right;
    }

    // Medium precedence: AND
    private Node parseAnd() {
        Node right = parseUnary();
        while (pos < tokens.size() && tokens.get(pos).equals("AND")) {
            String op = tokens.get(pos++);
            OperatorNode andNode = new OperatorNode(op);
            andNode.children.add(parseUnary());
            andNode.children.add(right);

            right = andNode;
        }
        return right;
    }

    // Highest precedence: NOT, parentheses, or term
    private Node parseUnary() {
        if (pos >= tokens.size()) return null;

        String token = tokens.get(pos++);
        if (token.equals("NOT")) {
            OperatorNode notNode = new OperatorNode("NOT");
            notNode.children.add(parseUnary());
            return notNode;
        } else if (token.equals("(")) {
            Node expr = parseOr(); // inside parentheses, start from lowest precedence
            if (pos >= tokens.size() || !tokens.get(pos).equals(")")) {
                throw new RuntimeException("Mismatched parentheses");
            }
            pos++;
            return expr;
        } else { // term
            String[] parts = token.split(":", 2);
            String rawValue;
            if(parts.length == 2) {
                //structured
                rawValue = parts[1];
            } else{
                //not structured
                rawValue = parts[0];
            }

            boolean isPhrase = rawValue.startsWith("\"") && rawValue.endsWith("\"");
            String cleanValue = rawValue.replaceAll("\"", ""); // remove quotes
            return new TermNode(parts.length == 2 ? parts[0] : null, cleanValue, isPhrase);
        }
    }
}
