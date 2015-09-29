package Databases;

import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.*;

public class SQLiteClass {
    public static Connection conn;
    public static Statement stat;
    public static ResultSet rs;

    public static void Conn() throws ClassNotFoundException, SQLException, NamingException {
        Class.forName("org.sqlite.JDBC");

        //полный путь к базе данных
        conn = DriverManager.getConnection("jdbc:sqlite:/Users/Nurislam/Downloads/untitled6/ChatDatabase");
    }

    public static boolean checkKeyGenDb(String keyGen) throws ClassNotFoundException, SQLException {
        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select id from keyGens where keyGen = '" + keyGen + "'" +
                "and isUse != " + "'1'");
        while (rs.next()) {
            rs.close();
            stat.close();
            return true;
        }

        rs.close();
        stat.close();
        return false;
    }

    public static void addUserDatabase(String userName, String keyGen) throws ClassNotFoundException, SQLException {
        stat = conn.createStatement();

        int n = stat.executeUpdate("UPDATE keyGens SET isUse = '1' WHERE keyGen = '" + keyGen + "'");

        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO freeUsers (name,  userKeyGen) VALUES ( ?, ?)");
            statement.setString(1, userName);
            statement.setString(2, keyGen);

            statement.execute();
            statement.close();
        } catch (Exception e) {
            //nothing
        }
        finally {
            stat.close();
        }
    }

    public static String getNameDb(String keyGen) throws ClassNotFoundException, SQLException,  NamingException
    {
        Conn();

        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select name from freeUsers where userKeyGen = '" + keyGen + "'");

        while (rs.next()) {
            String answer = rs.getString("name");
            rs.close();
            stat.close();
            CloseDB();
            return answer;
        }

        rs.close();
        stat.close();
        CloseDB();

        return "";
    }

    public static void CloseDB() throws ClassNotFoundException, SQLException {
        conn.close();
    }
}