package topups;

import db.MyConnection;
import java.io.*;
import java.net.*;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MpesaAPI {

    Connection con = MyConnection.getConnection();
    PreparedStatement ps;

    private static final String CONSUMER_KEY = "jVHFGZ4Inne9QLfsCXvtGype4u5tZLzgjtwhzsGfFuQF4AOW";
    private static final String CONSUMER_SECRET = "0z9vTWF3qB5R8MatnXF282EAzfvkpWR7SrXvTUNIAjoQ2G0r4TeEc9GcAGxxrGRa";

    public static String getAccessToken() throws IOException {
        String url = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
        String authString = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes("UTF-8"));

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Basic " + encodedAuth);
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                // Extract access token (assuming JSON format)
                return response.toString(); // Parse JSON here to get the actual token value
            }
        } else {
            // Handle error response and avoid NullPointerException
            InputStream errorStream = con.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String inputLine;
                    while ((inputLine = errorReader.readLine()) != null) {
                        errorResponse.append(inputLine);
                    }
                    System.out.println("Error response: " + errorResponse.toString());
                }
            }
            throw new IOException("Failed to get access token. HTTP response code: " + responseCode);
        }
    }

    public void initiateSTKPush(String phoneNumber, double amount, String cardNumber) {
        try {
            String accessToken = getAccessToken();
            String stkPushUrl = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";

            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            URL url = new URL(stkPushUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + accessToken);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{"
                    + "\"BusinessShortCode\": \"174379\","
                    + "\"Password\": \"MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkx\","
                    + "\"Timestamp\": \"" + timestamp + "\","
                    + "\"TransactionType\": \"CustomerPayBillOnline\","
                    + "\"Amount\": \"" + amount + "\","
                    + "\"PartyA\": \"" + phoneNumber + "\","
                    + "\"PartyB\": \"174379\","
                    + "\"PhoneNumber\": \"" + phoneNumber + "\","
                    + "\"CallBackURL\": \"https://mydomain.com/path\","
                    + "\"AccountReference\": \"" + cardNumber + "\","
                    + "\"TransactionDesc\": \"Top-up for card " + cardNumber + "\""
                    + "}";

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("STK Push Response: " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTopUpRecord(String student_id, double amount) {
        try {
            // Insert top-up record
            String insertQuery = "INSERT INTO topups (student_id, amount, date_time, paymentMethod) VALUES (?, ?, NOW(), 'Mpesa')";
            ps = con.prepareStatement(insertQuery);
            ps.setString(1, student_id);
            ps.setDouble(2, amount);
            ps.executeUpdate();

            // Update balance
            String updateBalanceQuery = "UPDATE students SET balance = balance + ? WHERE student_id = ?";
            ps = con.prepareStatement(updateBalanceQuery);
            ps.setDouble(1, amount);
            ps.setString(2, student_id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Top-up successful! Record saved.");
        } catch (SQLException ex) {
            Logger.getLogger(MpesaAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
