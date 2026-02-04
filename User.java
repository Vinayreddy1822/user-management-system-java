package bikeconsultantapp;

public class User {
    private String customerName;
    private String mobileNumber;
    private double amount;
    private double interestRate;
    private String creationDate;
    private double remainingAmount;
    private String isFirstTime;
    private String lastPaymentDate;
    private double interestAmount;
    private String address;

    public User(String name, String mobile, double amount, double interestRate, String date, double remainingAmount,
            String isFirstTime, String lastPaymentDate, String address) {
        this.customerName = name;
        this.mobileNumber = mobile;
        this.amount = amount;
        this.interestRate = interestRate;
        this.creationDate = date;
        this.remainingAmount = remainingAmount;
        this.isFirstTime = isFirstTime;
        this.lastPaymentDate = lastPaymentDate;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(String lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public String getIsFirstTime() {
        return isFirstTime;
    }

    public void setIsFirstTime(String isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getName() {
        return customerName;
    }

    public String getMobile() {
        return mobileNumber;
    }

    public double getAmount() {
        return amount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getDate() {
        return creationDate;
    }
}
