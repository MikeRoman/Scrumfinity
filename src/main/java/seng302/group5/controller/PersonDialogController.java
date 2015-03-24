package seng302.group5.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seng302.group5.Main;
import seng302.group5.model.Person;

/**
 * Created by Zander on 18/03/2015.
 */
public class PersonDialogController {

  private Main mainApp;
  private Stage thisStage;

  @FXML private TextField personIDField;
  @FXML private TextField personFirstNameField;
  @FXML private TextField personLastNameField;

  /**
   * Sets the current instance of Main as mainApp.
   * @param mainApp Current instance of Main.
   */
  public void setMainApp(Main mainApp){
    this.mainApp = mainApp;
  }

  public void setStage(Stage stage) {
    this.thisStage = stage;
  }

  /**
   * Creates a new Person from the textfield data
   * on click of 'Create' button.
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnCreatePersonClick(ActionEvent event) {
    StringBuilder errors = new StringBuilder();
    errors.append("Invalid Fields:");
    int noErrors = 0;

    String personID = "";
    String personFirstName = personFirstNameField.getText().trim();
    String personLastName = personLastNameField.getText().trim();

    try {
      personID = parsePersonID(personIDField.getText());
    }
    catch (Exception e) {
      noErrors++;
      errors.append(String.format("\n\t%s", e.getMessage()));
    }

    // Display all errors if they exist
    if (noErrors > 0) {
      String title;
      if (noErrors == 1) {
        title = String.format("%d Invalid Field", noErrors);
      }
      else {
        title = String.format("%d Invalid Fields", noErrors);
      }
      // TODO: Dialogs for errors
      System.out.println(String.format("%s\n%s", title, errors.toString()));
    }
    else {
      Person person = new Person(personID, personFirstName, personLastName);
      mainApp.addPerson(person);
      thisStage.close();
    }
  }

  /**
   * Closes the CreatePerson dialog box in click of
   * 'Cancel' button.
   *
   * @param event Event generated by event listener.
   */
  @FXML
  protected void btnCancelClick(ActionEvent event) {
    thisStage.close();
  }

  /**
   * Checks that the Person ID entry box contains
   * valid input.
   *
   * @param inputPersonID Person ID from entry field.
   * @return Person ID if ID is valid.
   * @throws Exception Any invalid input.
   */
  private String parsePersonID(String inputPersonID) throws Exception {
    inputPersonID = inputPersonID.trim();

    if (inputPersonID.isEmpty()) {
      throw new Exception("Person ID is empty.");
    }
    else if (inputPersonID.length() > 8) {
      throw new Exception("Person ID is more than 8 characters long");
    }
    else {
      for (Person person : mainApp.getPeople()) {
        if (person.getPersonID().equals(inputPersonID)) {
          throw new Exception("Person ID is not unique.");
        }
      }
      return inputPersonID;
    }
  }
}
