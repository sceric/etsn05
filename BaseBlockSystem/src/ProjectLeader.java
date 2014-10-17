import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Random;

/**
 * Servlet implementation class Administration. Constructs a page for
 * administration purpose. Checks first if the user is logged in and then if it
 * is the administrator. If that is OK it displays all users and a form for
 * adding new users.
 * 
 * @author Martin Host
 * @version 1.0
 */

@WebServlet("/ProjectLeader")
public class ProjectLeader extends servletBase {
	private static final long serialVersionUID = 1L;

	/**
	 * @see servletBase#servletBase()
	 */
	public ProjectLeader() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		access.updateLog(null, null);
		PrintWriter out = response.getWriter();
		out.println(getPageIntro());
		out.println(printMainMenu());
		String myName = "";
		HttpSession session = request.getSession(true);
		// WHICH OF THESE NEED TO BE FETCHED?
		//String groupIDString = request.getParameter("groupID");
		Object nameObj = session.getAttribute("name");
		Object groupObj = session.getAttribute("groupID");
		int groupID = Integer.parseInt((String) groupObj);
		String newGroupIDStr = request.getParameter("groupID");
		if (newGroupIDStr != null) {
			int newGroupID = Integer.parseInt(newGroupIDStr);
			groupID = newGroupID;
			session.setAttribute("groupID", newGroupIDStr);
		}
		String role = request.getParameter("role");
		String username = request.getParameter("changename");
		System.out.println(role+ "doPost");

		if (nameObj != null) {
			myName = (String) nameObj; // if the name exists typecast the name
										// to a string
		}
		boolean isAllowed = projectLeaderOrAdmin(myName);

		if (!loggedIn(request)) { //Check that user is logged in
			response.sendRedirect("LogIn");
		} else if (isAllowed) { //Check that user is allowed
			out.println("<h1>Project Management " + "</h1>");
			if (username != null) {
				if (myName.equals("admin")) {
					listAllGroups(out);
				}
				try {
					Statement stmt1 = conn.createStatement();		    
					ResultSet rs1 = stmt1.executeQuery("select * from user_group INNER JOIN users on user_group.user_id = users.id where user_group.group_id = "+groupID+" and users.username = '"+username+"'");
					rs1.first();
					int userIDint = rs1.getInt("user_group.ID");
					if(!changeRole(userIDint, role)) {
						out.println("<p>User role was not changed, likely due to too many users having that role</p>");
					}
					showAllUsers(groupID, out);
				} catch (SQLException ex) {
					System.out.println("doPost");
				    System.out.println("SQLException: " + ex.getMessage());
				    System.out.println("SQLState: " + ex.getSQLState());
				    System.out.println("VendorError: " + ex.getErrorCode());
				}
			} else if (myName.equals("admin")){
				//SHOW LIST OF GROUPS AND USERS
				listAllGroups(out);
				showAllUsers(groupID, out);
			} else {
				showAllUsers(groupID, out);
			}
		} else {
			out.println("<p>You do not have access to this page</p>");
		}
	}

	private void listAllGroups(PrintWriter out) {
		try {
			Statement stmt = conn.createStatement();		    
		    ResultSet rs = stmt.executeQuery("select * from groups order by name asc");
		    out.println("<p>Project groups:</p>");
		    out.println("<table border=" + formElement("1") + ">");
		    out.println("<tr><td>NAME</td><td>Projectleader 1</td><td>Projectleader 2</td><td></td></tr>");
		    while (rs.next( )) {
		    	String name = rs.getString("name");
		    	//Hämta projektledarnas namn
		    	int groupID = 0;
		    	Statement stmt1 = conn.createStatement();		    
				ResultSet rs1 = stmt1.executeQuery("select * from groups where name='" + name +"'");
				if (rs1.first()) {
					groupID = rs1.getInt("id");
				}
				stmt1.close();
		    	String editURL = "ProjectLeader?groupID="+groupID;//osäker på denna raden
		    	String editCode = "<a href=" + formElement(editURL) +
		    			            " onclick="+formElement("return confirm('Are you sure you want to edit "+name+"?')") + 
		    			            "> edit </a>";
		    	if (name.equals("admin")) 
		    		editCode = "";
		    	out.println("<tr>");
		    	out.println("<td>" + name + "</td>");
		    	out.println("<td>" + " " + "</td>");
		    	out.println("<td>" + " " + "</td>");
		    	out.println("<td>" + editCode + "</td>");
		    	out.println("</tr>");
		    }
		    out.println("</table>");
		    stmt.close();
		} catch (SQLException ex) {
			System.out.println("listAllGroups");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		//out.println(addProjectForm());
		
	}

	/**
	 * Checks if the user is either an admin or a Project Leader of the group
	 * 
	 * @param myName
	 * @return true if the user is admin/project leader, else false
	 */
	private boolean projectLeaderOrAdmin(String myName) {
		if (myName.equals("admin")) {
			return true;
		} else {
			try {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt
						.executeQuery("select * from user_group INNER JOIN users on user_group.user_id = users.ID where users.username = '"
								+ myName + "'");
				if (rs.first()) {
					String role = rs.getString("role");
					if (role.equals("Project Leader")) {
						return true;
					}
				}
			} catch (SQLException ex) {
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
		return false;
	}

	/**
	 * Displays a list of all the users in a group, their role and a button to
	 * change the role
	 * 
	 * @param ID
	 *            group ID
	 * @param out
	 *            PrintWriter
	 */
	
	private void showAllUsersOld(int groupID, PrintWriter out) {
		try {
			Statement s = conn.createStatement();		    
			ResultSet r = s.executeQuery("select * from groups where ID = "+groupID);
			String groupName = "";
			if (r.first()) {
				groupName = r.getString("name");
			}
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select * from user_group INNER JOIN users on user_group.user_id = users.ID where user_group.group_id = "
							+ groupID + "");
			if (groupName.equals("")) {
				out.println("<p><b>Choose a group to edit</b></p>");
			} else {
				out.println("<p><b>Users in "+groupName+"</b></p>");
				out.println("<table border=" + formElement("1") + ">");
				out.println("<tr><td>NAME</td><td>ROLE</td><td>CHANGE TO PG</td><td>CHANGE TO T1</td><td>CHANGE TO T2</td><td>CHANGE TO T3</td>");
				while (rs.next( )) {

					//THE FOLLOWING CODE IS UGLY AND WILL BE CHANGED
					String name = rs.getString("username");
					String role = rs.getString("role");
					String addPG = "ProjectLeader?changename="+name+"&role=" + PROJECT_LEADER;
					String addT1 = "ProjectLeader?changename="+name+"&role=" + t1;
					String addT2 = "ProjectLeader?changename="+name+"&role=" + t2;
					String addT3 = "ProjectLeader?changename="+name+"&role=" + t3;
					String addCodePG = "<a href=" + formElement(addPG) +
							" onclick="+formElement("return confirm('Are you sure you want to change role of "+name+"?')") + 
							"> add </a>";
					String addCodeT1 = "<a href=" + formElement(addT1) +
							" onclick="+formElement("return confirm('Are you sure you want to change role of "+name+"?')") + 
							"> add </a>";
					String addCodeT2 = "<a href=" + formElement(addT2) +
							" onclick="+formElement("return confirm('Are you sure you want to change role of "+name+"?')") + 
							"> add </a>";
					String addCodeT3 = "<a href=" + formElement(addT3) +
							" onclick="+formElement("return confirm('Are you sure you want to change role of "+name+"?')") + 
							"> add </a>";
					out.println("<tr>");
					out.println("<td>" + name + "</td>");
					out.println("<td>" + role + "</td>");
					out.println("<td>" + addCodePG + "</td>");
					out.println("<td>" + addCodeT1 + "</td>");
					out.println("<td>" + addCodeT2 + "</td>");
					out.println("<td>" + addCodeT3 + "</td>");
					out.println("</tr>");

					//UGLY CODE END
				}
			}
			out.println("</table>");
			stmt.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	private void showAllUsers(int groupID, PrintWriter out) {
		try {
			Statement s = conn.createStatement();		    
			ResultSet r = s.executeQuery("select * from groups where ID = "+groupID);
			String groupName = "";
			if (r.first()) {
				groupName = r.getString("name");
			}
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select * from user_group INNER JOIN users on user_group.user_id = users.ID where user_group.group_id = "
							+ groupID + "");
			if (groupName.equals("")) {
				out.println("<p><b>Choose a group to edit</b></p>");
			} else {
				out.println("<p><b>Users in "+groupName+"</b></p>");
				out.println("<table border=" + formElement("1") + ">");
				out.println("<tr><td>NAME</td><td>ROLE</td><td>Select user</td>");
				out.println("<p> <form name=" + formElement("input") + " method=" + formElement("post"));
				out.println("</tr>");
				while (rs.next( )) {

					String name = rs.getString("username");
					String role = rs.getString("role");
					out.println("<tr>");
					out.println("<td>" + name + "</td>");
					out.println("<td>" + role + "</td>");
					out.println("<td>" + "<input type=" + formElement("radio") + " name=" + formElement("changename") +
							" value=" + formElement(name) +"></td>");		//radiobutton
					out.println("</tr>");
				}
				out.println("</table>");
				out.println(selectRoleList());
				out.println("<p><input type=" + formElement("submit") + "value=" + formElement("Change role") + '>');
				out.println("</form>");
				stmt.close();
			}
		} catch (SQLException ex) {
			System.out.println("showAllUsers");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	private String selectRoleList(){
		String html = "";
		html += "<br><select name='role'>";
		html += "<option value='0' selected='true'>Select a role</option>";
		html += "<option value=" + PROJECT_LEADER + ">"
				+ PROJECT_LEADER + "</option>";
		html += "<option value=" + t1 + ">"
				+ t1 + "</option>";
		html += "<option value=" + t2 + ">"
				+ t2 + "</option>";
		html += "<option value=" + t3 + ">"
				+ t3 + "</option>";
		html += "</select>";
		return html;
	}

	/**
	 * Changes the role of a user in the group
	 * 
	 * @param ID
	 *            the user to be changed
	 * @param role
	 *            the new role for the user
	 */
	private boolean changeRole(int userGroupID, String role) {
		boolean changeOK = false;
		
		try {
			if (role.equals("Project")) {
				role = PROJECT_LEADER;
			}
			Statement stmt2 = conn.createStatement();
			ResultSet rs2 = stmt2.executeQuery("select * from user_group where ID = "+userGroupID);
			rs2.first();
			int groupID = rs2.getInt("group_id");
			String currentRole = rs2.getString("role");
			System.out.println(role+ "in changeRole");
			Statement stmt1 = conn.createStatement();
			ResultSet rs1 = stmt1.executeQuery("select * from user_group where group_id = "+groupID);
			int roleCount = 0;
			int plCount = 0;
			while (rs1.next()) {
				if (rs1.getString("role").equals(PROJECT_LEADER)) {
					plCount++;
				} else {
				roleCount++;
				}
			}
			if (currentRole.equals(PROJECT_LEADER) && plCount == 1) {
				return false;
			} else {
				if ((role.equals(PROJECT_LEADER) && plCount < 2) || ((!role.equals(PROJECT_LEADER)) && roleCount < 6)) {
					Statement stmt = conn.createStatement();
					String statement = "Update user_group SET role='"+role+"' where ID=" + userGroupID; 
					stmt.executeUpdate(statement);
					changeOK = true;
				}
			}
			
		} catch (SQLException ex) {
			System.out.println("changeRole");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return changeOK;
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}