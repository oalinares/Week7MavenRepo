package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;
import projects.dao.DbConnection;
import projects.entity.Project;

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	
	/**
	 * Method used to insert a project, and values associated with the project.
	 * @param project
	 * @return Returns a project with the values inserted by the user, and the project's ID
	 * is set by your schema's code.
	 */
	public Project insertProject(Project project) {
		// @formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				project.setProjectId(projectId);
				return project;
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch(SQLException e) {
			throw new DbException(e);
		}
	}
	/**
	 * No specification of ID other than being ordered by the name and selecting *, this method returns all projects created.
	 * @return Returns all projects.
	 */
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				try(ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();
					
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					
					return projects;
					
				}
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
	/**
	 * Method retrieves a single project based on the project's Id
	 * @param projectId
	 * @return Returns the project with details about it based on the ID.
	 */
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try {
				Project project = null;
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);
				
			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
					project = extract(rs, Project.class);
				}
			}
		}
			if(Objects.nonNull(project)) {
				project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
				project.getSteps().addAll(fetchStepsForProject(conn, projectId));
				project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
			}
			commitTransaction(conn);
			return Optional.ofNullable(project);
			
		} catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
		
	}
		catch(SQLException e) {
			throw new DbException(e);
		}
		}
	/**
	 * Method used to fetch the categories associated with the project through the project's ID by selecting all from the categories' table
	 * and joining with project category table to establish a relation between projectId and categoryId
	 * @param conn
	 * @param projectId
	 * @return Returns a List of categories associated with the project.
	 * @throws SQLException
	 */
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+"JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+"WHERE project_id = ?";
		//@formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
		try(ResultSet rs = stmt.executeQuery()) {
			List<Category> categories = new LinkedList<>();
			
			while(rs.next()) {
				categories.add(extract(rs, Category.class));
			}
			return categories;
			}
		}
	}
	/**
	 * Method used to fetch the steps needed for the project through the WHERE clause of the project's ID, and establishing a connection.
	 * @param conn
	 * @param projectId
	 * @return Returns a List of steps needed for the project
	 * @throws SQLException
	 */
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
				
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		}
	}
	/**
	 * Method used to fetch the materials needed for the project through the WHERE clause of the project's ID, and establishing a connection.
	 * @param conn
	 * @param projectId
	 * @return Returns a List of materials needed for the project
	 * @throws SQLException
	 */
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
		try(ResultSet rs = stmt.executeQuery()) {
			List<Material> materials = new LinkedList<>();
			
			while(rs.next()) {
				materials.add(extract(rs, Material.class));
			}
			return materials;
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
	public boolean modifyProjectDetails(Project project) {
		//@formatter:off
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		//@formatter:on
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, project.getProjectName(), String.class);
			setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
			setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
			setParameter(stmt, 4, project.getDifficulty(), Integer.class);
			setParameter(stmt, 5, project.getNotes(), String.class);
			setParameter(stmt, 6, project.getProjectId(), Integer.class);
			
			boolean updated = stmt.executeUpdate() == 1;
			commitTransaction(conn);
			
			return updated;
			
			
		} catch (Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
	} catch (SQLException e) {
		throw new DbException(e);
	}
}
	/**
	 * When this option is selected in the menu, it will display a list of the projects that have been created,
	 * and will ask for the projectId of the project that you want to delete.
	 * @param projectId
	 * @return The method should return '1' if the projectId selected has been deleted, else it should throw an exception if '0' is returned.
	 */
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection())	{
			startTransaction(conn);
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			boolean deleted = stmt.executeUpdate() == 1;
			
			commitTransaction(conn);
			
			return deleted;
			
		} catch (Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}
}