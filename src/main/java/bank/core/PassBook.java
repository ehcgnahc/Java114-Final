package bank.core;

public class PassBook {
    private String accID;
    private String bankID;

    public PassBook(String accID, String bankID) {
        this.accID = accID;
        this.bankID = bankID;
    }

    public String getAccID(){
        return this.accID;
    }
    
    public String getBankID(){
        return this.bankID;
    }
}
