package lib.annotations.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Documented
@Repeatable(CGNotes.class)
public @interface CGNote {

    CGCategory value();

    String description() default "";
}

