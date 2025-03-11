package datasahi.flow.eval;

import org.apache.commons.jexl3.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

    private final JexlEngine jexl;
    private final FieldExtractor fieldExtractor = new FieldExtractor();
    private final Map<String, JexlExpression> expressionMap = new HashMap<>();
    private final Map<String, List<String>> fieldsMap = new HashMap<>();

    public ExpressionEvaluator() {
        this.jexl = new JexlBuilder().strict(true).silent(false).create();
    }

    public Object evaluateExpression(String expression, Map<String, String> dataMap) {

        JexlExpression jexlExp = expressionMap.get(expression);
        if (jexlExp == null) {
            jexlExp = jexl.createExpression(expression);
            expressionMap.put(expression, jexlExp);
            List<String> fields = fieldExtractor.extractFields(expression);
            fieldsMap.put(expression, fields);
        }

        JexlContext context = new MapContext();
        List<String> fields = fieldsMap.get(expression);
        for (String field : fields) {
            String value = dataMap.get(field);
            context.set(field, value);
        }
        return jexlExp.evaluate(context);
    }
}
