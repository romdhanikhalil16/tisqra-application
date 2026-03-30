package com.tisqra.event.application.service;

import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.event.application.dto.TicketCategoryDTO;
import com.tisqra.event.application.mapper.TicketCategoryMapper;
import com.tisqra.event.domain.model.TicketCategory;
import com.tisqra.event.domain.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoryMapper ticketCategoryMapper;

    public TicketCategoryDTO getTicketCategory(UUID id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", id));
        return ticketCategoryMapper.toDTO(category);
    }

    @Transactional
    public void reserve(UUID categoryId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Quantity must be a positive number");
        }

        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", categoryId));
        category.reserve(quantity);
        ticketCategoryRepository.save(category);
    }

    @Transactional
    public void release(UUID categoryId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Quantity must be a positive number");
        }

        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", categoryId));
        category.releaseReservation(quantity);
        ticketCategoryRepository.save(category);
    }
}

