package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.dao.DbConnection;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
		private Scanner scanner = new Scanner(System.in);
		private ProjectService projectService = new ProjectService();
		private Project curProject;
	// @formatter:off
		private List<String> operations = List.of(
				"1) Add a project",
				"2) List projects",
				"3) Select a project",
				"4) Update project details",
				"5) Delete a project"
		);
		// @formatter:on

	public static void main(String[] args) {
		//	DbConnection.getConnection();
			new ProjectsApp().processUserSelections();
	}
		private void processUserSelections() {
			boolean done = false;
			
			while(!done) {
				try {
					int selection = getUserSelection();
					
					switch(selection) {
					case -1:
						done = exitMenu();
						break;
					case 1:
						createProject();
						break;
					case 2:
						listProjects();
						break;
					case 3:
						selectProject();
						break;
					case 4:
						updateProjectDetails();
						break;
					case 5:
						deleteProject();
						break;
					default:
						System.out.println("\n" + selection + " is not a valid selection. Try again.");
					}
				} catch (Exception e) {
					System.out.println("\nError: " + e + " Try again.");
					
				}
			}
		}
		/**
		 * When this option is selected in the menu, it will display a list of the projects that have been created,
		 * and will ask for the projectId of the project that you want to delete.
		 * @param projectId
		 * @return The method should return '1' if the projectId selected has been deleted, else it should throw an exception if '0' is returned.
		 */
		private void deleteProject() {
			listProjects();
			
			Integer projectId = getIntInput("Enter the project ID you want to delete.");
			
			if(Objects.nonNull(projectId)) {
				projectService.deleteProject(projectId);
				
				System.out.println("You have deleted project " + projectId);
				
			if(Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
				curProject = null;
			}
			}
		}
		/**
		 * This method will currently run if prior the user has selected a project, which enables for the project 
		 * to updated by using the curProject's projectId to get and set the same ID, but with new data in each
		 * parameter set by the user.
		 * @param project
		 * @return Returns the entire project with the new data that has been entered by the user, except Materials, Steps, and Categories,
		 * which currently are not being altered through this method.
		 */
		private void updateProjectDetails() {
			if(Objects.isNull(curProject)) {
				System.out.println("\nPlease select a project.");
			return;
			}
			String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
			BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
			BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");
			Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
			String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");
			
			Project project = new Project();
			
			project.setProjectId(curProject.getProjectId());
			project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
			project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
			project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
			project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
			project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
			
			projectService.modifyProjectDetails(project);
			curProject = projectService.fetchProjectById(curProject.getProjectId());
		}
		/**
		 * Method retrieves a single project based on the project's Id
		 * @param projectId
		 * @return Returns the project with details about it based on the ID.
		 */
		private void selectProject() {
			listProjects();
			
			Integer projectId = getIntInput("Enter a project ID to select a project");
			curProject = null;
			curProject = projectService.fetchProjectById(projectId);
		}
		/*
		 * The listProjects() will list all the projects that have been created by the user and/or already stored in the database.
		 * The method should print the project's ID and name.
		 */
		private void listProjects() {
			List<Project> projects = projectService.fetchAllProjects();
			
			System.out.println("\nProjects:");
			
			projects.forEach(project -> 
			System.out.println("   " + project.getProjectId() 
			+ ": " + project.getProjectName()));
		}
		/**
		 * Method used to insert a project, and values associated with the project.
		 * @param project
		 * @return Returns a project with the values inserted by the user, and the project's ID
		 * is set by your schema's code.
		 */
		private void createProject() {
			String projectName = getStringInput("Enter the project name");
			BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
			BigDecimal actualHours = getDecimalInput("Enter the actual hours");
			Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
			String notes = getStringInput("Enter the project notes");
			
			Project project = new Project();
			
			project.setProjectName(projectName);
			project.setEstimatedHours(estimatedHours);
			project.setActualHours(actualHours);
			project.setDifficulty(difficulty);
			project.setNotes(notes);
			
			Project dbProject = projectService.addProject(project);
			System.out.println("You added this project:\n" + dbProject);
			
			//curProject = projectService.fetchProjectById(dbProject.getRecipeId());
			
		}
		private BigDecimal getDecimalInput(String prompt) {
			String input = getStringInput(prompt);
			if(Objects.isNull(input)) {
				return null;
			}
			try {
				return new BigDecimal(input).setScale(2);
			} catch(NumberFormatException e) {
				throw new DbException(input + " is not a valid decimal number.");
			}
		
		}
		/**
		 * Exits the menu.
		 * @return Prompts that you have exited the menu.
		 */
		private boolean exitMenu() {
			System.out.println("\nExiting the menu. Goodbye!");
			return true;
		}
		/**
		 * Requests the user's selection from the menu
		 * @return Return's the user's selection from the menu, or exits.
		 */
		private int getUserSelection() {
			printOperations();
			Integer input = getIntInput("Enter a menu selection");
			return Objects.isNull(input) ? -1 : input;
		}
		private Integer getIntInput(String prompt) {
			String input = getStringInput(prompt);
			if(Objects.isNull(input)) {
				return null;
			}
			try {
				return Integer.valueOf(input);
			} catch(NumberFormatException e) {
				throw new DbException(input + " is not a valid number.");
			}
		}
		private String getStringInput(String prompt) {
			System.out.print(prompt + ": ");
			String input = scanner.nextLine();
			return input.isBlank() ? null : input.trim();
		}
		private void printOperations() {
			System.out.println("\nThese are the available selections. Press the Enter key to quit: ");
			operations.forEach(line -> System.out.println(" " + line));
			
			if(Objects.isNull(curProject)) {
				System.out.println("\nYou are not working with a project.");
			} else {
				System.out.println("\nYou are working with project: " + curProject);
			}
		}

}
