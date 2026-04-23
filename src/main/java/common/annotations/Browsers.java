package common.annotations;

import common.extensions.BrowserTestTemplateExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//@TestTemplate
//@ExtendWith(BrowserTestTemplateExtension.class)
public @interface Browsers {
    String[] value();
}
