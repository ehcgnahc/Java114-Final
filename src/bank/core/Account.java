package bank.core;

import java.security.MessageDigest;

public class Account {
    private double balance;
    private String accID;
    private String passwordHash;

    public Account(String accID, String password){
        this.accID = accID;
        this.balance = 0.0;
        this.passwordHash = hash(password);
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

    void deposit(double amount){
        this.balance += amount;
    }

    void withdraw(double amount){
        this.balance -= amount;
    }
}
