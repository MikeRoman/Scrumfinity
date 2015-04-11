package seng302.group5.model.undoredo;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.group5.Main;
import seng302.group5.controller.ListMainPaneController;
import seng302.group5.model.AgileItem;
import seng302.group5.model.Person;
import seng302.group5.model.Skill;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for undo and redo handler
 */
public class UndoRedoHandlerTest {

  private String personID;
  private String firstName;
  private String lastName;
  private ObservableList<Skill> skillSet = FXCollections.observableArrayList();
  private String newPersonID;
  private String newFirstName;
  private String newLastName;
//  private ObservableList<Skill> newSkillSet = FXCollections.observableArrayList(); // use later

  private Person person;

  private UndoRedoHandler undoRedoHandler;
  private Main mainApp;

  @Before
  public void setUp() throws Exception {

    ListMainPaneController listMainPaneController = mock(ListMainPaneController.class);

    mainApp = new Main();
    mainApp.setLMPC(listMainPaneController);

    undoRedoHandler = mainApp.getUndoRedoHandler();
  }

  private void newPerson() {
    personID = "ssc55";
    firstName = "Su-Shing";
    lastName = "Chen";
    person = new Person(personID, firstName, lastName, skillSet);

    mainApp.addPerson(person);

    UndoRedoObject undoRedoObject = new UndoRedoObject();
    undoRedoObject.setAction(Action.PERSON_CREATE);
    undoRedoObject.addDatum(new Person(person));

    undoRedoHandler.newAction(undoRedoObject);
  }

  private void deleteNewestPerson() {
    mainApp.deletePerson(person);

    UndoRedoObject undoRedoObject = new UndoRedoObject();
    undoRedoObject.setAction(Action.PERSON_DELETE);
    undoRedoObject.addDatum(new Person(person));

    undoRedoHandler.newAction(undoRedoObject);
  }

  private void editNewestPerson() {
    Person lastPerson = new Person(person);

    newPersonID = "apw76";
    newFirstName = "Alex";
    newLastName = "Woo";
    person.setPersonID(newPersonID);
    person.setFirstName(newFirstName);
    person.setLastName(newLastName);

    Person newPerson = new Person(person);

    UndoRedoObject undoRedoObject = new UndoRedoObject();
    undoRedoObject.setAction(Action.PERSON_EDIT);
    undoRedoObject.addDatum(lastPerson);
    undoRedoObject.addDatum(newPerson);

    undoRedoHandler.newAction(undoRedoObject);
  }

  @Test
  public void testPeekUndoStack() throws Exception {
    assertTrue(undoRedoHandler.getUndoStack().empty());

    newPerson();

    UndoRedoObject peekObject = undoRedoHandler.peekUndoStack();
    ArrayList<AgileItem> data = peekObject.getData();
    Person peekPerson = (Person) data.get(0);

    assertEquals(peekObject.getAction(), Action.PERSON_CREATE);
    assertEquals(peekPerson.getPersonID(), personID);
    assertEquals(peekPerson.getFirstName(), firstName);
    assertEquals(peekPerson.getLastName(), lastName);
  }

  @Test
  public void testClearStacks() throws Exception {
    assertTrue(undoRedoHandler.getUndoStack().empty());

    newPerson();
    newPerson();
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    // Add a random person to the redo stack
    Person tempPerson = new Person(personID, firstName, lastName, skillSet);
    UndoRedoObject undoRedoObject = new UndoRedoObject();
    undoRedoObject.setAction(Action.PERSON_CREATE);
    undoRedoObject.addDatum(new Person(tempPerson));
    undoRedoHandler.getRedoStack().add(undoRedoObject);

    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertEquals(undoRedoHandler.getRedoStack().size(), 1);

    // Clear stacks
    undoRedoHandler.clearStacks();
    assertTrue(undoRedoHandler.getUndoStack().empty());
    assertTrue(undoRedoHandler.getRedoStack().empty());
  }

  @Test
  public void testPersonCreateUndo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);

    undoRedoHandler.undo();
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());
  }

  @Test
  public void testPersonCreateRedo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getRedoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    undoRedoHandler.undo();
    assertTrue(mainApp.getPeople().isEmpty());
    assertEquals(undoRedoHandler.getRedoStack().size(), 1);

    undoRedoHandler.redo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
    assertTrue(undoRedoHandler.getRedoStack().empty());
  }

  @Test
  public void testPersonDeleteUndo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);

    deleteNewestPerson();
    assertEquals(mainApp.getPeople().size(), 0);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);

    undoRedoHandler.undo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
  }

  @Test
  public void testPersonDeleteRedo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());
    assertTrue(undoRedoHandler.getRedoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    deleteNewestPerson();
    assertEquals(mainApp.getPeople().size(), 0);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    undoRedoHandler.undo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
    assertEquals(undoRedoHandler.getRedoStack().size(), 1);

    undoRedoHandler.redo();
    assertEquals(mainApp.getPeople().size(), 0);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertTrue(undoRedoHandler.getRedoStack().empty());
  }

  @Test
  public void testPersonEditUndo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);

    Person createdPerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(createdPerson.getPersonID(), personID);
    assertEquals(createdPerson.getFirstName(), firstName);
    assertEquals(createdPerson.getLastName(), lastName);

    editNewestPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);

    Person editedPerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(editedPerson.getPersonID(), newPersonID);
    assertEquals(editedPerson.getFirstName(), newFirstName);
    assertEquals(editedPerson.getLastName(), newLastName);

    undoRedoHandler.undo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);

    Person undonePerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(undonePerson.getPersonID(), personID);
    assertEquals(undonePerson.getFirstName(), firstName);
    assertEquals(undonePerson.getLastName(), lastName);
  }

  @Test
  public void testPersonEditRedo() throws Exception {
    assertTrue(mainApp.getPeople().isEmpty());
    assertTrue(undoRedoHandler.getUndoStack().empty());
    assertTrue(undoRedoHandler.getRedoStack().empty());

    newPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    Person createdPerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(createdPerson.getPersonID(), personID);
    assertEquals(createdPerson.getFirstName(), firstName);
    assertEquals(createdPerson.getLastName(), lastName);

    editNewestPerson();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    Person editedPerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(editedPerson.getPersonID(), newPersonID);
    assertEquals(editedPerson.getFirstName(), newFirstName);
    assertEquals(editedPerson.getLastName(), newLastName);

    undoRedoHandler.undo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 1);
    assertEquals(undoRedoHandler.getRedoStack().size(), 1);

    Person undonePerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(undonePerson.getPersonID(), personID);
    assertEquals(undonePerson.getFirstName(), firstName);
    assertEquals(undonePerson.getLastName(), lastName);

    undoRedoHandler.redo();
    assertEquals(mainApp.getPeople().size(), 1);
    assertEquals(undoRedoHandler.getUndoStack().size(), 2);
    assertTrue(undoRedoHandler.getRedoStack().empty());

    Person redonePerson = mainApp.getPeople().get(mainApp.getPeople().size() - 1);
    assertEquals(redonePerson.getPersonID(), newPersonID);
    assertEquals(redonePerson.getFirstName(), newFirstName);
    assertEquals(redonePerson.getLastName(), newLastName);
  }
}
