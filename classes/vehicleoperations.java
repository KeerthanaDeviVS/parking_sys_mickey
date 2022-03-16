
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

public class vehicleoperations extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        // String param_function=req.getParameter("param");
        String action = req.getParameter("func");

        try {

            Persistence per = (Persistence) BeanUtil.lookup("Persistence");
            if (action.equals("register")) {

                String vehicle_type = req.getParameter("vehicle");
                String name = req.getParameter("user_name");
                String email = req.getParameter("usr_id");
                String passwd = req.getParameter("passwd");
                String phone = req.getParameter("phone");
                String query;
                SelectQuery sq = new SelectQueryImpl(Table.getTable("SLOTS"));
                Column col1 = new Column("SLOTS", "*");
                Column col2 = new Column("FLOORS", "*");
                ArrayList<Column> arr = new ArrayList<Column>();
                arr.add(col1);
                arr.add(col2);
                sq.addSelectColumns(arr);
                Criteria criteria1 = new Criteria(Column.getColumn("SLOTS", "IS_PARKED"), 0, QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(Column.getColumn("FLOORS", "IS_FULL"), 1, QueryConstants.NOT_EQUAL);
                Criteria criteria3 = criteria1.and(criteria2);
                Criteria criteria4 = new Criteria(Column.getColumn("FLOORS", "VEHICLE_TYPE"), vehicle_type,
                        QueryConstants.EQUAL);
                Criteria criteria11 = criteria3.and(criteria4);
                Criteria criteria12 = new Criteria(Column.getColumn("SLOTS", "RESERVED_USER_ID"), null,
                        QueryConstants.EQUAL);
                Criteria criteria = criteria11.and(criteria12);
                sq.setCriteria(criteria);
                Join join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                        Join.INNER_JOIN);
                sq.addJoin(join);
                DataObject dobj = DataAccess.get(sq);
                Row row = dobj.getFirstRow("SLOTS");
                int slot_id = (int) row.get(1);
                int floor_id = (int) row.get(2);
                // out.println(slot_id+" "+floor_id);

                Row nrow = new Row("USERS");
                nrow.set("USER_NAME", name);
                nrow.set("PASSWD", passwd);
                nrow.set("CONTACT_NUM", phone);
                dobj = new WritableDataObject();
                dobj.addRow(nrow);
                per.add(dobj);

                SelectQuery us_qry = new SelectQueryImpl(Table.getTable("USERS"));
                Column column = new Column("USERS", "*");
                us_qry.addSelectColumn(column);
                Criteria cri = new Criteria(Column.getColumn("USERS", "CONTACT_NUM"), phone, QueryConstants.EQUAL);
                us_qry.setCriteria(cri);
                dobj = DataAccess.get(us_qry);
                Row r = dobj.getRow("USERS");
                long usr_id = (long) r.get(1);
                // out.println(usr_id);

                UpdateQuery update = new UpdateQueryImpl("SLOTS");
                Criteria c_1 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                update.setCriteria(c_1);
                update.setUpdateColumn("RESERVED_USER_ID", usr_id);
                per.update(update);

                int count = 0;
                RelationalAPI relapi = RelationalAPI.getInstance();
                java.sql.Connection con = null;
                UpdateQuery qry = new UpdateQueryImpl("FLOORS");
                SelectQuery squery = new SelectQueryImpl(Table.getTable("SLOTS"));
                Criteria criteria6 = new Criteria(new Column("SLOTS", "IS_PARKED"), 0, QueryConstants.NOT_EQUAL);
                Criteria criteria61 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null, QueryConstants.EQUAL);
                Criteria criteria62 = criteria6.and(criteria61);
                Criteria criteria63 = new Criteria(new Column("SLOTS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                Criteria criteria64 = criteria63.and(criteria62);
                squery.setCriteria(criteria64);
                ArrayList<Column> lst = new ArrayList<Column>();
                ArrayList<Column> lst2 = new ArrayList<Column>();
                Column c1 = Column.getColumn("SLOTS", "IS_PARKED");
                lst.add(c1);
                Column parked_cnt = c1.count();
                parked_cnt.setColumnAlias("P_COUNT");
                lst.add(parked_cnt);
                squery.addSelectColumns(lst);
                con = relapi.getConnection();
                DataSet ds = relapi.executeQuery(squery, con);
                ds.next();
                int cnt = (int) ds.getValue("P_COUNT");
                out.println(cnt);
                SelectQuery sqry = new SelectQueryImpl(Table.getTable("SLOTS"));
                Criteria criteria65 = new Criteria(new Column("SLOTS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                Criteria criteria15 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                        QueryConstants.NOT_EQUAL);
                Criteria criteria66 = criteria65.and(criteria15);
                sqry.setCriteria(criteria66);
                Column c2 = Column.getColumn("SLOTS", "RESERVED_USER_ID");
                lst2.add(c2);
                Column res_cnt = c2.count();
                res_cnt.setColumnAlias("R_COUNT");
                lst2.add(res_cnt);
                sqry.addSelectColumns(lst2);
                con = relapi.getConnection();
                ds = relapi.executeQuery(sqry, con);
                ds.next();
                int cnt2 = (int) ds.getValue("R_COUNT");
                out.println(cnt2);
                int tot_count = cnt + cnt2;
                Criteria criteria7 = new Criteria(new Column("FLOORS", "CAPACITY"), tot_count, QueryConstants.EQUAL);
                Criteria criteria8 = new Criteria(new Column("FLOORS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                Criteria criteria9 = criteria8.and(criteria7);
                qry.setCriteria(criteria9);
                qry.setUpdateColumn("IS_FULL", 1);
                per.update(qry);
                ds.close();
                HttpSession session = req.getSession();
                session.setAttribute("USR_NAME", usr_id);

                resp.getWriter().write(usr_id + " - " + slot_id);
            } else if (action.equals("addnewslots")) {
                String vehicle_type = req.getParameter("vehicle");
                int user_id = Integer.parseInt(req.getParameter("usr_id"));
                SelectQuery sq = new SelectQueryImpl(Table.getTable("SLOTS"));
                Column col1 = new Column("SLOTS", "*");
                Column col2 = new Column("FLOORS", "*");
                ArrayList<Column> arr = new ArrayList<Column>();
                arr.add(col1);
                arr.add(col2);
                sq.addSelectColumns(arr);
                Criteria criteria1 = new Criteria(Column.getColumn("SLOTS", "IS_PARKED"), 0, QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(Column.getColumn("FLOORS", "IS_FULL"), 1, QueryConstants.NOT_EQUAL);
                Criteria criteria3 = criteria1.and(criteria2);
                Criteria criteria4 = new Criteria(Column.getColumn("FLOORS", "VEHICLE_TYPE"), vehicle_type,
                        QueryConstants.EQUAL);
                Criteria criteria11 = criteria3.and(criteria4);
                Criteria criteria12 = new Criteria(Column.getColumn("SLOTS", "RESERVED_USER_ID"), null,
                        QueryConstants.EQUAL);
                Criteria criteria = criteria11.and(criteria12);
                sq.setCriteria(criteria);
                Join join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                        Join.INNER_JOIN);
                sq.addJoin(join);
                DataObject dobj = DataAccess.get(sq);
                Row row = dobj.getFirstRow("SLOTS");
                int slot_id = (int) row.get(1);
                int floor_id = (int) row.get(2);
                // out.println(slot_id+" "+floor_id);

                UpdateQuery update = new UpdateQueryImpl("SLOTS");
                Criteria c_1 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                update.setCriteria(c_1);
                update.setUpdateColumn("RESERVED_USER_ID", user_id);
                per.update(update);

                int count = 0;
                RelationalAPI relapi = RelationalAPI.getInstance();
                java.sql.Connection con = null;
                UpdateQuery qry = new UpdateQueryImpl("FLOORS");
                SelectQuery squery = new SelectQueryImpl(Table.getTable("SLOTS"));
                Criteria criteria6 = new Criteria(new Column("SLOTS", "IS_PARKED"), 0, QueryConstants.NOT_EQUAL);
                Criteria criteria61 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null, QueryConstants.EQUAL);
                Criteria criteria62 = criteria6.and(criteria61);
                squery.setCriteria(criteria62);
                ArrayList<Column> lst = new ArrayList<Column>();
                ArrayList<Column> lst2 = new ArrayList<Column>();
                Column c1 = Column.getColumn("SLOTS", "IS_PARKED");
                lst.add(c1);
                Column parked_cnt = c1.count();
                parked_cnt.setColumnAlias("P_COUNT");
                lst.add(parked_cnt);
                squery.addSelectColumns(lst);
                con = relapi.getConnection();
                DataSet ds = relapi.executeQuery(squery, con);
                ds.next();
                int cnt = (int) ds.getValue("P_COUNT");
                // out.println(cnt);
                SelectQuery sqry = new SelectQueryImpl(Table.getTable("SLOTS"));
                Criteria criteria15 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                        QueryConstants.NOT_EQUAL);
                sqry.setCriteria(criteria15);
                Column c2 = Column.getColumn("SLOTS", "RESERVED_USER_ID");
                lst2.add(c2);
                Column res_cnt = c2.count();
                res_cnt.setColumnAlias("R_COUNT");
                lst2.add(res_cnt);
                sqry.addSelectColumns(lst2);
                con = relapi.getConnection();
                ds = relapi.executeQuery(sqry, con);
                ds.next();
                int cnt2 = (int) ds.getValue("R_COUNT");
                // out.println(cnt2);
                int tot_count = cnt + cnt2;
                Criteria criteria7 = new Criteria(new Column("FLOORS", "CAPACITY"), tot_count, QueryConstants.EQUAL);
                Criteria criteria8 = new Criteria(new Column("FLOORS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                Criteria criteria9 = criteria8.and(criteria7);
                qry.setCriteria(criteria9);
                qry.setUpdateColumn("IS_FULL", 1);
                per.update(qry);
                ds.close();
                HttpSession session = req.getSession();
                session.setAttribute("USR_NAME", user_id);

                resp.getWriter().write(user_id + " - " + slot_id);

            } else if (action.equals("signin")) {
                String user_id = req.getParameter("user_id");
                String password = req.getParameter("passwd");
                String method = req.getParameter("method");

                // out.println(user_id);

                SelectQuery sqry = new SelectQueryImpl(Table.getTable("USERS"));
                Column column1 = new Column("USERS", "*");
                sqry.addSelectColumn(column1);
                Criteria criteria1 = new Criteria(new Column("USERS", "USER_NAME"), user_id, QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(new Column("USERS", "PASSWD"), password, QueryConstants.EQUAL);
                Criteria criteria = criteria1.and(criteria2);
                sqry.setCriteria(criteria);
                DataObject dataobject = per.get(sqry);
                Row row = dataobject.getFirstRow("USERS");
                HttpSession session = req.getSession();
                session.setAttribute("USR_NAME", user_id);
                // out.println(row.get(1));
                // out.print(session.getAttribute("USR_NAME"));
                if (method.equals("register"))
                    resp.getWriter().write("success1");
                else
                    resp.getWriter().write("success2");
            } else if (action.equals("park")) {
                String vehicle_type = req.getParameter("vehicle");
                String vehicle_num = req.getParameter("vehicle_no");
                String contact_num = req.getParameter("telphone");
                SelectQuery sq = new SelectQueryImpl(Table.getTable("SLOTS"));
                Column c1 = new Column("SLOTS", "*");
                Column c2 = new Column("FLOORS", "*");
                ArrayList<Column> lst = new ArrayList<Column>();
                lst.add(c1);
                lst.add(c2);
                sq.addSelectColumns(lst);
                Criteria criteria1 = new Criteria(Column.getColumn("SLOTS", "IS_PARKED"), 0, QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(Column.getColumn("FLOORS", "IS_FULL"), 1, QueryConstants.NOT_EQUAL);
                Criteria criteria3 = criteria1.and(criteria2);
                Criteria criteria4 = new Criteria(Column.getColumn("FLOORS", "VEHICLE_TYPE"), vehicle_type,
                        QueryConstants.EQUAL);
                Criteria criteria11 = criteria3.and(criteria4);
                Criteria criteria12 = new Criteria(Column.getColumn("SLOTS", "RESERVED_USER_ID"), null,
                        QueryConstants.EQUAL);
                Criteria criteria = criteria11.and(criteria12);
                sq.setCriteria(criteria);
                Join join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                        Join.INNER_JOIN);
                sq.addJoin(join);
                DataObject dobj = DataAccess.get(sq);
                Row row = dobj.getFirstRow("SLOTS");
                int slot_id = (int) row.get(1);
                int floor_id = (int) row.get(2);
                out.println(slot_id + " " + floor_id);

                if (!(dobj.isEmpty())) {
                    UpdateQuery query = new UpdateQueryImpl("SLOTS");
                    Criteria criteria5 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                    query.setCriteria(criteria5);
                    query.setUpdateColumn("IS_PARKED", 1);
                    DataAccess.update(query);

                    int count = 0;
                    RelationalAPI relapi = RelationalAPI.getInstance();
                    java.sql.Connection con = null;
                    UpdateQuery qry = new UpdateQueryImpl("FLOORS");
                    SelectQuery squery = new SelectQueryImpl(Table.getTable("SLOTS"));
                    Criteria c_1 = new Criteria(new Column("SLOTS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                    Criteria criteria6 = new Criteria(new Column("SLOTS", "IS_PARKED"), 0, QueryConstants.NOT_EQUAL);
                    Criteria criteria61 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                            QueryConstants.EQUAL);
                    Criteria c_2 = criteria6.and(criteria61);

                    Criteria criteria62 = c_1.and(c_2);
                    squery.setCriteria(criteria62);
                    ArrayList<Column> lst1 = new ArrayList<Column>();
                    ArrayList<Column> lst2 = new ArrayList<Column>();
                    Column cl1 = Column.getColumn("SLOTS", "IS_PARKED");
                    lst1.add(cl1);
                    Column parked_cnt = cl1.count();
                    parked_cnt.setColumnAlias("P_COUNT");
                    lst1.add(parked_cnt);
                    squery.addSelectColumns(lst1);
                    con = relapi.getConnection();
                    DataSet ds = relapi.executeQuery(squery, con);
                    ds.next();
                    int cnt = (int) ds.getValue("P_COUNT");
                    // out.println(cnt);
                    SelectQuery sqry = new SelectQueryImpl(Table.getTable("SLOTS"));
                    Criteria crit = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                            QueryConstants.NOT_EQUAL);
                    Criteria cri = crit.and(c_1);
                    sqry.setCriteria(crit);
                    Column cl2 = Column.getColumn("SLOTS", "RESERVED_USER_ID");
                    lst2.add(cl2);
                    Column res_cnt = cl2.count();
                    res_cnt.setColumnAlias("R_COUNT");
                    lst2.add(res_cnt);
                    sqry.addSelectColumns(lst2);
                    con = relapi.getConnection();
                    ds = relapi.executeQuery(sqry, con);
                    ds.next();
                    int cnt2 = (int) ds.getValue("R_COUNT");
                    // out.println(cnt2);
                    int tot_cnt = cnt + cnt2;
                    // out.println(cnt+cnt2);
                    Criteria criteria7 = new Criteria(new Column("FLOORS", "CAPACITY"), tot_cnt, QueryConstants.EQUAL);
                    Criteria criteria8 = new Criteria(new Column("FLOORS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                    Criteria criteria9 = criteria8.and(criteria7);
                    qry.setCriteria(criteria9);
                    qry.setUpdateColumn("IS_FULL", 0);
                    per.update(qry);
                    ds.close();
                    // out.println(floor_id);

                    Row nrw = new Row("PARKINGRECORDS");
                    nrw.set("SLOT_ID", slot_id);
                    nrw.set("VEHICLE_NUM", vehicle_num);
                    nrw.set("CONTACT_NUM", contact_num);
                    nrw.set("ENTRY", "04-03-2022 16:12:00");
                    dobj = new WritableDataObject();
                    dobj.addRow(nrw);
                    per.add(dobj);
                    resp.getWriter().print(slot_id);
                } else {
                    resp.getWriter().write("failure");
                }

            }

            else if (action.equals("parkuser")) {
                int user_id = Integer.parseInt(req.getParameter("email"));
                String vehicle_num = req.getParameter("vehic_num");
                String vehicle_type = req.getParameter("vehic_typ");
                SelectQuery query = new SelectQueryImpl(Table.getTable("SLOTS"));
                Column c1 = new Column("SLOTS", "*");
                query.addSelectColumn(c1);
                Criteria criteria1 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), user_id,
                        QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(new Column("SLOTS", "IS_PARKED"), 0, QueryConstants.EQUAL);
                Criteria criteria3 = criteria1.and(criteria2);
                Criteria criteria4 = new Criteria(new Column("FLOORS", "VEHICLE_TYPE"), vehicle_type,
                        QueryConstants.EQUAL);
                Criteria criteria5 = criteria3.and(criteria4);
                query.setCriteria(criteria5);
                Join join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                        Join.LEFT_JOIN);
                query.addJoin(join);
                DataObject dobj = DataAccess.get((SelectQuery) query);
                Row row = dobj.getFirstRow("SLOTS");
                int slot_id = (int) row.get(1);
                out.println(slot_id);

                if (!(dobj.isEmpty())) {
                    UpdateQuery qry = new UpdateQueryImpl("SLOTS");
                    Criteria criteria7 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                    qry.setCriteria(criteria7);
                    qry.setUpdateColumn("IS_PARKED", 1);
                    per.update(qry);

                    SelectQuery us_qry = new SelectQueryImpl(Table.getTable("USERS"));
                    Column column = new Column("USERS", "*");
                    us_qry.addSelectColumn(column);
                    Criteria cri = new Criteria(Column.getColumn("USERS", "USER_ID"), user_id, QueryConstants.EQUAL);
                    us_qry.setCriteria(cri);
                    dobj = DataAccess.get(us_qry);
                    Row r = dobj.getRow("USERS");
                    String usr_contact = (String) r.get(4);

                    Row rw = new Row("PARKINGRECORDS");
                    rw.set("SLOT_ID", slot_id);
                    rw.set("USER_ID", user_id);
                    rw.set("VEHICLE_NUM", vehicle_num);
                    rw.set("CONTACT_NUM", usr_contact);
                    rw.set("ENTRY", "04-03-2022 16:12:00");
                    dobj = new WritableDataObject();
                    dobj.addRow(rw);
                    DataAccess.add(dobj);
                    resp.getWriter().print(slot_id);
                } else {
                    resp.getWriter().write("failure");
                }
            } else if (action.equals("exitvehicle")) {
                String vehic_num = req.getParameter("vehic_num");
                SelectQuery query = new SelectQueryImpl(Table.getTable("PARKINGRECORDS"));
                Column c1 = new Column("PARKINGRECORDS", "*");
                query.addSelectColumn(c1);
                Criteria criteria1 = new Criteria(new Column("PARKINGRECORDS", "VEHICLE_NUM"), vehic_num,
                        QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(new Column("PARKINGRECORDS", "CHARGE"), 0, QueryConstants.EQUAL);
                Criteria criteria = criteria1.and(criteria2);
                query.setCriteria(criteria);
                DataObject dobj = DataAccess.get(query);
                Row row = dobj.getFirstRow("PARKINGRECORDS");
                int Parkingrec_id = (int) row.get(1);
                int slot_id = (int) row.get(2);
                String entry = (String) row.get(6);
                // out.println(Parkingrec_id+" "+slot_id+" "+entry);

                if (!(dobj.isEmpty())) {
                    UpdateQuery qry = new UpdateQueryImpl("PARKINGRECORDS");
                    Criteria criteria5 = new Criteria(new Column("PARKINGRECORDS", "PARKINGREC_ID"), Parkingrec_id,
                            QueryConstants.EQUAL);
                    qry.setCriteria(criteria5);
                    qry.setUpdateColumn("EXIT", "04-03-2022 17:12");
                    qry.setUpdateColumn("CHARGE", 200);
                    DataAccess.update(qry);

                    UpdateQuery uqry = new UpdateQueryImpl("SLOTS");
                    Criteria criteria6 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                    uqry.setCriteria(criteria6);
                    uqry.setUpdateColumn("IS_PARKED", 0);
                    DataAccess.update(uqry);
                    resp.getWriter().print(Parkingrec_id);
                } else {
                    resp.getWriter().write("failure");
                }

            } else if (action.equals("exit")) {
                int Parkingrec_id = 0;
                String vehic_num = req.getParameter("slotno");
                SelectQuery query = new SelectQueryImpl(Table.getTable("PARKINGRECORDS"));
                Column c1 = new Column("PARKINGRECORDS", "*");
                query.addSelectColumn(c1);
                Criteria criteria1 = new Criteria(new Column("PARKINGRECORDS", "VEHICLE_NUM"), vehic_num,
                        QueryConstants.EQUAL);
                Criteria criteria2 = new Criteria(new Column("PARKINGRECORDS", "CHARGE"), 0, QueryConstants.EQUAL);
                Criteria criteria = criteria1.and(criteria2);
                query.setCriteria(criteria);
                DataObject dobj = DataAccess.get(query);
                Row row1 = dobj.getFirstRow("PARKINGRECORDS");
                Parkingrec_id = (int) row1.get(1);
                int slot_id = (int) row1.get(2);
                String entry = (String) row1.get(6);
                // out.println(Parkingrec_id+" "+slot_id+" "+entry);

                if (Parkingrec_id == 0) {
                    resp.getWriter().write("failure");
                } else {
                    SelectQuery qy = new SelectQueryImpl(Table.getTable("SLOTS"));
                    Column cc = new Column("SLOTS", "*");
                    qy.addSelectColumn(cc);
                    Criteria crit = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                    qy.setCriteria(crit);
                    dobj = DataAccess.get(qy);
                    Row row = dobj.getFirstRow("SLOTS");
                    int floor_id = (int) row.get(2);
                    // out.println(floor_id);

                    UpdateQuery query1 = new UpdateQueryImpl("PARKINGRECORDS");
                    Criteria criteria5 = new Criteria(new Column("PARKINGRECORDS", "PARKINGREC_ID"), Parkingrec_id,
                            QueryConstants.EQUAL);
                    query1.setCriteria(criteria5);
                    query1.setUpdateColumn("EXIT", "04-03-2022 17:12");
                    query1.setUpdateColumn("CHARGE", 200);
                    DataAccess.update(query1);

                    UpdateQuery uqry = new UpdateQueryImpl("SLOTS");
                    Criteria criteria60 = new Criteria(new Column("SLOTS", "SLOT_ID"), slot_id, QueryConstants.EQUAL);
                    uqry.setCriteria(criteria60);
                    uqry.setUpdateColumn("IS_PARKED", 0);
                    DataAccess.update(uqry);

                    int count = 0;
                    RelationalAPI relapi = RelationalAPI.getInstance();
                    java.sql.Connection con = null;
                    UpdateQuery qry = new UpdateQueryImpl("FLOORS");
                    SelectQuery squery = new SelectQueryImpl(Table.getTable("SLOTS"));
                    Criteria criteria6 = new Criteria(new Column("SLOTS", "IS_PARKED"), 0, QueryConstants.NOT_EQUAL);
                    Criteria criteria61 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                            QueryConstants.EQUAL);
                    Criteria criteria62 = criteria6.and(criteria61);
                    squery.setCriteria(criteria62);
                    Join join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                            Join.INNER_JOIN);
                    squery.addJoin(join);
                    ArrayList<Column> lst = new ArrayList<Column>();
                    ArrayList<Column> lst2 = new ArrayList<Column>();
                    Column col = Column.getColumn("SLOTS", "IS_PARKED");
                    lst.add(col);
                    Column parked_cnt = col.count();
                    parked_cnt.setColumnAlias("P_COUNT");
                    lst.add(parked_cnt);
                    squery.addSelectColumns(lst);
                    con = relapi.getConnection();
                    DataSet ds = relapi.executeQuery(squery, con);
                    ds.next();
                    int cnt = (int) ds.getValue("P_COUNT");
                    // out.println(cnt);
                    SelectQuery sqry = new SelectQueryImpl(Table.getTable("SLOTS"));
                    Criteria criteria15 = new Criteria(new Column("SLOTS", "RESERVED_USER_ID"), null,
                            QueryConstants.NOT_EQUAL);
                    sqry.setCriteria(criteria15);
                    join = new Join("SLOTS", "FLOORS", new String[] { "FLOOR_ID" }, new String[] { "FLOOR_ID" },
                            Join.INNER_JOIN);
                    sqry.addJoin(join);
                    Column c2 = Column.getColumn("SLOTS", "RESERVED_USER_ID");
                    lst2.add(c2);
                    Column res_cnt = c2.count();
                    res_cnt.setColumnAlias("R_COUNT");
                    lst2.add(res_cnt);
                    sqry.addSelectColumns(lst2);
                    con = relapi.getConnection();
                    ds = relapi.executeQuery(sqry, con);
                    ds.next();
                    int cnt2 = (int) ds.getValue("R_COUNT");
                    // out.println(cnt2);
                    int tot_count = cnt + cnt2;
                    Criteria criteria7 = new Criteria(new Column("FLOORS", "CAPACITY"), tot_count,
                            QueryConstants.NOT_EQUAL);
                    Criteria criteria8 = new Criteria(new Column("FLOORS", "FLOOR_ID"), floor_id, QueryConstants.EQUAL);
                    Criteria criteria9 = criteria8.and(criteria7);
                    qry.setCriteria(criteria9);
                    qry.setUpdateColumn("IS_FULL", 0);
                    per.update(qry);
                    ds.close();
                    // out.println(Parkingrec_id);
                    resp.getWriter().print(Parkingrec_id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}