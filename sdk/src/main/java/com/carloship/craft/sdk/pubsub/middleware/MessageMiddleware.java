package com.carloship.craft.sdk.pubsub.middleware;


import com.carloship.craft.sdk.pubsub.message.IncomingMessage;

/**
 * Define um middleware para interceptação de mensagens recebidas
 * em um sistema de Pub/Sub.
 * <p>
 * Implementações de {@code MessageMiddleware} permitem executar lógica
 * antes e depois da entrega de uma mensagem ao tópico/consumidor,
 * possibilitando casos de uso como:
 * <ul>
 *     <li>Validação de payload</li>
 *     <li>Autenticação e autorização</li>
 *     <li>Enriquecimento ou transformação de mensagens</li>
 *     <li>Logging, métricas e tracing</li>
 * </ul>
 *
 * <p>
 * O fluxo típico de execução é:
 * <ol>
 *     <li>{@link #preHandle(IncomingMessage)} antes da entrega</li>
 *     <li>Entrega da mensagem ao consumidor</li>
 *     <li>{@link #postHandle(IncomingMessage)} após entrega bem-sucedida</li>
 * </ol>
 *
 * <p>
 * Caso uma exceção seja lançada em {@code preHandle}, a entrega da
 * mensagem é abortada.
 * </p>
 */
public interface MessageMiddleware {

    /**
     * Executado antes da entrega da mensagem ao tópico ou consumidor.
     * <p>
     * Pode retornar a mesma mensagem ou uma versão modificada.
     * Caso uma exceção seja lançada, o processamento da mensagem
     * é interrompido e a entrega não ocorre.
     *
     * @param message a mensagem recebida
     * @param <T>     o tipo do payload
     * @return a mensagem original ou transformada
     * @throws Exception caso o processamento prévio falhe
     */
    <T> IncomingMessage<T> preHandle(IncomingMessage<T> message) throws Exception;

    /**
     * Executado após a entrega bem-sucedida da mensagem.
     * <p>
     * Geralmente utilizado para logging, métricas, auditoria
     * ou limpeza de recursos.
     *
     * @param message a mensagem que foi entregue
     * @param <T>     o tipo do payload
     * @throws Exception caso ocorra erro no pós-processamento
     */
    <T> void postHandle(IncomingMessage<T> message) throws Exception;
}
