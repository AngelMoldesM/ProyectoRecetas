package com.example.proyectorecetas

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit


object SupabaseManager {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)

            httpEngine = OkHttp.create {
                config {
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(10, TimeUnit.SECONDS)
                }
            }
        }

    }
}
