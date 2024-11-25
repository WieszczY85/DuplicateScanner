package pl.syntaxdevteam

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Scanner

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            val scanner = Scanner(System.`in`)
            println("Podaj ścieżkę początkową:")
            val startDirPath = scanner.nextLine()
            val startDir = File(startDirPath)

            if (!startDir.exists() || !startDir.isDirectory) {
                println("Podana ścieżka nie jest prawidłowym katalogiem.")
                return@runBlocking
            }

            println("Proszę czekać, przetwarzanie plików...")

            val reportFile = File("raport.txt") // Zmień na ścieżkę do pliku raportu
            val fileHashes = mutableMapOf<String, MutableList<File>>()

            val jobs = mutableListOf<Job>()

            startDir.walkTopDown().forEach { file ->
                if (file.isDirectory) {
                    println("Sprawdzanie katalogu: ${file.absolutePath}")
                }
                if (file.isFile) {
                    val job = launch(Dispatchers.IO) {
                        val hash = file.md5()
                        synchronized(fileHashes) {
                            fileHashes.computeIfAbsent(hash) { mutableListOf() }.add(file)
                        }
                    }
                    jobs.add(job)
                }
            }

            jobs.forEach { it.join() }

            reportFile.printWriter().use { out ->
                fileHashes.forEach { (hash, files) ->
                    if (files.size > 1) {
                        out.println("Duplikaty dla hash $hash:")
                        files.forEach { file ->
                            out.println("${file.name} - ${file.absolutePath}")
                        }
                    }
                }
            }

            println("Raport został zapisany do ${reportFile.absolutePath}")
        }

        private fun File.md5(): String {
            val buffer = ByteArray(1024)
            val md = MessageDigest.getInstance("MD5")
            DigestInputStream(FileInputStream(this), md).use { dis ->
                while (dis.read(buffer) != -1) {
                    // Przetwarzanie pliku
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
