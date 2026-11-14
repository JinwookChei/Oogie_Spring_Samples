package com.study.matzip;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
public class ConfigTest {
	
	@Autowired
	DataSource ds;
	
	@Test
	public void checkDataSource() {
		Assertions.assertNotNull(ds);
		log.debug("DataSource: {}", ds.getClass().getName());
	}
}
