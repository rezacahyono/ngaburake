package com.rezacah.ngaburake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.rezacah.ngaburake.data.fixture.ApiKeyStore
import com.rezacah.ngaburake.data.fixture.LegacyAuthManager
import com.rezacah.ngaburake.data.fixture.PaymentManager
import com.rezacah.ngaburake.data.fixture.TokenStore
import com.rezacah.ngaburake.ui.ObfuscationCheckScreen
import com.rezacah.ngaburake.ui.theme.NgaburakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Dogfooding fixtures for the obfuscationVerify {} plugin config below — println (not
        // Log.d: proguard-android-optimize.txt has -assumenosideeffects on android.util.Log,
        // which would let R8 strip these calls and the classes entirely) with a non-constant
        // runtime value so R8 can't constant-fold the call either. Each fixture must still
        // appear as a header in mapping.txt (or be kept by rule) for the build-time check.
        val nonConstant = System.currentTimeMillis().toDouble()
        println("payment ok=${PaymentManager().processPayment(nonConstant)}")
        println("apiKey=${ApiKeyStore().getApiKey()}")
        println("secret=${TokenStore().secretToken}")
        println("legacy=${LegacyAuthManager().getLegacyToken()}")
        setContent {
            NgaburakeTheme {
                Surface {
                    ObfuscationCheckScreen()
                }
            }
        }
    }
}