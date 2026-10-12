package com.pedidos360.catalogo.messaging.comun;

import com.pedidos360.catalogo.config.RabbitMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Ejecuta la logica de un consumidor reintentando SOLO los errores recuperables,
 * con espera exponencial (1 s, 2 s, 4 s...). La cantidad de intentos y los tiempos
 * vienen de application.yml (pedidos360.rabbitmq.reintentos).
 *
 * Si el error es no recuperable, o se agotan los intentos, relanza la excepcion
 * para que el consumidor decida el NACK hacia la DLQ.
 */
@Component
public class EjecutorConReintentos {

    /** Trabajo a ejecutar por el consumidor (normalmente una llamada a un servicio de negocio). */
    @FunctionalInterface
    public interface Tarea {
        void ejecutar() throws Exception;
    }

    private static final Logger log = LoggerFactory.getLogger(EjecutorConReintentos.class);

    private final int maxIntentos;
    private final long intervaloInicialMs;
    private final double multiplicador;

    @Autowired
    public EjecutorConReintentos(RabbitMQProperties props) {
        this(props.reintentos().maxIntentos(),
             props.reintentos().intervaloInicialMs(),
             props.reintentos().multiplicador());
    }

    /** Constructor directo (usado en los tests unitarios). */
    public EjecutorConReintentos(int maxIntentos, long intervaloInicialMs, double multiplicador) {
        this.maxIntentos = maxIntentos;
        this.intervaloInicialMs = intervaloInicialMs;
        this.multiplicador = multiplicador;
    }

    public void ejecutar(String operacion, Tarea tarea) throws Exception {
        long espera = intervaloInicialMs;
        for (int intento = 1; ; intento++) {
            try {
                tarea.ejecutar();
                if (intento > 1) {
                    log.info("[{}] Exito en el intento {}/{}", operacion, intento, maxIntentos);
                }
                return;
            } catch (Exception e) {
                if (ClasificadorErrores.esNoRecuperable(e)) {
                    throw e; // reintentar no sirve: el consumidor lo manda a la DLQ
                }
                if (intento >= maxIntentos) {
                    log.warn("[{}] Error recuperable persistio tras {} intentos", operacion, maxIntentos);
                    throw e;
                }
                log.warn("[{}] Error recuperable en intento {}/{}: {}. Reintentando en {} ms",
                        operacion, intento, maxIntentos, e.getMessage(), espera);
                Thread.sleep(espera);
                espera = Math.round(espera * multiplicador);
            }
        }
    }

    public int getMaxIntentos() {
        return maxIntentos;
    }
}
