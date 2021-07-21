package br.com.zup

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller(value = "/api/fretes")
class CalculadoraDeFretesController(
  @Inject protected val grpcRequest: ExpondoDadosGRPCServiceGrpc.ExpondoDadosGRPCServiceBlockingStub) {

  @Get
  fun calcula(cep: String): FreteResponse {

    val request = CalculaFreteRequest.newBuilder()
      .setCep(cep)
      .build()
    val response = grpcRequest.calculaFrete(request)

    return FreteResponse(response.cep, response.valor)
  }
}

