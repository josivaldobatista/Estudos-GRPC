package br.com.zup

import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.exceptions.HttpStatusException
import javax.inject.Inject

@Controller(value = "/api/fretes")
class CalculadoraDeFretesController(
  @Inject protected val grpcRequest: ExpondoDadosGRPCServiceGrpc.ExpondoDadosGRPCServiceBlockingStub
) {

  @Get
  fun calcula(cep: String): FreteResponse {

    val request = CalculaFreteRequest.newBuilder()
      .setCep(cep)
      .build()

    try {
      val response = grpcRequest.calculaFrete(request)
      return FreteResponse(response.cep, response.valor)
    } catch (e: StatusRuntimeException) {

      val description = e.status.description
      val statusCode = e.status.code

      if (statusCode == Status.Code.INVALID_ARGUMENT) {
        throw HttpStatusException(HttpStatus.BAD_REQUEST, description)
      }

      if (statusCode == Status.Code.PERMISSION_DENIED) {
        val statusProto = StatusProto.fromThrowable(e)
        if (statusProto == null) {
          throw HttpStatusException(HttpStatus.FORBIDDEN, description)
        }
        val anyDetails: Any = statusProto.detailsList[0] //<- Primeiro item dos erros (Só tem um)
        val errorDetails =
          anyDetails.unpack(ErrorDetails::class.java) //<- Desempacotando (foram empacotados na outra API)
        throw HttpStatusException(
          HttpStatus.FORBIDDEN,
          "${errorDetails.code}: ${errorDetails.message}"
        )
      }
      throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
    }
  }
}

