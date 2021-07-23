package br.com.zup.carros

import br.com.zup.CarrosGrpcServiceGrpc
import br.com.zup.CarrosRequest
import br.com.zup.CarrosResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val carroRepository: CarroRepository) :
  CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {

  override fun adicionar(request: CarrosRequest, responseObserver: StreamObserver<CarrosResponse>?) {

    if (carroRepository.existsByPlaca(request.placa)) {
      responseObserver!!.onError(
        (Status.ALREADY_EXISTS
          .withDescription("A Placa desse carro ja existe no sistema")
          .asRuntimeException())
      )
      return
    }
    val carro = Carro(
      modelo = request!!.modelo,
      placa = request!!.placa
    )

    try {
      carroRepository.save(carro)
    } catch (e: ConstraintViolationException) {
      responseObserver?.onError(
        Status.INVALID_ARGUMENT
          .withDescription("Dados de entrada invalidos")
          .asRuntimeException()
      )
      return
    }
    responseObserver?.onNext(
      CarrosResponse.newBuilder().setId(carro.id!!)
        .build()
    )
    responseObserver?.onCompleted()
  }
}