package common.extensions;

import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class APIVersionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        APIVersion annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(APIVersion.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений по версии API");
        }

        String currentVersion = System.getProperty("api.version", "with_database");
        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(apiVersion -> apiVersion.equals(currentVersion));

        if (matches) {
            return ConditionEvaluationResult.enabled("Текущая версия удовлетворяет условию: " + currentVersion);
        } {
            return ConditionEvaluationResult.disabled("Тест пропущен, так как текущая версия API " + currentVersion +
                    " не находится в списке допустимых для теста: " + Arrays.toString(annotation.value()));
        }
    }
}
