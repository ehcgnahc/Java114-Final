package bank.core;

import java.time.LocalDate;

public class TransactionHistory {
    private LocalDate timestamp;
    private String type;
    private double amount;
    private double balance;
    private String msg;

    public TransactionHistory(String type, double amount, double balance, String msg){
        this.timestamp = LocalDate.now();
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.msg = msg;
    }

    public String toString(){
        return String.format("[%s] %s: $%.2f | 餘額: $%.2f | 備註: %s |", 
            this.timestamp, this.type, this.amount, this.balance, this.msg);
    }
}
