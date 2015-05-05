package seng302.group5.controller;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import seng302.group5.Main;
import seng302.group5.controller.enums.CreateOrEdit;
import seng302.group5.model.Person;
import seng302.group5.model.Role;
import seng302.group5.model.Team;
import seng302.group5.model.undoredo.Action;
import seng302.group5.model.undoredo.UndoRedoObject;

/**
 * Created by Zander on 24/03/2015.
 * Team Dialog Controller, manages the usage of Dialogs involved in the creating and editing of
 * teams.
 */
public class TeamDialogController {

  private Main mainApp;
  private Stage thisStage;

  private Team team;
  private Team lastTeam;

  private CreateOrEdit createOrEdit;

  private ObservableList<Person> availableMembers = FXCollections.observableArrayList();
  private ObservableList<PersonRole> selectedMembers = FXCollections.observableArrayList();
  private ArrayList<Person> membersToRemove = new ArrayList<>();

  @FXML private TextField teamIDField;
  @FXML private ListView<PersonRole> teamMembersList;
  @FXML private ComboBox<Person> teamMemberAddCombo;
  @FXML private ComboBox<Role> teamMemberRoleCombo;
  @FXML private TextArea teamDescriptionField;
  @FXML private Button btnConfirm;

  /**
   * Setup the project dialog controller
   *
   * @param mainApp      The main application object
   * @param thisStage    The stage of the dialog
   * @param createOrEdit If dialog is for creating or editing a Team
   * @param team         The team object if editing, null otherwise
   */
  public void setupController(Main mainApp, Stage thisStage, CreateOrEdit createOrEdit, Team team) {
    this.mainApp = mainApp;
    this.thisStage = thisStage;

    if (createOrEdit == CreateOrEdit.CREATE) {
      thisStage.setTitle("Create New Team");
      btnConfirm.setText("Create");
      initialiseLists(CreateOrEdit.CREATE, team);
    } else if (createOrEdit == CreateOrEdit.EDIT) {
      thisStage.setTitle("Edit Team");
      btnConfirm.setText("Save");

      teamIDField.setText(team.getLabel());
      initialiseLists(CreateOrEdit.EDIT, team);
      teamDescriptionField.setText(team.getTeamDescription());

    }
    this.createOrEdit = createOrEdit;

    if (team != null) {
      this.team = team;
      this.lastTeam = new Team(team);
    } else {
      this.team = null;
      this.lastTeam = null;
    }

    teamDescriptionField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        btnConfirm.fire();
      }
    });
    btnConfirm.setDefaultButton(true);
  }

  /**
   * Populates the people selection lists for assigning people to the team.
   *
   * @param createOrEdit the enum object to decide if it is creating or editing
   * @param team         the Team that is being created or edited
   */
  private void initialiseLists(CreateOrEdit createOrEdit, Team team) {
    try {
      if (createOrEdit == CreateOrEdit.CREATE) {
        for (Person person : mainApp.getPeople()) {
          if (!person.isInTeam()) {
            availableMembers.add(person);
          }
        }
      } else if (createOrEdit == CreateOrEdit.EDIT) {
        for (Person person : mainApp.getPeople()) {
          if (!person.isInTeam()) {
            availableMembers.add(person);
          }
        }
        for (Person person : team.getTeamMembers()) {
          Role role = team.getMembersRole().get(person);
          selectedMembers.add(new PersonRole(person, role));
        }
      }

      this.teamMemberAddCombo.setVisibleRowCount(5);
      this.teamMemberAddCombo.setItems(availableMembers);
      this.teamMembersList.setItems(selectedMembers);

      this.teamMemberRoleCombo.setItems(mainApp.getRoles());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles the add button for adding a Person to a team.
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnAddMemberClick(ActionEvent event) {
    try {
      Person selectedPerson = teamMemberAddCombo.getSelectionModel().getSelectedItem();
      Role selectedRole = teamMemberRoleCombo.getSelectionModel().getSelectedItem();

      // Get the number of times the selected role is already used
      int roleTally = 0;
      for (PersonRole personRole : selectedMembers) {
        if (personRole.getRole() == selectedRole) {
          roleTally++;
        }
      }

      if (selectedPerson != null) {
        if (selectedRole != null && selectedRole.getRequiredSkill() != null &&
            !selectedPerson.getSkillSet().contains(selectedRole.getRequiredSkill())) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Required skill not found");
          alert.setHeaderText(null);
          alert.setContentText(String.format("%s does not have the required skill of %s",
                                             selectedPerson, selectedRole.getRequiredSkill()));
          alert.showAndWait();
        } else if (selectedRole != null && roleTally >= selectedRole.getMemberLimit()) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Role taken");
          alert.setHeaderText(null);
          alert.setContentText(String.format("Max number of %s already assigned", selectedRole));
          alert.showAndWait();
        } else {
          this.selectedMembers.add(new PersonRole(selectedPerson, selectedRole));
          this.availableMembers.remove(selectedPerson);
          this.membersToRemove.remove(selectedPerson);
          this.teamMemberAddCombo.setItems(availableMembers);

          this.teamMemberRoleCombo.getSelectionModel().clearSelection();
          this.teamMemberRoleCombo.setValue(null);
          this.teamMemberAddCombo.getSelectionModel().clearSelection();
          this.teamMemberAddCombo.setValue(null);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles when the remove button is clicked for removing a person from a team
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnRemoveMemberClick(ActionEvent event) {
    try {
      PersonRole selectedPersonRole = teamMembersList.getSelectionModel().getSelectedItem();

      if (selectedPersonRole != null) {
        Person selectedPerson = selectedPersonRole.getPerson();
        this.availableMembers.add(selectedPerson);
        this.selectedMembers.remove(selectedPersonRole);
        this.membersToRemove.add(selectedPerson);
      }
    } catch (Exception e) {
      e.printStackTrace();
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
      undoRedoObject.setAction(Action.TEAM_CREATE);
    } else {
      undoRedoObject.setAction(Action.TEAM_EDIT);
      undoRedoObject.addDatum(lastTeam);
    }

    // Store a copy of skill to edit in stack to avoid reference problems
    undoRedoObject.setAgileItem(team);
    Team teamToStore = new Team(team);
    undoRedoObject.addDatum(teamToStore);

    return undoRedoObject;
  }

  /**
   * Handles the confirm click by error checking the inputs and if it passes then creating and
   * adding the object
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnConfirmClick(ActionEvent event) {
    StringBuilder errors = new StringBuilder();
    errors.append("Invalid Fields:");
    int noErrors = 0;

    String teamID = "";
    String teamDescription = teamDescriptionField.getText().trim();

    try {
      teamID = parseTeamID(teamIDField.getText());
    } catch (Exception e) {
      noErrors++;
      errors.append(String.format("\n\t%s", e.getMessage()));
    }

    // Display all errors if they exist
    if (noErrors > 0) {
      String title = String.format("%d Invalid Field", noErrors);
      if (noErrors > 1) {
        title += "s";  // plural
      }
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(errors.toString());
      alert.showAndWait();
    } else {
      if (createOrEdit == CreateOrEdit.CREATE) {
        team = new Team(teamID, teamDescription);
        for (PersonRole personRole : selectedMembers) {
          team.addTeamMember(personRole.getPerson(), personRole.getRole());
          personRole.getPerson().assignToTeam(team);
        }
        mainApp.addTeam(team);

      } else if (createOrEdit == CreateOrEdit.EDIT) {

        team.setLabel(teamID);
        team.setTeamDescription(teamDescription);
        team.getTeamMembers().clear();
        team.getMembersRole().clear();
        for (PersonRole personRole : selectedMembers) {
          team.addTeamMember(personRole.getPerson(), personRole.getRole());
          personRole.getPerson().assignToTeam(team);
        }
        for (Person memberToRemove : membersToRemove) {
          memberToRemove.removeFromTeam();
        }
        mainApp.refreshList();
      }

      UndoRedoObject undoRedoObject = generateUndoRedoObject();
      mainApp.newAction(undoRedoObject);
      thisStage.close();
    }
  }

  /**
   * Handles when the cancel button is clicked by not applying changes and closing dialog
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnCancelClick(ActionEvent event) {
    thisStage.close();
  }

  /**
   * Checks if teamID field contains valid input.
   *
   * @param inputTeamID String teamID.
   * @return teamID if teamID is valid.
   * @throws Exception If teamID is not valid.
   */
  private String parseTeamID(String inputTeamID) throws Exception {
    inputTeamID = inputTeamID.trim();

    if (inputTeamID.isEmpty()) {
      throw new Exception("Team ID is empty.");
    } else if (inputTeamID.length() > 8) {
      throw new Exception("Team ID is more than 8 characters long");
    } else {
      String lastTeamID;
      if (lastTeam == null) {
        lastTeamID = "";
      } else {
        lastTeamID = lastTeam.getLabel();
      }
      for (Team team : mainApp.getTeams()) {
        String teamID = team.getLabel();
        if (team.getLabel().equals(inputTeamID) && !teamID.equals(lastTeamID)) {
          throw new Exception("Team ID is not unique.");
        }
      }
      return inputTeamID;
    }
  }

  /**
   * Inner class for storing/displaying allocated team members with their respective roles
   */
  private class PersonRole {

    private Person person;
    private Role role;

    public PersonRole(Person person, Role role) {
      this.person = person;
      this.role = role;
    }

    /**
     * gets the person object from this object
     *
     * @return Person object
     */
    public Person getPerson() {
      return person;
    }

    /**
     * gets the Role object from this object
     *
     * @return Role object
     */
    public Role getRole() {
      return role;
    }

    @Override
    public String toString() {
      if (role != null) {
        return person.toString() + " - Role: " + role.toString();
      } else {
        return person.toString();
      }
    }

  }
}
