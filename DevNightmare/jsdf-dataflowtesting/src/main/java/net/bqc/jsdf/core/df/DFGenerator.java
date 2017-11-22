package net.bqc.jsdf.core.df;

import net.bqc.jsdf.core.helper.JGraphUtils;
import net.bqc.jsdf.core.model.DecisionVertex;
import net.bqc.jsdf.core.model.Edge;
import net.bqc.jsdf.core.model.Variable;
import net.bqc.jsdf.core.model.Vertex;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.mozilla.javascript.ast.*;


import java.util.*;

public class DFGenerator {

    private DirectedGraph<Vertex, Edge> cfg;
    private List<GraphPath> graphPaths;
    private Map<String, Variable> variableMap;

    public DFGenerator(DirectedGraph<Vertex, Edge> cfg, List<GraphPath> graphPaths) {
        this.cfg = cfg;
        this.graphPaths = graphPaths;
        variableMap = new HashMap<>();
        generateVertexType();

        System.out.println(variableMap.keySet());

//        JGraphUtils.printPaths(graphPaths);
    }

    private void generateVertexType() {
        Set<Vertex> vertexSet = cfg.vertexSet();
        vertexSet.forEach(vertex -> {
            AstNode vertexAstNode = vertex.getAstNode();
            if (vertexAstNode != null) {
                // p-use
                if (vertex instanceof DecisionVertex) {
                    AstNode condition = null;
                    if (vertexAstNode instanceof IfStatement) {
                        IfStatement ifStatement = (IfStatement) vertexAstNode;
                        condition = ifStatement.getCondition();
                        addPUses(getVariableNamesInside(condition), vertex);
                    }
                    else if (vertexAstNode instanceof WhileLoop) {
                        WhileLoop whileLoop = (WhileLoop) vertexAstNode;
                        condition = whileLoop.getCondition();
                    }

                    if (condition != null) {
                        addPUses(getVariableNamesInside(condition), vertex);
                    }
                }
            }
        });
    }

    private void addPUses(Set<String> variableNames, Vertex vertex) {
        variableNames.forEach(name -> vertex.addC_uses(getVariableByName(name)));
    }

    private Variable getVariableByName(String name) {
        Variable variable = variableMap.get(name);
        if (variable == null) variableMap.put(name, new Variable(name));
        return variable;
    }

    private Set<String> getVariableNamesInside(AstNode astNode) {
        NameVisitor nameVisitor = new NameVisitor();
        astNode.visit(nameVisitor);
        return nameVisitor.getVariableNames();
    }

    private class NameVisitor implements NodeVisitor {
        private Set<String> variableNames = new HashSet<>();

        @Override
        public boolean visit(AstNode astNode) {
            if (astNode instanceof Name) {
                variableNames.add(astNode.toSource().trim());
            }
            return true;
        }

        public Set<String> getVariableNames() {
            return variableNames;
        }
    }
}
