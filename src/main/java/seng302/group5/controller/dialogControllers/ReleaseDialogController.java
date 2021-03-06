package seng302.group5.controller.dialogControllers;

import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import seng302.group5.Main;
import seng302.group5.controller.enums.CreateOrEdit;
import seng302.group5.model.AgileController;
import seng302.group5.controller.enums.DialogMode;
import seng302.group5.model.Backlog;
import seng302.group5.model.Release;
import seng302.group5.model.Project;
import seng302.group5.model.Sprint;
import seng302.group5.model.undoredo.Action;
import seng302.group5.model.undoredo.UndoRedoObject;
import seng302.group5.model.util.Settings;

/**
 * Created by Craig Barnard on 7/04/2015.
 * Release Dialog Controller, manages the usage of Dialogs involved in the creating and editing of releases.
 */
public class ReleaseDialogController implements AgileController {

  @FXML private TextField releaseLabelField;
  @FXML private TextArea releaseDescriptionField;
  @FXML private DatePicker releaseDateField;
  @FXML private TextArea releaseNotesField;

  @FXML private Button btnConfirm;
  @FXML private Label projectContainer; // Dirty container but works
  @FXML private ComboBox<Project> projectComboBox;
  @FXML private HBox btnContainer;
  @FXML private Button btnNewProject;
  @FXML private Button btnEditProject;

  private Main mainApp;
  private Stage thisStage;
  private CreateOrEdit createOrEdit;
  private DialogMode dialogMode;
  private Release release = new Release();
  private Release lastRelease;

  private ObservableList<Project> availableProjects = FXCollections.observableArrayList();

  /**
   * Handles when the Create button is pushed by finalising all changes made or creating the new
   * Release object and adding it after checking for errors.
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnCreateRelease(ActionEvent event) {
    StringBuilder errors = new StringBuilder();
    int noErrors = 0;

    String releaseId = releaseLabelField.getText().trim();
    String releaseDescription = releaseDescriptionField.getText().trim();
    LocalDate releaseDate = releaseDateField.getValue();
    String releaseNotes = releaseNotesField.getText().trim();
    Project selectedProject = projectComboBox.getValue();

    Project releaseProject = new Project();

    try {
      releaseId = parseReleaseLabel(releaseLabelField.getText());
    } catch (Exception e) {
      noErrors++;
      errors.append(String.format("%s\n", e.getMessage()));
    }

    try {
      releaseDate = parseReleaseDate(releaseDate);
    } catch (Exception e) {
      noErrors++;
      errors.append(String.format("%s\n", e.getMessage()));
    }

    try {
      releaseProject = parseReleaseProject(selectedProject);
    } catch (Exception e) {
      noErrors++;
      errors.append(String.format("%s\n", e.getMessage()));
    }

//    try {
//      releaseNotes = parseReleaseNotes(releaseNotes);
//    } catch (Exception e) {
//      noErrors++;
//      errors.append(String.format("%s\n", e.getMessage()));
//    }

    // Display all errors if they exist
    if (noErrors > 0) {
      String title = String.format("%d invalid field", noErrors);
      if (noErrors > 1) {
        title += "s";  // plural
      }
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(null);
      noErrors += 1;
      alert.getDialogPane().setPrefHeight(60 + 30 * noErrors);
      alert.setContentText(errors.toString());
      alert.showAndWait();
    } else {
      if (createOrEdit == CreateOrEdit.CREATE) {
        release = new Release(releaseId, releaseDescription, releaseNotes, releaseDate, releaseProject);
        mainApp.addRelease(release);
        if (Settings.correctList(release)) {
          mainApp.refreshList(release);
        }
      } else if (createOrEdit == CreateOrEdit.EDIT) {
        release.setLabel(releaseId);
        release.setReleaseDescription(releaseDescription);
        release.setReleaseDate(releaseDate);
        release.setReleaseNotes(releaseNotes);
        release.setProjectRelease(releaseProject);

        releaseDateField.setValue(release.getReleaseDate());
        projectComboBox.setValue(release.getProjectRelease());
        if (Settings.correctList(release)) {
          mainApp.refreshList(release);
        }
      }
      UndoRedoObject undoRedoObject = generateUndoRedoObject();
      mainApp.newAction(undoRedoObject);
      mainApp.popControllerStack();
      thisStage.close();
    }
  }

  /**
   * Generate an UndoRedoObject to place in the stack
   *
   * @return the UndoRedoObject to store
   */
  private UndoRedoObject generateUndoRedoObject() {
    UndoRedoObject undoRedoObject = new UndoRedoObject();

    if (createOrEdit == CreateOrEdit.CREATE) {
      undoRedoObject.setAction(Action.RELEASE_CREATE);
    } else {
      undoRedoObject.setAction(Action.RELEASE_EDIT);
      undoRedoObject.addDatum(lastRelease);
    }

    // Store a copy of project to edit in stack to avoid reference problems
    undoRedoObject.setAgileItem(release);
    Release releaseToStore = new Release(release);
    undoRedoObject.addDatum(releaseToStore);

    return undoRedoObject;
  }

  /**
   * Sets up the dialog controller
   *
   * @param mainApp      The main application object
   * @param thisStage    The stage of the dialog
   * @param createOrEdit If dialog is for creating or editing a project
   * @param release      the Release object if editing other wise null
   */
  public void setupController(Main mainApp,
                              Stage thisStage,
                              CreateOrEdit createOrEdit,
                              Release release) {
    this.mainApp = mainApp;
    this.thisStage = thisStage;
    this.dialogMode = DialogMode.DEFAULT_MODE;
    releaseDateField.setValue(LocalDate.now());

    String os = System.getProperty("os.name");

    if (!os.startsWith("Windows")) {
      btnContainer.getChildren().remove(btnConfirm);
      btnContainer.getChildren().add(btnConfirm);
    }

    if (createOrEdit == CreateOrEdit.CREATE) {
      thisStage.setTitle("Create New Release");
      btnConfirm.setText("Create");
      btnEditProject.setDisable(true);

      initialiseLists();
    } else if (createOrEdit == CreateOrEdit.EDIT) {
      thisStage.setTitle("Edit Release");
      btnConfirm.setText("Save");
      btnConfirm.setDisable(true);
      releaseLabelField.setText(release.getLabel());
      releaseDescriptionField.setText(release.getReleaseDescription());
      releaseNotesField.setText(release.getReleaseNotes());
      projectComboBox.setValue(release.getProjectRelease());
      releaseDateField.setValue(release.getReleaseDate());
      if (release.getProjectRelease() != null) {
        btnEditProject.setDisable(false);
        for (Sprint sprint : mainApp.getSprints()) {
          if (release.getProjectRelease().equals(sprint.getSprintProject())) {
            // release is in sprint, do not allow changing project
            btnNewProject.setDisable(true);
            projectComboBox.setDisable(true);
            Tooltip tooltip = new Tooltip("This cannot be changed because the release is currently "
                                          + "in a sprint");
            projectContainer.setTooltip(tooltip);
            break;
          }
        }
      } else {
        btnEditProject.setDisable(true);
      }

      initialiseLists();
    }
    this.createOrEdit = createOrEdit;

    if (release != null) {
      this.release = release;
      this.lastRelease = new Release(release);
    } else {
      this.release = null;
      this.lastRelease = null;
    }

    btnConfirm.setDefaultButton(true);
    thisStage.setResizable(false);

    thisStage.setOnCloseRequest(event -> {
      mainApp.popControllerStack();
    });

    // Handle TextField text changes.
    releaseLabelField.textProperty().addListener((observable, oldValue, newValue) -> {
      if(createOrEdit == CreateOrEdit.EDIT) {
        checkButtonDisabled();
      }
      if (newValue.trim().length() > 20) {
        releaseLabelField.setStyle("-fx-text-inner-color: red;");
      } else {
        releaseLabelField.setStyle("-fx-text-inner-color: black;");
      }
    });

    releaseDescriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
      //For disabling the button
      if(createOrEdit == CreateOrEdit.EDIT) {
        checkButtonDisabled();
      }
    });

    releaseNotesField.textProperty().addListener((observable, oldValue, newValue) -> {
      //For disabling the button
      if (createOrEdit == CreateOrEdit.EDIT) {
        checkButtonDisabled();
      }
    });

    projectComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      //For disabling the button
      if (createOrEdit == CreateOrEdit.EDIT) {
        checkButtonDisabled();
      }
      if (newValue != null) {
        btnEditProject.setDisable(false);
      } else {
        btnEditProject.setDisable(true);
      }
    });

    releaseDateField.valueProperty().addListener((observable, oldValue, newValue) -> {
      //For disabling the button
      if (createOrEdit == CreateOrEdit.EDIT) {
        checkButtonDisabled();
      }
    });
    thisStage.getIcons().add(new Image("Thumbnail.png"));
  }

  /**
   * Set up the dialog to be in sprint mode.
   *
   * @param project The project to be auto selected.
   */
  public void setupSprintMode(Project project) {
    dialogMode = DialogMode.SPRINT_MODE;
    projectComboBox.getSelectionModel().select(project);
    projectComboBox.setDisable(true);
    Tooltip tooltip = new Tooltip("This cannot be changed because the project has already been "
                                  + "specified in the sprint dialog");
    projectContainer.setTooltip(tooltip);
    btnNewProject.setDisable(true);
    btnEditProject.setDisable(true);
  }

  /**
   * checks if there are any changed fields and disables or enables the button accordingly
   */
  private void checkButtonDisabled() {
    if (releaseLabelField.getText().equals(release.getLabel()) &&
        releaseDescriptionField.getText().equals(release.getReleaseDescription()) &&
        releaseNotesField.getText().equals(release.getReleaseNotes()) &&
        projectComboBox.getSelectionModel().getSelectedItem().equals(release.getProjectRelease())
        && releaseDateField.getValue().toString().equals(release.getReleaseDate().toString())) {
      btnConfirm.setDisable(true);
    } else {
      btnConfirm.setDisable(false);
    }
  }

  /**
   * Initialise the contents of the lists according to whether the user is creating a new Release or
   * editing an existing one.
   */
  private void initialiseLists() {
    try {
      // loop for adding the specific project to release.
      availableProjects.addAll(mainApp.getProjects());

      this.projectComboBox.setVisibleRowCount(5);
      this.projectComboBox.setItems(availableProjects);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Takes the inputReleaseLabel and checks to see if it is allowed to be used as an label
   *
   * @param inputReleaseLabel the label that the user wants for this release
   * @return String returns this only if its an allowed label
   * @throws Exception Throws an exception if the label is not allowed
   */
  private String parseReleaseLabel(String inputReleaseLabel) throws Exception {
    inputReleaseLabel = inputReleaseLabel.trim();

    if (inputReleaseLabel.isEmpty()) {
      throw new Exception("Release label is empty.");
    } else {
      String lastReleaseLabel;
      if (lastRelease == null) {
        lastReleaseLabel = "";
      } else {
        lastReleaseLabel = lastRelease.getLabel();
      }
      for (Release releaseInList : mainApp.getReleases()) {
        String releaseLabel = releaseInList.getLabel();
        if (releaseLabel.equalsIgnoreCase(inputReleaseLabel) &&
            !releaseLabel.equalsIgnoreCase(lastReleaseLabel)) {
          throw new Exception("Release label is not unique.");
        }
      }
    }
    return inputReleaseLabel;
  }

  /**
   * @param inputReleaseDate the release date the user wants
   * @return String returns if date is allowed
   * @throws Exception throws if date is empty
   */
  private LocalDate parseReleaseDate(LocalDate inputReleaseDate) throws Exception {

    if (inputReleaseDate == null) {
      throw new Exception("Release date is empty.");
    }

    if (createOrEdit == CreateOrEdit.EDIT) {
      for (Sprint sprint : mainApp.getSprints()) {
        if (sprint.getSprintRelease().getLabel().equals(releaseLabelField.getText().trim())) {
          if (sprint.getSprintEnd().isAfter(releaseDateField.getValue())) {
            throw new Exception("Release date must be after the end date of the sprint"
                                + " it is assigned to.");
          }
        }
      }
    }

    return inputReleaseDate;
  }

  /**
   * @param inputReleaseProject the project that the user wants the release to be assigned too
   * @return Project object if it is allowed
   * @throws Exception throws if no project is assigned to this release
   */
  private Project parseReleaseProject(Project inputReleaseProject) throws Exception {

    if (inputReleaseProject == null) {
      throw new Exception("No project has been selected for this release.");
    }
    return inputReleaseProject;
  }

  /**
   * Handles when the cancel button is clicked by not applying any changes and closing dialog
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnCancelClick(ActionEvent event) {
    mainApp.popControllerStack();
    thisStage.close();
  }

  /**
   * A button which when clicked can edit the selected project in the project combo box.
   * Also adds to undo/redo stack so the edit is undoable.
   * @param event Button click
   */
  @FXML
  protected void editProject(ActionEvent event) {
    List<Project> tempProjectList = new ArrayList<>(availableProjects);
    Project selectedProject = projectComboBox.getSelectionModel().getSelectedItem();
    if (selectedProject != null) {
      mainApp.showProjectDialogWithinRelease(selectedProject, thisStage);
      availableProjects.setAll(tempProjectList);
      projectComboBox.getSelectionModel().select(selectedProject);
    }
  }

  /**
   * A button which when clicked can add a backlog to the system.
   * Also adds to undo/redo stack so creation is undoable.
   * @param event Button click
   */
  @FXML
  protected void addNewProject(ActionEvent event) {
    List<Project> tempProjectList = new ArrayList<>(availableProjects);
    mainApp.showProjectDialog(CreateOrEdit.CREATE);
    List<Project> tempNewProjectList = new ArrayList<>(mainApp.getProjects());
    if (!projectComboBox.isDisabled()) {
      for (Project project : tempNewProjectList) {
        if (!tempProjectList.contains(project)) {
          availableProjects.setAll(tempNewProjectList);
          projectComboBox.getSelectionModel().select(project);
          break;
        }
      }
    }
  }

  /**
   * Returns the label of the backlog if a backlog is being edited.
   *
   * @return The label of the backlog as a string.
   */
  public String getLabel() {
    if (createOrEdit == CreateOrEdit.EDIT) {
      return release.getLabel();
    }
    return "";
  }
}
