/* package org.javacs.kt.symbols

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.SymbolKind
import org.javacs.kt.position.range

typealias DocumentSymbolResult = Pair<String, List<DocumentSymbol>>

/**
 * Provides a facility to convert a list of IntelliJ PSI root objects to a list of document symbol objects. Each
 * document symbol object is itself a tree structure.
 */
interface DocumentSymbolBuilder {
    fun buildDocumentSymbolResult(kinds: List<SymbolKind>, roots: List<PsiElement>): DocumentSymbolResult
    fun addRoot(root: PsiElement): Int
    fun addChild(rootIndex: Int, child: PsiElement)
}

class DocumentSymbolBuilderImpl : DocumentSymbolBuilder {
    override fun buildDocumentSymbolResult(kinds: List<SymbolKind>, roots: List<PsiElement>): DocumentSymbolResult {
        fun getDocumentSymbolResult(): DocumentSymbolResult = when (rootList.size) {
            0 -> "No elements available!" to listOf()
            else -> "" to rootList.map { index -> getDocumentSymbol(kinds, index) }
        }

        return getDocumentSymbolResult()
    }

    override fun addRoot(root: PsiElement): Int {
        val index = elementList.size
        elementList.add(root)
        rootList.add(index)
        elementMap[index] = mutableListOf()
        return index
    }

    override fun addChild(rootIndex: Int, child: PsiElement) {
        val index = elementList.size
        elementList.add(child)
        elementMap[rootIndex]!!.add(index)
    }

    private fun getDocumentSymbol(kinds: List<SymbolKind>, index: Int): DocumentSymbol {
        val element: PsiElement = elementList[index]
        val span = range(element.text, element.textRange)
        val nameIdentifier = if (element is PsiNameIdentifierOwner) element.nameIdentifier else null
        val nameSpan = nameIdentifier?.let { range(element.text, it.textRange) } ?: span
        val (name, kind, detail) = element.pickHierarchyElements(kinds) ?:
        val children: List<DocumentSymbol> = getChildSymbolsFor(kinds, index)

        return DocumentSymbol(name, kind, span, nameSpan, detail, children)
    }

    private fun getChildSymbolsFor(kinds: List<SymbolKind>, index: Int): List<DocumentSymbol> =
        elementMap[index]?.let { list ->
            list.map { childIndex -> getDocumentSymbol(kinds, childIndex) }
        } ?: listOf()

    /**
     * The list of document symbol tree root elements. Normally, for the purposes of LSP, there is only a single root,
     * the File element.
     */
    private val rootList: MutableList<Int> = mutableListOf()

    /**
     * A list of all descendants of all roots.
     */
    private val elementList: MutableList<PsiElement> = mutableListOf()

    /**
     * A map associating a parent element list index with the LSP relevant children element list indices that define a
     * hierarchy relationship of interest to clients.
     */
    private val elementMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
}
*/
