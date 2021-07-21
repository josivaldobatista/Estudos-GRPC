package br.com.zup

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGRPCServer: ExpondoDadosGRPCServiceGrpc.ExpondoDadosGRPCServiceImplBase() {

  private val logger = LoggerFactory.getLogger(FretesGRPCServer::class.java)

  override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {
    logger.info("Calculando frate para request: $request")

    val response = CalculaFreteResponse.newBuilder()
      .setCep(request!!.cep)
      .setValor(Random.nextDouble(from = 0.0, until = 140.00))
      .build()

    logger.info("Frete calculado: $response")

    responseObserver!!.onNext(response)
    responseObserver.onCompleted()
  }
}