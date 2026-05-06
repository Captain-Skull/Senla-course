package com.senla.pas.controller;

import com.senla.pas.dto.request.CreateAdRequest;
import com.senla.pas.dto.request.UpdateAdRequest;
import com.senla.pas.dto.response.AdResponse;
import com.senla.pas.dto.response.UserShortResponse;
import com.senla.pas.enums.AdCategory;
import com.senla.pas.exception.GlobalExceptionHandler;
import com.senla.pas.exception.ResourceNotFoundException;
import com.senla.pas.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AdControllerTest extends AbstractControllerTest {

    @Mock
    private AdService adService;

    @InjectMocks
    private com.senla.pas.controller.AdController adController;

    @BeforeEach
    void init() {
        this.mockMvc = buildMockMvc(adController, new GlobalExceptionHandler());
    }

    @Test
    void getAds_happy() throws Exception {
        AdResponse a = new AdResponse(1L, "t", "d", AdCategory.OTHER, new UserShortResponse(1L, "u", 4.5), 100, true, false, LocalDateTime.now());
        when(adService.getAdsWithFilters(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of(a));

        mockMvc.perform(get("/api/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAdById_happy() throws Exception {
        AdResponse a = new AdResponse();
        a.setId(2L);
        when(adService.getAdById(2L)).thenReturn(a);

        mockMvc.perform(get("/api/ads/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getAdById_notFound() throws Exception {
        when(adService.getAdById(99L)).thenThrow(new ResourceNotFoundException("no ad"));

        mockMvc.perform(get("/api/ads/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("no ad"));
    }

    @Test
    void createAd_happy() throws Exception {
        CreateAdRequest req = new CreateAdRequest("t","d", AdCategory.OTHER, 50);
        AdResponse resp = new AdResponse();
        resp.setId(10L);
        when(adService.createAd(any())).thenReturn(resp);

        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/ads").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<CreateAdRequest> ac = ArgumentCaptor.forClass(CreateAdRequest.class);
        verify(adService).createAd(ac.capture());
        assertEquals("t", ac.getValue().getTitle());
    }

    @Test
    void createAd_validationFail() throws Exception {
        CreateAdRequest req = new CreateAdRequest("","", null, -1);
        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/ads").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));
    }

    @Test
    void updateAd_happy() throws Exception {
        UpdateAdRequest req = new UpdateAdRequest("nt", null, null, 100, true);
        AdResponse resp = new AdResponse(); resp.setId(11L);
        when(adService.updateAd(11L, req)).thenReturn(resp);

        String json = objectMapper.writeValueAsString(req);
        mockMvc.perform(put("/api/ads/11").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));
    }

    @Test
    void deleteAd_happy() throws Exception {
        AdResponse resp = new AdResponse(); resp.setId(12L);
        when(adService.deleteAd(12L)).thenReturn(resp);

        mockMvc.perform(delete("/api/ads/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));
    }
}
