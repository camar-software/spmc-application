package topups;


public class MpesaTest {

    public static void main(String[] args) {
        // Simulated test data
        String phoneNumber = "254708374149"; // Replace with a test phone number
        double amount = 100.00; // Test amount
        String cardNumber = "123456789"; // Example card number
        String student_id = "1001"; // Example student ID

        // Create a single instance of MpesaAPI to handle the STK push and update
        MpesaAPI mpesaAPI = new MpesaAPI();

        try {
            // Initiate the STK push to the provided phone number for the given amount
            mpesaAPI.initiateSTKPush(phoneNumber, amount, cardNumber);

            // Update the database after receiving a confirmation
            mpesaAPI.updateTopUpRecord(student_id, amount);

            System.out.println("Top-up process completed. Check your database for updates.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred during the top-up test.");
        }
    }
}


