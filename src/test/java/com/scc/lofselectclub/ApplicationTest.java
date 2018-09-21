package com.scc.lofselectclub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {
	
	/*
	* Référence documentation: http://www.baeldung.com/spring-boot-testing
	*/
	
    @Test
    public void contextLoads() {
    }
}