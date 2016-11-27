package com.networknt.service;

import com.networknt.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stevehu on 2016-11-26.
 */
public class SingletonServiceFactory {
    static String CONFIG_NAME = "service";
    static Logger logger = LoggerFactory.getLogger(SingletonServiceFactory.class);

    private static Map<Class, Object> serviceMap = new HashMap<>();

    static {
        ServiceConfig serviceConfig =
                (ServiceConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, ServiceConfig.class);
        List<String> singletons = serviceConfig.getSingletons();
        try {
            if(singletons != null && singletons.size() > 0) {
                for(String singleton: singletons) {
                    int p = singleton.indexOf(':');
                    String interfaceName = singleton.substring(0, p);
                    Class interfaceClass = Class.forName(interfaceName);

                    String implNames = singleton.substring(p + 1);
                    if(implNames.contains(",")) {
                        String[] impls = implNames.split(",");
                        Object array = Array.newInstance(interfaceClass, impls.length);
                        for(int i = 0; i < impls.length; i++) {
                            Class implClass = Class.forName(impls[i]);
                            Array.set(array, i, implClass.newInstance());
                        }
                        serviceMap.put(interfaceClass, array);
                    } else {
                        Class implClass = Class.forName(implNames);
                        serviceMap.put(interfaceClass, implClass.newInstance());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception:", e);
        }
    }

    public static Object getBean(Class interfaceClass) {
       return serviceMap.get(interfaceClass);
    }
}