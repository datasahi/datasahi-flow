package datasahi.flow.eval;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldExtractor {

    private final JexlEngine jexl;

    public FieldExtractor() {
        this.jexl = new JexlBuilder()
                .strict(true)
                .silent(false)
                .create();
    }

    public List<String> extractFields(String expression) {
        try {
            JexlScript script = jexl.createScript(expression);
            Set<List<String>> variables = script.getVariables();
            Set<String> fields = variables.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            return new ArrayList<>(fields);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expression: " + expression, e);
        }
    }
}