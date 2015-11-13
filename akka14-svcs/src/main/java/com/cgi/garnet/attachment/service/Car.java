package com.cgi.garnet.attachment.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 10/10/2015.
 */
@Component
@Scope("prototype")
public class Car {
    @Autowired
    private Engine engine;
    @Autowired
    private Transmission transmission;

    public void startCar(String eventType) {
        transmission.setGear(1);
        engine.engineOn();
        System.out.println("Car started");
        if(StringUtils.equals(eventType,"ACQUIRE")){
            try {
                Thread.sleep(0);
                System.out.println("Car: ACQUIRE done waiting @@@@@@@@@@@@@@@@@@@@@@@@@@@");

                throw new ServiceUnavailable("Service is not available");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(StringUtils.equals(eventType,"CLOSE")){
            try {
                Thread.sleep(0);
                System.out.println("Car: CLOSE done waiting @@@@@@@@@@@@@@@@@@@@@@@@@@@");

                throw new DataStoreException("Car: Data Store is not available");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}