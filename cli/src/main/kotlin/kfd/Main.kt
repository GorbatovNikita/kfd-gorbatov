
package kfd

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Использование: analyzer <путь-к-репозиторию>")
        exitProcess(1)
    }

    try {
        val report = RepositoryAnalyzer().analyze(File(args[0]))

        println("Статистика по расширениям:")
        report.extensionStats.forEach {
            println("${it.extension}: файлов=${it.fileCount}, строк=${it.totalLines}")
        }

        println("\nТри самых длинных файла:")
        report.longestFiles.forEach {
            println("${it.path}: ${it.lines} строк")
        }
    } catch (exception: IllegalArgumentException) {
        System.err.println(exception.message)
        exitProcess(1)
    }
}
