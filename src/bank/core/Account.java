package bank.core;

public class Account {
    private double balance;
    private String accID;

    public Account(String ID){
        this.accID = ID;
        this.balance = 0.0;
    }

    void deposit(double amount){
        this.balance += amount;
    }

    void withdraw(double amount){
        this.balance -= amount;
    }

    public double getBalance(){
        return balance;
    }
}
