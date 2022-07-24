package com.lf.distrifs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistrifsApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DistrifsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.currentThread().join();
    }
}
