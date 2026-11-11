package com.carloship.craft.sdk.pubsub.serializer;

import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;

/**
 * Define uma estratégia para converter objetos de alto nível do tipo
 * {@link OutgoingMessage} para um formato de transporte {@link String},
 * bem como para reconstruir payloads tipados a partir de dados brutos
 * recebidos.
 * <p>
 * Uma implementação de {@code MessageSerializer} é normalmente utilizada
 * por um {@code MessageChannel} antes de publicar mensagens no broker e
 * após receber payloads brutos do transporte. As implementações podem
 * escolher qualquer formato de codificação, como JSON, binário-para-texto
 * (Base64), Protocol Buffers ou formatos customizados delimitados.
 *
 * <h2>Exemplo de uso</h2>
 * <pre>{@code
 * MessageSerializer serializer = new JsonMessageSerializer();
 *
 * // Serializa uma mensagem de saída
 * String raw = serializer.serialize(new OutgoingMessage<>(meuPayload));
 *
 * // Desserializa uma mensagem recebida
 * MeuTipo payload = serializer.deserialize(raw, MeuTipo.class);
 * }</pre>
 *
 * <p>
 * Sempre que possível, as implementações devem ser <b>thread-safe</b>,
 * pois serializadores geralmente são compartilhados entre múltiplos
 * canais de comunicação.
 * </p>
 */
public interface MessageSerializer {

    /**
     * Converte um {@link OutgoingMessage} para sua representação codificada
     * no formato de transporte.
     *
     * @param message a mensagem estruturada contendo payload e metadados
     * @param <T>     o tipo do payload
     * @return a representação codificada pronta para transporte
     *         (por exemplo, uma string JSON)
     * @throws Exception se a serialização falhar por qualquer motivo, como
     *                   erro de I/O, tipos não suportados ou estrutura inválida
     */
    <T> String serialize(OutgoingMessage<T> message) throws Exception;

    /**
     * Reconstrói um objeto de payload tipado a partir de sua representação
     * textual codificada.
     * <p>
     * As implementações devem interpretar os dados brutos e convertê-los
     * para o tipo Java solicitado, lançando uma exceção caso os dados sejam
     * incompatíveis ou não correspondam ao tipo esperado.
     * </p>
     *
     * @param raw  os dados brutos recebidos da camada de transporte
     * @param type o tipo de destino esperado para desserialização
     * @param <T>  o tipo do payload
     * @return o objeto de payload desserializado
     * @throws Exception se os dados não puderem ser interpretados ou
     *                   convertidos para o tipo desejado
     */
    <T> T deserialize(String raw, Class<T> type) throws Exception;
}
