package com.sentinovo.carbuildervin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarBuilderVinApplication {

    private static final Logger log = LoggerFactory.getLogger(CarBuilderVinApplication.class);

    public static void main(String[] args) {
        log.info("##### STARTING ######");
        SpringApplication.run(CarBuilderVinApplication.class, args);
        log.info("##### APP STARTED ######");
    }

}
