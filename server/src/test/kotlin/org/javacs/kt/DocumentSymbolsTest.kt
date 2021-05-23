package org.javacs.kt

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.javacs.kt.util.filePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URI
import java.net.URL

class DocumentSymbolsTest : LanguageServerTestFixture("symbols") {

    @Test
    fun `When using a default (open) client configuration verify interesting document symbols`() {
        val fileRootPath = "DocumentSymbols"
        open("$fileRootPath.kt")
        runTest(fileRootPath)
    }

    @Test
    fun `When the file is accessed directly, verify correct results`() {
        runTest("DocumentSymbols")
    }

    @Test
    fun `When a package is added to the file, verify correct behavior`() {
        runTest("hw/HelloWorld")
    }

    @Test
    fun `When getting symbols for basic expressions and statements, verify correct behavior`() {
        runTest("basics")
    }

    @Test
    fun `When a function argument has a default value, verify correct behavior`() {
        runTest("functionArgument")
    }

    private fun getTestFileParams(fileRootPath: String): DocumentSymbolParams {
        val resource: URL = classLoader.getResource("symbols/$fileRootPath.kt")!!
        val testFileUri: URI = resource.toURI().filePath!!.toUri()
        return DocumentSymbolParams(TextDocumentIdentifier(testFileUri.toString()))
    }

    private fun runTest(fileRootPath: String) {
        val testFileParams = getTestFileParams(fileRootPath)
        val expected: List<String> = listOf(classLoader.getExpectedSource(fileRootPath))
        doAsserts(fileRootPath, expected, languageServer.textDocumentService.documentSymbol(testFileParams).get())
    }

    private fun doAsserts(
        fileRootPath: String,
        expected: List<String>,
        found: List<Either<SymbolInformation, DocumentSymbol>>
    ) {
        fun getResult(result: DocumentSymbol): String {
            fun getDocumentSymbolAsString(level: Int, symbol: DocumentSymbol): String {
                fun getRanges(): String {
                    fun isSame(): Boolean = symbol.range == symbol.selectionRange
                    fun getRange() = RangeTextGetter.getText(symbol.range)
                    fun getSelection() = RangeTextGetter.getText(symbol.selectionRange)

                    return if (isSame()) getRange() else "${getRange()}, ${getSelection()}"
                }

                val prefix = "DocumentSymbol"
                val name = symbol.name
                val kind = symbol.kind
                val detail = symbol.detail

                val indent = if (level == 0) "" else " ".repeat(4 * level)
                val basic = """$indent$prefix("$name", $kind, $detail, ${getRanges()})"""
                val children = symbol.children.map { child -> getDocumentSymbolAsString(level + 1, child) }

                val resultList = listOf(basic) + children
                return resultList.joinToString("\n")
            }

            return getDocumentSymbolAsString(0, result)
        }

        RangeTextGetter.setText(classLoader, fileRootPath)
        assertEquals("The number of document symbols found is wrong!", expected.size, found.size)
        found.forEachIndexed { index, entry ->
            assertTrue("There are unexpected SymbolInformation entries at index $index!", entry.left == null)
            assertEquals(expected[index], "${getResult(entry.right)}\n")
        }
    }

    private val classLoader: ClassLoader = this::class.java.classLoader
    val foo = File(classLoader.getResource("symbols/basics.kt")!!.file).readText()
    val x = 24
}

fun ClassLoader.getExpectedSource(fileRootPath: String): String {
    val url = this.getResource("symbols/$fileRootPath.expected") ?: return ""
    val symbolsPath = this.getResource("symbols")!!.toString().replace("file:", "")
    return File(url.file).readText().replace("||path-to-symbols||", symbolsPath)
}

object RangeTextGetter {
    fun setText(classLoader: ClassLoader, fileRootPath: String) {
        fun initializePositionMap(lines: List<String>) {
            var currentPosition = 0
            positionMap.clear()
            lines.indices.forEach { index ->
                positionMap[index] = currentPosition
                currentPosition += lines[index].length + 1
            }
            positionMap[lines.size] = currentPosition
        }

        val url = classLoader.getResource("symbols/$fileRootPath.kt") ?: return
        val file = File(url.file)
        val lines = file.readLines()
        text = file.readText()
        initializePositionMap(lines)
    }

    fun getText(range: Range): String {
        val start: Int = positionMap[range.start.line]?.plus(range.start.character) ?: -1
        val end: Int = positionMap[range.end.line]?.plus(range.end.character) ?: -1
        return if (start == -1 || end == -1) "" else text.substring(start, end).replace("\n", "@")
    }

    lateinit var text: String
    private val positionMap: MutableMap<Int, Int> = mutableMapOf()
}
