package seng302.group5.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Shingy on 3/04/2015.
 */
public class ProjectTest {

  private String projectLabel;
  private String projectName;
  private String projectDescription;
  private Project project;

  @Before
  public void setUp() throws Exception {
    projectLabel = "abcedef";
    projectName = "New Project";
    projectDescription = "This is a description. This field can be much longer than the others.";
    project = new Project(projectLabel, projectName, projectDescription);
  }

  @Test
  public void testToString() {
    String result = project.toString();
    assertEquals(projectLabel, result);
  }
}
