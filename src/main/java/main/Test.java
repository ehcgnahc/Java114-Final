package main;

import java.util.List;
import bank.core.*;

public class Test {
    public static void main(String[] args){
        // 1. 初始化系統
        BankSystem bankNetwork = new BankSystem();
        Bank bankA = new Bank("BankA");
        Bank bankB = new Bank("BankB");

        bankNetwork.addBank(bankA);
        bankNetwork.addBank(bankB);
        
        bankNetwork.setCrossBankFee("BankA", "BankB", 5.0);
        bankNetwork.setCrossBankFee("BankB", "BankA", 15.0);

        ATM atmA = new ATM(bankNetwork, bankA);
        ATM atmB = new ATM(bankNetwork, bankB);

        // 2. 初始化使用者
        User User1 = new User("ehcgnahc", 5000);
        User User2 = new User("ehcgnahc2", 5000);

        // 3. 開戶 (User1 在 BankA 開兩個，BankB 開一個)
        bankA.createAcc(User1, "1234");
        bankA.createAcc(User1, "1234");
        bankB.createAcc(User1, "5678");
        
        // User2 開戶
        bankA.createAcc(User2, "0000");
        bankB.createAcc(User2, "0000");

        System.out.println("User1 目前持有的卡片數量: " + User1.getCard().size());

        // 4. 取得 User1 在 BankA 的所有存摺
        List<PassBook> passBooks = User1.getPassBookListByBank("BankA");
        System.out.println("User1 在 BankA 有 " + passBooks.size() + " 本存摺");
        
        // 取得第一本存摺物件 (方便後續操作)
        PassBook myPassBook = passBooks.get(0);
        String myAccID = myPassBook.getAccID();

        // 5. 使用 ATM 存款
        System.out.println("\n--- 開始 ATM 操作 ---");
        // 使用與該存摺對應的卡片插入 ATM
        atmA.insertCard(User1.getCardByAccount("BankA", myAccID));
        atmA.login("1234");
        atmA.checkBalance();
        atmA.deposit(1000); // 存 1000
        atmA.checkBalance(); // 餘額應變動
        atmA.ejectCard();

        // 6. 查看存摺 (此時應該要是空的，或是舊的，因為還沒補登)
        System.out.println("\n--- 補登前查看存摺 ---");
        myPassBook.printHistory(); 

        // 7. 執行補登 (這步最關鍵！)
        System.out.println("\n--- 執行補登 ---");
        bankA.updatePassBook(myPassBook);

        // 8. 再次查看存摺
        System.out.println("\n--- 補登後查看存摺 ---");
        myPassBook.printHistory();

        // 9. 測試轉帳邏輯 (修正你的註解程式碼)
        /* 
           原本你的寫法: atmA.transfer("BankB", User1.getPassBookByAccount(...), 100);
           錯誤點: transfer 第二個參數要 String (帳號ID)，但你傳了 PassBook 物件
        */
        System.out.println("\n--- 測試轉帳 ---");
        atmA.insertCard(User1.getCardByAccount("BankA", myAccID));
        atmA.login("1234");
        
        // 假設我們要轉給 User1 在 BankB 的帳戶
        PassBook targetBook = User1.getPassBookListByBank("BankB").get(0);
        String targetAccID = targetBook.getAccID();
        
        atmA.transfer("BankB", targetAccID, 100); // 正確傳遞 ID
        atmA.ejectCard();

        // 轉帳後再次補登並查看
        bankA.updatePassBook(myPassBook);
        myPassBook.printHistory();
    }
}