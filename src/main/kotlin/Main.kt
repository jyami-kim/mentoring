package com.jyami

// gc options:
// -Xms10m
// -Xmx50m
// -Xlog:gc*:logs/gc.log:t,l,tg:filecount=10,filesize=10M
// -XX:+HeapDumpOnOutOfMemoryError
// -XX:HeapDumpPath=logs
//

fun main() {
    val list = mutableListOf<String>()
    println("Hello World!")

    val scanner = java.util.Scanner(System.`in`)

    while (true) {
        println("Enter command (add/remove/exit):")
        when (scanner.nextLine()) {
            "add" -> {
                println("Enter item to add:")
                val item = scanner.nextLine()
                list.add(item)
                println("Item added: $item")
            }

            "remove" -> {
                println("Enter item to remove:")
                val item = scanner.nextLine()
                if (list.remove(item)) {
                    println("Item removed: $item")
                } else {
                    println("Item not found: $item")
                }
            }

            "exit" -> {
                println("Exiting program.")
                break
            }

            "loop" -> {
                println("start loop")
                var count = 0
                while (true) {
                    list.add("loop:${count++}")
                    if (count % 1000 == 0) {
                        println("Item add count: $count")
//                        Thread.sleep(10)
                    }
                }
            }

            else -> println("Invalid command.")
        }
    }
}
