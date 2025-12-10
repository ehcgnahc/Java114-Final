package main;

import bank.core.*;
import bank.model.User;

public class Main {
    public static void main(String[] args){
        BankSystem bankNetwork = new BankSystem();

        Bank bankA = new Bank("BankA");
        Bank bankB = new Bank("BankB");

        ATM atmA = new ATM(bankNetwork, bankA);

        bankNetwork.addBank(bankA);
        bankNetwork.addBank(bankB);

        User User1 = new User("ehcgnahc", 5000);
        User User2 = new User("ehcgnahc2", 5000);

        Card Card1 = bankA.createAcc("test01", "password");
        Card Card2 = bankB.createAcc("test02", "password");

        User1.setCard(Card1);
        User2.setCard(Card1);
        
        // atmA.insertCard(Card1); //待修正
        atmA.insertCard(User1.getCard());

        atmA.checkBalance();
        atmA.deposit(1234);
        atmA.login("null");
        atmA.deposit(1234);
        atmA.login("password");
        atmA.checkBalance();
        atmA.deposit(1234);
        atmA.withdraw(234);
        atmA.checkBalance();
        atmA.ejectCard();


        // bankNetwork.deposit(User1.getCard(), "password", 500);
        // bankNetwork.

        // myBank.deposit("TEST", 12333);
        // myBank.deposit("1233", 3333);
        // myBank.getBalance("TEST");
        // myBank.getBalance("1233");
    }
}