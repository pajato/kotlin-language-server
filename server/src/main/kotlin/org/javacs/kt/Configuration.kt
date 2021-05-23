package org.javacs.kt

import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.SymbolKind.Array
import org.eclipse.lsp4j.SymbolKind.File

data class SnippetsConfiguration(
    /** Whether code completion should return VSCode-style snippets. */
    var enabled: Boolean = true
)

data class CompletionConfiguration(
    val snippets: SnippetsConfiguration = SnippetsConfiguration()
)

data class LintingConfiguration(
    /** The time interval between subsequent lints in ms. */
    var debounceTime: Long = 250L
)

data class JVMConfiguration(
    /** Which JVM target the Kotlin compiler uses. See Compiler.jvmTargetFrom for possible values. */
    var target: String = "default"
)

data class CompilerConfiguration(
    val jvm: JVMConfiguration = JVMConfiguration()
)

data class IndexingConfiguration(
    /** Whether an index of global symbols should be built in the background. */
    var enabled: Boolean = true
)

data class ExternalSourcesConfiguration(
    /** Whether kls-URIs should be sent to the client to describe classes in JARs. */
    var useKlsScheme: Boolean = false,
    /** Whether external classes classes should be automatically converted to Kotlin. */
    var autoConvertToKotlin: Boolean = true
)

class ClientConfiguration {
    /** Allow the client capabilities to be set just once as part of the initialize request. **/
    fun setClientCapabilitiesOneTimeOnly(capabilities: ClientCapabilities) {
        initialClientCapabilities = capabilities
        LogMessage(LogLevel.INFO, "Client capabilities have been established: ${this.capabilities}")
        initialClientCapabilities = null
    }

    /** Establish the symbol kinds supported by the client. */
    val kindsSupported: List<SymbolKind> by lazy(::getSupportedSymbolKinds)

    /** Establish the type to be returned by a document symbol request. */
    val preferDocumentSymbolOverSymbolInformation: Boolean by lazy(::getPreferDocumentSymbol)

    private var initialClientCapabilities: ClientCapabilities? = null
    private val capabilities: ClientCapabilities? by lazy { initialClientCapabilities }

    private fun getSupportedSymbolKinds(): List<SymbolKind> {
        val defaultKinds by lazy { (File.ordinal..Array.ordinal).map { SymbolKind.values()[it] } }

        return capabilities?.workspace?.symbol?.symbolKind?.valueSet ?: defaultKinds
    }

    private fun getPreferDocumentSymbol(): Boolean =
        capabilities?.textDocument?.documentSymbol?.hierarchicalDocumentSymbolSupport ?: true
}

data class Configuration(
    val compiler: CompilerConfiguration = CompilerConfiguration(),
    val completion: CompletionConfiguration = CompletionConfiguration(),
    val linting: LintingConfiguration = LintingConfiguration(),
    var indexing: IndexingConfiguration = IndexingConfiguration(),
    val externalSources: ExternalSourcesConfiguration = ExternalSourcesConfiguration(),
    val client: ClientConfiguration = ClientConfiguration(),
)
