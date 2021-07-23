package br.com.zup.carros

import br.com.zup.CarrosGrpcServiceGrpc
import br.com.zup.CarrosRequest
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

/**
 * É importante que desliguemos o controle transacional no ambiente de teste
 * porque o servido gRPC roda em uma thread separada e não participa da transação
 * que é aberta para cada @Teste, podendo gerar algum falso/positivo ou erros
 * em nossos testes.
 *
 * A melhor maneira nesse momento para lidar com isso é desligando o controle transacional.
 * (É o que tem pra hoje :/)
 * */
@MicronautTest(transactional = false) //<- Desligando controle transacional
internal class CarrosEndpointTest(
  val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub,
  val repository: CarroRepository
) {

  /**
   * Precisamos testar:
   * 1 - Happy path (quando o cadastro acontece sem problemas)
   * 2 - Quando ja existe um carro com a placa.
   * 3 - Quando os dados de entrada são invalidos.
   * */

  @BeforeEach
  fun setUp() {
    repository.deleteAll() //<- Virou auto-commit abrindo e fechando transação imediatamente
  }

  @Test
  fun `deve cadastrar um novo carro`() {
    // Cenario

    // Ação
    val response = grpcClient.adicionar(
      CarrosRequest.newBuilder()
        .setModelo("BMW X5")
        .setPlaca("MDF5458")
        .build()
    )

    // Verificação
    with(response) {
      assertNotNull(id)
      assertTrue(repository.existsById(id))//<- Efeito colateral
    }
  }

  @Test
  fun `nao deve adicionar novo carro quando placa ja estiver cadastrada`() {
    // Cenario
    val carro = Carro(modelo = "BMW X5", placa = "MDF5458")
    repository.save(carro)

    // Ação
    val err = assertThrows<StatusRuntimeException> {
      grpcClient.adicionar(
        CarrosRequest.newBuilder()
          .setModelo(carro.modelo)
          .setPlaca(carro.placa).build()
      )
    }
    // Validação
    with(err) {
      assertEquals(Status.ALREADY_EXISTS.code, status.code)
      assertEquals("A Placa desse carro ja existe no sistema", status.description)
    }
  }

  @Test
  fun `nao deve adicionar novo carro quando dados de entrada forem invalidos`() {
    // Cenario

    // Ação
    val err = assertThrows<StatusRuntimeException> {
      grpcClient.adicionar(CarrosRequest.newBuilder().build())
    }

    with(err) {
      assertEquals(Status.INVALID_ARGUMENT.code, status.code)
      assertEquals("Dados de entrada invalidos", status.description)
      // TODO: Posso também verificar as violações da bean validation
    }
  }

}

@Factory
class Clients {

  @Singleton
  fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel):
      CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
    return CarrosGrpcServiceGrpc.newBlockingStub(channel)
  }
}
