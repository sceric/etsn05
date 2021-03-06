import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/ChangePassword")
public class ChangePassword extends servletBase {

	private static final long serialVersionUID = 1L;
	private static final int PASSWORD_LENGTH = 6;
	/**
	 * generates a form for changing password
	 * @return HTML code for the form
	 */
	private String changePasswordForm() {
		String html;
		html = "<p> <form name=" + formElement("input");
		html += " method=" + formElement("post");
		html += "<p> Old password: <input type=" + formElement("text") + " name=" + formElement("oldpw") + '>';     	
		html += "<p> New password: <input type=" + formElement("text") + " name=" + formElement("newpw") + '>';  
		html += "<input type=" + formElement("submit") + "value=" + formElement("Change") + '>';
		html += "</form>";
		return html;
	}
	
	/**
	 * Check and makes sure the password is valid. 
	 * @param newPass: String the password to check
	 * @return True if the password is valid
	 */
	private boolean checkNewPass(String newPass){
		if(newPass.length() != PASSWORD_LENGTH){ //password har wrong length
			return false; //password has wrong length
		}else{
			for(int i =0; i < PASSWORD_LENGTH; i++){
				int ci = (int)newPass.charAt(i);
				boolean thisOk = (ci>=97 && ci<=122); 
				if(!thisOk){
					return false; //password has incorrect character
				}
			}
		}		
		return true; //password has right length and right chars.		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		access.updateLog(null, null);
		
		PrintWriter out = response.getWriter();
		out.println(getPageIntro());
		out.println(printMainMenu(request));
		String myName = "";
		HttpSession session = request.getSession(true);
		Object nameObj = session.getAttribute("name");
		Object idObj = session.getAttribute("userID");
		int id =(int)idObj;
		System.out.println(id);
		String oldPw = request.getParameter("oldpw");
		String newPw = request.getParameter("newpw");
		
		if (nameObj != null) {
			myName = (String)nameObj;  // if the name exists typecast the name to a string
		}

		// check that the user is logged in
		if (!loggedIn(request)) {
			response.sendRedirect("LogIn");
		} else {
			if (myName.equals(ADMIN)) { 
				out.println("<p>Error: Admin is not allowed to change password</p>");
			} else {
				if (oldPw!=null&&newPw!=null) {
					Statement stmt;
					try {
						stmt = conn.createStatement();
						String statement = "select * from users where ID=" + id; 
						ResultSet rs= stmt.executeQuery(statement);
						String pw = null;
						while (rs.next( )) {
							pw = rs.getString("password");
						}
						if(!checkNewPass(newPw)){
							out.println("<p>Error: New password has incorrect length or not allowed characters</p>");
						}else if (pw.equals(oldPw)) {
							stmt = conn.createStatement();
							statement = "Update users SET password='"+newPw+"' where ID=" + id; 
							stmt.executeUpdate(statement);
							out.println("<p>Successfully changed password</p>");
						} else {
							out.println("<p>Error: entered old password does not match password in database</p>");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				out.println(changePasswordForm());
			}
		}
	}
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}



