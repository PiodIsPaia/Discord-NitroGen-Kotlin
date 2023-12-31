package org.yuki

fun main() {
    print("Quantos tokens você quer gerar e salvar? ")
    val numTokens = readlnOrNull()?.toIntOrNull() ?: 0

    if (numTokens > 0) {
        val tokenGenerator = TokenGenerator(numTokens)
        tokenGenerator.generateAndSaveTokens()
    } else {
        println("Número inválido de tokens. O programa será encerrado.")
    }
}