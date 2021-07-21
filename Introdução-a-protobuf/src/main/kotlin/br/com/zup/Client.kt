package br.com.zup

import io.grpc.ManagedChannelBuilder

fun main() {

  val channel = ManagedChannelBuilder
    .forAddress("localhost", 50051)
    .usePlaintext()
    .build()
  val client = FuncionarioServiceGrpc.newBlockingStub(channel)

  val request = FuncionarioRequest.newBuilder()
    .setNome("Bob Bronw")
    .setCpf("95175325878")
    .setIdade(25)
    .setSalario(2500.00)
    .setAtivo(true)
    .setCargo(Cargo.QA)
    .addEndereco(FuncionarioRequest.Endereco.newBuilder()
      .setLogradouro("Rua das Cores")
      .setCep("95175385245")
      .setComplemento("Casa 1")
      .build())
    .build()

  val response = client.cadastrar(request)

  println(response)
}