package kifio.subwaysandentrances

import android.app.Application
import timber.log.*

import timber.log.Timber.DebugTree

class App: Application() {

	override fun onCreate() {
		super.onCreate()
		Timber.plant(DebugTree())
	}
}