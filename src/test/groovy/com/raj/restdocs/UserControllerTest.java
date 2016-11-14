package com.raj.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raj.restdocs.model.User;
import com.raj.restdocs.repo.UserRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    ConstrainedFields fields = new ConstrainedFields(User.class);

    FieldDescriptor[] user = new FieldDescriptor[] {
            fields.withPath("id").description("The persons' ID"),
            fields.withPath("firstName").description("The person's first name"),
            fields.withPath("lastName").description("The person's last name"),
            fields.withPath("age").description("The user's age")};

    @Test
    public void getUser() throws Exception {
        User user = userRepository.save(User.builder().firstName("George").lastName("King").build());

        this.mockMvc.perform(get("/users/{userId}", user.getId()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andDo(document("{method-name}",
                        pathParameters(parameterWithName("userId").description("The user Id")),
                        responseFields(attributes(key("title").value("User fields")), this.user)));
    }

    @Test
    public void listUsers() throws Exception {
        userRepository.save(User.builder().firstName("George").lastName("King").build());
        userRepository.save(User.builder().firstName("Mary").lastName("Queen").build());

        this.mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andDo(document("{method-name}",
                    responseFields(attributes(key("title").value("User fields")),
                            fieldWithPath("[]").description("An array of users")).andWithPrefix("[].", user)));
    }

    @Test
    public void createUser() throws Exception {
        Map<String, String> newUser = new HashMap<>();
        newUser.put("id", null);
        newUser.put("firstName", "Anne");
        newUser.put("lastName", "Queen");
        newUser.put("age", "34");

        this.mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
        ).andExpect(status().isCreated()).andDo(document("{method-name}",
                requestFields(attributes(key("title").value("Fields for user creation")), user)));
    }

    private static class ConstrainedFields {
        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            String opath = path;
            if (path.startsWith("[].")) {
                opath = path.substring(3);
            }
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions.descriptionsForProperty(opath), ". ")));
        }
    }
}
