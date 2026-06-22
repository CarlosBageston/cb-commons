package br.com.cb.validation;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@RequestScoped
public class SimpleValidator {

    @Inject
    Validator validator;

    public <T> void validate(T object) {

        var violations = validator.validate(object);
        System.out.println("Violations " + violations);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}