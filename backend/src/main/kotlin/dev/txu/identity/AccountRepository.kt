package dev.txu.identity

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class AccountRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {
    fun create(
        email: String,
        displayName: String,
        passwordHash: String,
        role: AccountRole = AccountRole.MEMBER,
    ): Account {
        val id = UUID.randomUUID()
        jdbc.update(
            """INSERT INTO accounts(id, email, display_name, password_hash, role)
               VALUES (:id, :email, :displayName, :passwordHash, :role)""",
            mapOf("id" to id, "email" to email, "displayName" to displayName, "passwordHash" to passwordHash, "role" to role.name),
        )
        return findById(id) ?: error("Created account could not be loaded")
    }

    fun findByEmail(email: String): Account? =
        jdbc.query("SELECT * FROM accounts WHERE email = :email", mapOf("email" to email), ::map).firstOrNull()

    fun findById(id: UUID): Account? = jdbc.query("SELECT * FROM accounts WHERE id = :id", mapOf("id" to id), ::map).firstOrNull()

    fun existsByEmail(email: String): Boolean =
        jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM accounts WHERE email = :email)", mapOf("email" to email), Boolean::class.java) ==
            true

    private fun map(
        result: ResultSet,
        row: Int,
    ) = Account(
        id = result.getObject("id", UUID::class.java),
        email = result.getString("email"),
        displayName = result.getString("display_name"),
        passwordHash = result.getString("password_hash"),
        role = AccountRole.valueOf(result.getString("role")),
        createdAt = result.getTimestamp("created_at").toInstant(),
    )
}
