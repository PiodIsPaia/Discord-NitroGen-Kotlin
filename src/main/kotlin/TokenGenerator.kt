package org.yuki

import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File

class TokenGenerator(private val numTokens: Int) {
    private val client = OkHttpClient()
    private val url = "https://api.discord.gx.games/v1/direct-fulfillment"
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val headers = mapOf(
        "authority" to "api.discord.gx.games",
        "accept" to "*/*",
        "accept-language" to "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
        "content-type" to "application/json",
        "origin" to "https://www.opera.com",
        "referer" to "https://www.opera.com/",
        "sec-ch-ua" to "\"Opera GX\";v=\"105\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-fetch-dest" to "empty",
        "sec-fetch-mode" to "cors",
        "sec-fetch-site" to "cross-site",
        "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 OPR/105.0.0.0"
    )

    private val outputFile = File("generated_tokens.txt")

    private fun generateRandomString(): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..64)
            .map { characters.random() }
            .joinToString("")
    }

    fun generateAndSaveTokens() {
        for (i in 1..numTokens) {
            val partnerUserId = generateRandomString()
            val json = """
                {
                    "partnerUserId": "$partnerUserId"
                }
            """.trimIndent()

            val body = json.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .headers(headers.toHeaders())
                .build()

            try {
                val response = client.newCall(request).execute()
                handleResponse(response, i)
            } catch (e: Exception) {
                println("Ocorreu um erro: $e")
            }
        }

        println("Tokens gerados e salvos em ${outputFile.absolutePath}")
    }

    private fun handleResponse(response: Response, tokenIndex: Int) {
        when (response.code) {
            200 -> {
                val token = response.body.string().substringAfter("{\"token\":\"").substringBefore("\"}")
                val line = "https://discord.com/billing/partner-promotions/1180231712274387115/$token\n"
                outputFile.appendText(line)
                println("Token $tokenIndex gerado e salvo.")
            }
            429 -> {
                println("Limite de requisições excedido! Aguarde uns minutos.")
                Thread.sleep(60000)
            }
            504 -> {
                println("Servidor expirou! Tentando novamente em 5 segundos.")
                Thread.sleep(5000)
            }
            else -> {
                println("Falha na requisição com código de status ${response.code}.")
                println("Mensagem de erro: ${response.body.string()}")
            }
        }
    }
}
