package com.tisqra.event.application.service;

import com.tisqra.common.enums.EventCategory;
import com.tisqra.common.enums.EventStatus;
import com.tisqra.common.exception.BusinessException;
import com.tisqra.common.exception.ResourceNotFoundException;
import com.tisqra.event.application.dto.CreateEventRequest;
import com.tisqra.event.application.dto.EventDTO;
import com.tisqra.event.application.mapper.EventMapper;
import com.tisqra.event.application.mapper.TicketCategoryMapper;
import com.tisqra.event.application.mapper.EventScheduleMapper;
import com.tisqra.event.domain.model.Event;
import com.tisqra.event.domain.model.EventSchedule;
import com.tisqra.event.domain.model.TicketCategory;
import com.tisqra.event.domain.repository.EventRepository;
import com.tisqra.kafka.config.KafkaTopics;
import com.tisqra.kafka.events.EventPublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event service - Application layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final TicketCategoryMapper ticketCategoryMapper;
    private final EventScheduleMapper eventScheduleMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    public EventDTO createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.getName());

        // Check if organization can create events (subscription limits)
        checkOrganizationCanCreateEvent(request.getOrganizationId());

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }

        // Generate unique slug
        String slug = generateSlug(request.getName());

        // Create event
        Event event = eventMapper.toEntity(request);
        event.setSlug(slug);
        event.setStatus(EventStatus.DRAFT);

        // Add ticket categories
        if (request.getTicketCategories() != null) {
            final Event finalEvent = event;
            request.getTicketCategories().forEach(catRequest -> {
                TicketCategory category = ticketCategoryMapper.toEntity(catRequest);
                finalEvent.addCategory(category);
            });
        }

        // Add schedule items
        if (request.getScheduleItems() != null) {
            final Event finalEvent = event;
            request.getScheduleItems().forEach(schedRequest -> {
                EventSchedule schedule = eventScheduleMapper.toEntity(schedRequest);
                finalEvent.addScheduleItem(schedule);
            });
        }

        event = eventRepository.save(event);

        // Increment organization event count
        notifyOrganizationService(event.getOrganizationId());

        log.info("Event created with ID: {}", event.getId());
        return eventMapper.toDTO(event);
    }

    @Cacheable(value = "events", key = "#id")
    public EventDTO getEventById(UUID id) {
        log.debug("Fetching event by ID: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return eventMapper.toDTO(event);
    }

    @Cacheable(value = "events", key = "#slug")
    public EventDTO getEventBySlug(String slug) {
        log.debug("Fetching event by slug: {}", slug);
        Event event = eventRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "slug", slug));
        return eventMapper.toDTO(event);
    }

    public Page<EventDTO> getEventsByOrganization(UUID organizationId, Pageable pageable) {
        log.debug("Fetching events for organization: {}", organizationId);
        return eventRepository.findByOrganizationId(organizationId, pageable)
            .map(eventMapper::toDTO);
    }

    public Page<EventDTO> getUpcomingEvents(Pageable pageable) {
        log.debug("Fetching upcoming events");
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
            .map(eventMapper::toDTO);
    }

    public Page<EventDTO> getEventsByCategory(EventCategory category, Pageable pageable) {
        log.debug("Fetching events by category: {}", category);
        return eventRepository.findByCategory(category, pageable)
            .map(eventMapper::toDTO);
    }

    public Page<EventDTO> searchEvents(String query, Pageable pageable) {
        log.debug("Searching events with query: {}", query);
        return eventRepository.searchEvents(query, pageable)
            .map(eventMapper::toDTO);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public EventDTO updateEvent(UUID id, CreateEventRequest request) {
        log.info("Updating event: {}", id);

        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("Cannot update completed or cancelled event");
        }

        // Update basic fields
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setThumbnailImageUrl(request.getThumbnailImageUrl());

        event = eventRepository.save(event);
        log.info("Event updated: {}", id);
        return eventMapper.toDTO(event);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public EventDTO publishEvent(UUID id) {
        log.info("Publishing event: {}", id);

        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException("Only draft events can be published");
        }

        if (event.getCategories().isEmpty()) {
            throw new BusinessException("Event must have at least one ticket category");
        }

        event.publish();
        event = eventRepository.save(event);

        // Publish Kafka event
        EventPublishedEvent publishedEvent = EventPublishedEvent.builder()
            .eventId(event.getId())
            .organizationId(event.getOrganizationId())
            .eventName(event.getName())
            .slug(event.getSlug())
            .startDate(event.getStartDate())
            .endDate(event.getEndDate())
            .publishedAt(event.getPublishedAt())
            .build();
        kafkaTemplate.send(KafkaTopics.EVENT_PUBLISHED, publishedEvent);

        log.info("Event published: {}", id);
        return eventMapper.toDTO(event);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public void cancelEvent(UUID id) {
        log.info("Cancelling event: {}", id);

        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        event.cancel();
        eventRepository.save(event);
        log.info("Event cancelled: {}", id);
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();

        String finalSlug = slug;
        int counter = 1;
        while (eventRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter++;
        }

        return finalSlug;
    }

    private void checkOrganizationCanCreateEvent(UUID organizationId) {
        try {
            // Call organization service to check subscription limits
            String url = "http://organization-service/api/subscriptions/organization/" + 
                        organizationId + "/can-create-event";
            Boolean canCreate = restTemplate.getForObject(url, Boolean.class);
            
            if (canCreate == null || !canCreate) {
                throw new BusinessException("Organization has reached event creation limit");
            }
        } catch (Exception e) {
            log.warn("Could not verify organization subscription, allowing event creation", e);
            // In production, you might want to fail closed instead
        }
    }

    private void notifyOrganizationService(UUID organizationId) {
        try {
            // Notify organization service to increment event count
            String url = "http://organization-service/api/subscriptions/organization/" + 
                        organizationId + "/increment-event-count";
            restTemplate.postForObject(url, null, Void.class);
        } catch (Exception e) {
            log.error("Failed to notify organization service", e);
        }
    }
}
