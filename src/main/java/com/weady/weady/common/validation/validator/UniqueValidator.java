package com.weady.weady.common.validation.validator;

import com.weady.weady.common.validation.annotation.Unique;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    private final EntityManager em;
    private Class<?> entityClass;
    private String uniqueField;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.uniqueField = constraintAnnotation.field();
    }


    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String jpql = String.format("SELECT COUNT(e) > 0 FROM %s e WHERE e.%s = :value",
                entityClass.getSimpleName(), uniqueField);

        TypedQuery<Boolean> query = em.createQuery(jpql, Boolean.class);
        query.setParameter("value", value);

        boolean exists = query.getSingleResult();
        return !exists;
    }
}