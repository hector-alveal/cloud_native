package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.CrearBindingRequest;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.dto.TipoExchange;
import com.pedidos360.rabbitadmin.exception.RecursoDuplicadoException;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        service = new RabbitAdminService(rabbitAdmin);
    }

    @Test
    void crearColaConDlqDeclaraDlxDlqBindingYColaPrincipalConArgumentos() {
        // getQueueInfo devuelve null por defecto en el mock = la cola no existe
        List<String> creados = service.crearCola(
                new CrearColaRequest("pagos.queue", null, null, true, 60000, null));

        assertThat(creados).containsExactly("pagos.queue", "pagos.queue.dlx", "pagos.queue.dlq");

        ArgumentCaptor<Queue> colas = ArgumentCaptor.forClass(Queue.class);
        verify(rabbitAdmin, times(2)).declareQueue(colas.capture());
        Queue principal = colas.getAllValues().get(1);
        assertThat(principal.getName()).isEqualTo("pagos.queue");
        assertThat(principal.isDurable()).isTrue();
        assertThat(principal.getArguments())
                .containsEntry("x-dead-letter-exchange", "pagos.queue.dlx")
                .containsEntry("x-dead-letter-routing-key", "pagos.queue.dlq")
                .containsEntry("x-message-ttl", 60000);

        verify(rabbitAdmin).declareExchange(any(DirectExchange.class));
        verify(rabbitAdmin).declareBinding(any(Binding.class));
    }

    @Test
    void crearColaExistenteLanzaConflictoYNoDeclaraNada() {
        when(rabbitAdmin.getQueueInfo("pagos.queue")).thenReturn(new QueueInformation("pagos.queue", 0, 0));

        assertThatThrownBy(() -> service.crearCola(
                new CrearColaRequest("pagos.queue", null, null, null, null, null)))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("pagos.queue");

        verify(rabbitAdmin, never()).declareQueue(any(Queue.class));
    }

    @Test
    void eliminarColaInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> service.eliminarCola("fantasma.queue"))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(rabbitAdmin, never()).deleteQueue(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void crearExchangeTopicDeclaraUnTopicExchange() {
        when(rabbitAdmin.getRabbitTemplate()).thenReturn(rabbitTemplate);
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenReturn(Boolean.FALSE); // no existe aun

        service.crearExchange(new CrearExchangeRequest("pagos.exchange", TipoExchange.TOPIC, null));

        ArgumentCaptor<Exchange> exchange = ArgumentCaptor.forClass(Exchange.class);
        verify(rabbitAdmin).declareExchange(exchange.capture());
        assertThat(exchange.getValue()).isInstanceOf(TopicExchange.class);
        assertThat(exchange.getValue().getName()).isEqualTo("pagos.exchange");
        assertThat(exchange.getValue().isDurable()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void crearBindingConExchangeInexistenteLanzaNoEncontrado() {
        when(rabbitAdmin.getRabbitTemplate()).thenReturn(rabbitTemplate);
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenReturn(Boolean.FALSE);

        assertThatThrownBy(() -> service.crearBinding(
                new CrearBindingRequest("no.existe", "pagos.queue", "pago.creado")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no.existe");

        verify(rabbitAdmin, never()).declareBinding(any(Binding.class));
    }
}
