

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;
import com.adventnet.ds.query.*;
import com.adventnet.mfw.bean.BeanUtil;
import java.sql.*;
import com.adventnet.persistence.*;
import java.io.PrintWriter;
import com.adventnet.db.api.RelationalAPI;
import java.util.*; 
import java.util.Date;
import java.text.SimpleDateFormat;

public class flooroperations extends HttpServlet
{
    public void service(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter out = resp.getWriter();
        String param_function=req.getParameter("param");
        
        try{

        Persistence per = (Persistence)BeanUtil.lookup("Persistence"); 
        
        if(param_function.equals("addlot"))
		{
            String parkinglot_name=req.getParameter("lot_name"); 
        try
        {
        Row row=new Row("PARKINGLOT");
        row.set("PARKINGLOT_NAME",parkinglot_name);
        DataObject dobj=new WritableDataObject();
        dobj.addRow(row);
        per.add(dobj);
        resp.getWriter().write("success"); 
        }
        catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("AdminLogin.html");
        }
        }


        else if(param_function.equals("add"))
        {
           String vehicle_type=req.getParameter("vehicle");
			int floor_no=Integer.parseInt(req.getParameter("floor_id"));
			int parkinglot_id=Integer.parseInt(req.getParameter("lot_id"));
			int capacity=Integer.parseInt(req.getParameter("capacity"));

        
            Row row=new Row("FLOORS");
        row.set("FLOOR_NAME","Base"+floor_no);
        row.set("PARKINGLOT_ID",parkinglot_id);
        row.set("VEHICLE_TYPE",vehicle_type);
        row.set("CAPACITY",capacity);
        DataObject dobj=new WritableDataObject();
        dobj.addRow(row);
        per.add(dobj); 

        SelectQuery query=new SelectQueryImpl(Table.getTable("FLOORS"));
        Column c1=new Column("FLOORS","*");
        query.addSelectColumn(c1);
        Criteria criteria1=new Criteria(Column.getColumn("FLOORS","PARKINGLOT_ID"),parkinglot_id,QueryConstants.EQUAL);
        Criteria criteria2=new Criteria(Column.getColumn("FLOORS","FLOOR_NAME"),"Base"+floor_no,QueryConstants.EQUAL); 
        Criteria criteria3=criteria1.and(criteria2); 
        query.setCriteria(criteria3);
        dobj=per.get((SelectQuery)query);
        Row row1=dobj.getRow("FLOORS");
        int floorid=(int)row1.get("FLOOR_ID");
        out.println(floorid);
         
         int cnt=0;
        while(cnt!=capacity)
        {
        Row rw=new Row("SLOTS");
        rw.set("FLOOR_ID",floorid);
        dobj=new WritableDataObject(); 
        dobj.addRow(rw);
        DataAccess.add(dobj);
        cnt+=1;
        }
            
        resp.getWriter().write("success");
        }

        else if(param_function.equals("edit"))
        {
            String add_or_delete=req.getParameter("AddDelete");
			int floor_id=Integer.parseInt(req.getParameter("floor_id"));
			int slots=Integer.parseInt(req.getParameter("slots")); 
			int flag=0;
        
        SelectQuery query=new SelectQueryImpl(Table.getTable("FLOORS"));
        Column c1=new Column("FLOORS","*");
        query.addSelectColumn(c1);
        Criteria criteria1=new Criteria(Column.getColumn("FLOORS","FLOOR_ID"),floor_id,QueryConstants.EQUAL);
        query.setCriteria(criteria1);
        DataObject dobj=per.get((SelectQuery)query);

        if(!dobj.isEmpty())
        {
        Row row1=dobj.getRow("FLOORS");
        // out.println(row1);
        int floor_capacity=(int)row1.get("CAPACITY");
        out.println(floor_capacity);

        if(add_or_delete.equals("ADD_EXTRA_SLOTS"))
        {
        UpdateQuery updatequery=new UpdateQueryImpl("FLOORS");
        Criteria criteria2=new Criteria(new Column("FLOORS","FLOOR_ID"),floor_id,QueryConstants.EQUAL);
        updatequery.setCriteria(criteria2);
        updatequery.setUpdateColumn("CAPACITY",floor_capacity+slots);
        per.update(updatequery);

        int count=0;
        while(count!=slots){
        Row row2=new Row("SLOTS");
        row2.set("FLOOR_ID",floor_id);
        dobj=new WritableDataObject();
        dobj.addRow(row2);
        DataAccess.add(dobj);
        count+=1;
        } 
        resp.getWriter().write("success");
        } 
        else if(add_or_delete.equals("DELETE_SLOTS") && floor_capacity>=slots)
        {
            
        UpdateQuery updatequery=new UpdateQueryImpl("FLOORS");
        Criteria criteria2=new Criteria(new Column("FLOORS","FLOOR_ID"),floor_id,QueryConstants.EQUAL);
        updatequery.setCriteria(criteria2);
        if(floor_capacity>=slots)
        {
        updatequery.setUpdateColumn("CAPACITY",floor_capacity-slots);
        flag=1;
        DataAccess.update(updatequery);
        
        if(flag==1){
        Criteria criteria=new Criteria(Column.getColumn("SLOTS","FLOOR_ID"),floor_id,QueryConstants.EQUAL);
        dobj=DataAccess.get("SLOTS",criteria);
        Iterator itr=dobj.getRows("SLOTS",criteria);
        ArrayList<Row> lst=new ArrayList<Row>();
        while(itr.hasNext())
        {
            Row rw=(Row)itr.next();
            lst.add(rw);
        }
        Collections.reverse(lst);
        out.println(lst);
        for(int i=0;i<slots;i++)
        {
            Row r=(Row)lst.get(i);
            dobj.deleteRow(r);
            DataAccess.update(dobj);
        }
        resp.getWriter().write("success");
        }
        }
        }
        else
        {
            resp.getWriter().write("slotnot");
            
        }
        
        
        }
        else{
            resp.getWriter().write("floornot");
        }
        }
        

        else if(param_function.equals("viewhistory"))
        {
        String date=req.getParameter("dateday");
        SelectQuery query=new SelectQueryImpl(Table.getTable("PARKINGRECORDS"));
        Column c=new Column("PARKINGRECORDS","*");
        query.addSelectColumn(c);
        Criteria criteria1=new Criteria(Column.getColumn("PARKINGRECORDS","ENTRY"),null,QueryConstants.NOT_EQUAL);  
        query.setCriteria(criteria1);
        DataObject dobj=DataAccess.get(query); 
        Iterator itr=dobj.getRows("PARKINGRECORDS",criteria1); 
        out.println(itr); 

        if(!dobj.isEmpty())
        {
            
            
        // while(itr.hasNext())
        // {
        //     Row row=(Row)itr.next(); 
        //     String entryDT=(String)row.get(6);
        //     Date dt=formatter1.parse(entryDT);
        //     out.println(dt);
        //     String date1=formatter1.format(dt);
        //     out.println(date1);
        //     if(date1.equals(date)) 
        //     {
        //     int parkingrec_id=(int)row.get(1);
        //     int slot_id=(int)row.get(2);
        //     int user_id=(int)row.get(3);
        //     String vehicle_num=(String)row.get(4);
        //     String contact_num=(String)row.get(5);
        //     String entry=(String)row.get(6);
        //     String exit=(String)row.get(7);
        //     int charge=(int)row.get(8);
        //     out.println(parkingrec_id+" "+slot_id+" "+user_id+" "+vehicle_num+" "+contact_num+" "+entry+" "+exit+" "+charge);
        //     }
        // }
        resp.getWriter().write(date);
        }
        else {
            resp.getWriter().write("failure");
				
			
		    }
        }
        }
        catch (Exception e) {
            e.printStackTrace();
            
        }
    }
}