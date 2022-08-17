package io.exercise.api.models.validators;

import io.exercise.api.actions.ValidateObjectAction;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With(ValidateObjectAction.class)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidObject {
    Class<?> type() default Object.class;
}