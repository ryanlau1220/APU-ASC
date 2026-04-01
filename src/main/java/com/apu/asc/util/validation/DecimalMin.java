package com.apu.asc.util.validation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecimalMin { String value(); String message() default "must be greater than or equal to minimum"; }
