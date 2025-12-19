package bank.core;

public class TransactionResult {
    private boolean success;
    private String message;
    private double balance;

    public TransactionResult(boolean success, String message, double balance){
        this.success = success;
        this.message = message;
        this.balance = balance;
    }

    public boolean isSuccess(){
        return this.success;
    }

    public String getMessage(){
        return this.message;
    }

    public double getBalance(){
        return this.balance;
    }
}
