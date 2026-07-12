package com.javacadabra.tienda.catalogo.aplicacion.puerto.salida;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface ProductoRepositorioPuertoSalida {

	Producto guardar(Producto producto);

	Optional<Producto> buscarPorId(ProductoId id);

	List<Producto> buscarPorCategoria(CategoriaId categoriaId);

	List<Producto> buscarRecomendados(ProductoId productoId);

	void agregarRecomendacion(ProductoId productoId, ProductoId recomendadoId);
}
