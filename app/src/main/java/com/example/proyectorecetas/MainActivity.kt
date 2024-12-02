package com.example.proyectorecetas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.proyectorecetas.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el BottomNavigationView con el NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}
/*FATAL EXCEPTION: main
                                                                                                    Process: com.example.proyectorecetas, PID: 15882
                                                                                                    java.lang.RuntimeException: Cannot find implementation for com.example.proyectorecetas.AppDatabase. AppDatabase_Impl does not exist
                                                                                                    	at androidx.room.Room.getGeneratedImplementation(Room.kt:58)
                                                                                                    	at androidx.room.RoomDatabase$Builder.build(RoomDatabase.kt:1351)
                                                                                                    	at com.example.proyectorecetas.CreateRecipeFragment.saveRecipe(CreateRecipeFragment.kt:88)
                                                                                                    	at com.example.proyectorecetas.CreateRecipeFragment.onViewCreated$lambda$1(CreateRecipeFragment.kt:51)
                                                                                                    	at com.example.proyectorecetas.CreateRecipeFragment.$r8$lambda$3BNvRNuC6ZTTlck1Jen9G7MVUbY(Unknown Source:0)
                                                                                                    	at com.example.proyectorecetas.CreateRecipeFragment$$ExternalSyntheticLambda1.onClick(D8$$SyntheticClass:0)
                                                                                                    	at android.view.View.performClick(View.java:8028)
                                                                                                    	at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1213)
                                                                                                    	at android.view.View.performClickInternal(View.java:8005)
                                                                                                    	at android.view.View.-$$Nest$mperformClickInternal(Unknown Source:0)
                                                                                                    	at android.view.View$PerformClick.run(View.java:31229)
                                                                                                    	at android.os.Handler.handleCallback(Handler.java:959)
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:100)
                                                                                                    	at android.os.Looper.loopOnce(Looper.java:232)
                                                                                                    	at android.os.Looper.loop(Looper.java:317)
                                                                                                    	at android.app.ActivityThread.main(ActivityThread.java:8705)
                                                                                                    	at java.lang.reflect.Method.invoke(Native Method)
                                                                                                    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:580)
                                                                                                    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:886)*/
