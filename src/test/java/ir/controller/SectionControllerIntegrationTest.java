package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.SectionController;
import ir.dto.SectionListDto;
import ir.service.SectionService;
import ir.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(SectionController.class)
@Import(SecurityConfig.class)
public class SectionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SectionService sectionService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;


    @Test
    @WithMockUser
    void shouldShowSectionsPage() throws Exception {

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

        Page<SectionListDto> sections =
                new PageImpl<>(List.of(sectionListDto));

        when(sectionService.findAll(any(Pageable.class)))
                .thenReturn(sections);

        mockMvc.perform(
                        get("/sections")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("section"))
                .andExpect(model().attributeExists("sections"))
                .andExpect(model().attribute("currentPage",0))
                .andExpect(model().attribute("totalPages",1));

        verify(sectionService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void shouldReturnFragment() throws Exception {

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

        Page<SectionListDto> sections =
                new PageImpl<>(List.of(sectionListDto));

        when(sectionService.findAll(any(Pageable.class)))
                .thenReturn(sections);

        mockMvc.perform(
                        get("/sections")
                                .param("fragment","true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/section-fragments/sections-table :: sections-table"
                ));
    }

    @Test
    @WithMockUser
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

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

        Page<SectionListDto> sections =
                new PageImpl<>(List.of(sectionListDto));

        when(sectionService.findAll(any(Pageable.class)))
                .thenReturn(sections);

        mockMvc.perform(
                        get("/sections")
                                .param("size","0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(sectionService)
                .findAll(captor.capture());

        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }

}
