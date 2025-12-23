package bank.core;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private double balance;
    private String accID;
    private String passwordHash;
    private List<TransactionHistory> transactions = new ArrayList<>();


    public Account(String accID, String password){
        this.accID = accID;
        this.balance = 0.0;
        this.passwordHash = hash(password);
        this.transactions.add(new TransactionHistory("開戶", 0.0, 0.0, " "));
    }

    private String hash(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for(byte b : encodedhash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    boolean checkPassword(String password){
        return this.passwordHash.equals(hash(password));
    }

    double getBalance(){
        return this.balance;
    }

    void deposit(double amount, String msg){
        this.balance += amount;
        this.transactions.add(new TransactionHistory("存入", amount, balance, msg));
    }

    void withdraw(double amount, String msg){
        this.balance -= amount;
        this.transactions.add(new TransactionHistory("提款", amount, balance, msg));
    }

    List<TransactionHistory> getTransactions(){
        return this.transactions;
    }
}
