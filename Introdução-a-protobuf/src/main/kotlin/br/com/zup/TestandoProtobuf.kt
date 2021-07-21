package br.com.zup

import java.io.FileInputStream
import java.io.FileOutputStream

fun main() {

  val request = FuncionarioRequest.newBuilder()
    .setNome("Bob Bronw")
    .setCpf("95175325878")
    .setSalario(2500.00)
    .setAtivo(true)
    .setCargo(Cargo.QA)
    .addEndereco(FuncionarioRequest.Endereco.newBuilder()
      .setLogradouro("Rua das Cores")
      .setCep("95175385245")
      .setComplemento("Casa 1")
      .build())
    .build()

  // Escrevemos o objeto na rede
  println(request)
  request.writeTo(FileOutputStream("funcionario-request.bin"))

  // Lemos o objeto na rede ou disco
  val request2 = FuncionarioRequest.newBuilder()
    .mergeFrom(FileInputStream("funcionario-request.bin"))

  request2.setCargo(Cargo.GERENTE).build()

  println(request2)
}