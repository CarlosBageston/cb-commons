package br.com.cb.validation;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

import java.lang.reflect.Method;

@AutoValidate
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AutoValidateInterceptor {

    @Inject
    SimpleValidator simpleValidator;

    @AroundInvoke
    public Object validate(InvocationContext ctx) throws Exception {
        if(shouldValidate(ctx.getMethod())) {
            for (Object param : ctx.getParameters()) {
                if (param != null) {
                    simpleValidator.validate(param);
                }
            }
        }
        return ctx.proceed();
    }
    private boolean shouldValidate(Method method) {
        return method.isAnnotationPresent(POST.class)
                || method.isAnnotationPresent(PUT.class)
                || method.isAnnotationPresent(PATCH.class);
    }
}
