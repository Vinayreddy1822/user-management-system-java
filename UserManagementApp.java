package bikeconsultantapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class UserManagementApp {

    private static List<User> userList = new ArrayList<>();
    // History list is still just strings for the printer table, could be improved
    // but keeping minimal changes to logic
    private static List<String[]> userEntryHistory = new ArrayList<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
        JFrame frame = new JFrame("User Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        Font buttonFont = new Font("Arial", Font.BOLD, 20);

        JButton addUserButton = new JButton("Add User");
        addUserButton.setFont(buttonFont);
        addUserButton.setPreferredSize(new Dimension(200, 60));
        addUserButton.addActionListener(e -> openAddUserWindow());

        JButton searchUserButton = new JButton("Search User");
        searchUserButton.setFont(buttonFont);
        searchUserButton.setPreferredSize(new Dimension(200, 60));
        searchUserButton.addActionListener(e -> openSearchUserWindow());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(addUserButton, gbc);

        gbc.gridy = 1;
        panel.add(searchUserButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void openAddUserWindow() {
        JFrame addUserFrame = new JFrame("Add User");
        addUserFrame.setSize(600, 600);
        addUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addUserFrame.setLayout(new GridBagLayout());

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font textFieldFont = new Font("Arial", Font.PLAIN, 14);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Helper to add components
        int row = 0;
        JTextField nameField = addTextField(addUserFrame, "Customer Name:", gbc, labelFont, textFieldFont, row++);
        JTextField mobileField = addTextField(addUserFrame, "Mobile Number:", gbc, labelFont, textFieldFont, row++);
        JTextField amountField = addTextField(addUserFrame, "Amount:", gbc, labelFont, textFieldFont, row++);
        JTextField interestField = addTextField(addUserFrame, "Interest Rate:", gbc, labelFont, textFieldFont, row++);
        JTextField dateField = addTextField(addUserFrame, "Date (dd/MM/yyyy):", gbc, labelFont, textFieldFont, row++);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = row;
        addUserFrame.add(addressLabel, gbc);
        JTextArea addressField = new JTextArea(3, 10);
        addressField.setFont(textFieldFont);
        JScrollPane addressScrollPane = new JScrollPane(addressField);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        addUserFrame.add(addressScrollPane, gbc);
        row++;

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        addUserFrame.add(submitButton, gbc);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateFields(nameField.getText(), mobileField.getText(), amountField.getText(),
                        interestField.getText(), dateField.getText())) {

                    User newUser = new User(
                            nameField.getText(),
                            mobileField.getText(),
                            Double.parseDouble(amountField.getText()),
                            Double.parseDouble(interestField.getText()),
                            dateField.getText(),
                            Double.parseDouble(amountField.getText()),
                            "Y",
                            null,
                            addressField.getText());

                    // Add to DB in background
                    submitButton.setEnabled(false);
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            userDAO.addUser(newUser);
                            return null;
                        }

                        @Override
                        protected void done() {
                            submitButton.setEnabled(true);
                            try {
                                get();
                                // Success - update local lists
                                userList.add(newUser);
                                userEntryHistory.add(new String[] {
                                        newUser.getName(), newUser.getMobile(),
                                        String.valueOf(newUser.getAmount()), String.valueOf(newUser.getInterestRate()),
                                        newUser.getDate(), String.valueOf(newUser.getAmount()), null, "0"
                                });
                                customDialogBox("User added successfully!", "User Added");
                                addUserFrame.dispose();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                customDialogBox("Error adding user to DB: " + ex.getMessage(), "Error");
                            }
                        }
                    }.execute();
                }
            }
        });

        addUserFrame.setVisible(true);
    }

    private static JTextField addTextField(JFrame frame, String label, GridBagConstraints gbc, Font labelFont,
            Font textFont, int y) {
        JLabel l = new JLabel(label);
        l.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = y;
        frame.add(l, gbc);

        JTextField tf = new JTextField();
        tf.setFont(textFont);
        tf.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(tf, gbc);
        return tf;
    }

    private static void openSearchUserWindow() {
        JFrame searchUserFrame = new JFrame("Search User");
        searchUserFrame.setSize(600, 600);
        searchUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font textFieldFont = new Font("Arial", Font.PLAIN, 14);

        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(createLabel("Customer Name:", labelFont), gbc);
        gbc.gridx = 1;
        JTextField nameField = createTextField(textFieldFont);
        searchPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        searchPanel.add(createLabel("Mobile Number:", labelFont), gbc);
        gbc.gridx = 1;
        JTextField mobileField = createTextField(textFieldFont);
        searchPanel.add(mobileField, gbc);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(labelFont);
        gbc.gridx = 1;
        gbc.gridy = 2;
        searchPanel.add(searchButton, gbc);

        DefaultTableModel model = new DefaultTableModel(new String[] { "Customer Name", "Mobile Number", "Amount",
                "Interest Rate", "Date", "RemainingAmount", "IsFirstTime", "Action", "#" }, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        searchButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String mobile = mobileField.getText().trim();
            model.setRowCount(0);
            searchButton.setEnabled(false);

            new SwingWorker<List<User>, Void>() {
                @Override
                protected List<User> doInBackground() throws Exception {
                    return userDAO.searchUsers(name, mobile);
                }

                @Override
                protected void done() {
                    searchButton.setEnabled(true);
                    try {
                        List<User> results = get();
                        userList.clear();
                        userList.addAll(results);

                        for (User user : results) {
                            model.addRow(new Object[] {
                                    user.getName(), user.getMobile(), user.getAmount(),
                                    user.getInterestRate(), user.getDate(), user.getRemainingAmount(),
                                    user.getIsFirstTime(), "View", "Print"
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        customDialogBox("Error searching users: " + ex.getMessage(), "Error");
                    }
                }
            }.execute();
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    String name = table.getValueAt(row, 0).toString();
                    String mobile = table.getValueAt(row, 1).toString();
                    User user = getUserByNameOrMobile(name, mobile);

                    if (user != null) {
                        if (col == 7)
                            openViewUserWindow(user);
                        if (col == 8)
                            openPrintUserWindow(user);
                    }
                }
            }
        });

        searchUserFrame.add(searchPanel, BorderLayout.NORTH);
        searchUserFrame.add(scrollPane, BorderLayout.CENTER);
        searchUserFrame.setVisible(true);
    }

    private static JLabel createLabel(String text, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        return l;
    }

    private static JTextField createTextField(Font font) {
        JTextField tf = new JTextField();
        tf.setFont(font);
        tf.setPreferredSize(new Dimension(200, 30));
        return tf;
    }

    private static User getUserByNameOrMobile(String name, String mobile) {
        for (User user : userList) {
            if ((name != null && user.getName().equalsIgnoreCase(name)) ||
                    (mobile != null && user.getMobile().equals(mobile))) {
                return user;
            }
        }
        return null;
    }

    private static void openViewUserWindow(User user) {
        JFrame viewUserFrame = new JFrame("View User");
        viewUserFrame.setSize(600, 600);
        viewUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        viewUserFrame.setLayout(new GridBagLayout());

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font textFieldFont = new Font("Arial", Font.PLAIN, 14);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addReadOnlyField(viewUserFrame, "Customer Name:", user.getName(), gbc, labelFont, textFieldFont, row++);
        addReadOnlyField(viewUserFrame, "Mobile Number:", user.getMobile(), gbc, labelFont, textFieldFont, row++);
        addReadOnlyField(viewUserFrame, "Amount:", String.valueOf(user.getAmount()), gbc, labelFont, textFieldFont,
                row++);
        addReadOnlyField(viewUserFrame, "Interest Rate:", String.valueOf(user.getInterestRate()), gbc, labelFont,
                textFieldFont, row++);
        addReadOnlyField(viewUserFrame, "Date:", user.getDate(), gbc, labelFont, textFieldFont, row++);

        JLabel interestAmtLabel = new JLabel("Interest Amount as on Date:");
        interestAmtLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = row;
        viewUserFrame.add(interestAmtLabel, gbc);
        JTextField interestAmtField = new JTextField(calculateInterest(user));
        interestAmtField.setFont(textFieldFont);
        interestAmtField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        viewUserFrame.add(interestAmtField, gbc);
        row++;

        JTextField amountToBePaidField = addTextField(viewUserFrame, "Amount to be Paid:", gbc, labelFont,
                textFieldFont, row++);

        JLabel remainLabel = new JLabel("Remaining Principal:");
        remainLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = row;
        viewUserFrame.add(remainLabel, gbc);
        JTextField remainingAmountField = new JTextField(calculateRemainingAmount(user));
        remainingAmountField.setEditable(false);
        remainingAmountField.setFont(textFieldFont);
        remainingAmountField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        viewUserFrame.add(remainingAmountField, gbc);
        row++;

        addReadOnlyField(viewUserFrame, "Years:", String.valueOf(calculateNumberOfYears(user.getDate())), gbc,
                labelFont, textFieldFont, row++);
        addReadOnlyField(viewUserFrame, "Months:", String.valueOf(calculateNumberOfMonths(user)), gbc, labelFont,
                textFieldFont, row++);
        addReadOnlyField(viewUserFrame, "Days:", String.valueOf(calculateNumberOfDays(user)), gbc, labelFont,
                textFieldFont, row++);

        JTextField paymentDateField = addTextField(viewUserFrame, "Payment Date (dd/mm/yyyy):", gbc, labelFont,
                textFieldFont, row++);

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(labelFont);
        gbc.gridx = 1;
        gbc.gridy = row;
        viewUserFrame.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            if (validateViewFields(amountToBePaidField.getText(), paymentDateField.getText())) {
                submitButton.setEnabled(false);

                // Logic updates (keeping mostly same as original but cleaner)
                double paid = Double.parseDouble(amountToBePaidField.getText());
                double interest = Double.parseDouble(interestAmtField.getText());
                double currentRemaining = Double.parseDouble(remainingAmountField.getText());

                // Update User object logic
                // NOTE: This logic mimics original spaghetti but tries to be safer.
                // In a real app, logic should be clearer about WHAT gets reduced (principal vs
                // interest)
                if ("Y".equals(user.getIsFirstTime())) {
                    user.setRemainingAmount(user.getAmount()); // Reset base if needed? Original logic was weird.
                }

                if (paid <= interest) {
                    // Paying only interest or partial interest
                    user.setInterestAmount(interest - paid);
                    // Principle remains same
                    user.setRemainingAmount(currentRemaining);
                } else {
                    // Paying full interest + some principal
                    double principalDeduction = paid - interest;
                    user.setRemainingAmount(currentRemaining - principalDeduction);
                    user.setInterestAmount(0);
                }

                user.setIsFirstTime("N");
                user.setLastPaymentDate(paymentDateField.getText());

                // Update history in memory
                userEntryHistory.add(new String[] {
                        user.getName(), user.getMobile(),
                        String.valueOf(user.getAmount()), String.valueOf(user.getInterestRate()),
                        user.getDate(), String.valueOf(user.getRemainingAmount()),
                        paymentDateField.getText(), amountToBePaidField.getText()
                });

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        userDAO.addHistory(user, paid);
                        return null;
                    }

                    @Override
                    protected void done() {
                        submitButton.setEnabled(true);
                        try {
                            get();
                            customDialogBox("Data updated successfully!", "User updated");
                            viewUserFrame.dispose();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            customDialogBox("Error updating history: " + ex.getMessage(), "Error");
                        }
                    }
                }.execute();
            }
        });

        viewUserFrame.setVisible(true);
    }

    private static void addReadOnlyField(JFrame frame, String label, String value, GridBagConstraints gbc, Font lf,
            Font tf, int y) {
        JLabel l = new JLabel(label);
        l.setFont(lf);
        gbc.gridx = 0;
        gbc.gridy = y;
        frame.add(l, gbc);
        JTextField t = new JTextField(value);
        t.setEditable(false);
        t.setFont(tf);
        t.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(t, gbc);
    }

    private static void openPrintUserWindow(User user) {
        JFrame f = new JFrame("Table Printer");
        f.setSize(800, 500);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Filter history
        List<String[]> filteredHistory = new ArrayList<>();
        for (String[] entry : userEntryHistory) {
            // simplified logic: matches name OR matches mobile
            if ((user.getName() != null && entry[0] != null && entry[0].equalsIgnoreCase(user.getName())) ||
                    (user.getMobile() != null && entry[1] != null && entry[1].equals(user.getMobile()))) {
                filteredHistory.add(entry);
            }
        }

        String[][] data = filteredHistory.toArray(new String[0][]);
        String[] columns = { "Customer Name", "Mobile Number", "Amount", "Interest Rate", "Creation Date",
                "Remaining Principal", "Payment Date", "Amount Paid" };

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        f.add(scrollPane, BorderLayout.CENTER);

        JButton printButton = new JButton("Print Table");
        printButton.addActionListener(e -> {
            try {
                boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Table Print"),
                        new MessageFormat("Page - {0}"));
                if (complete)
                    customDialogBox("Print Completed!", "Print");
                else
                    customDialogBox("Print Cancelled!", "Print");
            } catch (PrinterException pe) {
                customDialogBox("Print Failed: " + pe.getMessage(), "Print");
            }
        });
        f.add(printButton, BorderLayout.SOUTH);
        f.setVisible(true);
    }

    // Calculations
    private static String calculateRemainingAmount(User user) {
        if ("Y".equals(user.getIsFirstTime())) {
            return String.valueOf(user.getAmount());
        }
        return String.valueOf(user.getRemainingAmount());
    }

    private static String calculateInterest(User user) {
        double interestAmountPerMonth;
        long noOfDays = calculateNumberOfDays(user);
        int noOfMonths = calculateNumberOfMonths(user);

        if ("Y".equals(user.getIsFirstTime())) {
            interestAmountPerMonth = user.getAmount() / 100 * user.getInterestRate();
            double total = noOfMonths * interestAmountPerMonth + (interestAmountPerMonth / 30 * noOfDays);
            return String.valueOf(Math.round(total));
        } else {
            // Logic for recurring payments
            interestAmountPerMonth = user.getRemainingAmount() / 100 * user.getInterestRate();
            double interestAmountDays = noOfMonths * interestAmountPerMonth + (interestAmountPerMonth / 30 * noOfDays);
            double total = user.getInterestAmount() + interestAmountDays;
            return String.valueOf(Math.round(total));
        }
    }

    private static long calculateNumberOfYears(String startDate) {
        try {
            Date d1 = sdf.parse(startDate);
            Date d2 = sdf.parse(sdf.format(new Date()));
            long diff = d2.getTime() - d1.getTime();
            return diff / (1000L * 60 * 60 * 24 * 365);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static long calculateNumberOfDays(User user) {
        try {
            String startDate = "Y".equals(user.getIsFirstTime()) ? user.getDate() : user.getLastPaymentDate();
            if (startDate == null)
                return 0;

            Date d1 = sdf.parse(startDate);
            Date d2 = sdf.parse(sdf.format(new Date()));
            int months = calculateNumberOfMonths(user);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            cal.add(Calendar.MONTH, months);
            Date d3 = cal.getTime();
            long diff = d2.getTime() - d3.getTime();
            return (diff / (1000 * 60 * 60 * 24)) % 365;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    private static int calculateNumberOfMonths(User user) {
        try {
            String startDate = "Y".equals(user.getIsFirstTime()) ? user.getDate() : user.getLastPaymentDate();
            if (startDate == null)
                return 0;

            Calendar start = Calendar.getInstance();
            start.setTime(sdf.parse(startDate));
            Calendar end = Calendar.getInstance();
            end.setTime(sdf.parse(sdf.format(new Date()))); // strip time

            int months = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
                    (end.get(Calendar.MONTH) - start.get(Calendar.MONTH));
            return Math.max(0, months);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void customDialogBox(String msg, String title) {
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 16));
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private static boolean validateFields(String name, String mobile, String amount, String interest, String date) {
        if (!name.matches("[a-zA-Z ]+")) {
            customDialogBox("Invalid name. Only alphabets are allowed.", "Customer Name");
            return false;
        }
        if (!mobile.matches("\\d{10}")) {
            customDialogBox("Invalid mobile number. Must be 10 digits.", "Mobile number");
            return false;
        }
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            customDialogBox("Invalid amount.", "Total Amount");
            return false;
        }
        try {
            Double.parseDouble(interest);
        } catch (NumberFormatException e) {
            customDialogBox("Invalid interest rate.", "Interest Rate");
            return false;
        }
        try {
            sdf.setLenient(false);
            sdf.parse(date);
        } catch (Exception e) {
            customDialogBox("Invalid date (dd/MM/yyyy).", "Creation Date");
            return false;
        }
        return true;
    }

    private static boolean validateViewFields(String amount, String paymentDate) {
        if (amount == null || amount.isEmpty() || Double.parseDouble(amount) <= 0) {
            customDialogBox("Please enter valid amount.", "Amount");
            return false;
        }
        try {
            sdf.setLenient(false);
            sdf.parse(paymentDate);
        } catch (Exception e) {
            customDialogBox("Invalid date (dd/MM/yyyy).", "Payment Date");
            return false;
        }
        return true;
    }
}
