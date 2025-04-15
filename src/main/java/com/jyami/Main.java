package com.jyami;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// gc options:
// -Xms10m
// -Xmx50m
// -Xlog:gc*:logs/gc.log:t,l,tg:filecount=10,filesize=10M
// -XX:+HeapDumpOnOutOfMemoryError
// -XX:HeapDumpPath=logs
//

public class Main {

    public static void main(String[] args) throws InterruptedException {
        List<String> list = new ArrayList<>();
        System.out.println("Hello World!");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter command (add/remove/exit):");
            String command = scanner.nextLine();

            switch (command) {
                case "add":
                    System.out.println("Enter item to add:");
                    String itemToAdd = scanner.nextLine();
                    list.add(itemToAdd);
                    System.out.println("Item added: " + itemToAdd);
                    break;

                case "remove":
                    System.out.println("Enter item to remove:");
                    String itemToRemove = scanner.nextLine();
                    if (list.remove(itemToRemove)) {
                        System.out.println("Item removed: " + itemToRemove);
                    } else {
                        System.out.println("Item not found: " + itemToRemove);
                    }
                    break;

                case "exit":
                    System.out.println("Exiting program.");
                    return;

                case "loop":
                    System.out.println("start loop");
                    int count = 0;
                    while (true) {
                        list.add("loop:" + count++);
                        if (count % 1000 == 0) {
                            System.out.println("Item add count: " + count);
                             Thread.sleep(10); // 주석 해제 시 지연 가능
                        }
                    }

                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }
}
