package bank.core;

public class Account {
    private double balance;
    private String accID;
    private String passwordHash;

    public Account(String ID, String password){
        this.accID = ID;
        this.balance = 0.0;
        this.passwordHash = hash(password);
    }

    private String hash(String input){
        return Integer.toHexString(input.hashCode());
    }

    boolean checkPassword(String password){
        return this.passwordHash.equals(hash(password));
    }

    double getBalance(){
        return this.balance;
    }

    void deposit(double amount){
        this.balance += amount;
    }

    void withdraw(double amount){
        this.balance -= amount;
    }
}
