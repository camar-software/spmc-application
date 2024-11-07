
package pos;

import db.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;


public class payment {

    Connection con = (Connection) MyConnection.getConnection();
    PreparedStatement ps;

    public void processPayment(String cardNumber, double amount) {
        try {
            // Check if card exists and fetch student_id, status, and balance
            String query = "SELECT student_id, status, balance FROM students WHERE card_number = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, cardNumber);
            ResultSet rs =ps.executeQuery();

            if (rs.next()) {
                String studentId = rs.getString("student_id");
                String status = rs.getString("status");
                double currentBalance = rs.getDouble("balance");

                // Check if the card is active
                if (!"Activate".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(null, "Payment failed: Card is deactivated.");
                    return;
                }

                // Check if the balance is sufficient
                if (currentBalance < amount) {
                    JOptionPane.showMessageDialog(null, "Payment failed: Insufficient balance.");
                    return;
                }

                // Deduct the amount and update the balance
                double newBalance = currentBalance - amount;
                String updateQuery = "UPDATE students SET balance = ? WHERE card_number = ?";
                ps = con.prepareStatement(updateQuery);
                ps.setDouble(1, newBalance);
                ps.setString(2, cardNumber);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated > 0) {
                    // Insert transaction record into the transactions table
                    String insertTransactionQuery = "INSERT INTO transactions (student_id, amount, balance_after) VALUES (?, ?, ?)";
                    ps = con.prepareStatement(insertTransactionQuery);
                    ps.setString(1, studentId);
                    ps.setDouble(2, amount);
                    ps.setDouble(3, newBalance);

                    int transactionInserted = ps.executeUpdate();
                    if (transactionInserted > 0) {
                        JOptionPane.showMessageDialog(null, "Payment successful! New balance: " + newBalance);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: Unable to log the transaction.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Unable to update the balance.");
                }
            } else {
                // Card not found
                JOptionPane.showMessageDialog(null, "Payment failed: Card not found.");
            }

        } catch (SQLException ex) {
            Logger.getLogger(payment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


