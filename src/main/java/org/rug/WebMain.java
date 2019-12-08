package org.rug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WebMain {

    // More info: https://spring.io/guides/gs/rest-service/
    // Live changes: https://stackoverflow.com/questions/33349456/how-to-make-auto-reload-with-spring-boot-on-idea-intellij
    public static void main(String[] args) {
        SpringApplication.run(WebMain.class, args);
    }

}
