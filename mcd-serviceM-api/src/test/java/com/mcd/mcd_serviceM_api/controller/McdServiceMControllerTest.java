package com.mcd.mcd_serviceM_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcd.mcd_serviceM_api.model.McdServiceMRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class McdServiceMControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void submitLoyaltyReturnsSuccessForValidRequest() throws Exception {
    McdServiceMRequest validRequest = new McdServiceMRequest("input");
    // Set validRequest properties here

    mockMvc.perform(post("/loyalty")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(validRequest)))
        .andExpect(status().isOk());
  }

  @Test
  public void submitLoyaltyReturnsBadRequestForNullRequest() throws Exception {
    mockMvc.perform(post("/loyalty")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}