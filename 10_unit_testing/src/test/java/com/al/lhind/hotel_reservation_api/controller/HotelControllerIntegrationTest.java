package com.al.lhind.hotel_reservation_api.controller;

import com.al.lhind.hotel_reservation_api.dto.HotelDTO;
import com.al.lhind.hotel_reservation_api.service.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@Import(HotelControllerIntegrationTest.MockHotelServiceConfiguration.class)
class HotelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelService hotelService;

    @Test
    void shouldCreateHotelAndReturnCreatedResponse() throws Exception {
        HotelDTO createdHotel = new HotelDTO();
        createdHotel.setId(1L);
        createdHotel.setName("Central Park Hotel");
        createdHotel.setCity("Paris");
        createdHotel.setAddress("25 Central Avenue");
        createdHotel.setStarRating(4);
        when(hotelService.createHotel(any(HotelDTO.class))).thenReturn(createdHotel);

        String requestBody = """
                {
                  "name": "Central Park Hotel",
                  "city": "Paris",
                  "address": "25 Central Avenue",
                  "starRating": 4
                }
                """;

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central Park Hotel"))
                .andExpect(jsonPath("$.city").value("Paris"));

        verify(hotelService).createHotel(any(HotelDTO.class));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MockHotelServiceConfiguration {
        @Bean
        HotelService hotelService() {
            return mock(HotelService.class);
        }
    }
}
