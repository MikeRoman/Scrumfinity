package seng302.group5.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.group5.controller.enums.Status;

/**
 * Story model. So that managers can keep track of the things people need to do, a way to record
 * work items as user stories. For now, it just have the most basic detail but we’ll add more
 * in subsequent stories.
 * Contains the information about a single story, which includes the label, name,
 * a description, who the creator is, dependencies, acceptance criteria, and the state of the story.
 *
 * Created by Zander on 5/05/2015.
 */
public class Story implements AgileItem, Comparable<Story> {

  private String label;
  private String storyName;
  private String description;
  private Person creator;
  private List<Story> dependencies;
  private boolean isReady = false;
  private ObservableList<String> acceptanceCriteria;
  private Status status;

  /**
   * Empty constructor used for save/load.
   */
  public Story() {
    this.label = "";
    this.storyName = "";
    this.description = "";
    this.creator = null;
    this.acceptanceCriteria = FXCollections.observableArrayList();
    this.isReady = false;
    this.dependencies = new ArrayList<>();
    this.status = Status.NOT_STARTED;
  }

  /**
   * Constructor for label, storyName, description, creator
   * @param label Not-null ID/label.
   * @param storyName Long name for story.
   * @param description Description of story.
   * @param creator Person assigned to creation of this story..
   */
  public Story(String label, String storyName, String description, Person creator) {
    this.label = label;
    this.storyName = storyName;
    this.description = description;
    this.creator = creator;
    this.acceptanceCriteria = FXCollections.observableArrayList();
    this.isReady = false;
    this.dependencies = new ArrayList<>();
    this.status = Status.NOT_STARTED;
  }

  /**
   * Constructor for label, storyName, description, creator, acceptanceCriteria.
   * @param label Not-null ID/label.
   * @param storyName Long name for story.
   * @param description Description of story.
   * @param creator Person assigned to creation of this story..
   * @param acceptanceCriteria The criteria for the story to be considered done.
   */
  public Story(String label, String storyName, String description, Person creator,
                 ObservableList<String> acceptanceCriteria) {
    this.label = label;
    this.storyName = storyName;
    this.description = description;
    this.creator = creator;
    if (acceptanceCriteria == null) {
      this.acceptanceCriteria = FXCollections.observableArrayList();
    } else {
      this.acceptanceCriteria = acceptanceCriteria;
    }
    this.isReady = false;
    this.dependencies = new ArrayList<>();
    this.status = Status.NOT_STARTED;
  }

  /**
   * Constructor for all fields but not STATUS.
   * @param label               Unique none-null id of the story.
   * @param storyName           A full name for this story.
   * @param description         A full description for this story.
   * @param creator             The owner of the story, who physically created it in the first place.
   * @param acceptanceCriteria  List of acceptance criteria of the story.
   * @param dependencies        List of stories that this story depends on.
   */
  public Story(String label, String storyName, String description, Person creator,
               ObservableList<String> acceptanceCriteria, List<Story> dependencies) {
    this.label = label;
    this.storyName = storyName;
    this.description = description;
    this.creator = creator;
    if (acceptanceCriteria == null) {
      this.acceptanceCriteria = FXCollections.observableArrayList();
    } else {
      this.acceptanceCriteria = acceptanceCriteria;
    }
    if (dependencies == null) {
      this.dependencies = new ArrayList<>();
    } else {
      this.dependencies = dependencies;
    }
    this.status = Status.NOT_STARTED;
  }

  /**
   * Constructor for all fields. minus dependancies.
   * @param label               Unique none-null id of the story.
   * @param storyName           A full name for this story.
   * @param description         A full description for this story.
   * @param creator             The owner of the story, who physically created it in the first place.
   * @param acceptanceCriteria  List of acceptance criteria of the story.
   * @param status              The status that the story is in. (Done, Not started, etc)
   */
  public Story(String label, String storyName, String description, Person creator,
               ObservableList<String> acceptanceCriteria, Status status) {
    this.label = label;
    this.storyName = storyName;
    this.description = description;
    this.creator = creator;
    if (acceptanceCriteria == null) {
      this.acceptanceCriteria = FXCollections.observableArrayList();
    } else {
      this.acceptanceCriteria = acceptanceCriteria;
    }
    this.dependencies = new ArrayList<>();
    this.status = status;
  }


  /**
   * Constructor for all fields.
   * @param label               Unique none-null id of the story.
   * @param storyName           A full name for this story.
   * @param description         A full description for this story.
   * @param creator             The owner of the story, who physically created it in the first place.
   * @param acceptanceCriteria  List of acceptance criteria of the story.
   * @param dependencies        List of stories that this story depends on.
   * @param status              The status that the story is in. (Done, Not started, etc)
   */
  public Story(String label, String storyName, String description, Person creator,
               ObservableList<String> acceptanceCriteria, List<Story> dependencies, Status status) {
    this.label = label;
    this.storyName = storyName;
    this.description = description;
    this.creator = creator;
    if (acceptanceCriteria == null) {
      this.acceptanceCriteria = FXCollections.observableArrayList();
    } else {
      this.acceptanceCriteria = acceptanceCriteria;
    }
    if (dependencies == null) {
      this.dependencies = new ArrayList<>();
    } else {
      this.dependencies = dependencies;
    }
    this.status = status;
  }

  /**
   * Constructor to create a clone of existing story.
   *
   * @param clone Story to clone.
   */
  public Story(Story clone) {
    this.label = clone.getLabel();
    this.storyName = clone.getStoryName();
    this.description = clone.getDescription();
    this.creator = clone.getCreator();
    this.acceptanceCriteria = FXCollections.observableArrayList();
    if (clone.getAcceptanceCriteria() != null) {
      this.acceptanceCriteria.addAll(clone.getAcceptanceCriteria());
    }
    this.dependencies = new ArrayList<>();
    if (clone.getDependencies() != null) {
      this.dependencies.addAll(clone.getDependencies());
    }
    this.isReady = clone.getStoryState();
    this.status = clone.getStatus();
  }

  /**
   * Gets label of story.
   * @return Label of story.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Sets label of story.
   * @param label New label as String type.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets long name of story.
   * @return Long name of story.
   */
  public String getStoryName() {
    return this.storyName;
  }

  /**
   * Sets long name of story.
   * @param storyName New long name as String type.
   */
  public void setStoryName(String storyName) {
    this.storyName = storyName;
  }

  /**
   * Gets description of story.
   * @return Description of story.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets description of story.
   * @param description Description of story as String type.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets Person assigned as creator of story.
   * @return Person who created story.
   */
  public Person getCreator() {
    return this.creator;
  }

  /**
   * Sets Person assigned as creator of story.
   * @param creator Person as Person type.
   */
  public void setCreator(Person creator) {
    this.creator = creator;
  }

  /**
   * Returns whether or not the story is marked as ready.
   * @return Story is ready or not as a boolean.
   */
  public boolean getStoryState() {
    return this.isReady;
  }

  /**
   * Sets the readiness state of the story.
   * @param isReady Whether story is ready or not as a boolean.
   */
  public void setStoryState(boolean isReady) {
    this.isReady = isReady;
  }

  /**
   * gets the acceptance criteria
   * @return List which contains the ACS
   */
  public ObservableList<String> getAcceptanceCriteria() {
    return acceptanceCriteria;
  }

  /**
   * sets the Acceptance criteria
   * @param acceptanceCriteria List which is the ACS
   */
  public void setAcceptanceCriteria(ObservableList<String> acceptanceCriteria) {
    this.acceptanceCriteria = acceptanceCriteria;
  }

  /**
   * Gets the dependencies of this story.
   *
   * @return A list of stories that this story depends on.
   */
  public List<Story> getDependencies() {
    return Collections.unmodifiableList(this.dependencies);
  }

  /**
   * Add a story which this story depends on to the model.
   *
   * @param story A story which this story depends on.
   */
  public void addDependency(Story story) {
    this.dependencies.add(story);
  }

  /**
   * Add a collection of stories which depend on this story to the model.
   *
   * @param stories A collection of stories which depends on this story to be added.
   */
  public void addAllDependencies(Collection<Story> stories) {
    this.dependencies.addAll(stories);
  }

  /**
   * Remove a story which depends on this story from the model.
   *
   * @param story A story which depends on this story to be removed.
   */
  public void removeDependency(Story story) {
    this.dependencies.remove(story);
  }

  /**
   * Remove all stories which depend on this story from the model.
   */
  public void removeAllDependencies() {
    this.dependencies.clear();
  }


  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Copies the story input fields into current object.
   * @param agileItem Story that's fields are to be copied.
   */
  @Override
  public void copyValues(AgileItem agileItem) {
    if (agileItem instanceof Story) {
      Story clone = (Story) agileItem;
      this.label = clone.getLabel();
      this.storyName = clone.getStoryName();
      this.description = clone.getDescription();
      this.creator = clone.getCreator();
      this.isReady = clone.getStoryState();
      this.acceptanceCriteria.clear();
      this.acceptanceCriteria.addAll(clone.getAcceptanceCriteria());
      this.dependencies.clear();
      this.dependencies.addAll(clone.getDependencies());
      this.status = clone.getStatus();
    }
  }

  /**
   * Overrides the toString method with story label.
   * @return Label of story.
   */
  @Override
  public String toString() {
    return this.label;
  }

  /**
   * Override equals method
   * @param o Object being compared to
   * @return whether objects are equal or not
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Story story = (Story) o;

    if (isReady != story.isReady) {
      return false;
    }
    if (!label.equals(story.label)) {
      return false;
    }
    if (!storyName.equals(story.storyName)) {
      return false;
    }
    if (!description.equals(story.description)) {
      return false;
    }
    if (!creator.equals(story.creator)) {
      return false;
    }
    if (!dependencies.equals(story.dependencies)) {
      return false;
    }
    if(!status.equals(story.status)) {
      return false;
    }
    return acceptanceCriteria.equals(story.acceptanceCriteria);

  }

  /**
   * Hashcode override, generated by intelliJ
   */
  @Override
  public int hashCode() {
    int result = label.hashCode();
    result = 31 * result + storyName.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + dependencies.hashCode();
    result = 31 * result + (isReady ? 1 : 0);
    result = 31 * result + acceptanceCriteria.hashCode();
    result = 31 * result + status.name().hashCode();
    return result;
  }

  /**
   * Compare this story label to story o's label
   * @param o story you wish to compare to
   * @return whether it is greater or lesser.
   */
  @Override
  public int compareTo(Story o) {
    return this.label.toLowerCase().compareTo(o.label.toLowerCase());
  }
}
