package datasahi.flow.commons.db;

import io.micronaut.core.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class SqlParameterExtractor {

    private static final Pattern COLON_PARAMETER_PATTERN = Pattern.compile(":(\\w+)");

    private Map<String, Set<String>> queryToParametersMap = new ConcurrentHashMap<>();

    public Set<String> extractParameters(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return Collections.emptySet();
        }
        return queryToParametersMap.computeIfAbsent(sql, this::extractParametersAsSet);
    }

    /**
     * Extracts unique parameter names and returns them as a Set. Anything starting with ":"
     */
    private Set<String> extractParametersAsSet(String sqlString) {
        if (StringUtils.isEmpty(sqlString)) {
            return Collections.emptySet();
        }

        Set<String> parameters = new HashSet<>();

        COLON_PARAMETER_PATTERN.matcher(sqlString)
                .results()
                .forEach(match -> parameters.add(match.group(1)));

        return Collections.unmodifiableSet(parameters);
    }
}