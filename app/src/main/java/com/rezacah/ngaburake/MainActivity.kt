package com.rezacah.ngaburake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rezacah.ngaburake.ui.theme.NgaburakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Dogfooding fixture for the obfuscationVerify {} plugin config below — println (not
        // Log.d: proguard-android-optimize.txt has -assumenosideeffects on android.util.Log,
        // which would let R8 strip these calls and the classes entirely) with a non-constant
        // runtime value so R8 can't constant-fold the call either. Both classes must still
        // appear as headers in mapping.txt.
        val nonConstant = System.currentTimeMillis().toDouble()
        println("payment ok=${PaymentManager().processPayment(nonConstant)}")
        println("apiKey=${ApiKeyStore().getApiKey()}")
        setContent {
            NgaburakeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NgaburakeTheme {
        Greeting("Android")
    }
}