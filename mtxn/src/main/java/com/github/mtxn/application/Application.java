package com.github.mtxn.application;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@Component
public class Application implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T resolve(String name) {
        return (T) applicationContext.getBean(name);
    }

    public static <T> T resolve(Class<T> serviceType) {
        return applicationContext.getBean(serviceType);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        Application.applicationContext = applicationContext;
    }

}