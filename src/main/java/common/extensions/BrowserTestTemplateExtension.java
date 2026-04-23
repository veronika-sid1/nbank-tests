package common.extensions;

import com.codeborne.selenide.Configuration;
import common.annotations.Browsers;
import org.junit.jupiter.api.extension.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Используется для запуска на нескольких браузерах. Для использования вставить аннотации:
 @TestTemplate
 @ExtendWith(BrowserTestTemplateExtension.class)
 в класс Browsers. Лучше использовать с закрытием браузера в базовом классе:
 @AfterEach
 void tearDown() {
 closeWebDriver();}
 */

public class BrowserTestTemplateExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
                .map(method -> method.isAnnotationPresent(Browsers.class))
                .orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        Browsers browsers = extensionContext.getRequiredTestMethod().getAnnotation(Browsers.class);
        return Arrays.stream(browsers.value())
                .map(BrowserInvocationContext::new);
    }

    public static class BrowserInvocationContext implements TestTemplateInvocationContext {
        private final String browser;

        BrowserInvocationContext(String browser) {
            this.browser = browser;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return "browser = " + browser;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(new BeforeEachCallback() {
                @Override
                public void beforeEach(ExtensionContext context) {
                    Configuration.browser = browser;
                }
            });
        }
    }
}
