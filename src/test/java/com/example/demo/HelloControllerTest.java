package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    @Test
    void helloReturnsExpectedMessage() {
        assertEquals("Hello from Spring Boot CI/CD!", new HelloController().hello());
    }
}
