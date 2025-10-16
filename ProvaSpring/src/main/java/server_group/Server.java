package server_group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity

public class  Server {

    public static void main(String[] args) {

        SpringApplication.run(Server.class, args);
    }

}


