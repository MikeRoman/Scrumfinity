package seng302.group5.model.util;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.group5.Main;
import seng302.group5.model.AgileHistory;
import seng302.group5.model.AgileItem;
import seng302.group5.model.Backlog;
import seng302.group5.model.Person;
import seng302.group5.model.Project;
import seng302.group5.model.Release;
import seng302.group5.model.Role;
import seng302.group5.model.Skill;
import seng302.group5.model.Story;
import seng302.group5.model.Team;


/**
 * Created by Michael + Craig on 5/5/2015. A class that reads the data from the application and
 * saves it in a human readable format.
 */
public class ReportWriter {

  private Document report;
  private Element rootElement;
  Element projElem;
  Element projElement;
  Element releasesElement;
  Element teamElement;
  Element membersElement;
  Element skillElement;
  Element orphanTeam;
  Element orphanPeople;
  Element unusedSkills;
  Element allStories;
  Element allBacklogs;
  LocalDate date;
  String dateFormat = "dd/MM/yyyy";

  private Main mainApp;
  ObservableList<Skill> skills = FXCollections.observableArrayList();
  ObservableList<Team> teams = FXCollections.observableArrayList();
  ObservableList<Story> stories = FXCollections.observableArrayList();
  ObservableList<Project> projectItems = FXCollections.observableArrayList();
  ObservableList<Person> peoples = FXCollections.observableArrayList();
  ObservableList<Release> releases = FXCollections.observableArrayList();
  ObservableList<Backlog> backlogs = FXCollections.observableArrayList();

  ObservableList<Team> orphanTeamsList = FXCollections.observableArrayList();
  ObservableList<Skill> unassignedSkills = FXCollections.observableArrayList();
  ObservableList<Story> unassignedStories = FXCollections.observableArrayList();

  /**
   * Creates a report based on data currently stored in the main application memory. Uses XML
   * format, no pretty print.
   *
   * @param mainApp      The currently opened main application
   * @param saveLocation Where the report is saved to
   */
  public void writeReport(Main mainApp, File saveLocation) {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      unassignedSkills.setAll(mainApp.getSkills());
      report = docBuilder.newDocument();
      date = LocalDate.now();
      String datesString = date.format(
          DateTimeFormatter.ofPattern(dateFormat));

      //header = report.createTextNode(headerText);
      rootElement = report.createElement("Header");
      String orgName;
      if (!Settings.organizationName.isEmpty()) {
        orgName = Settings.organizationName;
      } else {
        orgName = "__undefined__";
      }
      rootElement.setAttribute("Label", "Report created on " + datesString + " for " +
                                        orgName);
      report.appendChild(rootElement);

      projElement = report.createElement("Projects");
      rootElement.appendChild(projElement);
      for (Project project : mainApp.getProjects()) {
        orphanTeamsList.setAll(mainApp.getTeams());

        projElem = report.createElement("Project");
        projElement.appendChild(projElem);
        projElem.setAttribute("label", project.getLabel());

        Element projName = report.createElement("Name");
        projName.appendChild(report.createTextNode(project.getProjectName()));
        projElem.appendChild(projName);

        Element projDesc = report.createElement("Description");
        if (project.getProjectDescription() != null && !project.getProjectDescription().isEmpty()) {
          projDesc.appendChild(report.createTextNode(project.getProjectDescription()));
        }
        projElem.appendChild(projDesc);

        releasesElement = report.createElement("Releases");
        projElem.appendChild(releasesElement);

        for (Release release : mainApp.getReleases()) {
          if (release.getProjectRelease().getLabel().equals(project.getLabel())) {
            createReleaseChild(release, releasesElement);
          }
        }
        teamElement = report.createElement("Teams");
        projElem.appendChild(teamElement);
        for (AgileHistory team : project.getAllocatedTeams()) {
          if (orphanTeamsList.contains(team.getAgileItem())) {
            orphanTeamsList.remove(team.getAgileItem());
          }
          createTeamChild(mainApp, team);
        }

      }

      orphanTeam = report.createElement("UnassignedTeams");
      rootElement.appendChild(orphanTeam);
      for (Team team : orphanTeamsList) {
        createOrphanTeam(team);
      }

      orphanPeople = report.createElement("UnassignedPeople");
      rootElement.appendChild(orphanPeople);
      for (Person person : mainApp.getPeople()) {
        if (!person.isInTeam()) {
          createOrphanPeople(person);
        }
      }

      unusedSkills = report.createElement("UnassignedSkills");
      rootElement.appendChild(unusedSkills);
      for (Skill skill : unassignedSkills) {
        createSkillChild(skill, unusedSkills);
      }

      allStories = report.createElement("Stories");
      rootElement.appendChild(allStories);
      for (Story story : unassignedStories) {
        createStoryChild(story);
      }

      allBacklogs = report.createElement("Backlogs");
      rootElement.appendChild(allBacklogs);
      for (Backlog backlog : mainApp.getBacklogs()) {
        createBacklogChild(backlog);
      }

      String filename = saveLocation.toString();
      if (!filename.endsWith(".xml")) {
        filename = filename + ".xml";
      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(report);
      StreamResult result = new StreamResult(filename);

      transformer.transform(source, result);
//      System.out.println("Report Created");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the release child element that contains the releases information, formats it under
   * relevant tags as a child of the releaseElement.
   *
   * @param release         the release that information will be displayed for
   * @param releasesElement the parent of the release, i.e. displayed as a child of project or of
   *                        all releases.
   */
  public void createReleaseChild(Release release, Element releasesElement) {
    Element releaseElem = report.createElement("Release");
    releasesElement.appendChild(releaseElem);
    releaseElem.setAttribute("label", release.getLabel());

    Element releaseDesc = report.createElement("Description");
    releaseDesc.appendChild(report.createTextNode(release.getReleaseDescription()));
    releaseElem.appendChild(releaseDesc);

    Element releaseNotes = report.createElement("Notes");
    releaseNotes.appendChild(report.createTextNode(release.getReleaseNotes()));
    releaseElem.appendChild(releaseNotes);

    String releaseDateString = release.getReleaseDate().format(
        DateTimeFormatter.ofPattern(dateFormat));
    Element releaseDate = report.createElement("ReleaseDate");
    releaseDate.appendChild(report.createTextNode(releaseDateString));
    releaseElem.appendChild(releaseDate);

  }


  /**
   * Create a team child element that contains the teams information, formats it under the project
   * that the team is assigned to.
   *
   * @param mainApp passed so that it can get a list of all teams collect the member info for the
   *                correct team
   * @param team    the team who's information is to be displayed.
   */
  public void createTeamChild(Main mainApp, AgileHistory team) {

    Element teamElem = report.createElement("Team");
    teamElement.appendChild(teamElem);
    teamElem.setAttribute("label", team.getAgileItem().getLabel());

    String theString = team.getStartDate().format(
        DateTimeFormatter.ofPattern(dateFormat));
    Element teamStartDate = report.createElement("StartDate");
    teamStartDate.appendChild(report.createTextNode(theString));
    teamElem.appendChild(teamStartDate);

    String endDate;
    if (team.getEndDate() != null) {
      endDate = team.getEndDate().format(
          DateTimeFormatter.ofPattern(dateFormat));
    } else {
      endDate = "No end Date";
    }

    Element teamEndDate = report.createElement("EndDate");
    teamEndDate.appendChild(report.createTextNode(endDate));
    teamElem.appendChild(teamEndDate);

    membersElement = report.createElement("Members");
    teamElem.appendChild(membersElement);
    for (Team listTeam : mainApp.getTeams()) {
      if (team.getAgileItem().getLabel().equals(listTeam.getLabel())) {
        createPersonChild(listTeam);
      }
    }
  }

  /**
   * Create a person element that contains the person information, formats it under the assigned
   * team's tag
   *
   * @param listTeam The team which will be used to collect the list of members to get there info.
   */
  public void createPersonChild(Team listTeam) {
    for (Person member : listTeam.getTeamMembers()) {
      Element memberElem = report.createElement("Member");
      membersElement.appendChild(memberElem);
      memberElem.setAttribute("label", member.getLabel());

      Element teamMemberName = report.createElement("FirstName");
      teamMemberName.appendChild(report.createTextNode(member.getFirstName()));
      memberElem.appendChild(teamMemberName);

      Element teamMemberLastName = report.createElement("LastName");
      teamMemberLastName.appendChild(report.createTextNode(member.getLastName()));
      memberElem.appendChild(teamMemberLastName);

      Role role = listTeam.getMembersRole().get(member);
      Element teamMemberRole = report.createElement("Role");
      if (role != null) {
        teamMemberRole.appendChild(report.createTextNode(role.toString()));
      } else {
        teamMemberRole.appendChild(report.createTextNode("No role"));
      }
      memberElem.appendChild(teamMemberRole);

      skillElement = report.createElement("Skills");
      memberElem.appendChild(skillElement);

      for (Skill skill : member.getSkillSet()) {
        if (unassignedSkills.contains(skill)) {
          unassignedSkills.remove(skill);
        }
        Element skillElem = report.createElement("Skill");
        skillElement.appendChild(skillElem);
        skillElem.setAttribute("label", skill.getLabel());

        Element skillDescription = report.createElement("Description");
        skillDescription.appendChild(report.createTextNode(skill.getSkillDescription()));
        skillElem.appendChild(skillDescription);
      }
    }
  }

  /**
   * Create a orphan team element i.e. a team that is not assigned to a project. Will be displayed
   * under the unnasigned teams tag.
   *
   * @param team The team which will be used to collect the list of members to get there info.
   */
  public void createOrphanTeam(Team team) {

    Element orphanTeamElem = report.createElement("Team");
    orphanTeamElem.setAttribute("label", team.getLabel());
    orphanTeam.appendChild(orphanTeamElem);

    for (Person person : team.getTeamMembers()) {
      Element memberElem = report.createElement("Member");
      orphanTeamElem.appendChild(memberElem);
      memberElem.setAttribute("label", person.getLabel());

      Element teamMemberName = report.createElement("FirstName");
      teamMemberName.appendChild(report.createTextNode(person.getFirstName()));
      memberElem.appendChild(teamMemberName);

      Element teamMemberLastName = report.createElement("LastName");
      teamMemberLastName.appendChild(report.createTextNode(person.getLastName()));
      memberElem.appendChild(teamMemberLastName);

      Role role = team.getMembersRole().get(person);
      Element teamMemberRole = report.createElement("Role");
      if (role != null) {
        teamMemberRole.appendChild(report.createTextNode(role.toString()));
      } else {
        teamMemberRole.appendChild(report.createTextNode("No role"));
      }
      memberElem.appendChild(teamMemberRole);

      skillElement = report.createElement("Skills");
      memberElem.appendChild(skillElement);
      for (Skill skill : person.getSkillSet()) {
        Element skillElem = report.createElement("Skill");
        skillElement.appendChild(skillElem);
        skillElem.setAttribute("label", skill.getLabel());

        Element skillDescription = report.createElement("Description");
        skillDescription.appendChild(report.createTextNode(skill.getSkillDescription()));
        skillElem.appendChild(skillDescription);
      }
    }
  }

  /**
   * Create an orphan person element that contains the information for a person who is not assigned
   * to any team. Will be displayed under the unassigned people tag.
   *
   * @param person The person who's information is to be displayed.
   */
  public void createOrphanPeople(Person person) {

    Element orphanPersonElem = report.createElement("Person");
    orphanPersonElem.setAttribute("label", person.getLabel());
    orphanPeople.appendChild(orphanPersonElem);

    Element teamMemberName = report.createElement("FirstName");
    teamMemberName.appendChild(report.createTextNode(person.getFirstName()));
    orphanPersonElem.appendChild(teamMemberName);

    Element teamMemberLastName = report.createElement("LastName");
    teamMemberLastName.appendChild(report.createTextNode(person.getLastName()));
    orphanPersonElem.appendChild(teamMemberLastName);

    skillElement = report.createElement("Skills");
    orphanPersonElem.appendChild(skillElement);
    for (Skill skill : person.getSkillSet()) {
      if (unassignedSkills.contains(skill)) {
        unassignedSkills.remove(skill);
      }
      Element skillElem = report.createElement("Skill");
      skillElement.appendChild(skillElem);
      skillElem.setAttribute("label", skill.getLabel());

      Element skillDescription = report.createElement("Description");
      skillDescription.appendChild(report.createTextNode(skill.getSkillDescription()));
      skillElem.appendChild(skillDescription);
    }
  }

  /**
   * Create a skill element that displays the information for a skill, the skill element can be
   * displayed under a person or under the list of all skills tag.
   *
   * @param skill The skill who's information will be displayed.
   */
  public void createSkillChild(Skill skill, Element allSkills) {
    Element skillElem = report.createElement("Skill");
    allSkills.appendChild(skillElem);
    skillElem.setAttribute("label", skill.getLabel());

    Element skillDescription = report.createElement("Description");
    skillDescription.appendChild(report.createTextNode(skill.getSkillDescription()));
    skillElem.appendChild(skillDescription);

  }

  /**
   * Create a story element that displays the story's information underneath the stories tag.
   *
   * @param story the story who's information will be displayed.
   */
  public void createStoryChild(Story story) {
    Element storyElem = report.createElement("Story");
    allStories.appendChild(storyElem);
    storyElem.setAttribute("label", story.getLabel());

    Element storyName = report.createElement("Name");
    storyName.appendChild(report.createTextNode(story.getStoryName()));
    storyElem.appendChild(storyName);

    Element storyDescription = report.createElement("Description");
    storyDescription.appendChild(report.createTextNode(story.getDescription()));
    storyElem.appendChild(storyDescription);

    Element storyCreator = report.createElement("Creator");
    storyCreator.appendChild(report.createTextNode(story.getCreator().getLabel()));
    storyElem.appendChild(storyCreator);
  }

  /**
   * Creates a backlog element and displays the backlog information under backlogs tag.
   * @param backlog all the backlogs saved in main
   */
  public void createBacklogChild(Backlog backlog) {
    Element backlogElem = report.createElement("Backlog");
    allBacklogs.appendChild(backlogElem);
    backlogElem.setAttribute("label", backlog.getLabel());

    Element backlogName = report.createElement("Name");
    backlogName.appendChild(report.createTextNode(backlog.getBacklogName()));
    backlogElem.appendChild(backlogName);

    Element backlogDescription = report.createElement("Description");
    backlogDescription.appendChild(report.createTextNode(backlog.getBacklogDescription()));
    backlogElem.appendChild(backlogDescription);

    createBacklogPerson(backlog.getProductOwner(), backlogElem, "ProductOwner");

    Element backlogStories = report.createElement("Stories");
    for (Story story : backlog.getStories()) {
      if (!unassignedStories.contains(story)) {
        unassignedStories.add(story);
      }
        createBacklogStory(story, backlogStories, "Story");
      }

    }

  /**
   * Gets all person fields and puts them into the backlogOwner element for creating backlogs
   * @param person The porduct owner of the backlog
   * @param backlogOwner The element "Product Owner" in the backlog
   */
  public void createBacklogPerson(Person person, Element backlogOwner, String typeOfPerson) {
    Element productOwner = report.createElement(typeOfPerson);
    productOwner.setAttribute("label", person.getLabel());
    backlogOwner.appendChild(productOwner);

    Element teamMemberName = report.createElement("FirstName");
    teamMemberName.appendChild(report.createTextNode(person.getFirstName()));
    backlogOwner.appendChild(teamMemberName);

    Element teamMemberLastName = report.createElement("LastName");
    teamMemberLastName.appendChild(report.createTextNode(person.getLastName()));
    backlogOwner.appendChild(teamMemberLastName);

    skillElement = report.createElement("Skills");
    backlogOwner.appendChild(skillElement);
    for (Skill skill : person.getSkillSet()) {
      Element skillElem = report.createElement("Skill");
      skillElement.appendChild(skillElem);
      skillElem.setAttribute("label", skill.getLabel());

      Element skillDescription = report.createElement("Description");
      skillDescription.appendChild(report.createTextNode(skill.getSkillDescription()));
      skillElem.appendChild(skillDescription);
    }
  }

  public void createBacklogStory(Story story, Element backlogStory, String typeOfStory) {
    Element storyElem = report.createElement(typeOfStory);
    backlogStory.appendChild(storyElem);
    storyElem.setAttribute("label", story.getLabel());

    Element storyName = report.createElement("Name");
    storyName.appendChild(report.createTextNode(story.getStoryName()));
    storyElem.appendChild(storyName);

    Element storyDescription = report.createElement("Description");
    storyDescription.appendChild(report.createTextNode(story.getDescription()));
    storyElem.appendChild(storyDescription);

    Element storyCreator = report.createElement("Creator");
    storyCreator.appendChild(report.createTextNode(story.getCreator().getLabel()));
    storyElem.appendChild(storyCreator);
  }

  public void setLists() {
    skills.setAll(mainApp.getSkills());
    teams.setAll(mainApp.getTeams());
    projectItems.setAll(mainApp.getProjects());
    releases.setAll(mainApp.getReleases());
    peoples.setAll(mainApp.getPeople());
    stories.setAll(mainApp.getStories());
    backlogs.setAll(mainApp.getBacklogs());
  }

  public void removeItem(ObservableList<AgileItem> list, AgileItem item) {
    list.remove(item);
  }

  public void addItem(ObservableList<AgileItem> list, AgileItem item) {
    list.add(item);
  }

  public ObservableList<Skill> getSkills() {
    return skills;
  }

  public ObservableList<Team> getTeams() {
    return teams;
  }

  public ObservableList<Story> getStories() {
    return stories;
  }

  public ObservableList<Project> getProjectItems() {
    return projectItems;
  }

  public ObservableList<Person> getPeoples() {
    return peoples;
  }

  public ObservableList<Release> getReleases() {
    return releases;
  }
}
