package bank.core;

public class Card {
    private String cardID;
    private String accID;
    private String bankID;

    public Card(String cardID, String accID, String bankID){
        this.cardID = cardID;
        this.accID = accID;
        this.bankID = bankID;
    }

    public String getCardID(){
        return cardID;
    }

    public String getAccID(){
        return accID;
    }

    public String getBankID(){
        return bankID;
    }
}
