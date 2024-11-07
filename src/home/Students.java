
package home;

import db.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class Students {
    Connection con=(Connection) MyConnection.getConnection();
    PreparedStatement ps;
    
    //insert data into students table
    public void insert(String admission,String sname,String pnumber,String card,String balance,String status){
        String sql ="insert into students values(?,?,?,?,?,?)";
        try{
            ps=con.prepareStatement(sql);
            ps.setString(1, admission);
            ps.setString(2, sname);
            ps.setString(3, pnumber);
            ps.setString(4, card);
            ps.setString(5, balance);
            ps.setString(6, status);
            if(ps.executeUpdate()>0){
                JOptionPane.showMessageDialog(null, "New Student Added Successfully");
            }
        }
         catch (SQLIntegrityConstraintViolationException ex) {
            // Show duplicate entry error in a dialog box
            JOptionPane.showMessageDialog(null, 
                    "Error: A student with Admission " + admission + " already exists.", 
                    "Duplicate Entry Error", 
                    JOptionPane.ERROR_MESSAGE);

        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                    "Database error: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public boolean isCardExist(String card){
        try{
            ps=con.prepareStatement("select * from students where card_number = ?");
            ps.setString(1,card);
            ResultSet rs =ps.executeQuery();
            if(rs.next()){
                return true;
            }
        }
        catch(SQLException ex){
            Logger.getLogger(Students.class.getName()).log(Level.SEVERE,null,ex);
        }
        return false;
    }
    public void getStudentValue(JTable table,String searchValue){
        String sql ="SELECT * FROM students WHERE concat( student_id,Student_name,parent_number,card_number,balance,status) LIKE ? order by student_id desc";
        try{
           ps=con.prepareStatement(sql);
           ps.setString(1, "%"+searchValue+"%");
           ResultSet rs=ps.executeQuery();
           DefaultTableModel model=(DefaultTableModel) table.getModel();
           boolean studentFound = false;
           model.setRowCount(0);
           Object[] row;
            while (rs.next()) {
                row = new Object[6];
                row[0] = rs.getString(1);
                row[1] = rs.getString(2);
                row[2] = rs.getString(3);
                row[3] = rs.getString(4);
                row[4] = rs.getString(5);
                row[5] = rs.getString(6);
                model.addRow(row);
            }
           
        }
        catch(SQLException ex){
            Logger.getLogger(Students.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    public void update(String admission,String sname,String pnumber,String card,String balance,String status){
        String sql ="UPDATE students SET Student_name = ?, parent_number = ?, card_number = ?, balance = ?, status = ? WHERE student_id = ?";
        try{
            ps=con.prepareStatement(sql);
            ps.setString(1, sname);
            ps.setString(2, pnumber);
            ps.setString(3, card);
            ps.setString(4, balance);
            ps.setString(5, status);
            ps.setString(6, admission);
            if(ps.executeUpdate()>0){
                JOptionPane.showMessageDialog(null, "Student Update Successfully");
            }
        }
            catch(SQLException ex){
            Logger.getLogger(Students.class.getName()).log(Level.SEVERE,null,ex);
        }
            
    }
}
