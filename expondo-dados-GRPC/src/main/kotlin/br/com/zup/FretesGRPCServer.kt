package br.com.zup

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGRPCServer : ExpondoDadosGRPCServiceGrpc.ExpondoDadosGRPCServiceImplBase() {

  private val logger = LoggerFactory.getLogger(FretesGRPCServer::class.java)

  override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {
    logger.info("Calculando frate para request: $request")

    val cep = request?.cep
    if (cep == null || cep.isBlank()) {
      val err = Status.INVALID_ARGUMENT
        .withDescription("Cep deve ser informado querido!")
        .asRuntimeException()
      responseObserver?.onError(err)
    }

    if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
      val err = Status.INVALID_ARGUMENT
        .withDescription("Cep inválido querido!")
        .augmentDescription("formato esperado é 99999-99")
        .asRuntimeException()
      responseObserver?.onError(err)
    }

    // SIMULA um erro verificação de segurança
    if (cep.endsWith("333")) {
      val statusProto = com.google.rpc.Status.newBuilder()
        .setCode(Code.PERMISSION_DENIED.number)
        .setMessage("Usuario nao pode acessar esse recurso")
        .addDetails(
          Any.pack(
            ErrorDetails.newBuilder()
              .setCode(401)
              .setMessage("Token expirado")
              .build()
          )
        )
        .build()

      val err = StatusProto.toStatusRuntimeException(statusProto)
      responseObserver?.onError(err)
    }

    var valorRamdom = 0.0
    try {
      valorRamdom = Random.nextDouble(from = 0.0, until = 140.00)
      if (valorRamdom > 100) {
        throw IllegalStateException("Erro inesperado ao executar logica de negocio")
      }
    } catch (e: Exception) {
      responseObserver?.onError(
        Status.INTERNAL
          .withDescription(e.message)
          .withCause(e.cause) // Anexado ao Status mas não é envidado ao cliente.
          .asRuntimeException()
      )
    }
    val response = CalculaFreteResponse.newBuilder()
      .setCep(request!!.cep)
      .setValor(valorRamdom)
      .build()

    logger.info("Frete calculado: $response")

    responseObserver!!.onNext(response)
    responseObserver.onCompleted()
  }
}