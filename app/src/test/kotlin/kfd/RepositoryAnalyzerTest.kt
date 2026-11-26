
package kfd

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class RepositoryAnalyzerTest {

    @TempDir
    lateinit var tempDir: Path

    private val analyzer = RepositoryAnalyzer()

    @Test
    fun `counts files and lines by extension`() {
        File(tempDir.toFile(), "A.kt").writeText("one\ntwo\n")
        File(tempDir.toFile(), "B.kt").writeText("three\n")
        File(tempDir.toFile(), "README.md").writeText("# Title\nText\n")
        File(tempDir.toFile(), "notes.txt").writeText("ignored\n")

        val report = analyzer.analyze(tempDir.toFile())

        assertEquals(
            listOf(
                ExtensionStats("kt", 2, 3),
                ExtensionStats("md", 1, 2)
            ),
            report.extensionStats
        )
    }

    @Test
    fun `returns three longest files in correct order`() {
        File(tempDir.toFile(), "a.kt").writeText("1\n")
        File(tempDir.toFile(), "b.kt").writeText("1\n2\n")
        File(tempDir.toFile(), "c.kt").writeText("1\n2\n3\n")
        File(tempDir.toFile(), "d.kt").writeText("1\n2\n3\n4\n")

        val report = analyzer.analyze(tempDir.toFile())

        assertEquals(
            listOf(
                LongestFile("d.kt", 4),
                LongestFile("c.kt", 3),
                LongestFile("b.kt", 2)
            ),
            report.longestFiles
        )
    }

    @Test
    fun `returns empty report for empty directory`() {
        val report = analyzer.analyze(tempDir.toFile())

        assertTrue(report.extensionStats.isEmpty())
        assertTrue(report.longestFiles.isEmpty())
    }

    @Test
    fun `rejects nonexistent path`() {
        val missing = tempDir.resolve("missing").toFile()

        assertThrows(IllegalArgumentException::class.java) {
            analyzer.analyze(missing)
        }
    }

    @Test
    fun `ignores excluded directories`() {
        val root = tempDir.toFile()

        File(root, "visible.kt").writeText("one\n")

        val gitDir = File(root, ".git")
        gitDir.mkdirs()
        File(gitDir, "hidden.kt").writeText("hidden\n")

        val gradleDir = File(root, ".gradle")
        gradleDir.mkdirs()
        File(gradleDir, "cache.kt").writeText("hidden\n")

        val buildDir = File(root, "build")
        buildDir.mkdirs()
        File(buildDir, "generated.kt").writeText("hidden\n")

        val report = analyzer.analyze(root)

        assertEquals(
            listOf(ExtensionStats("kt", 1, 1)),
            report.extensionStats
        )
    }
}
