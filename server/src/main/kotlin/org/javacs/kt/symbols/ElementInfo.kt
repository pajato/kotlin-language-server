package org.javacs.kt.symbols

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.eclipse.lsp4j.SymbolKind
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.psi.*

data class ElementInfo(val name: String, val kind: SymbolKind, val detail: String)

private tailrec fun PsiElement.getAncestorName(parent: PsiElement?): String = when (parent) {
    null -> ""
    is KtNamedDeclaration -> parent.nameAsSafeName.asString()
    else -> parent.getAncestorName(this.parent)
}

private tailrec fun PsiElement.getAncestorKind(parent: PsiElement?): SymbolKind? = when (parent) {
    null -> null
    is KtProperty -> SymbolKind.Property
    is KtFunction -> SymbolKind.Function
    else -> parent.getAncestorKind(this.parent)
}

internal fun PsiElement.getSelectionRange(): TextRange = when (this) {
    is KtPackageDirective -> if (this.children.isNotEmpty()) this.children[0].textRange else this.textRange
    else -> this.textRange
}

internal fun getName(element: PsiElement) = element.namedUnwrappedElement?.name
    ?: throw IllegalStateException("Expected to have a named ancestor for $element!")

private fun getNameAndKind(element: PsiElement, className: String) = when (element.parent) {
    is KtNamedFunction -> getName(element) to SymbolKind.Function
    is KtSecondaryConstructor -> getName(element) to SymbolKind.Constructor
    is KtConstructorDelegationCall -> getName(element) to SymbolKind.Constructor
    is KtCallExpression -> getName(element) to SymbolKind.Function
    else ->
        throw IllegalStateException("Invalid parent for $className!")
}

internal fun KtBlockExpression.getElementInfo(className: String) : ElementInfo {
    val (name, kind) = getNameAndKind(this, className)
    return ElementInfo(name, kind, className)
}

internal fun KtPackageDirective.getElementInfo(className: String): ElementInfo {
    val name = this.name
    val kind = SymbolKind.Package
    return ElementInfo(name, kind, className)
}

internal fun KtValueArgumentList.getElementInfo(className: String): ElementInfo {
    val (_, kind) = getNameAndKind(this, className)
    return ElementInfo(this.getAncestorName(this.parent), kind, className)
}

internal fun KtNameReferenceExpression.getElementInfo(className: String) = when (this.parent) {
    is KtPackageDirective -> ElementInfo(this.text, SymbolKind.Package, className)
    is KtCallExpression -> ElementInfo(this.text, SymbolKind.Function, className)
    else -> null
}

internal fun KtDeclarationModifierList.getElementInfo(className: String) = when (this.parent.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

internal fun KtLiteralStringTemplateEntry.getElementInfo(className: String) = when (this.parent.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

internal fun KtStringTemplateExpression.getElementInfo(className: String) = when (this.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

internal fun KtParameterList.getElementInfo(className: String) = when (this.parent) {
    is KtNamedFunction -> ElementInfo(this.text, SymbolKind.Function, className)
    else -> null
}

internal fun KtDotQualifiedExpression.getElementInfo(className: String) = when (this.parent) {
    is KtPackageDirective -> ElementInfo(this.text, SymbolKind.Package, className)
    else -> null
}

internal fun KtIfExpression.getElementInfo(className: String) = when (this.parent) {
    is KtProperty -> ElementInfo(this.parent.text, SymbolKind.Property, className)
    else -> null
}

internal fun KtContainerNode.getElementInfo(className: String): ElementInfo? {
    val kind = this.getAncestorKind(this.parent)
    return if (kind == null) null else when (this.parent) {
        is KtIfExpression -> ElementInfo(this.parent.text, kind, className)
        else -> null
    }
}

internal fun KtTypeArgumentList.getElementInfo(className: String): ElementInfo? {
    val name = getAncestorName(this.parent)
    val kind = getAncestorKind(this.parent)
    return if (name.isEmpty() || kind == null) null else ElementInfo(name, kind, className)
}
