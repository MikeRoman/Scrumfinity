package seng302.group5.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Shingy on 3/04/2015.
 */
public class PersonTest {

  private String personID;
  private String firstName;
  private String lastName;

  private Person person;

  @Before
  public void setUp() throws Exception {
    personID = "ssc55";
    firstName = "Su-Shing";
    lastName = "Chen";
    person = new Person(personID, firstName, lastName);
  }

  @Test
  public void testAssignToTeam() throws Exception {
    // TODO
  }

  @Test
  public void testRemoveFromTeam() throws Exception {
    // TODO
  }

  @Test
  public void testIsInTeam() throws Exception {
    // TODO
  }

  @Test
  public void testToString() throws Exception {
    String result = person.getPersonID();
    assertEquals(personID, result);
  }
}
