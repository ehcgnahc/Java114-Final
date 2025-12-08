package main;

import bank.core.*;
import bank.model.User;

public class Main {
    public static void main(String[] args){
        BankSystem bankNetwork = new BankSystem();

        Bank bankA = new Bank("BankA");
        Bank bankB = new Bank("BankB");

        bankNetwork.addBank(bankA);
        bankNetwork.addBank(bankB);

        User User1 = new User("ehcgnahc", 5000);

        Card Card1 = bankA.createAcc("test01");
        Card Card2 = bankB.createAcc("test02");

        User1.setCard(Card1);
        User1.setCard(Card2);


        bankNetwork.deposit(User1.getCard(), 500);

        // myBank.deposit("TEST", 12333);
        // myBank.deposit("1233", 3333);
        // myBank.getBalance("TEST");
        // myBank.getBalance("1233");
    }
}