package br.com.cb.validation;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;

@AutoValidate
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AutoValidateInterceptor {

    @Inject
    SimpleValidator simpleValidator;

    @AroundInvoke
    public Object validate(InvocationContext ctx) throws Exception {
        System.out.println("ctx " + Arrays.toString(ctx.getParameters()));
        for (Object param : ctx.getParameters()) {
            if (param != null) {
                simpleValidator.validate(param);
            }
        }
        return ctx.proceed();
    }
}
