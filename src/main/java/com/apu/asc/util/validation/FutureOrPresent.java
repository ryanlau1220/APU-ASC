package com.apu.asc.util.validation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FutureOrPresent { String message() default "must be a date in the present or in the future"; }
