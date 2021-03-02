package uk.gov.hmcts.ccf;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XUI {

    String label() default "";

    String hint() default "";

    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    String showCondition() default "";

    XUIType type() default XUIType.Default;

    String typeParameter() default "";

    boolean showSummaryContent() default false;

    boolean ignore() default false;
}
