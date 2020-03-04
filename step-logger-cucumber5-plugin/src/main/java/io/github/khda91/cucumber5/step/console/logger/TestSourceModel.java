package io.github.khda91.cucumber5.step.console.logger;

/*
 *  Copyright 2020 the original author of authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Node;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import io.cucumber.plugin.event.TestSourceRead;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;

final class TestSourceModel {

    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap<>();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap<>();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap<>();

    static ScenarioDefinition getScenarioDefinition(AstNode astNode) {
        return astNode.node instanceof ScenarioDefinition ? (ScenarioDefinition) astNode.node : (ScenarioDefinition) astNode.parent.parent.node;
    }

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    ScenarioDefinition getScenarioDefinition(String path, int line) {
        return getScenarioDefinition(getAstNode(path, line));
    }

    AstNode getAstNode(String path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get(line);
        }

        return null;
    }

    private void parseGherkinSource(String path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(pathToReadEventMap.get(path).getSource(), matcher);
            pathToAstMap.put(path, gherkinDocument);
            Map<Integer, AstNode> nodeMap = new HashMap<Integer, AstNode>();
            AstNode currentParent = new AstNode(gherkinDocument.getFeature(), null);
            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                processScenarioDefinition(nodeMap, child, currentParent);
            }
            pathToNodeMap.put(path, nodeMap);
        } catch (ParserException e) {
            // Ignore exceptions
        }
    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, ScenarioDefinition child, AstNode currentParent) {
        AstNode childNode = new AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getSteps()) {
            nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
        }
    }

    @AllArgsConstructor
    class AstNode {
        final Node node;
        final AstNode parent;
    }
}
