package datasahi.flow.eval;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionEvaluatorTest {

    @Test
    public void testExpression() {

        HashMap<String, String> map = new HashMap<>();
        map.put("status", "DONE");
        map.put("user", "JohnDoe");

        // Expression to check if status is "DONE"
        String expression = "(status == 'DONE' || status == 'CANCEL') && user == 'JohnDoe'";

        // Evaluate the expression
        Object result = new ExpressionEvaluator().evaluateExpression(expression, map);
        assertEquals(true, result);
    }
}
