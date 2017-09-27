package org.moskito.testing.junit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.moskito.testing.junit.runner.MoskitoJunitRunner;

/**
 * Test example running with {@link MoskitoJunitRunner}
 *
 * @author esmakula
 */
//TODO uncomment annotations when analyze-connector.json configured
@RunWith(MoskitoJunitRunner.class)
public class TestUnit {

	@Test
	public void test(){
		Monitored monitored = new Monitored();
		monitored.method();
	}

//	@Test
	public void test2(){
		Monitored monitored = new Monitored();
		monitored.method();
		monitored.method();
		monitored.method();
	}



}
