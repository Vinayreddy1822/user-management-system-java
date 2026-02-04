package bikeconsultantapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void addUser(User user) throws SQLException {
        String query = "INSERT INTO Customer (Customer_Name, Mobile_Number, Amount, Interest_Rate, Creation_Date, Remaining_Amount, Is_First_Time, Last_Payment_Date, Interest_Amount, Address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, user.getName());
            pst.setString(2, user.getMobile());
            pst.setDouble(3, user.getAmount());
            pst.setDouble(4, user.getInterestRate());
            pst.setString(5, user.getDate());
            pst.setDouble(6, user.getRemainingAmount());
            pst.setString(7, user.getIsFirstTime());
            pst.setString(8, user.getLastPaymentDate());
            pst.setDouble(9, user.getInterestAmount());
            pst.setString(10, user.getAddress());

            pst.executeUpdate();
        }
    }

    public List<User> searchUsers(String name, String mobile) throws SQLException {
        List<User> userList = new ArrayList<>();
        // Use wildcards for partial name search if name is provided
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasMobile = mobile != null && !mobile.trim().isEmpty();

        if (!hasName && !hasMobile)
            return userList; // Return empty if no search criteria

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM Customer WHERE 1=0");
        if (hasName)
            queryBuilder.append(" OR LOWER(Customer_Name) LIKE ?");
        if (hasMobile)
            queryBuilder.append(" OR Mobile_Number = ?");

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            if (hasName) {
                pst.setString(paramIndex++, "%" + name.toLowerCase() + "%");
            }
            if (hasMobile) {
                pst.setString(paramIndex++, mobile);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapResultSetToUser(rs));
                }
            }
        }
        return userList;
    }

    public void addHistory(User user, double paidAmount) throws SQLException {
        String query = "INSERT INTO Customers_History(Cust_ID, Customer_Name, Mobile_Number, Amount, Interest_Rate, Creation_Date, Remaining_Amount, Remaining_Principal, Payment_Date, Amount_Paid) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Note: Cust_ID should ideally be fetched/passed. Using placeholder 2 as in
        // original code logic (hardcoded 2 there).
        // A better approach would be to have user.getId() but we stick to current
        // schema limits for now.
        int placeholderCustId = 2;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, placeholderCustId);
            pst.setString(2, user.getName());
            pst.setString(3, user.getMobile());
            pst.setDouble(4, user.getAmount());
            pst.setDouble(5, user.getInterestRate());
            pst.setString(6, user.getDate());
            pst.setDouble(7, user.getRemainingAmount());
            pst.setDouble(8, 0.0); // Remaining principal placeholder from original code
            pst.setString(9, user.getLastPaymentDate()); // Confusingly, this is the current payment date
            pst.setDouble(10, paidAmount);

            pst.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("Customer_Name"),
                rs.getString("Mobile_Number"),
                rs.getDouble("Amount"),
                rs.getDouble("Interest_Rate"),
                rs.getString("Creation_Date"),
                rs.getDouble("Remaining_Amount"),
                rs.getString("Is_First_Time"),
                rs.getString("Last_Payment_Date"),
                rs.getString("Address"));
        // Note: original code's User constructor was messy.
        // We'll trust the User class we created covers this.
        // We might need to fetch Interest_Amount and Address to fully populate the User
        // object for the View window.
    }
}
