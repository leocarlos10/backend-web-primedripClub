package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.dto.pedido.PedidoRequest;

public interface PedidoRepository {
    
    /**
     * Crea un nuevo pedido en la base de datos.
     * @param pedido el pedido a guardar
     * @return el ID generado del pedido
     */
    Long crearPedido(PedidoRequest pedido);
    
}
