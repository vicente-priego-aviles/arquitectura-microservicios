package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockEntidad {

	@Id
	private String productoId;

	private Integer cantidad;
}
