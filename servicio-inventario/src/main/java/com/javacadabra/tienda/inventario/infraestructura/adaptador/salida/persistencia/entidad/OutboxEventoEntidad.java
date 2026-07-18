package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "outbox_evento")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventoEntidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String tipoEvento;
	private String payload;
	private Instant ocurridoEn;
	private boolean publicado;

	public void marcarPublicado() {
		this.publicado = true;
	}
}
