package org.javacs.kt.symbols

import com.intellij.psi.PsiElement
import org.eclipse.lsp4j.SymbolKind
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.psi.*

data class ElementInfo(val name: String, val kind: SymbolKind, val detail: String)

fun getName(element: PsiElement) = element.namedUnwrappedElement?.name
    ?: throw IllegalStateException("Expected to have a named ancestor for $element!")

fun getNameAndKind(element: PsiElement, className: String) = when (element.parent) {
    is KtNamedFunction -> getName(element) to SymbolKind.Function
    is KtSecondaryConstructor -> getName(element) to SymbolKind.Constructor
    is KtConstructorDelegationCall -> getName(element) to SymbolKind.Constructor
    is KtCallExpression -> getName(element) to SymbolKind.Function
    else ->
        throw IllegalStateException("Invalid parent for $className!")
}

fun KtBlockExpression.getElementInfo(className: String) : ElementInfo {
    val (name, kind) = getNameAndKind(this, className)
    return ElementInfo(name, kind, className)
}

fun KtPackageDirective.getElementInfo(className: String): ElementInfo {
    val name = this.name
    val kind = SymbolKind.Package
    return ElementInfo(name, kind, className)
}

fun KtValueArgumentList.getElementInfo(className: String): ElementInfo {
    val (_, kind) = getNameAndKind(this, className)
    return ElementInfo(this.getAncestorName(), kind, className)
}

fun KtNameReferenceExpression.getElementInfo(className: String) = when (this.parent) {
    is KtPackageDirective -> ElementInfo(this.text, SymbolKind.Package, className)
    is KtCallExpression -> ElementInfo(this.text, SymbolKind.Function, className)
    else -> null
}

fun KtDeclarationModifierList.getElementInfo(className: String) = when (this.parent.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

fun KtLiteralStringTemplateEntry.getElementInfo(className: String) = when (this.parent.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

fun KtStringTemplateExpression.getElementInfo(className: String) = when (this.parent) {
    is KtProperty -> ElementInfo(this.text, SymbolKind.Property, className)
    else -> null
}

fun KtParameterList.getElementInfo(className: String) = when (this.parent) {
    is KtNamedFunction -> ElementInfo(this.text, SymbolKind.Function, className)
    else -> null
}

fun KtDotQualifiedExpression.getElementInfo(className: String) = when (this.parent) {
    is KtPackageDirective -> ElementInfo(this.text, SymbolKind.Package, className)
    else -> null
}
