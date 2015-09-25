import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import javax.websocket.server.ServerEndpoint;

public class MyServlet extends HttpServlet {

    private ServletConfig config;

    String page = "/index.jsp";

    public void init(ServletConfig config)

            throws ServletException {

        this.config = config;

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        response.setContentType("text/html");

        String data = "Hello World!";

        request.setAttribute("textA", "111");

        RequestDispatcher dispatcher = request.getRequestDispatcher(page);

        if (dispatcher != null) {

            dispatcher.forward(request, response);

        }
    }

    String name = "";

    public String getName() {
        Connection conn = null;
        try {
            Context ctx = new InitialContext();
            //Context initContext = new InitialContext();

            //Context envContext  = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/myapp");

            conn = ds.getConnection();
            Statement stat = conn.createStatement();

            //stat.executeUpdate("INSERT INTO freeUsers (userKey, isConnected)\n" +
            //        "VALUES ('123456', 0);");
            //stat.executeUpdate("INSERT INTO freeUsers (userKey, isConnected)\n" +
            //        "VALUES ('123456', 0);");

            ResultSet rs = stat.executeQuery("select userKey from freeUsers");
            this.name = rs.getString(1);
        } catch (SQLException se) {
            return se.toString();
            //return "exception 1";
        } catch (NamingException ne) {
            return ne.toString();
            //return "exception 2";
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return e.toString();
            }
        }

        return this.name;
    }


}
