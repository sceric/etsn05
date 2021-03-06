

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/Start")
public class Start extends servletBase {
	private static final long serialVersionUID = 1L;
       
    
    public Start() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		access.updateLog(null, null);
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		
		if (loggedIn(request)){
			out.println(getPageIntro());
			out.println(printMainMenu(request));
		}
		else {
			response.sendRedirect("LogIn");
		}
	}
}
