package org.javacs.kt.symbols

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.SymbolKind.*
import org.eclipse.lsp4j.SymbolKind.Function
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.javacs.kt.ClientConfiguration
import org.javacs.kt.position.range
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.stubs.elements.KtDotQualifiedExpressionElementType

typealias DocumentSymbolsList = List<Either<SymbolInformation, DocumentSymbol>>

fun documentSymbols(config: ClientConfiguration, file: KtFile): DocumentSymbolsList {
    fun doDocumentSymbols(element: PsiElement): List<DocumentSymbol> {
        val children = element.children.flatMap(::doDocumentSymbols)

        return element.pickHierarchyElements(config.kindsSupported)?.let { info ->
            val text = element.containingFile.text
            val symbolRange = range(text, element.textRange)
            val selectionRange = range(text, element.getSelectionRange())
            val symbol = DocumentSymbol(info.name, info.kind, symbolRange, selectionRange, info.detail, children)
            listOf(symbol)
        } ?: children
    }

    return doDocumentSymbols(file).map { Either.forRight(it) }
}

internal fun PsiElement.pickHierarchyElements(kindsSupported: List<SymbolKind>): ElementInfo? {
    val className = this::class.java.simpleName
    val result = when (this) {
        is KtPackageDirective -> this.getElementInfo(className)
        is KtImportList -> null
        is KtDeclarationModifierList -> this.getElementInfo(className)
        is KtParameterList -> this.getElementInfo(className)
        is KtPrimaryConstructor -> ElementInfo(this.name!!, Constructor, className)
        is KtConstantExpression -> null
        is KtProperty -> ElementInfo(this.name!!, Property, className)
        is KtNameReferenceExpression -> this.getElementInfo(className)
        is KtParameter -> null
        is KtBlockExpression -> this.getElementInfo(className)
        is KtNamedFunction -> ElementInfo(this.name!!, Function, className)
        is KtConstructorDelegationReferenceExpression -> null
        is KtConstructorDelegationCall -> ElementInfo(getName(this), Constructor, className)
        is KtValueArgumentList -> this.getElementInfo(className)
        is KtSecondaryConstructor -> ElementInfo(this.name!!, Constructor, className)
        is KtClassBody -> ElementInfo("", Class, className)
        is KtClass -> ElementInfo(this.name!!, Class, className)
        is KtFile -> ElementInfo(this.name, File, className)
        is PsiWhiteSpace -> null
        is KtLiteralStringTemplateEntry -> this.getElementInfo(className)
        is KtStringTemplateExpression -> this.getElementInfo(className)
        is KtValueArgument -> null
        is KtCallExpression -> ElementInfo(getName(this), Function, className)
        is KtDotQualifiedExpression -> this.getElementInfo(className)
        is KtDotQualifiedExpressionElementType -> null
        is KtTypeArgumentList -> this.getElementInfo(className)
        is KtTypeProjection -> ElementInfo(this.text, Function, className)
        is KtTypeReference -> null
        is KtUserType -> null
        is KtOperationReferenceExpression -> null
        is KtBinaryExpression -> null
        is KtContainerNode -> this.getElementInfo(className)
        is KtIfExpression -> this.getElementInfo(className)
        else -> throw IllegalStateException("Unsupported type: $className in <${this.parent.text}>")
    }

    return if (result != null && kindsSupported.contains(result.kind)) result else null
}
