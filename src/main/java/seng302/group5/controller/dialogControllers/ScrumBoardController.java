package seng302.group5.controller.dialogControllers;

import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import seng302.group5.Main;
import seng302.group5.controller.enums.CreateOrEdit;
import seng302.group5.model.Backlog;
import seng302.group5.model.Sprint;
import seng302.group5.model.Status;
import seng302.group5.model.Story;
import seng302.group5.model.Task;
import seng302.group5.model.Taskable;
import seng302.group5.model.undoredo.Action;
import seng302.group5.model.undoredo.UndoRedoObject;
import seng302.group5.model.undoredo.UndoRedo;

/**
 * The controller class for the scrum board dialog. Tasks can be viewed from this dialog by
 * selecting the backlog->sprint->story accordingly. Also tasks are sorted by their status in four
 * different lists: not started, in progress, verify and done.
 *
 * Version 1: status are to be changed by double click editing.
 *
 * @author liangma
 */
public class ScrumBoardController {
  @FXML private ComboBox<Backlog> backlogCombo;
  @FXML private ComboBox<Sprint> sprintCombo;
  @FXML private ComboBox<Story> storyCombo;
  @FXML private ListView<Task> notStartedList;
  @FXML private ListView<Task> inProgressList;
  @FXML private ListView<Task> verifyList;
  @FXML private ListView<Task> doneList;
  @FXML private Button btnNewTask;
  @FXML private Button btnDeleteTask;

  private Main mainApp;
  private Stage stage;

  private Task task;
  private Task lastTask;
  private Task selectedTask;

  private ObservableList<Sprint> availableSprints;
  private ObservableList<Story> availableStories;
  private ObservableList<Task> notStartedTasks;
  private ObservableList<Task> inProgressTasks;
  private ObservableList<Task> verifyTasks;
  private ObservableList<Task> doneTasks;

  private Story nonStory;

  @FXML
  private void initialize() {
    nonStory = new Story();
    nonStory.setLabel("Non-story Tasks");
  }

  /**
   * This function sets up the scrum board dialog controller.
   * @param mainApp     The main application object
   * @param stage       The stage the application is in.
   */
  public void setupController(Main mainApp, Stage stage) {
    this.mainApp = mainApp;
    this.stage = stage;
    task = new Task();
    lastTask = new Task();
    initialiseLists();
    sprintCombo.setDisable(true);
    storyCombo.setDisable(true);
    btnNewTask.setDisable(true);

    setupListView();
  }

  /**
   * This re populates the combo boxes in the scrum board so that  the proper items are displayed
   * it then re-selects what ever you had previously selected if it still exists.
   */
  public void hardReset() {
    Backlog backlog = backlogCombo.getValue();
    Sprint sprint = sprintCombo.getValue();
    Story story = storyCombo.getValue();

    initialiseLists();

    sprintCombo.setDisable(true);
    storyCombo.setDisable(true);
    btnNewTask.setDisable(true);

    if (mainApp.getBacklogs().contains(backlog)) {
      backlogCombo.setValue(backlog);

      if(availableSprints.contains(sprint)){
        sprintCombo.setValue(sprint);
        //todo make stories in sprints be removed properly at some point.
        if(availableStories.contains(story) && mainApp.getStories().contains(story)) {
          storyCombo.setValue(story);
        } else {
          storyCombo.setValue(nonStory);
        }
      } else {
        sprintCombo.setValue(null);
        storyCombo.setValue(null);
      }
    }
    refreshLists();
  }



  /**
   * Initialises the models lists and populates these with values from the main application,
   * such as available backlogs, sprints and stories. These values
   * are then populated into their respective GUI elements. The backlog combo box has a listener
   * to update other GUI elements which depend on the backlog.
   */
  private void initialiseLists() {
    availableSprints = FXCollections.observableArrayList();
    availableStories = FXCollections.observableArrayList();
    notStartedTasks = FXCollections.observableArrayList();
    inProgressTasks = FXCollections.observableArrayList();
    verifyTasks = FXCollections.observableArrayList();
    doneTasks = FXCollections.observableArrayList();

    backlogCombo.getSelectionModel().clearSelection();
    backlogCombo.setItems(mainApp.getBacklogs());

    backlogCombo.getSelectionModel().selectedItemProperty().addListener(
      (observable, oldBacklog, newBacklog) -> {
        if (newBacklog != null) {
          sprintCombo.setDisable(false);

          // get backlog's sprints
          availableSprints.setAll(mainApp.getSprints().stream()
                                      .filter(
                                          sprint -> sprint.getSprintBacklog().equals(newBacklog))
                                      .collect(Collectors.toList()));
          sprintCombo.setItems(null);
          sprintCombo.setItems(availableSprints);
          refreshLists();
          sprintCombo.setValue(null);
          storyCombo.setValue(null);
          storyCombo.setDisable(true);
        }
      }
    );

    sprintCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldSprint, newSprint) -> {
          if (newSprint != null) {
            storyCombo.setDisable(false);


            availableStories.clear();
            for (Story story : newSprint.getSprintStories()) {
              if (mainApp.getStories().contains(story)) {
                availableStories.add(story);
              }
            }

            availableStories.add(0, nonStory);

            storyCombo.setItems(null);
            storyCombo.setItems(availableStories);
            storyCombo.setValue(nonStory);
            refreshLists();
          }
        }
    );

    storyCombo.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldStory, newStory) -> {
          if (oldStory != null && newStory != null) {
            refreshLists();
          }
        }
    );
  }

  /**
   * Sets the custom behaviour for all four tasks ListView.
   */
  private void setupListView() {
    //Sets the cell being populated with custom settings defined in the ListViewCell class.
    this.notStartedList.setCellFactory(listView -> new ListCell(notStartedList));
    this.inProgressList.setCellFactory(listView -> new ListCell(inProgressList));
    this.verifyList.setCellFactory(listView -> new ListCell(verifyList));
    this.doneList.setCellFactory(listView -> new ListCell(doneList));
  }

  /**
   * Allows us to override a ListViewCell - a single cell in the ListView.
   */
  private class ListCell extends TextFieldListCell<Task> {
    private String state;

    /**
     * rate an UndoRedoObject to represent a task edit action and store it globally. This is so
     * a cancel in a dialog higher in the hierarchy can undo the changes made to the task.
     */
    private void generateUndoRedoObject() {


      // Store a copy of task to edit in object to avoid reference problems
      if (task != null) {
        UndoRedo undoRedoObject = new UndoRedoObject();

        undoRedoObject.setAgileItem(task);

        undoRedoObject = new UndoRedoObject();
        undoRedoObject.setAction(Action.TASK_EDIT);
        undoRedoObject.addDatum(lastTask);

        // Store a copy of task to edit in object to avoid reference problems
        undoRedoObject.setAgileItem(task);
        Task taskToStore = new Task(task);
        undoRedoObject.addDatum(taskToStore);
        mainApp.newAction(undoRedoObject);
      }
    }



    public ListCell(ListView<Task> taskListView) {
      super();

      // double click for editing
      this.setOnMouseClicked(click -> {
        if (click.getClickCount() == 2 &&
            click.getButton() == MouseButton.PRIMARY &&
            !isEmpty()) {
          Task selectedTask = getItem();
          Taskable taskable;
          if (storyCombo.getValue() == nonStory) {
            taskable = sprintCombo.getValue();
          } else {
            taskable = storyCombo.getValue();
          }
          UndoRedo taskEdit = mainApp.showTaskDialog(taskable, selectedTask,
                                                     sprintCombo.getValue().getSprintTeam(),
                                                     CreateOrEdit.EDIT, stage);
          if (taskEdit != null) {
            mainApp.newAction(taskEdit);
            refreshLists();
          }
        }
      });

      taskListView.setOnMouseClicked(event -> {
        selectedTask = taskListView.getSelectionModel().getSelectedItem();
      });


      taskListView.setCursor(Cursor.OPEN_HAND);

      taskListView.setOnDragDetected(event -> {
        if (taskListView.getSelectionModel().getSelectedItem() != null) {
          state = "";
          lastTask = new Task(taskListView.getSelectionModel().getSelectedItem());
          task = taskListView.getSelectionModel().getSelectedItem();

          Dragboard dragBoard = taskListView.startDragAndDrop(TransferMode.ANY);

          ClipboardContent content = new ClipboardContent();
          content.putString(taskListView.getSelectionModel().getSelectedItem().getLabel());

          dragBoard.setContent(content);
          dragBoard.setDragViewOffsetX(1.0);
          dragBoard.setDragViewOffsetY(1.0);

          notStartedList.setOnDragOver(hover -> state = "notstarted");
          inProgressList.setOnDragOver(hover -> state = "progress");
          verifyList.setOnDragOver(hover -> state = "verify");
          doneList.setOnDragOver(hover -> state = "done");

        }
        event.consume();
      });
      taskListView.setOnDragDone(
          event -> {
            if (taskListView.getSelectionModel().getSelectedItem() != null) {
              if (state.equals("notstarted")) {
                task.setStatus(Status.NOT_STARTED);
              } else if (Objects.equals(state, "progress")) {
                task.setStatus(Status.IN_PROGRESS);
              } else if (Objects.equals(state, "verify")) {
                task.setStatus(Status.VERIFY);
              } else if (Objects.equals(state, "done")) {
                task.setStatus(Status.DONE);
              }
              if (!task.getStatus().equals(lastTask.getStatus())) {
                generateUndoRedoObject();
              }

              refreshLists();
            }
            event.consume();
          });
      refreshLists();
    }
  }


  /**
   * Refreshes the four list views when any of the tasks within the story is updated.
   */
  public void refreshLists() {
    notStartedTasks.clear();
    inProgressTasks.clear();
    verifyTasks.clear();
    doneTasks.clear();

    btnNewTask.setDisable(true);

    if (backlogCombo.getSelectionModel().getSelectedItem() != null &&
        sprintCombo.getSelectionModel().getSelectedItem() != null) {
      if (storyCombo.getValue() == nonStory) {
        btnNewTask.setDisable(false);
        sprintCombo.getValue().getTasks().forEach(this::sortTaskToLists);
      } else if (!storyCombo.getSelectionModel().getSelectedItem().getTasks().isEmpty()) {
        btnNewTask.setDisable(false);
        storyCombo.getValue().getTasks().forEach(this::sortTaskToLists);
      } else {
        btnNewTask.setDisable(false);
        Task newTask = new Task();
        notStartedTasks.add(newTask);
        inProgressTasks.add(newTask);
        verifyTasks.add(newTask);
        doneTasks.add(newTask);
        notStartedTasks.clear();
        inProgressTasks.clear();
        verifyTasks.clear();
        doneTasks.clear();
      }
    }
    notStartedList.setItems(notStartedTasks);
    inProgressList.setItems(inProgressTasks);
    verifyList.setItems(verifyTasks);
    doneList.setItems(doneTasks);

  }

  /**
   * Sorting tasks to the correct list according to their current status.
   * @param task the task that's within the story or sprint
   */
  private void sortTaskToLists(Task task) {
    if (task.getStatus().equals(Status.NOT_STARTED)) {
      notStartedTasks.add(task);
    } else if (task.getStatus().equals(Status.IN_PROGRESS)) {
      inProgressTasks.add(task);
    } else if (task.getStatus().equals(Status.VERIFY)) {
      verifyTasks.add(task);
    } else if (task.getStatus().equals(Status.DONE)) {
      doneTasks.add(task);
    }
  }

  /**
   * On loading the scrum board needs to be completely reset. This functions does that, clears all
   * selections.
   */
  public void clearSelections() {
    notStartedTasks.clear();
    inProgressTasks.clear();
    verifyTasks.clear();
    doneTasks.clear();
    storyCombo.getSelectionModel().clearSelection();
    storyCombo.getItems().clear();
    storyCombo.setDisable(true);
    sprintCombo.getSelectionModel().clearSelection();
    sprintCombo.getItems().clear();
    sprintCombo.setDisable(true);
    backlogCombo.getSelectionModel().clearSelection();
    backlogCombo.setItems(FXCollections.observableArrayList());
    btnNewTask.setDisable(true);

    initialiseLists();
  }


  /**
   * A button which when clicked can add a task to either the selected story, or if the "nonStory"
   * of sprint tasks, can add into there as well. Also adds to undo/redo stack so creationg is undoable
   * @param event Button click
   */
  @FXML
  protected void addNewTask(ActionEvent event) {
    Story story = storyCombo.getSelectionModel().getSelectedItem();
    if (storyCombo.getSelectionModel().getSelectedItem() != null) {
      Sprint sprint = sprintCombo.getSelectionModel().getSelectedItem();
      UndoRedo undoRedoCreate;
      if (story == nonStory) {
        undoRedoCreate = mainApp.showTaskDialog(sprint, null, sprint.getSprintTeam(), CreateOrEdit.CREATE, stage);
      } else {
        undoRedoCreate = mainApp.showTaskDialog(story, null, sprint.getSprintTeam(), CreateOrEdit.CREATE, stage);
      }
      if (undoRedoCreate != null) {
        mainApp.newAction(undoRedoCreate);
      }
    }
  }

  @FXML void deleteTask(ActionEvent event) {
    Story story = storyCombo.getValue();
    if (storyCombo.getSelectionModel().getSelectedItem() != null) {
      Sprint sprint = sprintCombo.getSelectionModel().getSelectedItem();
      UndoRedo undoRedoDelete;
      undoRedoDelete = new UndoRedoObject();
      if (story == nonStory) {
        if (selectedTask != null) {
          undoRedoDelete.setAction(Action.TASK_DELETE);
          sprint.removeTask(selectedTask);
          nonStory.removeTask(selectedTask);
          undoRedoDelete.setAgileItem(selectedTask);
          undoRedoDelete.addDatum(new Task(selectedTask));
          undoRedoDelete.addDatum(sprint);
          selectedTask = null;
          refreshLists();
        }
      } else {
        if (selectedTask != null) {
          undoRedoDelete.setAction(Action.TASK_DELETE);
          story.removeTask(selectedTask);
          undoRedoDelete.setAgileItem(selectedTask);
          undoRedoDelete.addDatum(new Task(selectedTask));
          undoRedoDelete.addDatum(story);
          selectedTask = null;
          refreshLists();
        }
      }
      if (undoRedoDelete.getAction().equals(Action.TASK_DELETE)) {
        System.out.println(undoRedoDelete.getAgileItem());
        mainApp.newAction(undoRedoDelete);
      }


    }

  }
}
