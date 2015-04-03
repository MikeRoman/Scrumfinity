package seng302.group5;

import java.io.IOException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import seng302.group5.controller.ListMainPaneController;
import seng302.group5.controller.MenuBarController;
import seng302.group5.controller.PersonDialogController;
import seng302.group5.controller.ProjectDialogController;
import seng302.group5.controller.TeamDialogController;
import seng302.group5.controller.enums.CreateOrEdit;
import seng302.group5.controller.SkillsDialogController;
import seng302.group5.model.AgileItem;
import seng302.group5.model.Project;
import seng302.group5.model.Skill;
import seng302.group5.model.Person;
import seng302.group5.model.Team;
import seng302.group5.model.undoredo.Action;
import seng302.group5.model.undoredo.UndoRedoHandler;
import seng302.group5.model.undoredo.UndoRedoObject;

/**
 * Main class to run the application
 */
public class Main extends Application {

  private Stage primaryStage;
  private BorderPane rootLayout;

  private ListMainPaneController LMPC;
  private MenuBarController MBC;

  private ObservableList<Project> projects = FXCollections.observableArrayList();
  private ObservableList<Team> teams = FXCollections.observableArrayList();
  private ObservableList<Skill> skills = FXCollections.observableArrayList();
  private ObservableList<Person> people = FXCollections.observableArrayList();

  private UndoRedoHandler undoRedoHandler;

  private UndoRedoObject lastSavedObject;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setTitle("Scrumfinity");
    // Constructs the application
    initRootLayout();
    showMenuBar();
    showListMainPane();

    // Initialise the undo/redo handler
    undoRedoHandler = new UndoRedoHandler(this);

    lastSavedObject = null;
  }


  /**
   * Initializes the root layout.
   */
  public void initRootLayout() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.class.getResource("/Main.fxml"));
      rootLayout = (BorderPane) loader.load();

      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shows the menu bar inside root layout
   */
  public void showMenuBar() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MenuBarController.class.getResource("/MenuBar.fxml"));
      MenuBar menuBar = (MenuBar) loader.load();

      MenuBarController controller = loader.getController();
      controller.setMainApp(this);
      MBC = controller;

     rootLayout.setTop(menuBar);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shows the ListMainPane
   */
  public void showListMainPane() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(ListMainPaneController.class.getResource("/ListMainPaneController.fxml"));
      SplitPane splitPane = (SplitPane) loader.load();

      ListMainPaneController controller = loader.getController();
      controller.setMainApp(this);
      controller.checkListType();   // Load objects into list view
      LMPC = controller;

      rootLayout.setCenter(splitPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void showProjectDialog(CreateOrEdit createOrEdit) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.class.getResource("/ProjectDialog.fxml"));
      VBox projectDialogLayout = (VBox) loader.load();

      ProjectDialogController controller = loader.getController();
      Scene projectDialogScene = new Scene(projectDialogLayout);
      Stage projectDialogStage = new Stage();

      Project project = null;
      if (createOrEdit == CreateOrEdit.EDIT) {
        project = (Project) LMPC.getSelected();
        if (project == null) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText(null);
          alert.setContentText("No project selected");
          alert.showAndWait();
          return;
        }
      }

      controller.setupController(this, projectDialogStage, createOrEdit, project);

      projectDialogStage.initModality(Modality.APPLICATION_MODAL);
      projectDialogStage.initOwner(primaryStage);
      projectDialogStage.setScene(projectDialogScene);
      projectDialogStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void showTeamDialog(CreateOrEdit createOrEdit) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.class.getResource("/TeamDialog.fxml"));
      VBox teamDialogLayout = (VBox) loader.load();

      TeamDialogController controller = loader.getController();

      Scene teamDialogScene = new Scene(teamDialogLayout);
      Stage teamDialogStage = new Stage();

      Team team = null;
      if (createOrEdit == CreateOrEdit.EDIT) {
        team = (Team) LMPC.getSelected();
        if (team == null) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText(null);
          alert.setContentText("No team selected");
          alert.showAndWait();
          return;
        }
      }

      controller.setupController(this, teamDialogStage, createOrEdit, team);

      teamDialogStage.initModality(Modality.APPLICATION_MODAL);
      teamDialogStage.initOwner(primaryStage);
      teamDialogStage.setScene(teamDialogScene);
      teamDialogStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void showPersonDialog(CreateOrEdit createOrEdit) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.class.getResource("/PersonDialog.fxml"));
      VBox personDialogLayout = (VBox) loader.load();

      PersonDialogController controller = loader.getController();
      Scene personDialogScene = new Scene(personDialogLayout);
      Stage personDialogStage = new Stage();

      Person person = null;
      if (createOrEdit == CreateOrEdit.EDIT) {
        person = (Person) LMPC.getSelected();    // TODO: Fix
        if (person == null) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText(null);
          alert.setContentText("No person selected");
          alert.showAndWait();
          return;
        }
      }
      controller.setupController(this, personDialogStage, createOrEdit, person);

      personDialogStage.initModality(Modality.APPLICATION_MODAL);
      personDialogStage.initOwner(primaryStage);
      personDialogStage.setScene(personDialogScene);
      personDialogStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void showSkillCreationDialog(CreateOrEdit createOrEdit) {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.class.getResource("/SkillsDialog.fxml"));
      VBox SkillsDialogLayout = (VBox) loader.load();

      SkillsDialogController controller = loader.getController();
      Scene skillDialogScene = new Scene(SkillsDialogLayout);
      Stage skillDialogStage = new Stage();

      Skill skill = null;
      if (createOrEdit == CreateOrEdit.EDIT) {
        skill = (Skill) LMPC.getSelected();    // TODO: Fix
        if (skill == null){
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText(null);
          alert.setContentText("No skill selected");
          alert.showAndWait();
          return;
        }
      }
      controller.setupController(this, skillDialogStage, createOrEdit, skill);

      skillDialogStage.initModality(Modality.APPLICATION_MODAL);
      skillDialogStage.initOwner(primaryStage);
      skillDialogStage.setScene(skillDialogScene);
      skillDialogStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Undo last action
   */
  public void undo() {
    try {
      undoRedoHandler.undo();
      checkSaved();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Redo last action
   */
  public void redo() {
    try {
      undoRedoHandler.redo();
      checkSaved();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Add a new action to the undo/redo stack
   * @param undoRedoObject Action to store
   */
  public void newAction(UndoRedoObject undoRedoObject) {
    undoRedoHandler.newAction(undoRedoObject);
    checkSaved();
  }

  /**
   * Refresh the last saved object to be the newest action on the undo stack
   */
  public void refreshLastSaved() {
    lastSavedObject = undoRedoHandler.peekUndoStack();
    checkSaved();
  }

  /**
   * Check if the newest action was the saved action and adjust the window title
   */
  private void checkSaved() {
    UndoRedoObject topObject = undoRedoHandler.peekUndoStack();
    boolean neverSaved = lastSavedObject == null && topObject == null;

    // Adjust the window title
    if (neverSaved || topObject == lastSavedObject) {
      primaryStage.setTitle("Scrumfinity");
    } else {
      primaryStage.setTitle("Scrumfinity *");
    }
  }

  public Stage getPrimaryStage(){
    return primaryStage;
  }

  public ListMainPaneController getLMPC() {
    return LMPC;
  }

  public MenuBarController getMBC() {
    return MBC;
  }

  public ObservableList<Project> getProjects() {
    return projects;
  }

  public ObservableList<Team> getTeams() {
    return teams;
  }

  public ObservableList<Person> getPeople() {
    return people;
  }

  public ObservableList<Skill> getSkills() {
    return skills;
  }

  public void addProject(Project project) {
    projects.add(project);
  }

  public void addPerson(Person person) {
    people.add(person);
  }

  public void addTeam(Team team) {
    teams.add(team);
  }

  public void refreshList() {
    LMPC.refreshList();
  }

  public void addSkill(Skill skill) {
    skills.add(skill);
  }

  /**
   * Delete a project from the list of projects
   * @param inputProject Project to delete - must be same object reference
   */
  public void deleteProject(Project inputProject) {
    for (Project project : projects) {
      if (project == inputProject) {
        projects.remove(project);
        break;
      }
    }
  }

  /**
   * Delete a person from the list of people
   * @param inputPerson Person to delete - must be the same object reference
   */
  public void deletePerson(Person inputPerson) {
    for (Person person : people) {
      if (person == inputPerson) {
        people.remove(person);
        break;
      }
    }
  }

  /**
   * Delete a skill from the list of skills
   * @param inputSkill Skill to delete - must be the same object reference
   */
  public void deleteSkill(Skill inputSkill) {
    for (Skill skill : skills) {
      if (skill == inputSkill) {
        skills.remove(skill);
        break;
      }
    }
  }

  /**
   * Delete a team from the list of teams
   * @param inputTeam Team to delete - must be the same object reference
   */
  public void deleteTeam(Team inputTeam) {
    for(Team team : teams) {
      if (team == inputTeam) {
        teams.remove(team);
        break;
      }
    }
  }

    /**
   * Generate an UndoRedoObject to place in the stack
     *
   * @param action The action to store in the object
   * @param agileItem The item to store in the object
   * @return the UndoRedoObject to store
   */
  private UndoRedoObject generateDelUndoRedoObject(Action action, AgileItem agileItem) {
    UndoRedoObject undoRedoObject = new UndoRedoObject();

    undoRedoObject.setAction(action);

    // Store a copy of object in stack to avoid reference problems
    AgileItem itemToStore;
    switch (action) {
      case PROJECT_DELETE:
        itemToStore = new Project((Project) agileItem);
        break;
      case PERSON_DELETE:
        itemToStore = new Person((Person) agileItem);
        break;
      case SKILL_DELETE:
        itemToStore = new Skill((Skill) agileItem);
        break;
      default:
        itemToStore = null;
        System.err.println("Unhandled case for generating undo/redo delete object");
        break;
    }

    undoRedoObject.addDatum(itemToStore);

    return undoRedoObject;
  }

  /**
   * Generic delete function which deletes an item from the appropriate list and then adds
   * the action to the undo/redo stack
   *
   * @param agileItem Item to delete
   */
  public void delete(AgileItem agileItem) {
    String listType = LMPC.getCurrentListType();
    UndoRedoObject undoRedoObject;
    switch (listType) {
      case "Project":
        deleteProject((Project) agileItem);
        undoRedoObject = generateDelUndoRedoObject(Action.PROJECT_DELETE, agileItem);
        newAction(undoRedoObject);
        break;
      case "People":
        deletePerson((Person) agileItem);
        undoRedoObject = generateDelUndoRedoObject(Action.PERSON_DELETE, agileItem);
        newAction(undoRedoObject);
        break;
      case "Skills":
        deleteSkill((Skill) agileItem);
        undoRedoObject = generateDelUndoRedoObject(Action.SKILL_DELETE, agileItem);
        newAction(undoRedoObject);
        break;
      case "Team":
        deleteTeam((Team) agileItem);
        // TODO: undo redo - cascading delete prompt
        break;
      default:
        System.err.println("Unhandled case for deleting agile item");
        break;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
