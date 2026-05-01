package common.extensions;

import api.configs.Config;
import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.Optional;

public class APIVersionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        APIVersion annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(APIVersion.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений по версии API");
        }

        String currentVersion = Optional.ofNullable(System.getProperty("api.version"))
                .orElse(Config.getProperty("api.version"));

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
