
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import com.adventnet.ds.query.*;
import com.adventnet.mfw.bean.BeanUtil;
import java.sql.*;
import com.adventnet.persistence.*;
import java.io.PrintWriter;
import com.adventnet.db.api.RelationalAPI;
import java.util.*;

public class login extends HttpServlet
{
    public void service(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        String username=req.getParameter("user_name");
		String password=req.getParameter("passwd");
        System.out.println(username+" "+password);
		
        try
        { 
            Persistence per = (Persistence)BeanUtil.lookup("Persistence");
            DataObject dataobject=null; 
            SelectQueryImpl squery=new SelectQueryImpl(Table.getTable("USERS"));
            Column column1=new Column("USERS","*");
            Criteria criteria1=new Criteria(new Column("USERS","USER_NAME"),username,QueryConstants.EQUAL);
            Criteria criteria2=new Criteria(new Column("USERS","PASSWD"),password,QueryConstants.EQUAL);
            Criteria criteria3=criteria1.and(criteria2);
            squery.addSelectColumn(column1);
            squery.setCriteria(criteria3);
            dataobject=per.get((SelectQuery)squery);
            Row row=dataobject.getFirstRow("USERS"); 
            String usr_name=(String)row.get(2);
            out.println(usr_name);
            HttpSession session=req.getSession(); 
            session.setAttribute("USR_NAME",usr_name);
            System.out.print(session.getAttribute("USR_NAME"));
            System.out.println("1");
            if(!dataobject.isEmpty())
           {
            resp.sendRedirect("AdminOperations.jsp");
           System.out.println("2");
            }
            else
            {
            resp.sendRedirect("AdminLogin.html");
            System.out.println("3");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            
        }
        
    }
}