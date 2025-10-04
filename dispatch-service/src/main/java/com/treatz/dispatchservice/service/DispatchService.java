package com.treatz.dispatchservice.service;

import com.treatz.dispatchservice.dto.OrderDTO;

public interface DispatchService {
    void processOrderForDispatch(OrderDTO orderDTO);
    // In DispatchService.java
    void releaseRiderForOrder(OrderDTO orderDTO);
}