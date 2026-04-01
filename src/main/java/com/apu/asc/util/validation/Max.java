package com.apu.asc.util.validation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Max { long value(); String message() default "must be less than or equal to max"; }
