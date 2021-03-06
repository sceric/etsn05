import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@WebServlet("/ReportHandling")
public class ReportHandling extends servletBase{
	
	private PrintWriter out;
	private String sort;
	private int reportID;
	private String type; 
	
	public ReportHandling(){
		super();
		sort = "username asc";
		type = "all";
		reportID = -1;
	}

	/**
	 * Returns the string representation of a signed or unsigned time report.
	 * @param signed: int that shows if the report is signed or not. 0=not signed 1=signed
	 * @return String: The string representation.
	 */
	private String signString(int signed){
		String signedStr = "NO";
		if (signed == 1) {
			signedStr = "YES";
		}
		return signedStr;
	}
	
	/**
	 * Shows a list of all the time reports, from the specified group with the given id.
	 * @param group: The project leaders group id.
	 */
	private void showAllReports(int groupID){
		try {
			Statement s = conn.createStatement();		    
			ResultSet r = s.executeQuery("select * from groups where ID = "+groupID);
			String groupName = "";
			if (r.first()) {
				groupName = r.getString("name");
			}
			s.close();
			Statement stmt = conn.createStatement();
			String sql = "select users.username, user_group.role, reports.id, reports.date, reports.week, reports.total_time, reports.signed "
					+ " from user_group INNER JOIN reports on user_group.id = reports.user_group_id "
					+ " INNER JOIN users on user_group.user_id = users.id"
					+ " where user_group.group_id =" + groupID;
			if(type.equals("signed")){
				sql += " and reports.signed = 1 ";
			}else if(type.equals("unsigned")){
				sql += " and reports.signed = 0 ";
			}			
			sql += " order by "+ sort;
			ResultSet rs = stmt.executeQuery(sql);
			out.println("<div class='floati'>");
			out.println("<b>Time reports for "+groupName+"</b>");
			out.println("<table border=" + formElement("1") + ">");
			out.println("<tr><td>Selection</td><td>Username</td><td>Last update</td><td>Week</td><td>Total Time</td><td>Signed</td></tr>");
			
			out.println("<p> <form name=" + formElement("input") + " method=" + formElement("post") + ">");
			out.println(selectSortList());
			out.println(selectReportTypes());
			out.println("<input type=" + formElement("submit") + " name='sort' value="+ formElement("Sort") + '>');
			while(rs.next()){
		    	String reportID = ""+rs.getInt("ID");
		    	String userName = rs.getString("username");
				Date date = rs.getDate("date");
				int week = rs.getInt("week");
				int totalTime = rs.getInt("total_time");
				int signed = rs.getInt("signed");
				out.println("<tr>");
				out.println("<td>" + "<input type=" + formElement("radio") + " name=" + formElement("reportID") +
						" value=" + formElement(reportID) +"></td>");		//radiobutton
				out.println("<td>" + userName + "</td>");				
				out.println("<td>" + date.toString() + "</td>");
				out.println("<td>" + week + "</td>");
				out.println("<td>" + totalTime + "</td>");
				out.println("<td>" + formElement(signString(signed)) + "</td>");
				out.println("</tr>");
			}
			out.println("</table><p><input type=" + formElement("submit") + " name='view' value=" + formElement("View") + '>');
			out.println("</form>");
			out.println("</div>");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Lists all groups as a html select box
	 * @param out: Used to print the from
	 */
	private void listAllGroups(PrintWriter out){
		try {
			String html = "";
			html += "<p> <form name=" + formElement("input") + " method=" + formElement("post") + ">";
			html += "<br><select name='SelectedGroupID'>";
			html += "<option value='0' selected='true'>Select group: </option>";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from groups order by name asc");
			while(rs.next()){
				html += "<option value=" + rs.getInt("id") + ">"
						+ rs.getString("name") + "</option>";
			}
			html += "</select>";
			
			html += "<input type=" + formElement("submit") + " name='OK' value="+ formElement("OK") + '>';
			html += "</form>";
			out.println(html);	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructs a html string for a drop down list with the sort alternatives.
	 * @return a html string
	 */
	private String selectSortList(){
		String html = "";
		html += "<br><select name='sort'>";
		html += "<option value='0' selected='true'>Sort by: </option>";
		html += "<option value=" + formElement("username asc") + ">"
				+ "Namn, stigande" + "</option>";
		html += "<option value=" + formElement("username desc" ) + ">"
				+ "Namn, fallande" + "</option>";
		html += "<option value=" + formElement("week asc") + ">"
				+ "Vecka, stigande" + "</option>";
		html += "<option value=" + formElement("week desc") + ">"
				+ "Vecka, fallande" + "</option>";
		html += "<option value=" + formElement("signed desc") + ">"
				+ "Signerat/Osignerat" + "</option>";
		html += "<option value=" + formElement("signed asc") + ">"
				+ "Osignerat/Signerat" + "</option>";
		html += "<option value=" + formElement("role desc") + ">"
				+ "Roll stigande" + "</option>";
		html += "<option value=" + formElement("role asc") + ">"
				+ "Roll fallande" + "</option>";
		html += "</select>";
		return html;
	}
	
	/**
	 * Construct html string for selecting wether to show signe/unsigned/all reports
	 * @return a html string
	 */
	private String selectReportTypes(){
		String html = "";
		html += "<br><select name='type'>";
		if(type.equals("all")){
			html += "<option value='all' selected='true'>All</option>";
			html += "<option value='signed'>"
					+ "Signed" + "</option>";
			html += "<option value='unsigned'>"
					+ "Not Signed" + "</option>";
		}else if(type.equals("signed")){
			html += "<option value='all'>All</option>";
			html += "<option value='signed' selected='true'>"
					+ "Signed" + "</option>";
			html += "<option value='unsigned'>"
					+ "Not Signed" + "</option>";
		}else if(type.equals("unsigned")){
			html += "<option value='all'>All</option>";
			html += "<option value='signed'>"
					+ "Signed" + "</option>";
			html += "<option value='unsigned' selected='true'>"
					+ "Not Signed" + "</option>";
		}
		
		html += "</select>";
		return html;
	}
	
	/**
	 * Fetches the data for the time report specified by the reportID parameter
	 * and passes the data on to the static method viewReport in the static ReportGenerator class.
	 * @param reportID: The id of the time report.
	 */
	private void printViewReport(){
		try {
			Statement stmt = conn.createStatement();
			String query = "select reports.week, reports.total_time, reports.signed, ";
			String q = "";
			for (int i = 0; i<ReportGenerator.act_sub_names.length; i++) {
				String valueStr = "report_times." + ReportGenerator.act_sub_names[i];
				q += valueStr+",";
			}
			for (int i = 0; i<ReportGenerator.lower_activities.length-1; i++) {
				String valueStr = ReportGenerator.lower_activities[i];
				q += valueStr+",";
			}
			q += ReportGenerator.lower_activities[ReportGenerator.lower_activities.length-1];
			query += q;
			query += ", reports.user_group_id, reports.date, users.username, groups.name from reports";
			String inner = " inner join report_times on reports.id = report_times.report_id";			
			String inner1 = " inner join user_group on reports.user_group_id = user_group.id";
			String inner2 = " inner join users on user_group.user_id = users.id";
			String inner3 = " inner join groups on user_group.group_id = groups.id";
			String end = " where reports.id = " + reportID;
			query += inner;
			query += inner1;
			query += inner2;
			query += inner3;
			query += end;
			ResultSet rs = stmt.executeQuery(query);
			if(rs.first()){	
				out.println("<div class='floati'>");
				out.println(ReportGenerator.viewReport(rs));
				rs = stmt.executeQuery("Select signed from reports where id=" + reportID);
				rs.first();
				out.println("<p> <form name=" + formElement("input") + " method=" + formElement("post"));
				if(rs.getInt("signed") == 0){
					out.println("<p><input type=" + formElement("submit") + " name='sign' value="+ formElement("Sign") 
							+ " onclick=" +formElement("return confirm('Are you sure you want to sign this report?')") + '>');
				} else {
					out.println("<p><input type=" + formElement("submit") + " name='unsign' value="+ formElement("Unsign") 
							+ " onclick=" +formElement("return confirm('Are you sure you want to unsign this report?')") + '>');
				}
				out.println("</form>");
				out.println("</div>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}
	
	/**
	 * Signs a time report
	 * @param timeReportID: The id of the report that will be signed.
	 * @return boolean: True if the time report was successfully signed.
	 */
	private boolean signTimeReport(int timeReportID){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			String statement = "Update reports SET signed = 1 where ID=" + timeReportID; 
			int i = stmt.executeUpdate(statement);
			if(i == 1){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return false;
	}
	
	/**
	 * Unsigns a time report
	 * @param timeReportID: The id of the report that will be unsigned.
	 * @return boolean: True if the time report was successfully unsigned.
	 */
	private boolean unsignTimeReport(int timeReportID){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			String statement = "Update reports SET signed = 0 where ID=" + timeReportID; 
			int i = stmt.executeUpdate(statement);
			if(i == 1){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return false;
	}

	/**
	 * Redirect to doPost
	 */
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	/**
	 * All requests are forwarded to the doGet method.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		access.updateLog(null, null); // check timestamps
		out = response.getWriter();
		out.println(getPageIntro());
		out.println(printMainMenu(request));
		out.println("<h1>Project Management " + "</h1>");
		HttpSession session = request.getSession(true);
		int groupID;
		if (!loggedIn(request))
			response.sendRedirect("LogIn");
		else
		{	
			if(request.getParameter("OK") != null){
				String newGroupIDStr = request.getParameter("SelectedGroupID");
				if (newGroupIDStr != null) {
					int newGroupID = Integer.parseInt(newGroupIDStr);
					if(newGroupID != 0){
						groupID = newGroupID;
						session.setAttribute("groupID", newGroupIDStr);
					}
				}
			}			
			Object groupIDObject = session.getAttribute("groupID");		
			groupID = Integer.parseInt((String) groupIDObject);
			if(groupID > 0){
				String reportIDString = request.getParameter("reportID");
				String buttonView = request.getParameter("view");
				if(reportIDString != null && buttonView != null){
					reportID = Integer.parseInt(reportIDString);
					printViewReport();
				} else {
					String buttonSign = request.getParameter("sign");
					String buttonUnsign = request.getParameter("unsign");
					if(buttonSign != null){
						signTimeReport(reportID);
						out.println("Report has been signed.");
					}	
					if(buttonUnsign != null){
						unsignTimeReport(reportID);	
						out.println("Report has been unsigned.");
						
					}			
					String buttonSort = request.getParameter("sort");
					if(buttonSort != null){
						String sortOrder = request.getParameter("sort");
						if(!sortOrder.equals("0")){
							sort = sortOrder;
						}
					}
					String types = request.getParameter("type");
					if(types != null){
						type = types;
					}
					if(session.getAttribute("name").equals(ADMIN)){
						listAllGroups(out);
					}
					showAllReports(groupID);
				}
			} else {
				listAllGroups(out);
			}
		}
	}
}
