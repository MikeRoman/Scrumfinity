package seng302.group5.model;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static org.junit.Assert.*;

/**
 * Created by Zander on 6/05/2015.
 */
public class StoryTest {

  private String storyLabel;
  private String storyLongName;
  private String storyDescription;
  private Person storyCreator;
  private ObservableList<String> storyAC;

  private Story story;

  @Before
  public void setUp() throws Exception {
    this.storyLabel = "Story";
    this.storyLongName = "An awesome story";
    this.storyDescription = "Once upon a time...";
    this.storyCreator = new Person("John", "John", "Doe", null);
    this.storyAC = FXCollections.observableArrayList();
    storyAC.add("ac1");
    storyAC.add("ac2");

    this.story = new Story(storyLabel, storyLongName, storyDescription, storyCreator, storyAC);
  }

  @Test
  public void testACsExist() throws Exception {
    String firstAC = story.getAcceptanceCriteria().get(0);
    String secondAC = story.getAcceptanceCriteria().get(1);
    assertEquals("ac1", firstAC);
    assertEquals("ac2", secondAC);
  }

  @Test
  public void testToString() throws Exception {
    String result = story.toString();
    assertEquals(storyLabel, result);
  }
}
