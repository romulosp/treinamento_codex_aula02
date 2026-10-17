package br.com.romulopenha.sistemaprototipoandroid.platform

import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercita regras puras de caminho, conflito e ordenação do menu. */
class MenuTreeTest {
    /** Agrupadores compartilhados são unificados e suas folhas permanecem ordenadas. */
    @Test
    fun `unifica agrupador e ordena folhas`() {
        val leaves = listOf("Zebra", "Alfa").map { label ->
            MenuLeaf(
                MenuPathValidator.validate("Principal > Relatórios > $label"),
                BusinessMenuItem(label.lowercase(), label, label.lowercase(), 0),
                label,
            )
        }

        val tree = MenuTreeBuilder.build(leaves)

        assertEquals(listOf("Relatórios"), tree.map { it.titulo })
        assertEquals(listOf("Alfa", "Zebra"), tree.single().filhos.map { it.titulo })
    }

    /** Folhas iguais sem distinção de maiúsculas impedem um menu ambíguo. */
    @Test(expected = MenuFatalException::class)
    fun `rejeita caminho duplicado sem diferenciar maiusculas`() {
        val item = BusinessMenuItem("pix", "PIX", "pix", 0)
        MenuTreeBuilder.build(
            listOf(
                MenuLeaf(MenuPathValidator.validate("Principal > PIX"), item, "a.apk"),
                MenuLeaf(MenuPathValidator.validate("principal > pix"), item, "b.apk"),
            ),
        )
    }

    /** Segmento vazio é inválido porque o delimitador é reservado. */
    @Test(expected = MenuFatalException::class)
    fun `rejeita segmento vazio`() {
        MenuPathValidator.validate("Principal > > PIX")
    }
}
