package bank.core;

public class User {
    private String name;
    private double cash;
    private Card cardID;

    public User(String name, double cash){
        this.name = name;
        this.cash = cash;
    }

    public String getName(){
        return this.name;
    }

    void setCard(Card card){
        this.cardID = card;
        System.out.println(this.name + " 收到了卡片，卡號是: " + card.getCardID());
    }

    public Card getCard(){
        return this.cardID;
    }

    public double getCash(){
        return this.cash;
    }

    public void receiveCash(double amount){
        this.cash += amount;
    }

    public boolean giveCash(double amount){
        if(this.cash < amount){
            System.out.println("現金不足");
            return false;
        }

        this.cash -= amount;
        return true;
    }
}