package br.com.romulopenha.sistemaprototipoandroid.platform

import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import java.util.Locale

/** Códigos de falha do contrato de montagem do menu de negócio. */
internal enum class MenuErrorCode { MENU_001, MENU_002, MENU_003 }

/** Falha fatal que impede a publicação de um menu com identidade ambígua. */
internal class MenuFatalException(
    val code: MenuErrorCode,
    message: String,
) : IllegalStateException(message)

/** Caminho validado, com chaves estáveis para comparação e identificação. */
internal data class NormalizedMenuPath(
    val segments: List<String>,
    val comparisonKey: String,
    val itemId: String,
)

/** Valida e normaliza a gramática de caminho declarada pelos plugins. */
internal object MenuPathValidator {
    private const val Root = "Principal"

    /**
     * Normaliza [path] sem depender do locale do dispositivo.
     *
     * @throws MenuFatalException quando o caminho obrigatório é ausente ou não
     * respeita a raiz, os delimitadores e os segmentos exigidos.
     */
    fun validate(path: String?): NormalizedMenuPath {
        if (path.isNullOrBlank()) {
            throw MenuFatalException(MenuErrorCode.MENU_002, "Caminho obrigatório ausente")
        }
        val segments = path.split('>').map { raw -> raw.trim().replace(Regex("\\s+"), " ") }
        if (segments.size < 2 || segments.any(String::isBlank) || !segments.first().equals(Root, ignoreCase = true)) {
            throw MenuFatalException(MenuErrorCode.MENU_003, "Caminho de menu inválido")
        }
        val display = segments.joinToString(" > ")
        val comparison = display.lowercase(Locale.ROOT)
        val id = segments.joinToString("-") { segment ->
            segment.lowercase(Locale.ROOT).replace(' ', '-')
        }
        return NormalizedMenuPath(segments, comparison, id)
    }
}

/** Folha validada, associada ao APK e à classe que a declarou. */
internal data class MenuLeaf(
    val path: NormalizedMenuPath,
    val item: BusinessMenuItem,
    val source: String,
)

/** Monta uma árvore ordenada e detecta conflito de folhas antes de publicá-la. */
internal object MenuTreeBuilder {
    /** Converte [leaves] em itens recursivos abaixo da raiz `Principal`. */
    fun build(leaves: List<MenuLeaf>): List<BusinessMenuItem> {
        val root = Node("Principal", "principal")
        val registered = mutableMapOf<String, MenuLeaf>()
        leaves.forEach { leaf ->
            val previous = registered.putIfAbsent(leaf.path.comparisonKey, leaf)
            if (previous != null) {
                throw MenuFatalException(
                    MenuErrorCode.MENU_001,
                    "[FATAL] Inicialização interrompida por conflito de caminho de menu entre plugins. " +
                        "Caminho: ${leaf.path.comparisonKey}; fontes: ${previous.source}, ${leaf.source}",
                )
            }
            var current = root
            leaf.path.segments.drop(1).forEachIndexed { index, label ->
                if (current.leaf != null) {
                    throw MenuFatalException(MenuErrorCode.MENU_001, "Folha não pode ser agrupador")
                }
                val key = label.lowercase(Locale.ROOT)
                current = current.children.getOrPut(key) { Node(label, "${current.id}-$key") }
                if (index == leaf.path.segments.drop(1).lastIndex) {
                    if (current.children.isNotEmpty()) {
                        throw MenuFatalException(MenuErrorCode.MENU_001, "Agrupador não pode ser folha")
                    }
                    current.leaf = leaf
                }
            }
        }
        return root.children.values.sortedWith(nodeComparator).map(::toItem)
    }

    private fun toItem(node: Node): BusinessMenuItem {
        val children = node.children.values.sortedWith(nodeComparator).map(::toItem)
        val leaf = node.leaf
        return if (leaf == null) {
            BusinessMenuItem(node.id, node.label, "", 0, children)
        } else {
            check(children.isEmpty()) { "Folha não pode possuir filhos" }
            leaf.item.copy(id = leaf.path.itemId, filhos = emptyList())
        }
    }

    private val nodeComparator = compareBy<Node> { it.label.lowercase(Locale.ROOT) }.thenBy { it.label }

    /** Nó mutável restrito à construção e nunca publicado à UI. */
    private class Node(val label: String, val id: String) {
        val children = mutableMapOf<String, Node>()
        var leaf: MenuLeaf? = null
    }
}
