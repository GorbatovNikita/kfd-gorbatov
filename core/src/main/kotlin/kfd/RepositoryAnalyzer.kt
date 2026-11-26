
package kfd

import java.nio.file.Files
import java.io.File

data class ExtensionStats(
    val extension: String,
    val fileCount: Int,
    val totalLines: Int
)

data class LongestFile(
    val path: String,
    val lines: Int
)

data class AnalysisReport(
    val extensionStats: List<ExtensionStats>,
    val longestFiles: List<LongestFile>
)

class RepositoryAnalyzer {
    private val includedExtensions = setOf("kt", "kts", "md")
    private val excludedDirectories = setOf(".git", ".gradle", "build")

    fun analyze(root: File): AnalysisReport {
        require(root.exists() && root.isDirectory) {
            "Указанный путь не существует или не является каталогом: ${root.path}"
        }

        val files = root.walkTopDown()
            .onEnter { directory ->
                directory == root ||
                    (directory.name !in excludedDirectories &&
                        !Files.isSymbolicLink(directory.toPath()))
            }
            .filter { it.isFile && !Files.isSymbolicLink(it.toPath()) }
            .filter { it.extension in includedExtensions }
            .toList()

        val stats = files
            .groupBy { it.extension }
            .map { (extension, extensionFiles) ->
                ExtensionStats(
                    extension = extension,
                    fileCount = extensionFiles.size,
                    totalLines = extensionFiles.sumOf(::countLines)
                )
            }
            .sortedBy { it.extension }

        val longest = files
            .map {
                LongestFile(
                    path = root.toPath().relativize(it.toPath()).toString(),
                    lines = countLines(it)
                )
            }
            .sortedWith(
                compareByDescending<LongestFile> { it.lines }
                    .thenBy { it.path }
            )
            .take(3)

        return AnalysisReport(stats, longest)
    }

    private fun countLines(file: File): Int =
        file.useLines { lines -> lines.count() }
}
