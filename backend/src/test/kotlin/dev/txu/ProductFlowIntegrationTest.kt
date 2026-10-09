package dev.txu

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class ProductFlowIntegrationTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val json: ObjectMapper,
) {
    @Test
    fun `receipt travels through issue acknowledgement reporting and moderation`() {
        val member = register()
        val draftId = createDraft(member)
        val issued = issue(member, draftId)
        val publicId = issued.at("/receipt/publicId").asString()
        val token = issued.at("/recipientUrl").asString().substringAfterLast('/')

        mvc
            .perform(get("/api/public/receipts/{id}", publicId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("VERIFIED"))
            .andExpect(jsonPath("$.integrity").value("VALID"))

        mvc
            .perform(post("/api/acknowledgements/{token}", token).with(csrf()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.state").value("ACKNOWLEDGED"))

        mvc
            .perform(
                post("/api/public/receipts/{id}/reports", publicId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"reason":"OTHER","details":"Integration review"}"""),
            ).andExpect(status().isCreated)

        loginModerator()
        val moderator = Actor("moderator@txu.local", "MODERATOR")
        val reports =
            mvc
                .perform(get("/api/moderation/reports").with(moderator.authentication()))
                .andExpect(status().isOk)
                .andReturn()
        val reportId =
            json
                .readTree(reports.response.contentAsString)
                .first()
                .at("/id")
                .asString()

        decide(moderator, reportId, "HIDE")
        mvc
            .perform(get("/api/public/receipts/{id}", publicId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("HIDDEN"))

        decide(moderator, reportId, "RESTORE")
        mvc
            .perform(get("/api/public/receipts/{id}", publicId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value("VERIFIED_ACKNOWLEDGED"))
    }

    private fun register(): Actor {
        val email = "ari-${UUID.randomUUID()}@example.test"
        mvc
            .perform(
                post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"displayName":"Ari Sender","email":"$email","password":"correct-horse-battery"}"""),
            ).andExpect(status().isCreated)
        return Actor(email, "MEMBER")
    }

    private fun loginModerator() {
        mvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"moderator@txu.local","password":"local-txu-moderator"}"""),
            ).andExpect(status().isOk)
    }

    private fun createDraft(actor: Actor): String {
        val result =
            mvc
                .perform(
                    post("/api/receipts")
                        .with(actor.authentication())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """{"recipientLabel":"Nadia","title":"Made the release calmer","message":"Your careful review helped the whole team ship with confidence.","category":"TEAMWORK"}""",
                        ),
                ).andExpect(status().isCreated)
                .andReturn()
        return json.readTree(result.response.contentAsString).at("/id").asString()
    }

    private fun issue(
        actor: Actor,
        draftId: String,
    ) = json.readTree(
        mvc
            .perform(post("/api/receipts/{id}/issue", draftId).with(actor.authentication()).with(csrf()))
            .andExpect(status().isOk)
            .andReturn()
            .response.contentAsString,
    )

    private fun decide(
        actor: Actor,
        reportId: String,
        decision: String,
    ) {
        mvc
            .perform(
                post("/api/moderation/reports/{id}/decision", reportId)
                    .with(actor.authentication())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"decision":"$decision","reason":"Verified during integration review."}"""),
            ).andExpect(status().isOk)
    }

    private data class Actor(
        val email: String,
        val role: String,
    ) {
        fun authentication() = user(email).roles(role)
    }
}
