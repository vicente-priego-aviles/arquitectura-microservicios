package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "eventos_procesados")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesadoEntidad {

	@Id
	private String pedidoId;

	private Instant procesadoEn;
}
