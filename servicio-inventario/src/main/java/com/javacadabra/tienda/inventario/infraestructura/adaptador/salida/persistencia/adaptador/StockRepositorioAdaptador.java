package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.mapper.StockEntidadMapper;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.StockRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class StockRepositorioAdaptador implements StockRepositorioPuertoSalida {

	private final StockRepositorioJpa stockRepositorioJpa;
	private final StockEntidadMapper stockEntidadMapper;

	@Override
	public Stock guardar(Stock stock) {
		var entidadGuardada = stockRepositorioJpa.save(stockEntidadMapper.aEntidad(stock));
		return stockEntidadMapper.aDominio(entidadGuardada);
	}

	@Override
	public Optional<Stock> buscarPorProductoId(ProductoId productoId) {
		return stockRepositorioJpa.findById(productoId.valor()).map(stockEntidadMapper::aDominio);
	}
}
