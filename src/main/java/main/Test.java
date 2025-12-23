package main;

import java.util.List;
import bank.core.*;

public class Test {
    public static void main(String[] args){
        BankSystem bankNetwork = new BankSystem();
        Bank bankA = new Bank("BankA");
        Bank bankB = new Bank("BankB");
        bankNetwork.addBank(bankA);
        bankNetwork.addBank(bankB);
        bankNetwork.setCrossBankFee("BankA", "BankB", 5.0);
        bankNetwork.setCrossBankFee("BankB", "BankA", 15.0);

        ATM atmA = new ATM(bankNetwork, bankA);
        ATM atmB = new ATM(bankNetwork, bankB);

        User User1 = new User("ehcgnahc", 5000);
        User User2 = new User("ehcgnahc2", 5000);

        bankA.createAcc(User1, "1234");
        bankA.createAcc(User1, "1234");
        bankB.createAcc(User1, "5678");
        bankA.createAcc(User2, "0000");
        bankB.createAcc(User2, "0000");

        System.out.println("User1 目前持有的卡片數量: " + User1.getCard().size());

        List<PassBook> passBooks = User1.getPassBookListByBank("BankA");
        System.out.println("User1 在 BankA 有 " + passBooks.size() + " 本存摺");

        PassBook myPassBook = passBooks.get(0);
        String myAccID = myPassBook.getAccID();

        System.out.println("\n--- 開始 ATM 操作 ---");
        atmA.insertCard(User1.getCardByAccount("BankA", myAccID));
        atmA.login("1234");
        atmA.checkBalance();
        atmA.deposit(1000);
        atmA.checkBalance();
        atmA.ejectCard();

        System.out.println("\n--- 補登前查看存摺 ---");
        myPassBook.printHistory();

        System.out.println("\n--- 執行補登 ---");
        bankA.updatePassBook(myPassBook);

        System.out.println("\n--- 補登後查看存摺 ---");
        myPassBook.printHistory();

        System.out.println("\n--- 測試轉帳 ---");
        atmA.insertCard(User1.getCardByAccount("BankA", myAccID));
        atmA.login("1234");

        PassBook targetBook = User1.getPassBookListByBank("BankB").get(0);
        String targetAccID = targetBook.getAccID();

        atmA.transfer("BankB", targetAccID, 100);
        atmA.ejectCard();

        bankA.updatePassBook(myPassBook);
        myPassBook.printHistory();
    }
}