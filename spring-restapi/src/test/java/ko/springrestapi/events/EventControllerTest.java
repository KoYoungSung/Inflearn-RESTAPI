package ko.springrestapi.events;

import ko.springrestapi.accounts.Account;
import ko.springrestapi.accounts.AccountRepository;
import ko.springrestapi.accounts.AccountRole;
import ko.springrestapi.accounts.AccountService;
import ko.springrestapi.common.BaseControllerTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @Description("정상적으로 이벤트 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .endEventDateTime(LocalDateTime.of(2020, 10, 31, 17, 58))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("삼육대학교")
                .build();

        mockMvc.perform(post("/api/events/")
                    .header(HttpHeaders.AUTHORIZATION,"Bearer"+getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline    ").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("basePrice of  new event"),
                                fieldWithPath("maxPrice").description("maxPrice of  new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of  new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("basePrice of  new event"),
                                fieldWithPath("maxPrice").description("maxPrice of  new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of  new event"),
                                fieldWithPath("free").description("it tells is this is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query event list"),
                                fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                fieldWithPath("_links.profile.href").description("link to profile")

                        )
                ));
    }

    private String getAccessToken() throws Exception {

        String username = "ko@naver.com";
        String password = "ko";
        Account ko = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(ko);

        String clientId = "myApp";
        String clientSecret = "pass";


        ResultActions perform = this.mockMvc.perform(MockMvcRequestBuilders.post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
        );
        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }



    @Test
    @Description("입력 받을 수 없는 값을 사용할 경우 에러 발생 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()

                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .endEventDateTime(LocalDateTime.of(2020, 10, 31, 17, 58))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("삼육대학교")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @Description("입력값이 비어있을 때 에러 발생")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();
        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION,"Bearer"+getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Description("입력값이 잘못 되었을 때 에러 발생")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 28, 17, 58))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .endEventDateTime(LocalDateTime.of(2020, 10, 28, 17, 58))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("삼육대학교")
                .build();
        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
//                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @Description("30개의 이벤트를 10개씩 조회하는 두번쨰 페이지 조회하기 ")
    public void queryEvents() throws Exception {
        //Given
        IntStream.range(0, 30).forEach(this::generatedEvent);

        //When
        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("order", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;

    }

    @Test
    @Description("기존의 이벤트를 하나 조회")
    public void getEvent() throws Exception {

        Event event = this.generatedEvent(100);

        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        ;

    }

    @Test
    @Description("없는 이벤트 조회 시 404응답")
    public void getEvent404() throws Exception {


        this.mockMvc.perform(get("/api/events/3334"))
                .andExpect(status().isNotFound())
        ;

    }

    @Test
    @Description("이벤트를 정상적으로 수행")
    public void updateEvent() throws Exception {
        Event event = this.generatedEvent(200);
        String eventName = "Updated Event";
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists());
    }
   @Test
    @Description("입력값이 없는 경우 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        Event event = this.generatedEvent(200);

       EventDto eventDto = new EventDto();

       this.mockMvc.perform(put("/api/events/{id}", event.getId())
               .contentType(MediaType.APPLICATION_JSON)
               .content(this.objectMapper.writeValueAsString(eventDto)))

               .andDo(print())
               .andExpect(status().isBadRequest());
    }

    @Test
    @Description("입력값이 잘못된 경우 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        Event event = this.generatedEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))

                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Description("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        Event event = this.generatedEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        this.mockMvc.perform(put("/api/events/1245422")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }




    private Event generatedEvent(int index) {
        Event event = Event.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 30, 17, 58))
                .endEventDateTime(LocalDateTime.of(2020, 10, 31, 17, 58))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("삼육대학교")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();


        return this.eventRepository.save(event);


    }
}