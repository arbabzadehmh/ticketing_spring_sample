package ir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.config.SecurityConfig;
import ir.controller.api.SectionApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.SectionDto;
import ir.dto.SectionListDto;
import ir.dto.mapper.SectionMapper;

import ir.model.entity.Section;
import ir.service.SectionService;

import ir.service.impl.CustomUserDetailsService;
import ir.service.impl.EntityLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(SectionApi.class)
@Import(SecurityConfig.class)
public class SectionApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SectionService sectionService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    EntityLockService lockService;

    @MockitoBean
    SectionMapper sectionMapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAvailableParents() throws Exception {

        Section section = new Section();
        section.setId(2L);

        SectionDto dto =
                new SectionDto(
                        2L,
                        "Transport",
                        1L,
                        "Main",
                        3L,
                        "1ST Building",
                        4L
                );

        when(sectionService.findAll())
                .thenReturn(List.of(section));

        when(sectionMapper.toDto(section))
                .thenReturn(dto);

        mockMvc.perform(
                        get("/rest/sections")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser
    void shouldGetAllSections() throws Exception {

        SectionListDto sectionListDto =
                new SectionListDto(
                        2L,
                        "Transport",
                        1L,
                        "Main",
                        3L,
                        "1ST Building",
                        4L
                );

        Page<SectionListDto> page =
                new PageImpl<>(List.of(sectionListDto),
                        PageRequest.of(0, 10),
                        1
                );

        when(sectionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/sections/get-all")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(sectionService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void shouldSearchBySectionTitle() throws Exception {

        SectionListDto sectionListDto =
                new SectionListDto(
                        2L,
                        "IT",
                        1L,
                        "Main",
                        3L,
                        "1ST Building",
                        4L
                );

        Page<SectionListDto> page =
                new PageImpl<>(List.of(sectionListDto),
                        PageRequest.of(0, 10),
                        1
                );

        when(sectionService.findByTitleContaining(
                eq("IT"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/sections/get-all")
                                .param("sectionTitle","IT")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(sectionService)
                .findByTitleContaining(
                        eq("IT"),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser
    void shouldSearchByParentTitle() throws Exception {

        SectionListDto sectionListDto =
                new SectionListDto(
                        2L,
                        "IT",
                        1L,
                        "Support",
                        3L,
                        "1ST Building",
                        4L
                );

        Page<SectionListDto> page =
                new PageImpl<>(List.of(sectionListDto),
                        PageRequest.of(0, 10),
                        1
                );

        when(sectionService.findByParentSectionTitleContaining(
                eq("Support"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/sections/get-all")
                                .param("parentSectionTitle","Support")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(sectionService)
                .findByParentSectionTitleContaining(
                        eq("Support"),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveSection() throws Exception {

        Section section = new Section();
        section.setTitle("IT");

        when(messageSource.getMessage(
                eq("sections.create.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("created");

        mockMvc.perform(
                        post("/rest/sections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(section))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("created"));

        verify(sectionService)
                .save(any(Section.class));
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void shouldStartEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/sections/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("locked"));

        verify(lockService)
                .lock("section",1L,"admin");
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void shouldStopEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/sections/1/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .unlock("section",1L,"admin");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateSection() throws Exception {

        Section section = new Section();
        section.setTitle("Updated");

        when(messageSource.getMessage(
                eq("sections.edit.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("updated");

        mockMvc.perform(
                        put("/rest/sections/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(section))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("updated"));

        verify(sectionService)
                .update(eq(1L), any(Section.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteSection() throws Exception {

        when(messageSource.getMessage(
                eq("sections.delete.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("deleted");

        mockMvc.perform(
                        delete("/rest/sections/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("deleted"));

        verify(sectionService)
                .deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForSave() throws Exception {

        Section section = new Section();

        mockMvc.perform(
                        post("/rest/sections")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(section))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForEditStart() throws Exception {

        mockMvc.perform(
                        post("/rest/sections/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
