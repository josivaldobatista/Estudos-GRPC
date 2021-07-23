package br.com.zup.carros

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface CarroRepository: CrudRepository<Carro, Long> {
  fun existsByPlaca(placa: String?): Boolean
}