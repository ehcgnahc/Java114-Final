package bank.model;

import bank.core.Card;

public class User {
    private String name;
    private double cash;
    private Card cardID;

    public User(String name, double cash){
        this.name = name;
        this.cash = cash;
    }

    public void setCard(Card card){
        this.cardID = card;
        System.out.println(this.name + " 收到了卡片，卡號是：" + card.getCardID());
    }

    public Card getCard(){
        return this.cardID;
    }
}
