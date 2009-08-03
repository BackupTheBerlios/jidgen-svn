package de.rrze.idmone.utils.jidgen.tests;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.rrze.idmone.utils.jidgen.IdGenerator;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	/*
    	 * IdGenerator initialized like CLI version
    	 */
    	System.out.println("1st run:");
    	// get an instance of the id generator
    	// and pass the CLI options right along to the constructor 
    	IdGenerator idGen_1 = new IdGenerator("-Ta Test -T a:[N++]");
    	// get a list of generated ids
    	List<String> ids_1 = idGen_1.generateIDs(5);
    	// to have something to look at - output them 
    	idGen_1.print(ids_1);
    	
    	/*
    	 * IdGenerator initialized via setOption() method
    	 */
    	System.out.println("\n2nd run:");
    	// get an instance of the id generator
    	IdGenerator idGen_2 = new IdGenerator();
    	// set some options
    	idGen_2.setOption("Ta", "Test");
    	idGen_2.setOption("T", "a:[N++]");    	
    	// get a list of generated ids
    	List<String> ids_2 = idGen_2.generateIDs(5);
    	// to have something to look at - output them
    	idGen_1.print(ids_2);
    	
    	
    	assertTrue( !ids_1.isEmpty() 
    				&& !ids_2.isEmpty() );
    }
}
