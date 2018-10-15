package kifio

import com.google.api.client.googleapis.auth.oauth2.*
import java.io.*

object Common {

    const val baseUrl = "https://subways-and-entrances.firebaseio.com"

	fun generateToken(inputStream: InputStream): String {
		val googleCred = GoogleCredential.fromStream(inputStream)
		val scoped = googleCred.createScoped(listOf(
			"https://www.googleapis.com/auth/firebase.database",
		    "https://www.googleapis.com/auth/userinfo.email"))
		scoped.refreshToken()
		return scoped.getAccessToken()
	}
}