package com.pedidos360.rabbitadmin.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.pedidos360.rabbitadmin.dto.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Traduce las excepciones a respuestas HTTP claras con el formato {@link RespuestaError}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de validacion del body (@Valid @RequestBody). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarBodyInvalido(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        List<String> detalles = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> detalles.add(error.getField() + ": " + error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> detalles.add(error.getDefaultMessage()));
        detalles.sort(String::compareTo);
        return responder(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles, request);
    }

    /** Errores de validacion de parametros de ruta o query (@PathVariable / @RequestParam). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespuestaError> manejarParametroInvalido(ConstraintViolationException ex,
                                                                  HttpServletRequest request) {
        List<String> detalles = new ArrayList<>();
        for (ConstraintViolation<?> violacion : ex.getConstraintViolations()) {
            String ruta = violacion.getPropertyPath().toString();
            String parametro = ruta.substring(ruta.lastIndexOf('.') + 1);
            detalles.add(parametro + ": " + violacion.getMessage());
        }
        detalles.sort(String::compareTo);
        return responder(HttpStatus.BAD_REQUEST, "Parámetros inválidos", detalles, request);
    }

    /** JSON mal formado o valor de enum inexistente (ej: "tipo": "CIRCULAR"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        String detalle = "El cuerpo de la petición no es un JSON válido";
        if (ex.getCause() instanceof InvalidFormatException formato
                && formato.getTargetType() != null
                && formato.getTargetType().isEnum()
                && !formato.getPath().isEmpty()) {
            String campo = formato.getPath().get(formato.getPath().size() - 1).getFieldName();
            detalle = campo + ": el valor '" + formato.getValue() + "' no es válido. Valores permitidos: "
                    + Arrays.toString(formato.getTargetType().getEnumConstants());
        }
        return responder(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", List.of(detalle), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaError> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                  HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Parámetros inválidos",
                List.of(ex.getParameterName() + ": es obligatorio"), request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<RespuestaError> manejarDuplicado(RecursoDuplicadoException ex,
                                                          HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "El recurso ya existe", List.of(ex.getMessage()), request);
    }

    /** RabbitMQ apagado o inalcanzable. */
    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<RespuestaError> manejarSinConexion(AmqpConnectException ex,
                                                            HttpServletRequest request) {
        log.error("No se pudo conectar con RabbitMQ: {}", ex.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no disponible",
                List.of("No se pudo conectar con el broker. Verifique que RabbitMQ esté levantado."), request);
    }

    /** El broker rechazo la operacion (ej: argumentos incompatibles con un recurso existente). */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<RespuestaError> manejarErrorRabbit(AmqpException ex, HttpServletRequest request) {
        Throwable causa = ex;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        log.error("RabbitMQ rechazó la operación: {}", causa.getMessage(), ex);
        return responder(HttpStatus.BAD_GATEWAY, "RabbitMQ rechazó la operación",
                List.of(String.valueOf(causa.getMessage())), request);
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus status, String error, List<String> detalles,
                                                     HttpServletRequest request) {
        RespuestaError cuerpo = new RespuestaError(
                LocalDateTime.now(), status.value(), error, detalles, request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
